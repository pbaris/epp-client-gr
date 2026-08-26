package gr.netmechanics.epp.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EppConstants {

    public static final String BASE_PACKAGE = "gr.netmechanics.epp.client";

    public static final String BEAN_EPP_CLIENT = "eppClient";
    public static final String BEAN_REST_CLIENT = "eppRestClient";

    public static final String URL_SANDBOX = "https://uat-regepp.ics.forth.gr:700/epp/proxy";
    public static final String URL_PRODUCTION = "https://regepp.ics.forth.gr:700/epp/proxy";

}
