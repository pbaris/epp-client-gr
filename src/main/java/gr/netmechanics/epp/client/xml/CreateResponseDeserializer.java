package gr.netmechanics.epp.client.xml;

import gr.netmechanics.epp.client.impl.commands.create.CreateResponse;
import gr.netmechanics.epp.client.impl.commands.create.contact.ContactCreateResponse;
import gr.netmechanics.epp.client.impl.commands.create.domain.DomainCreateResponse;
import gr.netmechanics.epp.client.impl.commands.create.host.HostCreateResponse;

/**
 * @author Panos Bariamis (pbaris)
 */
public class CreateResponseDeserializer extends AbstractResponseDeserializer<CreateResponse> {

    @Override
    protected Class<DomainCreateResponse> getDomainResponseClass() {
        return DomainCreateResponse.class;
    }

    @Override
    protected Class<ContactCreateResponse> getContactResponseClass() {
        return ContactCreateResponse.class;
    }

    @Override
    protected Class<HostCreateResponse> getHostResponseClass() {
        return HostCreateResponse.class;
    }
}
