package gr.netmechanics.epp.client.impl.commands.info.contact;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.info.InfoRequest;
import gr.netmechanics.epp.client.impl.schema.ContactSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContactInfoRequest implements ContactSchema, InfoRequest {

    @JacksonXmlProperty(localName = "contact:id")
    private String id;

    public static ContactInfoRequestBuilder builder() {
        return new ContactInfoRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ContactInfoRequestBuilder implements EppBuilder {
        private String id;

        public ContactInfoRequestBuilder contactId(final String id) {
            this.id = id;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ContactInfoRequest build() {
            var req = new ContactInfoRequest();
            req.id = requireNonEmpty(id, "Contact ID must be specified");
            return req;
        }
    }
}
