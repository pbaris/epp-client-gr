package gr.netmechanics.epp.client.impl.commands.check;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gr.netmechanics.epp.client.impl.EppRequest;
import gr.netmechanics.epp.client.impl.commands.check.contact.ContactCheckRequest;
import gr.netmechanics.epp.client.impl.commands.check.domain.DomainCheckRequest;
import gr.netmechanics.epp.client.impl.commands.check.host.HostCheckRequest;

/**
 * @author Panos Bariamis (pbaris)
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DomainCheckRequest.class, name = "domain:check"),
    @JsonSubTypes.Type(value = ContactCheckRequest.class, name = "contact:check"),
    @JsonSubTypes.Type(value = HostCheckRequest.class, name = "host:check"),
})
public interface CheckRequest extends EppRequest {
}
