package gr.netmechanics.epp.client.xml;

import gr.netmechanics.epp.client.impl.commands.info.InfoResponse;
import gr.netmechanics.epp.client.impl.commands.info.contact.ContactInfoResponse;
import gr.netmechanics.epp.client.impl.commands.info.domain.DomainInfoResponse;
import gr.netmechanics.epp.client.impl.commands.info.host.HostInfoResponse;

/**
 * @author Panos Bariamis (pbaris)
 */
public class InfoResponseDeserializer extends AbstractResponseDeserializer<InfoResponse> {

    @Override
    protected Class<DomainInfoResponse> getDomainResponseClass() {
        return DomainInfoResponse.class;
    }

    @Override
    protected Class<ContactInfoResponse> getContactResponseClass() {
        return ContactInfoResponse.class;
    }

    @Override
    protected Class<HostInfoResponse> getHostResponseClass() {
        return HostInfoResponse.class;
    }
}
