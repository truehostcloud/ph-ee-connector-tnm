package org.mifos.connector.tnm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response class for Paybill Pay request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayBillPayResponse implements Serializable {

    private Integer status;
    private String message;
    @JsonProperty("receipt_number")
    private String receiptNumber;

}
