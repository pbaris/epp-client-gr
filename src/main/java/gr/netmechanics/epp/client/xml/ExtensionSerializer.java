package gr.netmechanics.epp.client.xml;

import java.io.IOException;
import javax.xml.namespace.QName;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import gr.netmechanics.epp.client.impl.EppExtension;
import gr.netmechanics.epp.client.impl.elements.ext.DomainDeleteExtension;
import gr.netmechanics.epp.client.impl.elements.ext.DomainExtension;
import gr.netmechanics.epp.client.impl.elements.ext.DomainIssueTokenExtension;
import gr.netmechanics.epp.client.impl.elements.ext.DomainTransferExtension;

/**
 * @author Panos Bariamis (pbaris)
 */
public class ExtensionSerializer<T extends EppExtension> extends JsonSerializer<T> {

    @Override
    public void serialize(final T value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        ToXmlGenerator xmlGen = (ToXmlGenerator) gen;

        Class<?> valueType = null;
        if (value instanceof DomainDeleteExtension) {
            xmlGen.setNextName(new QName(null, "extdomain:delete"));
            valueType = DomainDeleteExtension.class;

        } else if (value instanceof DomainExtension) {
            xmlGen.setNextName(new QName(null, "extdomain:update"));
            valueType = DomainExtension.class;

        } else if (value instanceof DomainTransferExtension) {
            xmlGen.setNextName(new QName(null, "extdomain:transfer"));
            valueType = DomainTransferExtension.class;

        } else if (value instanceof DomainIssueTokenExtension) {
            xmlGen.setNextName(new QName(null, "dacor:issueToken"));
            valueType = DomainIssueTokenExtension.class;
        }

        if (valueType != null) {
            JsonSerializer<Object> defaultSerializer = provider.findValueSerializer(valueType, null);
            defaultSerializer.serialize(value, gen, provider);
        }
    }
}
