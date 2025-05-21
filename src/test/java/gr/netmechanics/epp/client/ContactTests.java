package gr.netmechanics.epp.client;

import static gr.netmechanics.epp.client.impl.EppCommandRequest.request;
import static org.assertj.core.api.Assertions.assertThat;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.check.contact.ContactCheckRequest;
import gr.netmechanics.epp.client.impl.commands.check.contact.ContactCheckResponse;
import gr.netmechanics.epp.client.impl.commands.create.contact.ContactCreateRequest;
import gr.netmechanics.epp.client.impl.commands.info.contact.ContactInfoRequest;
import gr.netmechanics.epp.client.impl.commands.info.contact.ContactInfoResponse;
import gr.netmechanics.epp.client.impl.commands.update.contact.ContactUpdateRequest;
import gr.netmechanics.epp.client.impl.elements.Address;
import gr.netmechanics.epp.client.impl.elements.PostalInfo;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @author Panos Bariamis (pbaris)
 */
@TestMethodOrder(OrderAnnotation.class)
class ContactTests extends EppClientTestBase {

    @Test
    @Order(1)
    void test_contact_info_marshalling() throws Exception {
        var infoRequest = ContactInfoRequest.builder()
            .contactId("714_epp-client-r")
            .build();

        assertThat(xmlUtil.toXml(request(infoRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("contact_info_request.xml"));
    }

    @Test
    @Order(2)
    void test_contact_check_marshalling() throws Exception {
        var checkRequest = ContactCheckRequest.builder()
            .contactIds("714_epp-client-a", "714_epp-client-t", "714_epp-client-b")
            .build();

        assertThat(xmlUtil.toXml(request(checkRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("contact_check_request.xml"));
    }

    @Test
    @Order(3)
    void test_contact_create_marshalling() throws Exception {
        var createRequest = ContactCreateRequest.builder()
            .contactId("714_epp-client-r")
            .voice("+30.2810320530")
            .email("panos@netmechanics.gr")
            .localPostalInfo(PostalInfo.builder()
                .name("Epp Client")
                .address(Address.builder()
                    .street1("Solonos 23")
                    .city("Heraklion")
                    .stateOrProvince("Crete")
                    .postalCode("71409")
                    .countryCode("GR")))
            .build();

        assertThat(xmlUtil.toXml(request(createRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("contact_create_request.xml"));
    }

    @Test
    @Order(4)
    void test_contact_update_marshalling() throws Exception {
        var updateRequest = ContactUpdateRequest.builder()
            .contactId("714_epp-client-r")
            .voice("+30.2810320531")
            .localPostalInfo(PostalInfo.builder()
                .name("Epp Client R")
                .address(Address.builder()
                    .street1("Solonos 23")
                    .city("Heraklion")
                    .stateOrProvince("Crete")
                    .postalCode("71408")
                    .countryCode("GR")))
            .build();

        assertThat(xmlUtil.toXml(request(updateRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("contact_update_request.xml"));
    }

    @Test
    @Order(5)
    void test_contact_info() {
        var infoRequest = ContactInfoRequest.builder()
            .contactId("714_epp-client-r")
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.getContactInfo(infoRequest));

        assertThat(cmd.<ContactInfoResponse>getInfoResponse()).satisfies(info -> {
            assertThat(info).isNotNull();
            assertThat(info.getId()).isEqualTo("714_epp-client-r");

            assertThat(info.getPrimaryPostalInfo()).satisfies(pi -> {
                assertThat(pi.getAddress().getStreets()).hasSize(1);
                assertThat(pi.getAddress().getStreets().getFirst()).isEqualTo("Solonos 23");
            });
        });
    }

    @Test
    @Order(6)
    void test_contact_check_single_id() {
        var checkRequest = ContactCheckRequest.builder()
            .contactIds("714_epp-client-r")
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.checkContacts(checkRequest));

        assertThat(cmd.<ContactCheckResponse>getCheckResponse()).satisfies(check -> {
            assertThat(check).isNotNull();

            assertThat(check.getCheckData()).satisfies(cd -> {
                assertThat(cd).isNotNull().hasSize(1);

                assertThat(cd.getFirst().getContact()).satisfies(contact -> {
                    assertThat(contact.getId()).isEqualTo("714_epp-client-r");
                    assertThat(contact.getAvailable()).isFalse();
                });
            });
        });

        assertCommentEqual(cmd, "Ο κωδικός προσώπου επαφής είναι ήδη σε χρήση.");
    }

    @Test
    @Order(7)
    void test_contact_check_multi_ids() {
        var checkRequest = ContactCheckRequest.builder()
            .contactIds("714_epp-client-a", "714_epp-client-f")
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.checkContacts(checkRequest));

        assertThat(cmd.<ContactCheckResponse>getCheckResponse()).satisfies(check -> {
            assertThat(check).isNotNull();

            assertThat(check.getCheckData()).satisfies(cd -> {
                assertThat(cd).isNotNull().hasSize(2);

                assertThat(cd.getFirst().getContact()).satisfies(contact -> {
                    assertThat(contact.getId()).isEqualTo("714_epp-client-a");
                    assertThat(contact.getAvailable()).isFalse();
                });

                assertThat(cd.getLast().getContact()).satisfies(contact -> {
                    assertThat(contact.getId()).isEqualTo("714_epp-client-f");
                    assertThat(contact.getAvailable()).isTrue();
                });
            });
        });
    }

    @Test
    @Order(8)
    void test_contact_update() {
        var updateRequest = ContactUpdateRequest.builder()
            .contactId("714_epp-client-r")
            .voice("+30.2810320531")
            .localPostalInfo(PostalInfo.builder()
                .name("Epp Client R")
                .address(Address.builder()
                    .street1("Solonos 23")
                    .city("Heraklion")
                    .stateOrProvince("Crete")
                    .postalCode("71408")
                    .countryCode("GR")))
            .build();

        assertCommandSuccess(eppClient.updateContact(updateRequest));
    }
}
