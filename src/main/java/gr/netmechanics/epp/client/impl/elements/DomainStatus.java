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
@JsonDeserialize(using = DomainStatus.StatusDeserializer.class)
public enum DomainStatus {
    UNKNOWN("unknown", "Unknown"),

    OK("ok", "The domain has no pending operations or restrictions."),
    INACTIVE("inactive", "The domain has no associated host objects, so it is not active in the DNS."),

    CLIENT_DELETE_PROHIBITED("clientDeleteProhibited", "The client is not allowed to delete the domain."),
    CLIENT_HOLD("clientHold", "The domain is placed on hold by the client; DNS will not resolve the domain."),
    CLIENT_RENEW_PROHIBITED("clientRenewProhibited", "The client is not allowed to renew the domain."),
    CLIENT_TRANSFER_PROHIBITED("clientTransferProhibited", "The client is not allowed to transfer the domain to another registrar."),
    CLIENT_UPDATE_PROHIBITED("clientUpdateProhibited", "The client is not allowed to update the domain."),

    PENDING_CREATE("pendingCreate", "The domain is in the process of being created."),
    PENDING_DELETE("pendingDelete", "The domain is in the process of being deleted."),
    PENDING_RENEW("pendingRenew", "The domain is in the process of being renewed."),
    PENDING_TRANSFER("pendingTransfer", "The domain is being transferred between registrars."),
    PENDING_UPDATE("pendingUpdate", "The domain is being updated."),

    SERVER_DELETE_PROHIBITED("serverDeleteProhibited", "The server (registry) has prohibited deletion of the domain."),
    SERVER_HOLD("serverHold", "The server (registry) has placed the domain on hold; DNS resolution is disabled."),
    SERVER_RENEW_PROHIBITED("serverRenewProhibited", "The server has prohibited renewal of the domain."),
    SERVER_TRANSFER_PROHIBITED("serverTransferProhibited", "The server has prohibited transfer of the domain."),
    SERVER_UPDATE_PROHIBITED("serverUpdateProhibited", "The server has prohibited updates to the domain.");

    private final String label;
    private final String description;

    DomainStatus(final String label, final String description) {
        this.label = label;
        this.description = description;
    }

    private static DomainStatus fromLabel(final String label) {
        for (final DomainStatus s : DomainStatus.values()) {
            if (s.label.equalsIgnoreCase(label)) {
                return s;
            }
        }

        return UNKNOWN;
    }

    static class StatusDeserializer extends JsonDeserializer<DomainStatus> {
        @Override
        public DomainStatus deserialize(final JsonParser p, final DeserializationContext ctx) throws IOException, JacksonException {
            if (p.getCodec() instanceof XmlMapper xmlMapper) {
                JsonNode node = xmlMapper.readTree(p);
                String label = node.get("s").asText();
                return DomainStatus.fromLabel(label);
            }

            return DomainStatus.UNKNOWN;
        }
    }
}
