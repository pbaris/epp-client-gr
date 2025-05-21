package gr.netmechanics.epp.client.impl;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.schema.EppSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EppCommandRequest implements EppSchema {

    @JacksonXmlProperty(localName = "command")
    private EppCommand command;

    public static <T extends EppRequest> EppCommandRequest request(final T request, final long cti) {
        var cmdReq = new EppCommandRequest();
        cmdReq.command = new EppCommand(request, cti);
        return cmdReq;
    }
}
