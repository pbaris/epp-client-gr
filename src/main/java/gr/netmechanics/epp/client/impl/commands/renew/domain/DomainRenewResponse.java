package gr.netmechanics.epp.client.impl.commands.renew.domain;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.commands.renew.RenewResponse;
import gr.netmechanics.epp.client.impl.elements.Audit;
import gr.netmechanics.epp.client.impl.elements.Domain;
import gr.netmechanics.epp.client.impl.schema.DomainSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainRenewResponse implements DomainSchema, RenewResponse {
    @JacksonXmlProperty(localName = "name")
    private Domain domain;

    @JsonUnwrapped
    private Audit audit;
}
