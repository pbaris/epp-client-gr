package gr.netmechanics.epp.client.impl.commands.info;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gr.netmechanics.epp.client.impl.EppRequest;
import gr.netmechanics.epp.client.impl.commands.info.contact.ContactInfoRequest;
import gr.netmechanics.epp.client.impl.commands.info.domain.DomainInfoRequest;
import gr.netmechanics.epp.client.impl.commands.info.host.HostInfoRequest;

/**
 * @author Panos Bariamis (pbaris)
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DomainInfoRequest.class, name = "domain:info"),
    @JsonSubTypes.Type(value = ContactInfoRequest.class, name = "contact:info"),
    @JsonSubTypes.Type(value = HostInfoRequest.class, name = "host:info")
})
public interface InfoRequest extends EppRequest {
}
