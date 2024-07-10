package org.mifos.connector.tnm.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility class.
 */
@Slf4j
@Component
public class TnmConstant {

    private TnmConstant() {
        // Hide the constructor
    }

    public static final String JSON_PARSE_EXCEPTION_CLIENT_MESSAGE = "Internal error while processing the request. Please try again later.";
    public static final String THIRD_PARTY_SYSTEM_UNACCESSIBLE_MESSAGE = "Internal systems are not available. Please try again later.";
    public static final String PAYMENT_SUCCESSFUL_MESSAGE = "Payment successful";
}
