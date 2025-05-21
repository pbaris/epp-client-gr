package gr.netmechanics.epp.client.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Panos Bariamis (pbaris)
 */
public class NameServerDeserializer extends StdDeserializer<List<String>> {

    public NameServerDeserializer() {
        super(List.class);
    }

    @Override
    public List<String> deserialize(final JsonParser p, final DeserializationContext ctx) throws IOException {
        List<String> ns = new ArrayList<>();
        TreeNode node = p.getCodec().readTree(p);

        if (node instanceof ObjectNode objNode) {
            JsonNode hostObjs = objNode.get("hostObj");

            if (hostObjs != null) {
                if (hostObjs.isArray()) {
                    for (JsonNode host : hostObjs) {
                        ns.add(host.asText());
                    }
                } else {
                    ns.add(hostObjs.asText());
                }
            }
        }

        return ns;
    }
}
