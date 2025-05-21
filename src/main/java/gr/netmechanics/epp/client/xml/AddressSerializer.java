package gr.netmechanics.epp.client.xml;

import java.io.IOException;
import java.util.Optional;
import javax.xml.namespace.QName;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import gr.netmechanics.epp.client.impl.elements.Address;

/**
 * @author Panos Bariamis (pbaris)
 */
public class AddressSerializer extends PrefixedJsonSerializer<Address> {

    @Override
    public void serialize(final Address value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        ToXmlGenerator xmlGen = (ToXmlGenerator) gen;

        // Start writing the <addr> element
        xmlGen.setNextName(new QName(null, getPrefix(xmlGen) + "addr"));
        xmlGen.writeStartObject();

        // Serialize each street in the List of streets
        Optional.ofNullable(value.getStreets())
            .ifPresent(streets ->
                streets.forEach(street -> writePrefixedIfNotNull(xmlGen, "street", street)));

        // Serialize other fields in the address
        writePrefixedIfNotNull(xmlGen, "city", value.getCity());
        writePrefixedIfNotNull(xmlGen, "sp", value.getStateOrProvince());
        writePrefixedIfNotNull(xmlGen, "pc", value.getPostalCode());
        writePrefixedIfNotNull(xmlGen, "cc", value.getCountryCode());

        xmlGen.writeEndObject();
    }
}