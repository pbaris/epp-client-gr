package gr.netmechanics.epp.client.impl.elements;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IPAddress {

    @JacksonXmlText
    private String address;

    @JacksonXmlProperty(isAttribute = true, localName = "ip")
    private String type;

    public IPAddress(final String address) {
        this.address = address;

        InetAddressValidator inetVal = InetAddressValidator.getInstance();
        if (inetVal.isValidInet4Address(address)) {
            this.type = "v4";

        } else {
            this.type = inetVal.isValidInet6Address(address) ? "v6" : null;
        }
    }
}
