package gr.netmechanics.epp.client.impl.elements.ext;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppExtension;
import gr.netmechanics.epp.client.impl.schema.DomainExtSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DomainTransferExtension implements DomainExtSchema, EppExtension {

    @JacksonXmlProperty(localName = "extdomain:registrantid")
    private String registrant;

    public DomainTransferExtension(final String registrant) {
        this.registrant = registrant;
    }
}
