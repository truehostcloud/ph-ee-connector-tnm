package org.mifos.connector.tnm.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is an error processing JSON.
 *
 */
public class TnmConnectorJsonProcessingException extends TnmConnectorException {

    public TnmConnectorJsonProcessingException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public TnmConnectorJsonProcessingException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
