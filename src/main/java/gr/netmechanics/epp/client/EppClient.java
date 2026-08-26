package gr.netmechanics.epp.client;

import static gr.netmechanics.epp.client.EppConstants.BEAN_EPP_CLIENT;
import static gr.netmechanics.epp.client.impl.EppCommandRequest.request;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.EppCommandRequest;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.EppRequest;
import gr.netmechanics.epp.client.impl.EppResponseResult;
import gr.netmechanics.epp.client.impl.EppResultCodes;
import gr.netmechanics.epp.client.impl.commands.Hello;
import gr.netmechanics.epp.client.impl.commands.LoginRequest;
import gr.netmechanics.epp.client.impl.commands.LogoutRequest;
import gr.netmechanics.epp.client.impl.elements.Greeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Command methods are grouped by object type and reached via {@link #domains()}, {@link #hosts()},
 * {@link #contacts()} and {@link #registrar()}. Every command returns {@code null} if no EPP
 * session could be established with the server.
 */
@Slf4j
@Component(BEAN_EPP_CLIENT)
@RequiredArgsConstructor
public class EppClient implements EppCommandSender {

    private final EppGateway eppGateway;
    private final EppSessionCookieStore cookieStore;

    private final ReentrantLock connectionLock = new ReentrantLock();

    private final AtomicReference<EppPropertiesProvider> eppProps = new AtomicReference<>();
    private final AtomicReference<Greeting> greeting = new AtomicReference<>();
    private final AtomicLong sessionGeneration = new AtomicLong();

    private final EppDomainCommands domainCommands = new EppDomainCommands(this);
    private final EppHostCommands hostCommands = new EppHostCommands(this);
    private final EppContactCommands contactCommands = new EppContactCommands(this);
    private final EppRegistrarCommands registrarCommands = new EppRegistrarCommands(this);

    private volatile boolean connected;

    //region Session (RFC3730)

    /**
     * Logs in to the EPP server using the provided login request details.
     *
     * @param loginRequest the login credentials and session options required to authenticate with the server
     * @return the response from the EPP server containing the result of the login attempt
     */
    public EppCommandResponse login(@NonNull final LoginRequest loginRequest) {
        connectionLock.lock();
        try {
            if (connected) {
                logout();
            }

            EppCommandResponse response = eppGateway.sendCommand(request(loginRequest, eppProps.get().getClTrId()));
            connected = response.isSuccess();
            if (connected) {
                sessionGeneration.incrementAndGet();
            }
            return response;
        } finally {
            connectionLock.unlock();
        }
    }

    /**
     * Logs out from the EPP server by sending a logout command request.
     *
     * @return the response from the EPP server, indicating the result of the logout attempt
     */
    public EppCommandResponse logout() {
        connectionLock.lock();
        try {
            EppCommandResponse cmd = eppGateway.sendCommand(request(new LogoutRequest(), eppProps.get().getClTrId()));
            clear();
            return cmd;
        } finally {
            connectionLock.unlock();
        }
    }

    /**
     * Sends a Hello message to the EPP server and retrieves the server's greeting response.
     *
     * @return the server's greeting response
     * @throws EppGatewayException if an error occurs during communication with the EPP server
     */
    public Greeting hello() {
        return eppGateway.hello(new Hello());
    }
    //endregion

    /**
     * Domain commands (RFC3731).
     *
     * @return the domain command group
     */
    public EppDomainCommands domains() {
        return domainCommands;
    }

    /**
     * Host commands (RFC3732).
     *
     * @return the host command group
     */
    public EppHostCommands hosts() {
        return hostCommands;
    }

    /**
     * Contact commands (RFC3733).
     *
     * @return the contact command group
     */
    public EppContactCommands contacts() {
        return contactCommands;
    }

    /**
     * Registrar commands (RFC3733).
     *
     * @return the registrar command group
     */
    public EppRegistrarCommands registrar() {
        return registrarCommands;
    }

    @Override
    public EppCommandResponse send(final EppRequest eppRequest) {
        return sendCommandRequest(request(eppRequest, eppProps.get().getClTrId()));
    }

    private EppCommandResponse sendCommandRequest(@NonNull final EppCommandRequest command) {
        ensureConnected();

        // Prevent gateway invocation if connect attempt failed, matching EppClientSessionTest#hello_failure_during_connect_does_not_throw
        if (!connected) {
            log.error("Not connected to the EPP server, command not sent: {}", command);
            return null;
        }

        long seenGeneration = sessionGeneration.get();
        EppCommandResponse response = eppGateway.sendCommand(command);
        if (isAuthorizationError(response) && reconnect(seenGeneration)) {
            response = eppGateway.sendCommand(command);
        }

        return response;
    }

    private void ensureConnected() {
        if (connected) {
            return;
        }

        connectionLock.lock();
        try {
            if (!connected) {
                connect();
            }
        } finally {
            connectionLock.unlock();
        }
    }

    private boolean reconnect(final long seenGeneration) {
        connectionLock.lock();
        try {
            if (connected && sessionGeneration.get() != seenGeneration) {
                log.debug("Session was already reconnected by another thread, retrying without reconnecting again");
                return true;
            }

            log.debug("Session appears to have expired, reconnecting");
            clear();
            return connect();
        } finally {
            connectionLock.unlock();
        }
    }

    private boolean connect() {
        Greeting greet = getGreeting();
        if (greet == null) {
            return false;
        }

        EppPropertiesProvider props = eppProps.get();
        String language = props.getLanguage();
        if (!greet.getLanguages().contains(language)) {
            language = greet.getDefaultLanguage();
        }

        LoginRequest loginRequest = LoginRequest.builder()
            .clientId(props.getClientId())
            .password(props.getPassword())
            .language(language)
            .version(greet.getVersion())
            .objectUris(greet.getObjectUris())
            .build();

        return login(loginRequest).isSuccess();
    }

    private Greeting getGreeting() {
        if (greeting.get() == null) {
            try {
                greeting.set(hello());

            } catch (EppGatewayException e) {
                log.error("Checking with hello failed, we are not connected, returning false!");
            }
        }

        return greeting.get();
    }

    private boolean isAuthorizationError(final EppCommandResponse response) {
        if (response.isSuccess()) {
            return false;
        }

        List<EppResponseResult> results = response.getResults();
        return results != null && !results.isEmpty()
            && results.getFirst().getCode() == EppResultCodes.AUTHORIZATION_ERROR;
    }

    private void clear() {
        connected = false;
        greeting.set(null);
        cookieStore.clear();
    }

    @Autowired
    void setEppProps(final EppPropertiesProvider eppProps) {
        connectionLock.lock();
        try {
            boolean hadSession = connected;
            this.eppProps.set(eppProps);
            if (hadSession) {
                logout();
            } else {
                clear();
            }
        } finally {
            connectionLock.unlock();
        }
    }
}
