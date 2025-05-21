package gr.netmechanics.epp.client.xml;

import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EXT_COMMON_LOC;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EXT_CONTACT_LOC;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EXT_DOMAIN_LOC;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EXT_HOST_LOC;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gr.netmechanics.epp.client.impl.EppExtension;
import gr.netmechanics.epp.client.impl.elements.ext.CommonExtension;
import gr.netmechanics.epp.client.impl.elements.ext.ContactExtension;
import gr.netmechanics.epp.client.impl.elements.ext.DomainExtension;
import gr.netmechanics.epp.client.impl.elements.ext.HostExtension;

/**
 * @author Panos Bariamis (pbaris)
 */
public class ExtensionDeserializer<T extends EppExtension> extends JsonDeserializer<T> {

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(final JsonParser p, final DeserializationContext ctx) throws IOException, JacksonException {
        JsonNode treeNode = p.getCodec().readTree(p);

        if (treeNode instanceof ObjectNode node) {
            String schemaLocation = node.get("schemaLocation").asText();

            if (schemaLocation.equalsIgnoreCase(NS_EXT_COMMON_LOC)) {
                return (T) p.getCodec().treeToValue(node, CommonExtension.class);

            } else if (schemaLocation.equalsIgnoreCase(NS_EXT_DOMAIN_LOC)) {
                return (T) p.getCodec().treeToValue(node, DomainExtension.class);

            } else if (schemaLocation.equalsIgnoreCase(NS_EXT_CONTACT_LOC)) {
                return (T) p.getCodec().treeToValue(node, ContactExtension.class);

            } else if (schemaLocation.equalsIgnoreCase(NS_EXT_HOST_LOC)) {
                return (T) p.getCodec().treeToValue(node, HostExtension.class);
            }
        }

        return null;
    }
}
