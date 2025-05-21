package gr.netmechanics.epp.client.impl.elements;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@JsonDeserialize(using = ContactStatus.StatusDeserializer.class)
public enum ContactStatus {
    UNKNOWN("unknown", "Unknown"),
    OK("ok", "No restrictions or pending operations; the contact is in a normal state."),
    LINKED("linked", "This contact is referenced (linked) by at least one domain object. It cannot be deleted while still in use."),

    CLIENT_DELETE_PROHIBITED("clientDeleteProhibited", "The client is not allowed to delete this contact object."),
    CLIENT_TRANSFER_PROHIBITED("clientTransferProhibited", "The client is not allowed to transfer this contact object to another registrar."),
    CLIENT_UPDATE_PROHIBITED("clientUpdateProhibited", "The client is not allowed to update this contact object."),

    PENDING_CREATE("pendingCreate", "The contact is in the process of being created."),
    PENDING_DELETE("pendingDelete", "The contact is in the process of being deleted."),
    PENDING_TRANSFER("pendingTransfer", "The contact is in the process of being transferred to another registrar."),
    PENDING_UPDATE("pendingUpdate", "The contact is in the process of being updated."),

    SERVER_DELETE_PROHIBITED("serverDeleteProhibited", "The server (registry) has prohibited deletion of this contact."),
    SERVER_TRANSFER_PROHIBITED("serverTransferProhibited", "The server has prohibited transfer of this contact."),
    SERVER_UPDATE_PROHIBITED("serverUpdateProhibited", "The server has prohibited updates to this contact.");

    private final String label;
    private final String description;

    ContactStatus(final String label, final String description) {
        this.label = label;
        this.description = description;
    }

    private static ContactStatus fromLabel(final String label) {
        for (final ContactStatus s : ContactStatus.values()) {
            if (s.label.equalsIgnoreCase(label)) {
                return s;
            }
        }

        return UNKNOWN;
    }

    static class StatusDeserializer extends JsonDeserializer<ContactStatus> {
        @Override
        public ContactStatus deserialize(final JsonParser p, final DeserializationContext ctx) throws IOException, JacksonException {
            if (p.getCodec() instanceof XmlMapper xmlMapper) {
                JsonNode node = xmlMapper.readTree(p);
                String label = node.get("s").asText();
                return ContactStatus.fromLabel(label);
            }

            return ContactStatus.UNKNOWN;
        }
    }
}
