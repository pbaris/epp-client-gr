package gr.netmechanics.epp.client;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.EppRequest;

interface EppCommandSender {

    EppCommandResponse send(EppRequest eppRequest);
}
