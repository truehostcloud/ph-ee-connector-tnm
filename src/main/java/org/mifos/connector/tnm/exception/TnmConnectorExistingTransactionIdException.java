package org.mifos.connector.tnm.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the transaction ID already exists.
 *
 */
public class TnmConnectorExistingTransactionIdException extends TnmConnectorException {

    public TnmConnectorExistingTransactionIdException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public TnmConnectorExistingTransactionIdException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }

}
