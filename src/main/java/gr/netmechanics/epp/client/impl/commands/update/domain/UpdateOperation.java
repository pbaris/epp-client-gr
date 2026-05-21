package gr.netmechanics.epp.client.impl.commands.update.domain;

import lombok.Getter;

@Getter
public enum UpdateOperation {
    CHANGE_OWNER("ownerChange"),
    CHANGE_OWNER_NAME("ownerNameChange"),
    CHANGE_REGISTRATION_TYPE("registrationTypeChange");

    private final String xmlName;

    UpdateOperation(final String xmlName) {
        this.xmlName = xmlName;
    }
}
