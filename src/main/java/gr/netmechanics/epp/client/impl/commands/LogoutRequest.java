package gr.netmechanics.epp.client.impl.commands;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppRequest;
import lombok.Getter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
public class LogoutRequest implements EppRequest {

    @JacksonXmlProperty(isAttribute = true)
    private final String logout = null;
}
