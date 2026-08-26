package gr.netmechanics.epp.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gr.netmechanics.epp.client.impl.EppCommandRequest;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.EppResultCodes;
import gr.netmechanics.epp.client.impl.commands.Hello;
import gr.netmechanics.epp.client.impl.commands.check.domain.DomainCheckRequest;
import gr.netmechanics.epp.client.impl.elements.Greeting;
import gr.netmechanics.epp.client.xml.EppXmlCodec;
import org.junit.jupiter.api.Test;

class EppClientSessionTest {

    @Test
    void concurrent_calls_before_any_login_trigger_exactly_one_login() throws Exception {
        int threadCount = 20;
        CountDownLatch loginEntered = new CountDownLatch(1);
        CountDownLatch loginRelease = new CountDownLatch(1);

        FakeEppGateway gateway = new FakeEppGateway();
        gateway.blockFirstLoginUntilReleased(loginEntered, loginRelease);

        EppClient client = new EppClient(gateway, new EppSessionCookieStore());
        client.setEppProps(fixedProvider());

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<EppCommandResponse>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Callable<EppCommandResponse> task = () -> {
                go.await();
                return client.checkDomains(DomainCheckRequest.builder().domainNames("example.gr").build());
            };
            futures.add(pool.submit(task));
        }

        go.countDown();

        assertThat(loginEntered.await(5, TimeUnit.SECONDS))
            .as("at least one thread must have entered the login critical section")
            .isTrue();
        loginRelease.countDown();

        for (Future<EppCommandResponse> future : futures) {
            assertThat(future.get(10, TimeUnit.SECONDS).isSuccess()).isTrue();
        }
        pool.shutdown();

