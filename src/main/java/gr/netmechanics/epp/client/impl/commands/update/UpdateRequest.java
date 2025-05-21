package gr.netmechanics.epp.client.impl.commands.update;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gr.netmechanics.epp.client.impl.EppRequest;
import gr.netmechanics.epp.client.impl.commands.update.contact.ContactUpdateRequest;
import gr.netmechanics.epp.client.impl.commands.update.domain.DomainUpdateRequest;
import gr.netmechanics.epp.client.impl.commands.update.host.HostUpdateRequest;

/**
 * @author Panos Bariamis (pbaris)
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DomainUpdateRequest.class, name = "domain:update"),
    @JsonSubTypes.Type(value = ContactUpdateRequest.class, name = "contact:update"),
    @JsonSubTypes.Type(value = HostUpdateRequest.class, name = "host:update")
})
public interface UpdateRequest extends EppRequest {
}
