package gr.netmechanics.epp.client.impl;

import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EppResultCodes {

    public static final int COMMAND_COMPLETED_SUCCESSFULLY = 1000;

    public static final int COMMAND_COMPLETED_SUCCESSFULLY_ACTION_PENDING = 1001;

    public static final int COMMAND_COMPLETED_SUCCESSFULLY_NO_MESSAGES = 1300;

    public static final int COMMAND_COMPLETED_SUCCESSFULLY_ACK_TO_DEQUEUE = 1301;

    public static final int COMMAND_COMPLETED_SUCCESSFULLY_ENDING_SESSION = 1500;

    public static final List<Integer> SUCCESS_CODES = List.of(
        COMMAND_COMPLETED_SUCCESSFULLY,
        COMMAND_COMPLETED_SUCCESSFULLY_ACTION_PENDING,
        COMMAND_COMPLETED_SUCCESSFULLY_NO_MESSAGES,
        COMMAND_COMPLETED_SUCCESSFULLY_ACK_TO_DEQUEUE,
        COMMAND_COMPLETED_SUCCESSFULLY_ENDING_SESSION);

    public static final int UNKNOWN_COMMAND = 2000;

    public static final int COMMAND_SYNTAX_ERROR = 2001;

    public static final int COMMAND_USE_ERROR = 2002;

    public static final int REQUIRED_PARAMETER_MISSING = 2003;

    public static final int PARAMETER_VALUE_RANGE_ERROR = 2004;

    public static final int PARAMETER_VALUE_SYNTAX_ERROR = 2005;

    public static final int UNIMPLEMENTED_PROTOCOL_VERSION = 2100;

    public static final int UNIMPLEMENTED_COMMAND = 2101;

    public static final int UNIMPLEMENTED_OPTION = 2102;

    public static final int UNIMPLEMENTED_EXTENSION = 2103;

    public static final int BILLING_FAILURE = 2104;

    public static final int OBJECT_IS_NOT_ELIGIBLE_FOR_RENEWAL = 2105;

    public static final int OBJECT_IS_NOT_ELIGIBLE_FOR_TRANSFER = 2106;

    public static final int AUTHENTICATION_ERROR = 2200;

    public static final int AUTHORIZATION_ERROR = 2201;

    public static final int INVALID_AUTHORIZATION_INFORMATION = 2202;

    public static final int OBJECT_PENDING_TRANSFER = 2300;

    public static final int OBJECT_NOT_PENDING_TRANSFER = 2301;

    public static final int OBJECT_EXISTS = 2302;

    public static final int OBJECT_DOES_NOT_EXIST = 2303;

    public static final int OBJECT_STATUS_PROHIBITS_OPERATION = 2304;

    public static final int OBJECT_ASSOCIATION_PROHIBITS_OPERATION = 2305;

    public static final int PARAMETER_VALUE_POLICY_ERROR = 2306;

    public static final int UNIMPLEMENTED_OBJECT_SERVICE = 2307;

    public static final int DATA_MANAGEMENT_POLICY_VIOLATION = 2308;

    public static final int COMMAND_FAILED = 2400;

    public static final int COMMAND_FAILED_SERVER_CLOSING_CONNECTION = 2500;

    public static final int AUTHENTICATION_ERROR_SERVER_CLOSING_CONNECTION = 2501;

    public static final int SESSION_LIMIT_EXCEEDED_SERVER_CLOSING_CONNECTION = 2502;
}
