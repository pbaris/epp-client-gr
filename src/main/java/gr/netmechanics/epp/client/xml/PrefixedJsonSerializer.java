package gr.netmechanics.epp.client.xml;

import java.io.IOException;

import com.ctc.wstx.sw.RepairingNsStreamWriter;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Panos Bariamis (pbaris)
 */
@Slf4j
public abstract class PrefixedJsonSerializer<T> extends JsonSerializer<T> {

    private String prefix;

    protected String getPrefix(final ToXmlGenerator xmlGen) {
        String parent = ((RepairingNsStreamWriter) xmlGen.getOutputTarget()).getCurrentElementName().toString();
        if (prefix == null || (parent != null && !parent.startsWith(prefix))) {
            prefix = parent != null && parent.contains(":") ? parent.substring(0, parent.indexOf(":") + 1) : "";
        }

        return prefix;
    }

    protected void writePrefixedIfNotNull(final ToXmlGenerator xmlGen, final String name, final String value) {
        if (value != null) {
            writePrefixed(xmlGen, name, value);
        }
    }

    protected void writePrefixed(final ToXmlGenerator xmlGen, final String name, final String value) {
        try {
            xmlGen.writeFieldName(getPrefix(xmlGen) + name);
            xmlGen.writeString(value);

        } catch (IOException e) {
            log.error("Failed to write field '{}' with value '{}': {}", name, value, e.getMessage());
        }
    }
}
