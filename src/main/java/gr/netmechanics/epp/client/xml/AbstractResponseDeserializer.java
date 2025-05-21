package gr.netmechanics.epp.client.xml;

import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_CONTACT_LOC;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_DOMAIN_LOC;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_HOST_LOC;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gr.netmechanics.epp.client.impl.EppResponse;

/**
 * @author Panos Bariamis (pbaris)
 */
public abstract class AbstractResponseDeserializer<T extends EppResponse> extends JsonDeserializer<T> {

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(final JsonParser p, final DeserializationContext ctx) throws IOException {
        JsonNode treeNode = p.getCodec().readTree(p);

        if (treeNode instanceof ObjectNode node) {
            String schemaLocation = node.get("schemaLocation").asText();

            if (schemaLocation.equalsIgnoreCase(NS_DOMAIN_LOC)) {
                return (T) p.getCodec().treeToValue(node, getDomainResponseClass());

            } else if (schemaLocation.equalsIgnoreCase(NS_CONTACT_LOC)) {
                return (T) p.getCodec().treeToValue(node, getContactResponseClass());

            } else if (schemaLocation.equalsIgnoreCase(NS_HOST_LOC)) {
                return (T) p.getCodec().treeToValue(node, getHostResponseClass());
            }
        }

        throw new IllegalStateException("Unknown structure in response");
    }

    protected abstract Class<? extends EppResponse> getDomainResponseClass();

    protected abstract Class<? extends EppResponse> getContactResponseClass();

    protected abstract Class<? extends EppResponse> getHostResponseClass();
}
