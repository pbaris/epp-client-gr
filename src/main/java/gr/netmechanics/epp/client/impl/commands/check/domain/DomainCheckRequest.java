package gr.netmechanics.epp.client.impl.commands.check.domain;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmptyMax;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.check.CheckRequest;
import gr.netmechanics.epp.client.impl.schema.DomainSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainCheckRequest implements DomainSchema, CheckRequest {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "domain:name")
    private List<String> names;

    public static DomainCheckRequestBuilder builder() {
        return new DomainCheckRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DomainCheckRequestBuilder implements EppBuilder {
        private List<String> names;

        public DomainCheckRequestBuilder domainNames(final String... names) {
            this.names = names != null ? Arrays.stream(names).filter(Objects::nonNull).toList() : null;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public DomainCheckRequest build() {
            var req = new DomainCheckRequest();
            req.names = requireNonEmptyMax(names, 7, "At least one domain name must be specified (up to 7 allowed)");
            return req;
        }
    }
}
