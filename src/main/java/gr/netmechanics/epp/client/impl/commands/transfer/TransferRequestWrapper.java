package gr.netmechanics.epp.client.impl.commands.transfer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.xml.TransferRequestSerializer;
import lombok.Getter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
public class TransferRequestWrapper {

    @JacksonXmlProperty(isAttribute = true, localName = "op")
    private final String operation = "request";

    @JsonSerialize(using = TransferRequestSerializer.class)
    @JacksonXmlProperty(localName = "transfer")
    private final TransferRequest request;

    public TransferRequestWrapper(final TransferRequest request) {
        this.request = request;
    }
}
