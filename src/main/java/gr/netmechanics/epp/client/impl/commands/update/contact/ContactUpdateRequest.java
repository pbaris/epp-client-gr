package gr.netmechanics.epp.client.impl.commands.update.contact;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.update.UpdateRequest;
import gr.netmechanics.epp.client.impl.elements.PostalInfo;
import gr.netmechanics.epp.client.impl.schema.ContactSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ContactUpdateRequest implements ContactSchema, UpdateRequest {
    @JacksonXmlProperty(localName = "contact:id")
    private String id;

    @JacksonXmlProperty(localName = "contact:chg")
    private ChangesNode changes;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private record ChangesNode(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "contact:postalInfo")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<PostalInfo> postalInfos,

        @JacksonXmlProperty(localName = "contact:voice")
        String voice,

        @JacksonXmlProperty(localName = "contact:fax")
        String fax,

        @JacksonXmlProperty(localName = "contact:email")
        String email) {
    }

    public static ContactUpdateRequestBuilder builder() {
        return new ContactUpdateRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ContactUpdateRequestBuilder implements EppBuilder {

        private String id;
        private String voice;
        private String fax;
        private String email;
        private PostalInfo.PostalInfoBuilder locPostalInfo;
        private PostalInfo.PostalInfoBuilder intPostalInfo;

        public ContactUpdateRequestBuilder contactId(final String id) {
            this.id = id;
            return this;
        }

        public ContactUpdateRequestBuilder voice(final String voice) {
            this.voice = voice;
            return this;
        }

        public ContactUpdateRequestBuilder fax(final String fax) {
            this.fax = fax;
            return this;
        }

        public ContactUpdateRequestBuilder email(final String email) {
            this.email = email;
            return this;
        }

        public ContactUpdateRequestBuilder localPostalInfo(final PostalInfo.PostalInfoBuilder postalInfoBuilder) {
            this.locPostalInfo = postalInfoBuilder;
            return this;
        }

        public ContactUpdateRequestBuilder internationalPostalInfo(final PostalInfo.PostalInfoBuilder postalInfoBuilder) {
            this.intPostalInfo = postalInfoBuilder;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ContactUpdateRequest build() {
            var req = new ContactUpdateRequest();
            req.id = EppBuilder.requireNonEmpty(id, "Contact ID must be specified");

            List<PostalInfo> postalInfos = new ArrayList<>();
            if (locPostalInfo != null) {
                postalInfos.add(locPostalInfo.update(true).type("loc").build());
            }

            if (intPostalInfo != null) {
                postalInfos.add(intPostalInfo.update(true).type("int").build());
            }

            req.changes = new ChangesNode(postalInfos, voice, fax, email);

            return req;
        }
    }
}
