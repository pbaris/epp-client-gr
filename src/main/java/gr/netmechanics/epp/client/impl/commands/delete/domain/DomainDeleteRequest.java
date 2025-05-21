package gr.netmechanics.epp.client.impl.commands.delete.domain;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.delete.DeleteRequest;
import gr.netmechanics.epp.client.impl.elements.ext.DomainDeleteExtension;
import gr.netmechanics.epp.client.impl.elements.ext.HasExtension;
import gr.netmechanics.epp.client.impl.schema.DomainSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DomainDeleteRequest implements DomainSchema, DeleteRequest, HasExtension {

    @JacksonXmlProperty(localName = "domain:name")
    private String name;

    @JsonIgnore
    private DomainDeleteExtension extension;

    public static DomainDeleteRequestBuilder builder() {
        return new DomainDeleteRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DomainDeleteRequestBuilder implements EppBuilder {

        private String name;
        private DeleteOperation operation;
        private String passwordOrProtocol;

        public DomainDeleteRequestBuilder domainName(final String name) {
            this.name = name;
            return this;
        }

        public DomainDeleteRequestBuilder deleteOperation(final DeleteOperation operation, final String passwordOrProtocol) {
            this.operation = operation;
            this.passwordOrProtocol = passwordOrProtocol;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public DomainDeleteRequest build() {
            var req = new DomainDeleteRequest();
            req.name = requireNonEmpty(name, "Domain name must be specified");

            if (operation == null || passwordOrProtocol == null) {
                throw new IllegalArgumentException("Domain delete operation and password or protocol must be specified");
            }

            req.extension = new DomainDeleteExtension(operation, passwordOrProtocol);

            return req;
        }
    }
}
