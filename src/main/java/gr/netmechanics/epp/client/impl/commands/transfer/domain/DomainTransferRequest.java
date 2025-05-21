package gr.netmechanics.epp.client.impl.commands.transfer.domain;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.EppExtension;
import gr.netmechanics.epp.client.impl.commands.transfer.TransferRequest;
import gr.netmechanics.epp.client.impl.elements.AuthorizationInfo;
import gr.netmechanics.epp.client.impl.elements.ext.DomainTransferExtension;
import gr.netmechanics.epp.client.impl.elements.ext.HasExtension;
import gr.netmechanics.epp.client.impl.schema.DomainSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainTransferRequest implements DomainSchema, TransferRequest, HasExtension {

    @JacksonXmlProperty(localName = "domain:name")
    private String name;

    @JacksonXmlProperty(localName = "domain:authInfo")
    private AuthorizationInfo authInfo;

    @JsonIgnore
    private EppExtension extension;

    public static DomainTransferRequestBuilder builder() {
        return new DomainTransferRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DomainTransferRequestBuilder implements EppBuilder {

        private String name;
        private String password;
        private String registrant;

        public DomainTransferRequestBuilder domainName(final String name) {
            this.name = name;
            return this;
        }

        public DomainTransferRequestBuilder password(final String password) {
            this.password = password;
            return this;
        }

        public DomainTransferRequestBuilder registrant(final String registrant) {
            this.registrant = registrant;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public DomainTransferRequest build() {
            var req = new DomainTransferRequest();
            req.name = requireNonEmpty(name, "Domain name must be specified");
            req.authInfo = new AuthorizationInfo(requireNonEmpty(password, "Password for authorization must be specified"));
            req.extension = new DomainTransferExtension(requireNonEmpty(registrant, "Registrant ID must be specified"));
            return req;
        }
    }
}