        assertThat(gateway.loginAttempts.get()).isEqualTo(1);
        assertThat(gateway.businessAttempts.get()).isEqualTo(threadCount);
    }

    @Test
    void authorization_error_triggers_exactly_one_reconnect_and_retry() {
        FakeEppGateway gateway = new FakeEppGateway();
        gateway.failNthBusinessCallWith(1, EppResultCodes.AUTHORIZATION_ERROR);

        EppClient client = new EppClient(gateway, new EppSessionCookieStore());
        client.setEppProps(fixedProvider());

        EppCommandResponse response = client.checkDomains(
            DomainCheckRequest.builder().domainNames("example.gr").build());

        assertThat(response.isSuccess()).isTrue();
        assertThat(gateway.loginAttempts.get()).isEqualTo(2);
        assertThat(gateway.businessAttempts.get()).isEqualTo(2);
    }

    @Test
    void non_authorization_error_is_returned_without_retry() {
        FakeEppGateway gateway = new FakeEppGateway();
        gateway.failNthBusinessCallWith(1, EppResultCodes.OBJECT_DOES_NOT_EXIST);

        EppClient client = new EppClient(gateway, new EppSessionCookieStore());
        client.setEppProps(fixedProvider());

        EppCommandResponse response = client.checkDomains(
            DomainCheckRequest.builder().domainNames("example.gr").build());

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getResults().getFirst().getCode()).isEqualTo(EppResultCodes.OBJECT_DOES_NOT_EXIST);
        assertThat(gateway.loginAttempts.get()).isEqualTo(1);
        assertThat(gateway.businessAttempts.get()).isEqualTo(1);
    }

    @Test
    void concurrent_authorization_errors_at_same_generation_trigger_exactly_one_reconnect() throws Exception {
        FakeEppGateway gateway = new FakeEppGateway();
        CyclicBarrier bothBusinessCallsArrived = new CyclicBarrier(2);
        gateway.failFirstBusinessAttemptsWith(2, EppResultCodes.AUTHORIZATION_ERROR, bothBusinessCallsArrived);

        EppClient client = new EppClient(gateway, new EppSessionCookieStore());
        client.setEppProps(fixedProvider());

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch go = new CountDownLatch(1);
        Callable<EppCommandResponse> task = () -> {
            go.await();
            return client.checkDomains(DomainCheckRequest.builder().domainNames("example.gr").build());
        };

        Future<EppCommandResponse> futureA = pool.submit(task);
        Future<EppCommandResponse> futureB = pool.submit(task);
        go.countDown();

        EppCommandResponse responseA = futureA.get(10, TimeUnit.SECONDS);
        EppCommandResponse responseB = futureB.get(10, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(responseA.isSuccess()).isTrue();
        assertThat(responseB.isSuccess()).isTrue();

        // The barrier forces both threads' initial business calls to observe the same
        // session generation before either receives its AUTHORIZATION_ERROR response, so
        // both then race into reconnect() with the same seenGeneration. Only the thread that
        // wins the connectionLock actually tears down and re-logs-in (1 reconnect login on
        // top of the 1 initial connect login = 2 total); the other observes the already-bumped
        // sessionGeneration and retries directly without logging in again.
        assertThat(gateway.loginAttempts.get()).isEqualTo(2);
        assertThat(gateway.businessAttempts.get()).isEqualTo(4);
    }

    @Test
    void hello_failure_during_connect_does_not_throw() {
        EppGateway gateway = new EppGateway() {
            @Override
            public Greeting hello(final Hello hello) {
                throw new gr.netmechanics.epp.client.error.EppGatewayException("sandbox unreachable");
            }

            @Override
            public EppCommandResponse sendCommand(final EppCommandRequest command) {
                throw new AssertionError("should never reach sendCommand if hello failed");
            }
        };

        EppClient client = new EppClient(gateway, new EppSessionCookieStore());
        client.setEppProps(fixedProvider());

        assertThat(client.checkDomains(DomainCheckRequest.builder().domainNames("example.gr").build())).isNull();
    }

    private static EppPropertiesProvider fixedProvider() {
        return new EppPropertiesProvider() {
            @Override
            public boolean isUseSandbox() {
                return true;
            }

            @Override
            public String getClientId() {
                return "client";
            }

            @Override
            public String getPassword() {
                return "password";
            }

            @Override
            public String getLanguage() {
                return "en";
            }

            @Override
            public Long getClTrId() {
                return 1L;
            }

            @Override
            public String getUrl() {
                return "http://localhost/epp/proxy";
            }
        };
    }

    private static final class FakeEppGateway implements EppGateway {

        private static final String GREETING_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<epp xmlns=\"urn:ietf:params:xml:ns:epp-1.0\">"
            + "<greeting>"
            + "<svID>.gr and .ελ ccTLD EPP Service</svID>"
            + "<svDate>2025-04-25T09:41:55.429Z</svDate>"
            + "<svcMenu>"
            + "<version>1.0</version>"
            + "<lang>en</lang>"
            + "<lang>el</lang>"
            + "<objURI>urn:ietf:params:xml:ns:host-1.0</objURI>"
            + "<objURI>urn:ietf:params:xml:ns:contact-1.0</objURI>"
            + "<objURI>urn:ietf:params:xml:ns:domain-1.0</objURI>"
            + "</svcMenu>"
            + "</greeting>"
            + "</epp>";

        private final EppXmlCodec codec = buildCodec();

        final AtomicInteger loginAttempts = new AtomicInteger();
        final AtomicInteger businessAttempts = new AtomicInteger();

        private volatile int failBusinessAttemptNumber = -1;
        private volatile int failBusinessAttemptCode;

        private volatile int failFirstBusinessAttemptsCount = -1;
        private volatile int failFirstBusinessAttemptsCode;
        private volatile CyclicBarrier failFirstBusinessAttemptsBarrier;

        private volatile CountDownLatch loginEntered;
        private volatile CountDownLatch loginRelease;

        void failNthBusinessCallWith(final int attemptNumber, final int code) {
            this.failBusinessAttemptNumber = attemptNumber;
            this.failBusinessAttemptCode = code;
        }

        /**
         * Fails every business attempt numbered 1..count with the given code. Each of those
         * attempts blocks on the given barrier immediately before returning its response, so
         * callers can force N concurrent callers to all observe the failure (and thus the same
         * pre-reconnect session generation) at the same time, rather than relying on timing.
         */
        void failFirstBusinessAttemptsWith(final int count, final int code, final CyclicBarrier barrier) {
            this.failFirstBusinessAttemptsCount = count;
            this.failFirstBusinessAttemptsCode = code;
            this.failFirstBusinessAttemptsBarrier = barrier;
        }

        void blockFirstLoginUntilReleased(final CountDownLatch entered, final CountDownLatch release) {
            this.loginEntered = entered;
            this.loginRelease = release;
        }

        @Override
        public Greeting hello(final Hello hello) {
            return codec.unmarshal(GREETING_XML, Greeting.class);
        }

        @Override
        public EppCommandResponse sendCommand(final EppCommandRequest command) {
            if (command.getCommand().getLoginRequest() != null) {
                int attempt = loginAttempts.incrementAndGet();
                if (attempt == 1 && loginEntered != null) {
                    loginEntered.countDown();
                    await(loginRelease);
                }
                return codec.unmarshal(responseXml(EppResultCodes.COMMAND_COMPLETED_SUCCESSFULLY, "OK"), EppCommandResponse.class);
            }

            int attempt = businessAttempts.incrementAndGet();
            if (attempt == failBusinessAttemptNumber) {
                return codec.unmarshal(responseXml(failBusinessAttemptCode, "failed"), EppCommandResponse.class);
            }
            if (attempt <= failFirstBusinessAttemptsCount) {
                await(failFirstBusinessAttemptsBarrier);
                return codec.unmarshal(responseXml(failFirstBusinessAttemptsCode, "failed"), EppCommandResponse.class);
            }
            return codec.unmarshal(responseXml(EppResultCodes.COMMAND_COMPLETED_SUCCESSFULLY, "OK"), EppCommandResponse.class);
        }

        private static void await(final CountDownLatch latch) {
            try {
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private static void await(final CyclicBarrier barrier) {
            if (barrier == null) {
                return;
            }

            try {
                barrier.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException | TimeoutException e) {
                throw new IllegalStateException("Barrier wait failed in test fake", e);
            }
        }

        private static String responseXml(final int code, final String message) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<epp xmlns=\"urn:ietf:params:xml:ns:epp-1.0\">"
                + "<response>"
                + "<result code=\"" + code + "\"><msg>" + message + "</msg></result>"
                + "<trID><clTRID>test-cltrid</clTRID><svTRID>test-svtrid</svTRID></trID>"
                + "</response>"
                + "</epp>";
        }

        private static EppXmlCodec buildCodec() {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.registerModule(new JavaTimeModule());
            return new EppXmlCodec(xmlMapper);
        }
    }
}
