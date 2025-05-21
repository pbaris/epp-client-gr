package gr.netmechanics.epp.client;

import static gr.netmechanics.epp.client.EppConstants.BEAN_EPP_CLIENT;
import static gr.netmechanics.epp.client.EppConstants.BEAN_SHARED_HEADERS;
import static gr.netmechanics.epp.client.impl.EppCommandRequest.request;

import java.time.Instant;
import java.util.Map;

import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.EppCommandRequest;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.Hello;
import gr.netmechanics.epp.client.impl.commands.LoginRequest;
import gr.netmechanics.epp.client.impl.commands.LogoutRequest;
import gr.netmechanics.epp.client.impl.commands.check.contact.ContactCheckRequest;
import gr.netmechanics.epp.client.impl.commands.check.domain.DomainCheckRequest;
import gr.netmechanics.epp.client.impl.commands.check.host.HostCheckRequest;
import gr.netmechanics.epp.client.impl.commands.create.contact.ContactCreateRequest;
import gr.netmechanics.epp.client.impl.commands.create.domain.DomainCreateRequest;
import gr.netmechanics.epp.client.impl.commands.create.host.HostCreateRequest;
import gr.netmechanics.epp.client.impl.commands.delete.domain.DomainDeleteRequest;
import gr.netmechanics.epp.client.impl.commands.delete.host.HostDeleteRequest;
import gr.netmechanics.epp.client.impl.commands.info.contact.ContactInfoRequest;
import gr.netmechanics.epp.client.impl.commands.info.domain.DomainInfoRequest;
import gr.netmechanics.epp.client.impl.commands.info.host.HostInfoRequest;
import gr.netmechanics.epp.client.impl.commands.renew.domain.DomainRenewRequest;
import gr.netmechanics.epp.client.impl.commands.transfer.domain.DomainTransferRequest;
import gr.netmechanics.epp.client.impl.commands.update.contact.ContactUpdateRequest;
import gr.netmechanics.epp.client.impl.commands.update.domain.DomainUpdateRequest;
import gr.netmechanics.epp.client.impl.commands.update.host.HostUpdateRequest;
import gr.netmechanics.epp.client.impl.elements.Greeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author Panos Bariamis (pbaris)
 */
@Slf4j
@Component(BEAN_EPP_CLIENT)
@RequiredArgsConstructor
public class EppClient {

    private final EppGateway eppGateway;

    @Qualifier(BEAN_SHARED_HEADERS)
    private final Map<String, Object> sharedHeaders;

    private EppPropertiesProvider eppProps;
    private Instant sessionExpiresAt;
    private Greeting greeting;

    //region Session (RFC3730)

    /**
     * Logs in to the EPP server using the provided login request details.
     *
     * @param loginRequest the login credentials and session options required to authenticate with the server
     * @return the response from the EPP server containing the result of the login attempt
     */
    public EppCommandResponse login(@NonNull final LoginRequest loginRequest) {
        if (isConnected()) {
            logout();
        }

        return eppGateway.sendCommand(request(loginRequest, eppProps.getClTrId()));
    }

    /**
     * Logs out from the EPP server by sending a logout command request.
     *
     * @return the response from the EPP server, indicating the result of the logout attempt
     */
    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    public EppCommandResponse logout() {
        EppCommandResponse cmd = sendCommandRequest(request(new LogoutRequest(), eppProps.getClTrId()));
        sessionExpiresAt = null;
        greeting = null;
        sharedHeaders.clear();
        return cmd;
    }

    /**
     * Sends a Hello message to the EPP server and retrieves the server's greeting response.
     *
     * @return the server's greeting response
     * @throws EppGatewayException if an error occurs during communication with the EPP server
     */
    public Greeting hello() {
        return eppGateway.hello(new Hello());
    }
    //endregion

    //region Domains (RFC3731)

    /**
     * Retrieves detailed information about a specific domain by sending a Domain Info request to the EPP server.
     *
     * @param infoRequest the request containing details of the domain for which information is being queried
     * @return the response from the EPP server containing the domain information
     */
    public EppCommandResponse getDomainInfo(@NonNull final DomainInfoRequest infoRequest) {
        return sendCommandRequest(request(infoRequest, eppProps.getClTrId()));
    }

