package gr.netmechanics.epp.client.impl.commands.update.domain;

import static gr.netmechanics.epp.client.impl.EppBuilder.mergeContacts;
import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.EppExtension;
import gr.netmechanics.epp.client.impl.commands.update.UpdateRequest;
import gr.netmechanics.epp.client.impl.elements.Contact;
import gr.netmechanics.epp.client.impl.elements.ext.DomainExtension;
import gr.netmechanics.epp.client.impl.elements.ext.DomainIssueTokenExtension;
import gr.netmechanics.epp.client.impl.elements.ext.HasExtension;
import gr.netmechanics.epp.client.impl.schema.DomainSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
//TODO Αλλαγή του Τύπου Εγγραφής Δεσμευμένης Μορφής Ονόματος Χώρου ...................... 52
//TODO Προσθήκη/Κατάργηση Εγγραφών DS ................................................... 53
public class DomainUpdateRequest implements DomainSchema, UpdateRequest, HasExtension {

    @JacksonXmlProperty(localName = "domain:name")
    private String name;

    @JacksonXmlProperty(localName = "domain:add")
    private ChangesNode additions;

    @JacksonXmlProperty(localName = "domain:rem")
    private ChangesNode removals;

    @JacksonXmlProperty(localName = "domain:chg")
    private ChangesNode changes;

    @JsonIgnore
    private EppExtension extension;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private record ChangesNode(
        @JacksonXmlElementWrapper(localName = "domain:ns")
        @JacksonXmlProperty(localName = "domain:hostObj")
        List<String> nameServers,

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "domain:contact")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<Contact> contacts,

        @JacksonXmlProperty(localName = "domain:registrant")
        String registrant) {
    }

    public static DomainUpdateRequestBuilder builder() {
        return new DomainUpdateRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DomainUpdateRequestBuilder implements EppBuilder {

        private String name;
        private String registrant;
        private List<String> nameServersAdd;
        private List<String> nameServersRemove;
        private List<String> adminContactsAdd;
        private List<String> adminContactsRemove;
        private List<String> techContactsAdd;
        private List<String> techContactsRemove;
        private List<String> billingContactsAdd;
        private List<String> billingContactsRemove;

        private EppExtension extension;

        public DomainUpdateRequestBuilder domainName(final String name) {
            this.name = name;
            return this;
        }

        public DomainUpdateRequestBuilder nameServersToAdd(final List<String> nameServersAdd) {
            this.nameServersAdd = nameServersAdd;
            return this;
        }

        public DomainUpdateRequestBuilder nameServersToRemove(final List<String> nameServersRemove) {
            this.nameServersRemove = nameServersRemove;
            return this;
        }

        public DomainUpdateRequestBuilder adminContactsToAdd(final List<String> adminContactsAdd) {
            this.adminContactsAdd = adminContactsAdd;
            return this;
        }

        public DomainUpdateRequestBuilder adminContactsToRemove(final List<String> adminContactsRemove) {
            this.adminContactsRemove = adminContactsRemove;
            return this;
        }

        public DomainUpdateRequestBuilder techContactsToAdd(final List<String> techContactsAdd) {
            this.techContactsAdd = techContactsAdd;
            return this;
        }

        public DomainUpdateRequestBuilder techContactsToRemove(final List<String> techContactsRemove) {
            this.techContactsRemove = techContactsRemove;
            return this;
        }

        public DomainUpdateRequestBuilder billingContactsToAdd(final List<String> billingContactsAdd) {
            this.billingContactsAdd = billingContactsAdd;
            return this;
        }

        public DomainUpdateRequestBuilder billingContactsToRemove(final List<String> billingContactsRemove) {
            this.billingContactsRemove = billingContactsRemove;
            return this;
        }

        public DomainUpdateRequestBuilder issueToken() {
            this.extension = new DomainIssueTokenExtension();
            return this;
        }

        public DomainUpdateRequestBuilder changeOwner(final String registrant) {
            this.registrant = registrant;
            this.extension = new DomainExtension(UpdateOperation.CHANGE_OWNER);
            return this;
        }

        public DomainUpdateRequestBuilder changeOwnerName(final String registrant) {
            this.registrant = registrant;
            this.extension = new DomainExtension(UpdateOperation.CHANGE_OWNER_NAME);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public DomainUpdateRequest build() {
            var req = new DomainUpdateRequest();
            req.name = requireNonEmpty(name, "Domain name must be specified");

            if (extension instanceof DomainIssueTokenExtension) {
                req.extension = extension;

            } else if (extension instanceof DomainExtension) {
                req.extension = extension;
                req.changes = new ChangesNode(null, null, requireNonEmpty(registrant, "Registrant ID must be specified"));

            } else {
                buildPlainUpdate(req);
            }

            return req;
        }

        private void buildPlainUpdate(final DomainUpdateRequest req) {
            // Additions
            List<Contact> contacts = mergeContacts(adminContactsAdd, techContactsAdd, billingContactsAdd);
            if (CollectionUtils.isNotEmpty(nameServersAdd) || CollectionUtils.isNotEmpty(contacts)) {
                req.additions = new ChangesNode(nameServersAdd, contacts, null);
            }

            // Removals
            contacts = mergeContacts(adminContactsRemove, techContactsRemove, billingContactsRemove);
            if (CollectionUtils.isNotEmpty(nameServersRemove) || CollectionUtils.isNotEmpty(contacts)) {
                req.removals = new ChangesNode(nameServersRemove, contacts, null);
            }
        }
    }
}
