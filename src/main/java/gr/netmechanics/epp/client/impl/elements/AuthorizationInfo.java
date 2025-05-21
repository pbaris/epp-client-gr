package gr.netmechanics.epp.client.impl.elements;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
public class AuthorizationInfo {

    @JacksonXmlProperty(localName = "pw")
    private PasswordNode passwordNode;

    @ToString.Include(name = "password")
    public String getPassword() {
        return passwordNode.value();
    }

    @ToString.Include(name = "repositoryObjectId")
    public String getRepositoryObjectId() {
        return passwordNode.repositoryObjectId();
    }

    public AuthorizationInfo(final String password) {
        this.passwordNode = new PasswordNode(null, password);
    }

    private record PasswordNode(
        @JacksonXmlProperty(isAttribute = true, localName = "roid")
        String repositoryObjectId,

        @JacksonXmlProperty
        String value) {
    }
}