    /**
     * Checks the availability of specified domain names against the EPP server.
     * This method sends a Domain Check request and retrieves the corresponding response.
     *
     * @param checkRequest the request containing the domain names to be checked for availability
     * @return the response from the EPP server indicating the availability of the specified domain names
     */
    public EppCommandResponse checkDomains(@NonNull final DomainCheckRequest checkRequest) {
        return sendCommandRequest(request(checkRequest, eppProps.getClTrId()));
    }

    /**
     * Creates a new domain by sending a Domain Create request to the EPP server.
     *
     * @param createRequest the request object containing the details of the domain to be created
     * @return the response from the EPP server indicating the result of the domain creation process
     */
    public EppCommandResponse createDomain(@NonNull final DomainCreateRequest createRequest) {
        return sendCommandRequest(request(createRequest, eppProps.getClTrId()));
    }

    /**
     * Updates an existing domain by sending a Domain Update request to the EPP server.
     *
     * @param updateRequest the request containing the details of the changes to be made to the domain,
     *                      such as contact modifications, name server adjustments, or status updates
     * @return the response from the EPP server indicating the result of the domain update process
     */
    public EppCommandResponse updateDomain(@NonNull final DomainUpdateRequest updateRequest) {
        return sendCommandRequest(request(updateRequest, eppProps.getClTrId()));
    }

    /**
     * Renews an existing domain by sending a Domain Renew request to the EPP server.
     *
     * @param renewRequest the request containing the details of the domain to be renewed,
     *                     including its name, current expiration date, and renewal period
     * @return the response from the EPP server indicating the result of the domain renewal process
     */
    public EppCommandResponse renewDomain(@NonNull final DomainRenewRequest renewRequest) {
        return sendCommandRequest(request(renewRequest, eppProps.getClTrId()));
    }

    /**
     * Transfers a domain by sending a Domain Transfer request to the EPP server.
     *
     * @param transferRequest the request containing the details of the domain transfer,
     *                        including the domain name, transfer operation type, and authentication code
     * @return the response from the EPP server indicating the result of the domain transfer process
     */
    public EppCommandResponse transferDomain(@NonNull final DomainTransferRequest transferRequest) {
        return sendCommandRequest(request(transferRequest, eppProps.getClTrId()));
    }

    /**
     * Deletes a domain by sending a Domain Delete request to the EPP server.
     *
     * @param deleteRequest the request containing the details of the domain to be deleted, including its name
     * @return the response from the EPP server indicating the result of the domain deletion process
     */
    public EppCommandResponse deleteDomain(@NonNull final DomainDeleteRequest deleteRequest) {
        return sendCommandRequest(request(deleteRequest, eppProps.getClTrId()));
    }
    //endregion

    //region Hosts (RFC3732)

    /**
     * Retrieves detailed information about a specific host by sending a Host Info request to the EPP server.
     *
     * @param infoRequest the request containing details of the host for which information is being queried
     * @return the response from the EPP server containing the host information
     */
    public EppCommandResponse getHostInfo(@NonNull final HostInfoRequest infoRequest) {
        return sendCommandRequest(request(infoRequest, eppProps.getClTrId()));
    }

    /**
     * Checks the availability of specified hostnames against the EPP server.
     * This method sends a Host Check request and retrieves the corresponding response.
     *
     * @param checkRequest the request containing the hostnames to be checked for availability
     * @return the response from the EPP server indicating the availability of the specified hostnames
     */
    public EppCommandResponse checkHosts(@NonNull final HostCheckRequest checkRequest) {
        return sendCommandRequest(request(checkRequest, eppProps.getClTrId()));
    }

    /**
     * Creates a new host by sending a Host Create request to the EPP server.
     *
     * @param createRequest the request object containing the details of the host to be created, including its name and optional IP addresses
     * @return the response from the EPP server indicating the result of the host creation process
     */
    public EppCommandResponse createHost(@NonNull final HostCreateRequest createRequest) {
        return sendCommandRequest(request(createRequest, eppProps.getClTrId()));
    }

