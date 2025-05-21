package gr.netmechanics.epp.client.impl.commands.update.domain;

import lombok.Getter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
public enum UpdateOperation {
    CHANGE_OWNER("ownerChange"),
    CHANGE_OWNER_NAME("ownerNameChange");

    private final String xmlName;

    UpdateOperation(final String xmlName) {
        this.xmlName = xmlName;
    }
}
