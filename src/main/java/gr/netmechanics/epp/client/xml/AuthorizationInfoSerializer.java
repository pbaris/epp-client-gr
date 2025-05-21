package gr.netmechanics.epp.client.xml;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import gr.netmechanics.epp.client.impl.elements.AuthorizationInfo;

/**
 * @author Panos Bariamis (pbaris)
 */
public class AuthorizationInfoSerializer extends PrefixedJsonSerializer<AuthorizationInfo> {

    @Override
    public void serialize(final AuthorizationInfo value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        ToXmlGenerator xmlGen = (ToXmlGenerator) gen;

        // Start writing the <authInfo> element
        xmlGen.writeStartObject();

        writePrefixed(xmlGen, "pw", value.getPassword());
        writePrefixedIfNotNull(xmlGen, "roid", value.getRepositoryObjectId());

        xmlGen.writeEndObject();

    }
}
