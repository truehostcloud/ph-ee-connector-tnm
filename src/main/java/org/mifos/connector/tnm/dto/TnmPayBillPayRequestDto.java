package org.mifos.connector.tnm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class representing the TNM PayBill Pay request.
 * <p>
 * Request example: { "TransactionType":"Pay Bill", "TransID":"RKTQDM7W6S", "TransTime":"20191122063845",
 * "TransAmount":"10", "BusinessShortCode":"600638", "BillRefNumber":"A123", "InvoiceNumber":"",
 * "OrgAccountBalance":"49197.00", "ThirdPartyTransID":"", "MSISDN":"2547*****149", "FirstName":"John", }
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TnmPayBillPayRequestDto {

    @JsonProperty("TransactionType")
    private String transactionType;

    @JsonProperty("trans_id")
    private String transactionId;

    @JsonProperty("amount")
    private String transactionAmount;

    @JsonProperty("BusinessShortCode")
    private String shortCode;

    @JsonProperty("BillRefNumber")
    private String billRefNo;
    @JsonProperty("InvoiceNumber")
    private String invoiceNumber;

    @JsonProperty("OrgAccountBalance")
    private String accountBalance;

    @JsonProperty("ThirdPartyTransID")
    private String thirdPartyTransactionId;

    @JsonProperty("msisdn")
    private String msisdn;

    @JsonProperty("FirstName")
    private String firstname;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonIgnore
    private String amsName;

    @JsonProperty("isValidationReferencePresent")
    private boolean validationReferencePresent;

    @JsonProperty("oafTransactionReference")
    private String oafValidationRef;

    @Override
    public String toString() {
        return "PayBillRequestDTO{" + "transactionType='" + transactionType + '\'' + ", transactionID='" + transactionId
                + '\'' + ", transactionAmount='" + transactionAmount + '\'' + ", shortCode='" + shortCode + '\''
                + ", billRefNo='" + billRefNo + '\'' + ", invoiceNumber='" + invoiceNumber + '\'' + ", accountBalance='"
                + accountBalance + '\'' + ", thirdPatrytransactionID='" + thirdPartyTransactionId + '\'' + ", msisdn='"
                + msisdn + '\'' + ", firstname='" + firstname + '\'' + '}';
    }
}
