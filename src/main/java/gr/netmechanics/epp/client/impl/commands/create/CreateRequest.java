package gr.netmechanics.epp.client.impl.commands.create;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gr.netmechanics.epp.client.impl.EppRequest;
import gr.netmechanics.epp.client.impl.commands.create.contact.ContactCreateRequest;
import gr.netmechanics.epp.client.impl.commands.create.domain.DomainCreateRequest;
import gr.netmechanics.epp.client.impl.commands.create.host.HostCreateRequest;

/**
 * @author Panos Bariamis (pbaris)
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DomainCreateRequest.class, name = "domain:create"),
    @JsonSubTypes.Type(value = ContactCreateRequest.class, name = "contact:create"),
    @JsonSubTypes.Type(value = HostCreateRequest.class, name = "host:create")
})
public interface CreateRequest extends EppRequest {
}
