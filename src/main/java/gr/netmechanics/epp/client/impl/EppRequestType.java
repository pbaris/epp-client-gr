package gr.netmechanics.epp.client.impl;

import gr.netmechanics.epp.client.impl.commands.LoginRequest;
import gr.netmechanics.epp.client.impl.commands.LogoutRequest;
import gr.netmechanics.epp.client.impl.commands.check.CheckRequest;
import gr.netmechanics.epp.client.impl.commands.create.CreateRequest;
import gr.netmechanics.epp.client.impl.commands.delete.DeleteRequest;
import gr.netmechanics.epp.client.impl.commands.info.InfoRequest;
import gr.netmechanics.epp.client.impl.commands.renew.RenewRequest;
import gr.netmechanics.epp.client.impl.commands.transfer.TransferRequest;
import gr.netmechanics.epp.client.impl.commands.update.UpdateRequest;

/**
 * @author Panos Bariamis (pbaris)
 */
public enum EppRequestType {
    INFO,
    CHECK,
    CREATE,
    UPDATE,
    RENEW,
    DELETE,
    TRANSFER,
    LOGIN,
    LOGOUT;

    public static EppRequestType fromInstance(final Object o) {
        return switch (o) {
            case InfoRequest r -> INFO;
            case CheckRequest r -> CHECK;
            case CreateRequest r -> CREATE;
            case UpdateRequest r -> UPDATE;
            case RenewRequest r -> RENEW;
            case DeleteRequest r -> DELETE;
            case TransferRequest r -> TRANSFER;
            case LoginRequest r -> LOGIN;
            case LogoutRequest r -> LOGOUT;
            default -> null;
        };
    }
}
