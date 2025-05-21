package gr.netmechanics.epp.client.impl.commands;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.schema.EppSchema;
import lombok.Getter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
public class Hello implements EppSchema {

    @JacksonXmlProperty(localName = "hello")
    private final String hello = null;
}
