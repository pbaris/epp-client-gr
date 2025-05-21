package gr.netmechanics.epp.client;

import static gr.netmechanics.epp.client.impl.EppCommandRequest.request;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.check.domain.DomainCheckRequest;
import gr.netmechanics.epp.client.impl.commands.check.domain.DomainCheckResponse;
import gr.netmechanics.epp.client.impl.commands.create.domain.DomainCreateRequest;
import gr.netmechanics.epp.client.impl.commands.create.domain.DomainCreateResponse;
import gr.netmechanics.epp.client.impl.commands.delete.domain.DeleteOperation;
import gr.netmechanics.epp.client.impl.commands.delete.domain.DomainDeleteRequest;
import gr.netmechanics.epp.client.impl.commands.info.domain.DomainInfoRequest;
import gr.netmechanics.epp.client.impl.commands.info.domain.DomainInfoResponse;
import gr.netmechanics.epp.client.impl.commands.renew.domain.DomainRenewRequest;
import gr.netmechanics.epp.client.impl.commands.renew.domain.DomainRenewResponse;
import gr.netmechanics.epp.client.impl.commands.transfer.domain.DomainTransferRequest;
import gr.netmechanics.epp.client.impl.commands.update.domain.DomainUpdateRequest;
import gr.netmechanics.epp.client.impl.elements.ext.DomainExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @author Panos Bariamis (pbaris)
 */
@TestMethodOrder(OrderAnnotation.class)
class DomainTests extends EppClientTestBase {

