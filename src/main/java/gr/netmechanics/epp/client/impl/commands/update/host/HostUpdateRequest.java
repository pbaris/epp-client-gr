package gr.netmechanics.epp.client.impl.commands.update.host;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.update.UpdateRequest;
import gr.netmechanics.epp.client.impl.elements.IPAddress;
import gr.netmechanics.epp.client.impl.schema.HostSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@JsonPropertyOrder({ "name", "addressesToAdd", "addressesToRemove", "newName" })
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HostUpdateRequest implements HostSchema, UpdateRequest {

    @JacksonXmlProperty(localName = "host:name")
    private String name;

    @JacksonXmlProperty(localName = "host:chg")
    private NameChangeNode newName;

    @JacksonXmlProperty(localName = "host:add")
    private AddressesChangeNode addressesToAdd;

    @JacksonXmlProperty(localName = "host:rem")
    private AddressesChangeNode addressesToRemove;

    private record NameChangeNode(
        @JacksonXmlProperty(localName = "host:name")
        String name) {
    }

    private record AddressesChangeNode(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "host:addr")
        List<IPAddress> addresses) {
    }

    public static HostUpdateRequestBuilder builder() {
        return new HostUpdateRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HostUpdateRequestBuilder implements EppBuilder {

        private String name;
        private String newName;
        private List<String> addressesAdd;
        private List<String> addressesRemove;

        public HostUpdateRequestBuilder hostName(final String name) {
            this.name = name;
            return this;
        }

        public HostUpdateRequestBuilder hostNewName(final String name) {
            this.newName = name;
            return this;
        }

        public HostUpdateRequestBuilder addressesToAdd(final List<String> addresses) {
            this.addressesAdd = addresses;
            return this;
        }

        public HostUpdateRequestBuilder addressesToRemove(final List<String> addresses) {
            this.addressesRemove = addresses;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public HostUpdateRequest build() {
            var req = new HostUpdateRequest();
            req.name = requireNonEmpty(name, "Host name must be specified");

            if (StringUtils.hasText(newName)) {
                req.newName = new NameChangeNode(newName);
            }

            if (addressesAdd != null) {
                req.addressesToAdd = new AddressesChangeNode(addressesAdd.stream()
                    .map(ip -> new IPAddress(requireNonEmpty(ip, "IPv4 address must be specified")))
                    .filter(ip -> ip.getType() != null)
                    .toList());
            }

            if (addressesRemove != null) {
                req.addressesToRemove = new AddressesChangeNode(addressesRemove.stream()
                    .map(ip -> new IPAddress(requireNonEmpty(ip, "IPv4 address must be specified")))
                    .filter(ip -> ip.getType() != null)
                    .toList());
            }

            return req;
        }
    }
}
