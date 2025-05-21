package gr.netmechanics.epp.client.xml;

import java.io.IOException;
import javax.xml.namespace.QName;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import gr.netmechanics.epp.client.impl.commands.transfer.TransferRequest;
import gr.netmechanics.epp.client.impl.commands.transfer.domain.DomainTransferRequest;

/**
 * @author Panos Bariamis (pbaris)
 */
public class TransferRequestSerializer extends JsonSerializer<TransferRequest> {

    @Override
    public void serialize(final TransferRequest value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        ToXmlGenerator xmlGen = (ToXmlGenerator) gen;

        JsonSerializer<Object> defaultSerializer = null;

        if (value instanceof DomainTransferRequest) {
            xmlGen.setNextName(new QName(null, "domain:transfer"));
            defaultSerializer = provider.findValueSerializer(DomainTransferRequest.class, null);
        }

        if (defaultSerializer != null) {
            defaultSerializer.serialize(value, gen, provider);
        }
    }
}
