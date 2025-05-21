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
@JsonDeserialize(using = HostStatus.StatusDeserializer.class)
public enum HostStatus {
    UNKNOWN("unknown", "Unknown"),
    OK("ok", "The host object is in a normal state with no restrictions."),
    LINKED("linked", "The host is linked to one or more domains. It cannot be deleted while linked."),

    CLIENT_DELETE_PROHIBITED("clientDeleteProhibited", "The client is not allowed to delete this host object."),
    CLIENT_UPDATE_PROHIBITED("clientUpdateProhibited", "The client is not allowed to update this host object."),

    PENDING_CREATE("pendingCreate", "The host object is in the process of being created."),
    PENDING_DELETE("pendingDelete", "The host object is in the process of being deleted."),
    PENDING_UPDATE("pendingUpdate", "The host object is in the process of being updated."),

    SERVER_DELETE_PROHIBITED("serverDeleteProhibited", "The server (registry) has prohibited deletion of this host object."),
    SERVER_UPDATE_PROHIBITED("serverUpdateProhibited", "The server has prohibited updates to this host object.");

    private final String label;
    private final String description;

    HostStatus(final String label, final String description) {
        this.label = label;
        this.description = description;
    }

    private static HostStatus fromLabel(final String label) {
        for (final HostStatus s : HostStatus.values()) {
            if (s.label.equalsIgnoreCase(label)) {
                return s;
            }
        }

        return UNKNOWN;
    }

    static class StatusDeserializer extends JsonDeserializer<HostStatus> {
        @Override
        public HostStatus deserialize(final JsonParser p, final DeserializationContext ctx) throws IOException, JacksonException {
            if (p.getCodec() instanceof XmlMapper xmlMapper) {
                JsonNode node = xmlMapper.readTree(p);
                String label = node.get("s").asText();
                return HostStatus.fromLabel(label);
            }

            return HostStatus.UNKNOWN;
        }
    }
}
