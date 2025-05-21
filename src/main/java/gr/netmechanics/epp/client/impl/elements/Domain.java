package gr.netmechanics.epp.client.impl.elements;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import gr.netmechanics.epp.client.xml.BooleanDeserializer;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Domain {

    @JacksonXmlText
    private String name;

    @JacksonXmlProperty(isAttribute = true, localName = "hosts")
    private String hosts;

    @JsonDeserialize(using = BooleanDeserializer.class)
    @JacksonXmlProperty(isAttribute = true, localName = "avail")
    private Boolean available;

    public Domain(final String name) {
        this(name, null);
    }

    public Domain(final String name, final String hosts) {
        this.name = name;
        this.hosts = hosts;
    }
}
