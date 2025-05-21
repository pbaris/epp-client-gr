package gr.netmechanics.epp.client.impl.elements.ext;

import static gr.netmechanics.epp.client.impl.commands.delete.domain.DeleteOperation.RECALL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppExtension;
import gr.netmechanics.epp.client.impl.commands.delete.domain.DeleteOperation;
import gr.netmechanics.epp.client.impl.schema.DomainExtSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DomainDeleteExtension implements DomainExtSchema, EppExtension {

    @JacksonXmlProperty(localName = "extdomain:pw")
    private String password;

    @JacksonXmlProperty(localName = "extdomain:op")
    private String operation;

    @JacksonXmlProperty(localName = "extdomain:protocol")
    private String protocol;

    public DomainDeleteExtension(final DeleteOperation operation, final String passwordOrProtocol) {
        this.password = operation != RECALL ? passwordOrProtocol : null;
        this.operation = operation.getXmlName();
        this.protocol = operation == RECALL ? passwordOrProtocol : null;
    }
}
