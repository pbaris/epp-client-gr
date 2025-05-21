package gr.netmechanics.epp.client.impl.elements.ext;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppExtension;
import gr.netmechanics.epp.client.impl.commands.update.domain.UpdateOperation;
import gr.netmechanics.epp.client.impl.elements.Bundle;
import gr.netmechanics.epp.client.impl.elements.Comment;
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
public class DomainExtension implements DomainExtSchema, EppExtension {

    @JacksonXmlProperty(localName = "protocol")
    private String protocol;

    @JacksonXmlProperty(localName = "bundle")
    private Bundle bundle;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "comment")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Comment> comments;

    @JacksonXmlProperty(localName = "extdomain:op")
    private String operation;

    public DomainExtension(final UpdateOperation operation) {
        this.operation = operation.getXmlName();
    }
}
