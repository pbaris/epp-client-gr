package gr.netmechanics.epp.client.xml;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * @author Panos Bariamis (pbaris)
 */
public class BooleanDeserializer extends JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(final JsonParser p, final DeserializationContext ctx) throws IOException {
        String text = p.getText().trim();
        return "1".equals(text) || "true".equalsIgnoreCase(text);
    }
}