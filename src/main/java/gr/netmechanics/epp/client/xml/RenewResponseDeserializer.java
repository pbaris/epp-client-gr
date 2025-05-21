package gr.netmechanics.epp.client.xml;

import gr.netmechanics.epp.client.impl.EppResponse;
import gr.netmechanics.epp.client.impl.commands.renew.RenewResponse;
import gr.netmechanics.epp.client.impl.commands.renew.domain.DomainRenewResponse;

/**
 * @author Panos Bariamis (pbaris)
 */
public class RenewResponseDeserializer extends AbstractResponseDeserializer<RenewResponse> {

    @Override
    protected Class<DomainRenewResponse> getDomainResponseClass() {
        return DomainRenewResponse.class;
    }

    @Override
    protected Class<EppResponse> getContactResponseClass() {
        throw new UnsupportedOperationException("Contact renewal is not defined");
    }

    @Override
    protected Class<EppResponse> getHostResponseClass() {
        throw new UnsupportedOperationException("Host renewal is not defined");
    }
}