    /**
     * Updates an existing host by sending a Host Update request to the EPP server.
     *
     * @param updateRequest the request containing the details of the changes to be made to the host,
     *                      such as name modifications and IP address adjustment, or status updates
     * @return the response from the EPP server indicating the result of the host update process
     */
    public EppCommandResponse updateHost(@NonNull final HostUpdateRequest updateRequest) {
        return sendCommandRequest(request(updateRequest, eppProps.getClTrId()));
    }

    /**
     * Deletes a host by sending a Host Delete request to the EPP server.
     *
     * @param deleteRequest the request containing the details of the host to be deleted, including its name
     * @return the response from the EPP server indicating the result of the host deletion process
     */
    public EppCommandResponse deleteHost(@NonNull final HostDeleteRequest deleteRequest) {
        return sendCommandRequest(request(deleteRequest, eppProps.getClTrId()));
    }
    //endregion

    //region Contacts (RFC3733)

    /**
     * Retrieves detailed information about a specific contact by sending a Contact Info request to the EPP server.
     *
     * @param infoRequest the request containing details of the contact for which information is being queried
     * @return the response from the EPP server containing the contact information
     */
    public EppCommandResponse getContactInfo(@NonNull final ContactInfoRequest infoRequest) {
        return sendCommandRequest(request(infoRequest, eppProps.getClTrId()));
    }

    /**
     * Checks the availability of specified contact IDs against the EPP server.
     * This method sends a Contact Check request and retrieves the corresponding response.
     *
     * @param checkRequest the request containing the contact IDs to be checked for availability
     * @return the response from the EPP server indicating the availability of the specified contact IDs
     */
    public EppCommandResponse checkContacts(@NonNull final ContactCheckRequest checkRequest) {
        return sendCommandRequest(request(checkRequest, eppProps.getClTrId()));
    }

    /**
     * Creates a new contact by sending a Contact Create request to the EPP server.
     *
     * @param createRequest the request object containing the details of the contact to be created
     * @return the response from the EPP server indicating the result of the contact creation process
     */
    public EppCommandResponse createContact(@NonNull final ContactCreateRequest createRequest) {
        return sendCommandRequest(request(createRequest, eppProps.getClTrId()));
    }

    /**
     * Updates an existing contact by sending a Contact Update request to the EPP server.
     *
     * @param updateRequest the request containing the details of the changes to be made to the contact,
     *                      such as contact information modifications or status updates
     * @return the response from the EPP server indicating the result of the contact update process
     */
    public EppCommandResponse updateContact(@NonNull final ContactUpdateRequest updateRequest) {
        return sendCommandRequest(request(updateRequest, eppProps.getClTrId()));
    }
    //endregion

    private EppCommandResponse sendCommandRequest(@NonNull final EppCommandRequest command) {
        if (isSessionValid()) {
            sessionExpiresAt = Instant.now().plusSeconds(14 * 60);
        }

        return eppGateway.sendCommand(command);
    }

    private boolean isSessionValid() {
        if (!isConnected()) {
            log.debug("Connection not established or wrong, try to connect");
            return connect();
        }
        return true;
    }

    private boolean isConnected() {
        if (sessionExpiresAt == null || Instant.now().isAfter(sessionExpiresAt)) {
            return false;
        }

        return getGreeting() != null;
    }

    private boolean connect() {
        Greeting greet = getGreeting();

        String language = eppProps.getLanguage();
        if (!greet.getLanguages().contains(language)) {
            language = greet.getDefaultLanguage();
        }

        LoginRequest loginRequest = LoginRequest.builder()
            .clientId(eppProps.getClientId())
            .password(eppProps.getPassword())
            .language(language)
            .version(greeting.getVersion())
            .objectUris(greet.getObjectUris())
            .build();

        return login(loginRequest).isSuccess();
    }

    private Greeting getGreeting() {
        if (greeting == null) {
            try {
                greeting = hello();

            } catch (EppGatewayException e) {
                log.error("Checking with hello failed, we are not connected, returning false!");
            }
        }

        return greeting;
    }

    @Autowired
    void setEppProps(final EppPropertiesProvider eppProps) {
        this.eppProps = eppProps;
        if (sessionExpiresAt != null || greeting != null) {
            logout();
        }
    }
}
