package gr.netmechanics.epp.client.impl.commands.info.host;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.info.InfoRequest;
import gr.netmechanics.epp.client.impl.schema.HostSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HostInfoRequest implements HostSchema, InfoRequest {

    @JacksonXmlProperty(localName = "host:name")
    private String name;

    public static HostInfoRequestBuilder builder() {
        return new HostInfoRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HostInfoRequestBuilder implements EppBuilder {
        private String name;

        public HostInfoRequestBuilder hostName(final String name) {
            this.name = name;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public HostInfoRequest build() {
            var req = new HostInfoRequest();
            req.name = requireNonEmpty(name, "Host name must be specified");
            return req;
        }
    }
}
