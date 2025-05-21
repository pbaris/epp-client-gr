package gr.netmechanics.epp.client.impl.commands.create.contact;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.create.CreateRequest;
import gr.netmechanics.epp.client.impl.elements.AuthorizationInfo;
import gr.netmechanics.epp.client.impl.elements.PostalInfo;
import gr.netmechanics.epp.client.impl.elements.PostalInfo.PostalInfoBuilder;
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
public class ContactCreateRequest implements ContactSchema, CreateRequest {
    @JacksonXmlProperty(localName = "contact:id")
    private String id;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "contact:postalInfo")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private final List<PostalInfo> postalInfos = new ArrayList<>();

    @JacksonXmlProperty(localName = "contact:voice")
    private String voice;

    @JacksonXmlProperty(localName = "contact:fax")
    private String fax;

    @JacksonXmlProperty(localName = "contact:email")
    private String email;

    @JacksonXmlProperty(localName = "contact:authInfo")
    private final AuthorizationInfo authorizationInfo = new AuthorizationInfo("");

    public static ContactCreateRequestBuilder builder() {
        return new ContactCreateRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ContactCreateRequestBuilder implements EppBuilder {

        private String id;
        private String voice;
        private String fax;
        private String email;
        private PostalInfoBuilder locPostalInfo;
        private PostalInfoBuilder intPostalInfo;

        public ContactCreateRequestBuilder contactId(final String id) {
            this.id = id;
            return this;
        }

        public ContactCreateRequestBuilder voice(final String voice) {
            this.voice = voice;
            return this;
        }

        public ContactCreateRequestBuilder fax(final String fax) {
            this.fax = fax;
            return this;
        }

        public ContactCreateRequestBuilder email(final String email) {
            this.email = email;
            return this;
        }

        public ContactCreateRequestBuilder localPostalInfo(final PostalInfoBuilder postalInfoBuilder) {
            this.locPostalInfo = postalInfoBuilder;
            return this;
        }

        public ContactCreateRequestBuilder internationalPostalInfo(final PostalInfoBuilder postalInfoBuilder) {
            this.intPostalInfo = postalInfoBuilder;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ContactCreateRequest build() {
            var req = new ContactCreateRequest();
            req.id = EppBuilder.requireNonEmpty(id, "Contact ID must be specified");
            req.voice = voice;
            req.fax = fax;
            req.email = email;

            if (locPostalInfo == null) {
                throw new IllegalStateException("Local postal info must be specified");
            }
            req.postalInfos.add(locPostalInfo.update(false).type("loc").build());

            if (intPostalInfo != null) {
                req.postalInfos.add(intPostalInfo.update(false).type("int").build());
            }

            return req;
        }
    }
}
