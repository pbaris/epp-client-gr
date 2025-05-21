package gr.netmechanics.epp.client.impl.commands.create.domain;

import static gr.netmechanics.epp.client.impl.EppBuilder.mergeContacts;
import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;
import static gr.netmechanics.epp.client.impl.EppBuilder.requireYears;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.create.CreateRequest;
import gr.netmechanics.epp.client.impl.elements.AuthorizationInfo;
import gr.netmechanics.epp.client.impl.elements.Contact;
import gr.netmechanics.epp.client.impl.elements.Period;
import gr.netmechanics.epp.client.impl.schema.DomainSchema;
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
public class DomainCreateRequest implements DomainSchema, CreateRequest {
    @JacksonXmlProperty(localName = "domain:name")
    private String name;

    @JacksonXmlProperty(localName = "domain:period")
    private Period period;

    @JacksonXmlElementWrapper(localName = "domain:ns")
    @JacksonXmlProperty(localName = "domain:hostObj")
    private List<String> nameServers;

    @JacksonXmlProperty(localName = "domain:registrant")
    private Contact registrant;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "domain:contact")
    private List<Contact> contacts;

    @JacksonXmlProperty(localName = "domain:authInfo")
    private final AuthorizationInfo authorizationInfo = new AuthorizationInfo("");

    public static DomainCreateRequestBuilder builder() {
        return new DomainCreateRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DomainCreateRequestBuilder implements EppBuilder {
        private String name;
        private int years;
        private List<String> nameServers;
        private String registrantId;
        private List<String> adminContacts;
        private List<String> techContacts;
        private List<String> billingContacts;

        public DomainCreateRequestBuilder domainName(final String name) {
            this.name = name;
            return this;
        }

        public DomainCreateRequestBuilder years(final int years) {
            this.years = years;
            return this;
        }

        public DomainCreateRequestBuilder nameServers(final List<String> nameServers) {
            this.nameServers = nameServers;
            return this;
        }

        public DomainCreateRequestBuilder registrant(final String registrantId) {
            this.registrantId = registrantId;
            return this;
        }

        public DomainCreateRequestBuilder adminContacts(final List<String> contacts) {
            this.adminContacts = contacts;
            return this;
        }

        public DomainCreateRequestBuilder techContacts(final List<String> contacts) {
            this.techContacts = contacts;
            return this;
        }

        public DomainCreateRequestBuilder billingContacts(final List<String> contacts) {
            this.billingContacts = contacts;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public DomainCreateRequest build() {
            var req = new DomainCreateRequest();
            req.name = requireNonEmpty(name, "Domain name must be specified");
            req.period = new Period(requireYears(years));
            req.nameServers = nameServers;

            req.registrant = new Contact(requireNonEmpty(registrantId, "Registrant ID must be specified"), null);
            req.contacts = mergeContacts(adminContacts, techContacts, billingContacts);

            if (req.contacts.size() > 11) {
                throw new IllegalArgumentException("Maximum number of allowed contacts is 11, but %d were specified".formatted(req.contacts.size()));
            }

            return req;
        }
    }
}
