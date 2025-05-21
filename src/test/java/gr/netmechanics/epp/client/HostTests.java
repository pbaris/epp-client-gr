package gr.netmechanics.epp.client;

import static gr.netmechanics.epp.client.impl.EppCommandRequest.request;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.check.host.HostCheckRequest;
import gr.netmechanics.epp.client.impl.commands.check.host.HostCheckResponse;
import gr.netmechanics.epp.client.impl.commands.create.host.HostCreateRequest;
import gr.netmechanics.epp.client.impl.commands.create.host.HostCreateResponse;
import gr.netmechanics.epp.client.impl.commands.delete.host.HostDeleteRequest;
import gr.netmechanics.epp.client.impl.commands.info.host.HostInfoRequest;
import gr.netmechanics.epp.client.impl.commands.info.host.HostInfoResponse;
import gr.netmechanics.epp.client.impl.commands.update.host.HostUpdateRequest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @author Panos Bariamis (pbaris)
 */
@TestMethodOrder(OrderAnnotation.class)
class HostTests extends EppClientTestBase {

    private static final String HOST1 = "ns3.epp-client.gr";
    private static final String HOST2 = "ns4.epp-client.gr";

    @Test
    @Order(1)
    void test_host_info_marshalling() throws Exception {
        var infoRequest = HostInfoRequest.builder()
            .hostName(HOST1)
            .build();

        assertThat(xmlUtil.toXml(request(infoRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("host_info_request.xml"));
    }

    @Test
    @Order(2)
    void test_host_check_marshalling() throws Exception {
        var checkRequest = HostCheckRequest.builder()
            .hostNames(HOST1, HOST2)
            .build();

        assertThat(xmlUtil.toXml(request(checkRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("host_check_request.xml"));
    }

    @Test
    @Order(3)
    void test_host_update_marshalling() throws Exception {
        var updateRequest = HostUpdateRequest.builder()
            .hostName(HOST1)
            .hostNewName("ns12.epp-client.gr")
            .addressesToAdd(List.of("100.100.100.113"))
            .addressesToRemove(List.of("122.122.122.122", "1080:0:0:0:8:800:200C:417A"))
            .build();

        assertThat(xmlUtil.toXml(request(updateRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("host_update_request.xml"));
    }

    @Test
    @Order(4)
    void test_host_create_marshalling() throws Exception {
        var createRequest = HostCreateRequest.builder()
            .hostName(HOST1)
            .addresses(List.of("122.122.122.122", "1080:0:0:0:8:800:200C:417A"))
            .build();

        assertThat(xmlUtil.toXml(request(createRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("host_create_request.xml"));
    }

    @Test
    @Order(5)
    void test_host_delete_marshalling() throws Exception {
        var deleteRequest = HostDeleteRequest.builder()
            .hostName(HOST1)
            .build();

        assertThat(xmlUtil.toXml(request(deleteRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("host_delete_request.xml"));
    }

    @Test
    @Order(6)
    void test_host_create1() {
        var createRequest = HostCreateRequest.builder()
            .hostName(HOST1)
            .addresses(List.of("122.122.122.122", "1080:0:0:0:8:800:200C:417A"))
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.createHost(createRequest));
        assertThat(cmd.<HostCreateResponse>getCreateResponse().getName()).isEqualTo(HOST1);

        createRequest = HostCreateRequest.builder()
            .hostName(HOST2)
            .addresses(List.of("122.122.122.122", "1080:0:0:0:8:800:200C:417A"))
            .build();

        cmd = assertCommandSuccess(eppClient.createHost(createRequest));
        assertThat(cmd.<HostCreateResponse>getCreateResponse().getName()).isEqualTo(HOST2);
    }

    @Test
    @Order(7)
    void test_host_create2() {
        var createRequest = HostCreateRequest.builder()
            .hostName("ns1.fail-epp-client.gr")
            .addresses(List.of("122.122.122.122", "1080:0:0:0:8:800:200C:417A"))
            .build();

        EppCommandResponse cmd = assertCommandFail(eppClient.createHost(createRequest));
        assertCommentEqual(cmd, "Το γονικό όνομα χώρου του εξυπηρετητή δεν είναι ορισμένο στο σύστημα ή είναι περιορισμένη η χρήση του.");
    }

    @Test
    @Order(8)
    void test_host_update() throws Exception {
        var updateRequest = HostUpdateRequest.builder()
            .hostName(HOST1)
            .hostNewName("ns12.epp-client.gr")
            .addressesToAdd(List.of("100.100.100.113"))
            .addressesToRemove(List.of("122.122.122.122", "1080:0:0:0:8:800:200C:417A"))
            .build();

        assertCommandSuccess(eppClient.updateHost(updateRequest));

        updateRequest = HostUpdateRequest.builder()
            .hostName("ns12.epp-client.gr")
            .hostNewName(HOST1)
            .build();

        assertCommandSuccess(eppClient.updateHost(updateRequest));
    }

    @Test
    @Order(9)
    void test_host_info() {
        var infoRequest = HostInfoRequest.builder()
            .hostName(HOST1)
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.getHostInfo(infoRequest));

        assertThat(cmd.<HostInfoResponse>getInfoResponse()).satisfies(info -> {
            assertThat(info).isNotNull();
            assertThat(info.getDomain().getName()).isEqualTo(HOST1);
        });
    }

    @Test
    @Order(10)
    void test_host_check_single_name() {
        var checkRequest = HostCheckRequest.builder()
            .hostNames(HOST1)
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.checkHosts(checkRequest));

        assertThat(cmd.<HostCheckResponse>getCheckResponse()).satisfies(check -> {
            assertThat(check).isNotNull();

            assertThat(check.getCheckData()).satisfies(cd -> {
                assertThat(cd).isNotNull().hasSize(1);

                assertThat(cd.getFirst().getDomain()).satisfies(domain -> {
                    assertThat(domain.getName()).isEqualTo(HOST1);
                    assertThat(domain.getAvailable()).isFalse();
                });
            });
        });

        assertCommentEqual(cmd, "Σε χρήση");
    }

    @Test
    @Order(11)
    void test_host_check_multi_names() {
        var checkRequest = HostCheckRequest.builder()
            .hostNames(HOST1, HOST2)
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.checkHosts(checkRequest));

        assertThat(cmd.<HostCheckResponse>getCheckResponse()).satisfies(check -> {
            assertThat(check).isNotNull();

            assertThat(check.getCheckData()).satisfies(cd -> {
                assertThat(cd).isNotNull().hasSize(2);

                assertThat(cd.getFirst().getDomain()).satisfies(domain -> {
                    assertThat(domain.getName()).isEqualTo(HOST1);
                    assertThat(domain.getAvailable()).isFalse();
                });

                assertThat(cd.getLast().getDomain()).satisfies(domain -> {
                    assertThat(domain.getName()).isEqualTo(HOST2);
                    assertThat(domain.getAvailable()).isFalse();
                });
            });
        });
    }

    @Test
    @Order(12)
    void test_host_delete() {
        assertCommandSuccess(eppClient.deleteHost(HostDeleteRequest.builder()
            .hostName(HOST1)
            .build()));

        assertCommandSuccess(eppClient.deleteHost(HostDeleteRequest.builder()
            .hostName(HOST2)
            .build()));
    }
}
