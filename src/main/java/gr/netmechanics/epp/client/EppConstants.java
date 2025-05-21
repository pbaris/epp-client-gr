package gr.netmechanics.epp.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EppConstants {

    public static final String BASE_PACKAGE = "gr.netmechanics.epp.client";

    public static final String HTTP_REQUEST_CHANNEL = "eppHttpRequestChannel";
    public static final String HTTP_RESPONSE_CHANNEL = "eppHttpResponseChannel";

    public static final String BEAN_EPP_CLIENT = "eppClient";
    public static final String BEAN_REQUEST_FLOW = "eppRequestFlow";
    public static final String BEAN_RESPONSE_FLOW = "eppResponseFlow";
    public static final String BEAN_SHARED_HEADERS = "eppGatewaySharedHeaders";

    public static final String URL_SANDBOX = "https://uat-regepp.ics.forth.gr:700/epp/proxy";
    public static final String URL_PRODUCTION = "https://regepp.ics.forth.gr:700/epp/proxy";


}