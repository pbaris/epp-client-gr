package gr.netmechanics.epp.client.xml;

import gr.netmechanics.epp.client.impl.commands.check.CheckResponse;
import gr.netmechanics.epp.client.impl.commands.check.contact.ContactCheckResponse;
import gr.netmechanics.epp.client.impl.commands.check.domain.DomainCheckResponse;
import gr.netmechanics.epp.client.impl.commands.check.host.HostCheckResponse;

/**
 * @author Panos Bariamis (pbaris)
 */
public class CheckResponseDeserializer extends AbstractResponseDeserializer<CheckResponse> {

    @Override
    protected Class<DomainCheckResponse> getDomainResponseClass() {
        return DomainCheckResponse.class;
    }

    @Override
    protected Class<ContactCheckResponse> getContactResponseClass() {
        return ContactCheckResponse.class;
    }

    @Override
    protected Class<HostCheckResponse> getHostResponseClass() {
        return HostCheckResponse.class;
    }
}
