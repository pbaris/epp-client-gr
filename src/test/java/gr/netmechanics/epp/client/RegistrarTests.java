package gr.netmechanics.epp.client;

import static gr.netmechanics.epp.client.impl.EppCommandRequest.request;
import static org.assertj.core.api.Assertions.assertThat;

import gr.netmechanics.epp.client.impl.commands.info.registrar.RegistrarInfoRequest;
import gr.netmechanics.epp.client.impl.commands.info.registrar.RegistrarInfoResponse;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
class RegistrarTests extends EppClientTestBase {

    @Test
    @Order(1)
    void test_registrar_info_marshalling() throws Exception {
        var infoRequest = new RegistrarInfoRequest();

        String xml = xmlUtil.toXml(request(infoRequest, eppProps.getClTrId()));
        assertThat(xml).contains("<account:info");
        assertThat(xml).contains("xmlns:account=\"urn:ics-forth:params:xml:ns:account-1.1\"");
    }

    @Test
    @Order(2)
    void test_registrar_info() {
        var infoRequest = new RegistrarInfoRequest();

        var cmd = assertCommandSuccess(eppClient.getRegistrarInfo(infoRequest));

        assertThat(cmd.<RegistrarInfoResponse>getInfoResponse()).satisfies(info -> {
            assertThat(info).isNotNull();
            assertThat(info.getClientId()).isEqualTo(eppProps.getClientId());
        });
    }
}
