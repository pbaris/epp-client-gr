package gr.netmechanics.epp.client.impl.commands.delete.host;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.delete.DeleteRequest;
import gr.netmechanics.epp.client.impl.schema.HostSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HostDeleteRequest implements HostSchema, DeleteRequest {

    @JacksonXmlProperty(localName = "host:name")
    private String name;

    public static HostDeleteRequestBuilder builder() {
        return new HostDeleteRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HostDeleteRequestBuilder implements EppBuilder {
        private String name;

        public HostDeleteRequestBuilder hostName(final String name) {
            this.name = name;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public HostDeleteRequest build() {
            var req = new HostDeleteRequest();
            req.name = requireNonEmpty(name, "Host name must be specified");
            return req;
        }
    }
}
