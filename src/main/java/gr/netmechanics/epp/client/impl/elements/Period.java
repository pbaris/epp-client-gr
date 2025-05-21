package gr.netmechanics.epp.client.impl.elements;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
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
public class Period {
    @JacksonXmlProperty(isAttribute = true, localName = "unit")
    private final String unit = "y";

    @JacksonXmlText
    private int years;

    public Period(final int years) {
        this.years = years;
    }
}
