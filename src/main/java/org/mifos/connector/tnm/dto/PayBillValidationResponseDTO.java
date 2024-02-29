package org.mifos.connector.tnm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mifos.connector.common.gsma.dto.CustomData;

/**
 * Class representing the PayBill validation response.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PayBillValidationResponseDTO {

    @JsonProperty("reconciled")
    private boolean reconciled;

    @JsonProperty("amsName")
    private String amsName;

    @JsonProperty("accountHoldingInstitutionId")
    private String accountHoldingInstitutionId;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("msisdn")
    private String msisdn;

    @JsonProperty("clientName")
    private String clientName;

    @JsonProperty("customData")
    private List<CustomData> customData;
}
