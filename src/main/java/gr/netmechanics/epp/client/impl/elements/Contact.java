package gr.netmechanics.epp.client.impl.elements;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import gr.netmechanics.epp.client.xml.BooleanDeserializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Contact {

    @JacksonXmlText
    private String id;

    @JacksonXmlProperty(isAttribute = true, localName = "type")
    private String type;

    @JsonDeserialize(using = BooleanDeserializer.class)
    @JacksonXmlProperty(isAttribute = true, localName = "avail")
    private Boolean available;

    public Contact(final String id, final String type) {
        this.id = id;
        this.type = Optional.ofNullable(type)
            .map(t -> List.of("admin", "tech", "billing").contains(t) ? type : null)
            .orElse(null);
    }
}
