package gr.netmechanics.epp.client.impl.commands.renew;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gr.netmechanics.epp.client.impl.EppRequest;
import gr.netmechanics.epp.client.impl.commands.renew.domain.DomainRenewRequest;

/**
 * @author Panos Bariamis (pbaris)
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DomainRenewRequest.class, name = "domain:renew")
})
public interface RenewRequest extends EppRequest {
}
