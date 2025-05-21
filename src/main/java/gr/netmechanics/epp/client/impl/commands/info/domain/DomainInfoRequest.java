package gr.netmechanics.epp.client.impl.commands.info.domain;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.info.InfoRequest;
import gr.netmechanics.epp.client.impl.elements.Domain;
import gr.netmechanics.epp.client.impl.schema.DomainSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainInfoRequest implements DomainSchema, InfoRequest {

    @JacksonXmlProperty(localName = "domain:name")
    private Domain domain;

    public static DomainInfoRequestBuilder builder() {
        return new DomainInfoRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DomainInfoRequestBuilder implements EppBuilder {

        private String name;

        public DomainInfoRequestBuilder domainName(final String name) {
            this.name = name;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public DomainInfoRequest build() {
            var req = new DomainInfoRequest();
            req.domain = new Domain(requireNonEmpty(name, "Domain name must be specified"), "all");
            return req;
        }
    }
}
