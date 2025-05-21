package gr.netmechanics.epp.client.impl.commands.create.host;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.create.CreateRequest;
import gr.netmechanics.epp.client.impl.elements.IPAddress;
import gr.netmechanics.epp.client.impl.schema.HostSchema;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HostCreateRequest implements HostSchema, CreateRequest {

    @JacksonXmlProperty(localName = "host:name")
    private String name;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "host:addr")
    private List<IPAddress> addresses = new ArrayList<>();

    public static HostCreateRequestBuilder builder() {
        return new HostCreateRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HostCreateRequestBuilder implements EppBuilder {
        private String name;
        private List<String> addresses;

        public HostCreateRequestBuilder hostName(final String name) {
            this.name = name;
            return this;
        }

        public HostCreateRequestBuilder addresses(final List<String> addresses) {
            this.addresses = addresses;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public HostCreateRequest build() {
            var req = new HostCreateRequest();
            req.name = requireNonEmpty(name, "Host name must be specified");

            if (addresses != null) {
                req.addresses.addAll(addresses.stream()
                    .map(ip -> new IPAddress(requireNonEmpty(ip, "IP address must be specified")))
                    .filter(ip -> ip.getType() != null)
                    .toList());
            }

            if (req.addresses.size() > 16) {
                throw new IllegalArgumentException("Maximum number of allowed IP is 16, but %d were specified".formatted(req.addresses.size()));
            }

            return req;
        }
    }
}
