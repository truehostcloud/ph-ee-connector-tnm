package org.mifos.connector.tnm.zeebe;

/**
 * Contains variables referenced in zeebe.
 */
public class ZeebeVariables {

    private ZeebeVariables() {}

    public static final String TRANSACTION_ID = "transactionId";
    public static final String ORIGIN_DATE = "originDate";
    public static final String PARTY_LOOKUP_FAILED = "partyLookupFailed";
    public static final String EXTERNAL_ID = "externalId";
    public static final String SERVER_TRANSACTION_ID = "tnmTransactionId";
    public static final String CURRENCY = "currency";
    public static final String TRANSFER_CREATE_FAILED = "transferCreateFailed";
    public static final String IS_VALIDATION_REFERENCE_PRESENT = "isValidationReferencePresent";

}
