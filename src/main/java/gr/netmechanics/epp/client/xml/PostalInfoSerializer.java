package gr.netmechanics.epp.client.xml;

import java.io.IOException;
import javax.xml.namespace.QName;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import gr.netmechanics.epp.client.impl.elements.PostalInfo;

/**
 * @author Panos Bariamis (pbaris)
 */
public class PostalInfoSerializer extends PrefixedJsonSerializer<PostalInfo> {

    @Override
    public void serialize(final PostalInfo value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        ToXmlGenerator xmlGen = (ToXmlGenerator) gen;

        // Start writing the <postalInfo> element
        xmlGen.setNextName(new QName(null, getPrefix(xmlGen) + "postalInfo"));
        xmlGen.writeStartObject();

        xmlGen.setNextIsAttribute(true);
        xmlGen.writeFieldName("type");
        xmlGen.writeString(value.getType());
        xmlGen.setNextIsAttribute(false);

        // Serialize other fields in the postalInfo
        writePrefixedIfNotNull(xmlGen, "name", value.getName());
        writePrefixedIfNotNull(xmlGen, "org", value.getOrganization());

        xmlGen.writeFieldName(getPrefix(xmlGen) + "addr");
        provider.defaultSerializeValue(value.getAddress(), xmlGen);

        xmlGen.writeEndObject();
    }
}