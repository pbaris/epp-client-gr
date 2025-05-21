package gr.netmechanics.epp.client.impl.commands.check.contact;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmptyMax;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.check.CheckRequest;
import gr.netmechanics.epp.client.impl.schema.ContactSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContactCheckRequest implements ContactSchema, CheckRequest {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "contact:id")
    private List<String> ids;

    public static ContactCheckRequestBuilder builder() {
        return new ContactCheckRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ContactCheckRequestBuilder implements EppBuilder {
        private List<String> ids;

        public ContactCheckRequestBuilder contactIds(final String... ids) {
            this.ids = ids != null ? Arrays.stream(ids).filter(Objects::nonNull).toList() : null;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ContactCheckRequest build() {
            var req = new ContactCheckRequest();
            req.ids = requireNonEmptyMax(ids, 3, "At least one contact ID must be specified (up to 3 allowed)");
            return req;
        }
    }
}
