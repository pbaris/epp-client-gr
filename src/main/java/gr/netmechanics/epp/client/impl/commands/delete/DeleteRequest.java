package gr.netmechanics.epp.client.impl.commands.delete;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gr.netmechanics.epp.client.impl.EppRequest;
import gr.netmechanics.epp.client.impl.commands.delete.domain.DomainDeleteRequest;
import gr.netmechanics.epp.client.impl.commands.delete.host.HostDeleteRequest;

/**
 * @author Panos Bariamis (pbaris)
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DomainDeleteRequest.class, name = "domain:delete"),
    @JsonSubTypes.Type(value = HostDeleteRequest.class, name = "host:delete")
})
public interface DeleteRequest extends EppRequest {
}
