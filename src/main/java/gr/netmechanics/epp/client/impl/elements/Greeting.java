package gr.netmechanics.epp.client.impl.elements;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.schema.EppSchema;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Panos Bariamis (pbaris)
 */
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Greeting implements EppSchema {

    @JacksonXmlProperty(localName = "greeting")
    private GreetingNode greeting;

    @ToString.Include(name = "serverId")
    public String getServerId() {
        return greeting.serverId();
    }

    @ToString.Include(name = "serverDate")
    public ZonedDateTime getServerDate() {
        return greeting.serverDate();
    }

    @ToString.Include(name = "languages")
    public List<String> getLanguages() {
        return greeting.servicesMenu().languages();
    }

    @ToString.Include(name = "defaultLanguage")
    public String getDefaultLanguage() {
        List<String> languages = getLanguages();
        return languages.contains("el") ? "el" : languages.getFirst();
    }

    @ToString.Include(name = "objectUris")
    public List<String> getObjectUris() {
        return greeting.servicesMenu().objectUris();
    }

    @ToString.Include(name = "version")
    public String getVersion() {
        return greeting.servicesMenu().version();
    }

    private record GreetingNode(
        @JacksonXmlProperty(localName = "svID")
        String serverId,

        @JacksonXmlProperty(localName = "svDate")
        ZonedDateTime serverDate,

        @JacksonXmlProperty(localName = "svcMenu")
        ServicesMenu servicesMenu) {
    }

    private record ServicesMenu(
        @JacksonXmlProperty(localName = "version")
        String version,

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "lang")
        List<String> languages,

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "objURI")
        List<String> objectUris) {

    }
}
