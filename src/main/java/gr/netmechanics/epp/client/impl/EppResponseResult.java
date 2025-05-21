package gr.netmechanics.epp.client.impl;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EppResponseResult {

    @JacksonXmlProperty(isAttribute = true, localName = "code")
    private int code;

    @JacksonXmlProperty(localName = "msg")
    private String message;

    public boolean isSuccess() {
        return EppResultCodes.SUCCESS_CODES.contains(code);
    }

    public String getFullMessage() {
        return String.format("[%d] %s", code, message);
    }
}
