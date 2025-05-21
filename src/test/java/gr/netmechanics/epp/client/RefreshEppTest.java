package gr.netmechanics.epp.client;

import gr.netmechanics.epp.client.impl.commands.info.domain.DomainInfoRequest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author Panos Bariamis (pbaris)
 */
@TestMethodOrder(OrderAnnotation.class)
class RefreshEppTest extends EppClientTestBase {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Test
    @Order(1)
    void test_refresh_event() {
        checkDomainInfo();
        publisher.publishEvent(new EppRefreshEvent(this));
        checkDomainInfo();
    }

    private void checkDomainInfo() {
        assertCommandSuccess(eppClient.getDomainInfo(DomainInfoRequest.builder()
            .domainName("epp-client.gr")
            .build()));
    }
}