    @Test
    @Order(1)
    void test_domain_info_marshalling() throws Exception {
        var infoRequest = DomainInfoRequest.builder()
            .domainName("epp-client.gr")
            .build();

        assertThat(xmlUtil.toXml(request(infoRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("domain_info_request.xml"));
    }

    @Test
    @Order(2)
    void test_domain_check_marshalling() throws Exception {
        var checkRequest = DomainCheckRequest.builder()
            .domainNames("epp-client.gr", "fail-epp-client.gr")
            .build();

        assertThat(xmlUtil.toXml(request(checkRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("domain_check_request.xml"));
    }

    @Test
    @Order(3)
    void test_domain_create_marshalling() throws Exception {
        var createRequest = DomainCreateRequest.builder()
            .domainName("epp-client-test1.gr")
            .years(2)
            .nameServers(List.of("ns1.epp-client.gr", "ns2.epp-client.gr"))
            .registrant("714_epp-client-r")
            .adminContacts(List.of("714_epp-client-a"))
            .techContacts(List.of("714_epp-client-t"))
            .billingContacts(List.of("714_epp-client-b"))
            .build();

        assertThat(xmlUtil.toXml(request(createRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("domain_create_request.xml"));
    }

    @Test
    @Order(4)
    void test_domain_change_owner_marshalling() throws Exception {
        var updateRequest = DomainUpdateRequest.builder()
            .domainName("epp-client-test2.gr")
            .changeOwner("714_epp-client-o")
            .build();

        assertThat(xmlUtil.toXml(request(updateRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("domain_change_owner_request.xml"));
    }

    @Test
    @Order(5)
    void test_domain_change_owner_name_marshalling() throws Exception {
        var updateRequest = DomainUpdateRequest.builder()
            .domainName("epp-client-test2.gr")
            .changeOwnerName("714_epp-client-o")
            .build();

        assertThat(xmlUtil.toXml(request(updateRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("domain_change_owner_name_request.xml"));
    }

    @Test
    @Order(7)
    void test_domain_renew_marshalling() throws Exception {
        var renewRequest = DomainRenewRequest.builder()
            .domainName("epp-client-test1.gr")
            .currentExpirationDate(LocalDate.of(2027, 5, 4))
            .years(4)
            .build();

        assertThat(xmlUtil.toXml(request(renewRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("domain_renew_request.xml"));
    }

    @Test
    @Order(8)
    void test_domain_transfer_marshalling() throws Exception {
        var transferRequest = DomainTransferRequest.builder()
            .domainName("epp-client-test1.gr")
            .registrant("reg_contact001")
            .password("password")
            .build();

        assertThat(xmlUtil.toXml(request(transferRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("domain_transfer_request.xml"));
    }

    @Test
    @Order(9)
    void test_domain_delete_marshalling() throws Exception {
        var deleteRequest = DomainDeleteRequest.builder()
            .domainName("epp-client-test1.gr")
            .deleteOperation(DeleteOperation.DOMAIN, "password")
            .build();

        assertThat(xmlUtil.toXml(request(deleteRequest, eppProps.getClTrId())))
            .isEqualTo(xmlUtil.xmlFromFile("domain_delete_request.xml"));
    }

    @Test
    @Disabled("This can run only once, by changing domain")
    @Order(10)
    void test_domain_create() {
        var createRequest = DomainCreateRequest.builder()
            .domainName("epp-client-test1.gr")
            .years(2)
            .nameServers(List.of("ns1.epp-client.gr", "ns2.epp-client.gr"))
            .registrant("714_epp-client-r")
            .adminContacts(List.of("714_epp-client-a"))
            .techContacts(List.of("714_epp-client-t"))
            .billingContacts(List.of("714_epp-client-b"))
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.createDomain(createRequest));

        assertThat(cmd.<DomainCreateResponse>getCreateResponse()).satisfies(create -> {
            assertThat(create).isNotNull();
            assertThat(create.getDomain().getName()).isEqualTo("epp-client-test1.gr");
        });
    }

    @Test
    @Order(11)
    void test_domain_info() {
        var infoRequest = DomainInfoRequest.builder()
            .domainName("epp-client-test1.gr")
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.getDomainInfo(infoRequest));

        assertThat(cmd.<DomainInfoResponse>getInfoResponse()).satisfies(info -> {
            assertThat(info).isNotNull();
            assertThat(info.getDomain().getName()).isEqualTo("epp-client-test1.gr");
            assertThat(info.getRegistrant()).isEqualTo("714_epp-client-r");
            assertThat(info.getHosts()).isNull();

            assertThat(info.getNameServers()).satisfies(ns -> {
                assertThat(ns).isNotNull().hasSize(2);
                assertThat(ns.getFirst()).isEqualTo("ns1.epp-client.gr");
                assertThat(ns.getLast()).isEqualTo("ns2.epp-client.gr");
            });

            assertThat(info.getContacts()).satisfies(contacts -> {
                assertThat(contacts).isNotNull().hasSize(3);

                assertThat(contacts.getFirst()).satisfies(contact -> {
                    assertThat(contact.getId()).isEqualTo("714_epp-client-a");
                    assertThat(contact.getType()).isEqualTo("admin");
                });

                assertThat(contacts.getLast()).satisfies(contact -> {
                    assertThat(contact.getId()).isEqualTo("714_epp-client-b");
                    assertThat(contact.getType()).isEqualTo("billing");
                });
            });
        });

        assertThat(cmd.<DomainExtension>getExtension()).satisfies(ext -> {
            assertThat(ext).isNotNull();
            assertThat(ext.getBundle().getName()).isEqualTo("epp-client-test1.gr");
        });
    }

    @Test
    @Order(12)
    void test_domain_check_single_domain() {
        var checkRequest = DomainCheckRequest.builder()
            .domainNames("epp-client.gr")
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.checkDomains(checkRequest));

        assertThat(cmd.<DomainCheckResponse>getCheckResponse()).satisfies(check -> {
            assertThat(check).isNotNull();

            assertThat(check.getCheckData()).satisfies(cd -> {
                assertThat(cd).isNotNull().hasSize(1);

                assertThat(cd.getFirst().getDomain()).satisfies(domain -> {
                    assertThat(domain.getName()).isEqualTo("epp-client.gr");
                    assertThat(domain.getAvailable()).isFalse();
                });
            });
        });
    }

    @Test
    @Order(13)
    void test_domain_check_multi_domains() {
        var checkRequest = DomainCheckRequest.builder()
            .domainNames("epp-client.gr", "epp-client-test1.gr")
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.checkDomains(checkRequest));

        assertThat(cmd.<DomainCheckResponse>getCheckResponse()).satisfies(check -> {
            assertThat(check).isNotNull();

            assertThat(check.getCheckData()).satisfies(cd -> {
                assertThat(cd).isNotNull().hasSize(2);

                assertThat(cd.getFirst().getDomain()).satisfies(domain -> {
                    assertThat(domain.getName()).isEqualTo("epp-client.gr");
                    assertThat(domain.getAvailable()).isFalse();
                });

                assertThat(cd.getLast().getDomain()).satisfies(domain -> {
                    assertThat(domain.getName()).isEqualTo("epp-client-test1.gr");
                    assertThat(domain.getAvailable()).isFalse();
                });
            });
        });
    }

    @Test
    @Order(14)
    void test_domain_renew() {
        var infoRequest = DomainInfoRequest.builder()
            .domainName("epp-client-test1.gr")
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.getDomainInfo(infoRequest));

        DomainInfoResponse info = cmd.getInfoResponse();
        LocalDate currentExpirationDate = info.getAudit().getExpirationDate();
        int yearsToRenew = 2;

        var renewRequest = DomainRenewRequest.builder()
            .domainName("epp-client-test1.gr")
            .currentExpirationDate(currentExpirationDate)
            .years(yearsToRenew)
            .build();

        cmd = assertCommandSuccess(eppClient.renewDomain(renewRequest));

        assertThat(cmd.<DomainRenewResponse>getRenewResponse()).satisfies(renew -> {
            assertThat(renew).isNotNull();

            assertThat(renew.getDomain().getName()).isEqualTo("epp-client-test1.gr");
            assertThat(renew.getAudit().getExpirationDate()).isEqualTo(currentExpirationDate.plusYears(yearsToRenew));
        });
    }

    @Test
    @Order(15)
    @Disabled
    void test_domain_update() {
        var updateRequest = DomainUpdateRequest.builder()
            .domainName("epp-client-test2.gr")
            .adminContactsToRemove(List.of("714_epp-client-a"))
            .adminContactsToAdd(List.of("714_epp-client-o"))
            .nameServersToAdd(List.of("ns1.epp-client.gr", "ns2.epp-client.gr"))
            .build();

        assertCommandSuccess(eppClient.updateDomain(updateRequest));

        // reset state
        updateRequest = DomainUpdateRequest.builder()
            .domainName("epp-client-test2.gr")
            .adminContactsToRemove(List.of("714_epp-client-o"))
            .adminContactsToAdd(List.of("714_epp-client-a"))
            .nameServersToRemove(List.of("ns1.epp-client.gr", "ns2.epp-client.gr"))
            .build();

        assertCommandSuccess(eppClient.updateDomain(updateRequest));
    }

    @Test
    @Order(16)
    void test_domain_change_owner() {
        var updateRequest = DomainUpdateRequest.builder()
            .domainName("epp-client-test2.gr")
            .changeOwner("714_epp-client-o")
            .build();

        assertCommandSuccess(eppClient.updateDomain(updateRequest));
    }

    @Test
    @Order(17)
    void test_domain_change_owner_name() {
        var updateRequest = DomainUpdateRequest.builder()
            .domainName("epp-client-test2.gr")
            .changeOwnerName("714_epp-client-r")
            .build();

        assertCommandSuccess(eppClient.updateDomain(updateRequest));
    }
}