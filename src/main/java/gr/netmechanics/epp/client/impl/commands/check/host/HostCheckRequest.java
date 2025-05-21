package gr.netmechanics.epp.client.impl.commands.check.host;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmptyMax;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.check.CheckRequest;
import gr.netmechanics.epp.client.impl.schema.HostSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HostCheckRequest implements HostSchema, CheckRequest {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "host:name")
    private List<String> names;

    public static HostCheckRequestBuilder builder() {
        return new HostCheckRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HostCheckRequestBuilder implements EppBuilder {
        private List<String> names;

        public HostCheckRequestBuilder hostNames(final String... names) {
            this.names = names != null ? Arrays.stream(names).filter(Objects::nonNull).toList() : null;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public HostCheckRequest build() {
            var req = new HostCheckRequest();
            req.names = requireNonEmptyMax(names, 10, "At least one domain name must be specified (up to 10 allowed)");
            return req;
        }
    }
}
