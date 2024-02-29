package org.mifos.connector.tnm.util;

import static org.mifos.connector.tnm.camel.config.CamelProperties.FINERACT_PRIMARY_IDENTIFIER_NAME;
import static org.mifos.connector.tnm.camel.config.CamelProperties.GET_ACCOUNT_DETAILS_FLAG;
import static org.mifos.connector.tnm.camel.config.CamelProperties.ROSTER_PRIMARY_IDENTIFIER_NAME;
import static org.mifos.connector.tnm.camel.config.CamelProperties.SECONDARY_IDENTIFIER_NAME;
import static org.mifos.connector.tnm.camel.config.CamelProperties.TNM_PAY_REQUEST_PAY_WAIT_PERIOD;
import static org.mifos.connector.tnm.camel.config.CamelProperties.TNM_TRX_ID;
import static org.mifos.connector.tnm.zeebe.ZeebeVariables.CURRENCY;
import static org.mifos.connector.tnm.zeebe.ZeebeVariables.PARTY_LOOKUP_FAILED;
import static org.mifos.connector.tnm.zeebe.ZeebeVariables.TRANSACTION_ID;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.mifos.connector.common.channel.dto.TransactionStatusResponseDTO;
import org.mifos.connector.common.gsma.dto.CustomData;
import org.mifos.connector.common.gsma.dto.GsmaTransfer;
import org.mifos.connector.common.gsma.dto.Party;
import org.mifos.connector.common.mojaloop.type.TransferState;
import org.mifos.connector.tnm.dto.ChannelRequestDto;
import org.mifos.connector.tnm.dto.ChannelValidationRequestDTO;
import org.mifos.connector.tnm.dto.PayBillValidationResponseDTO;
import org.mifos.connector.tnm.dto.TNMPayBillPayRequestDTO;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TNMUtils {

    // TODO: Move to ph-ee-connector-common
    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    /**
     * Build {@link ChannelValidationRequestDTO}
     *
     * @param accountID
     *            the client account number
     * @param phoneNumber
     *            the phone number
     * @param currency
     *            the currency
     * @return {@link ChannelValidationRequestDTO}
     */
    public static ChannelValidationRequestDTO createValidationRequest(String accountID, String amsName, String currency,
            String phoneNumber, boolean getAccountDetailsFlag) {

        // Mapping primary and secondary Identifier
        CustomData primaryIdentifier = new CustomData();

        primaryIdentifier.setKey(getPrimaryIdentifierName(amsName));
        primaryIdentifier.setValue(accountID);

        CustomData secondaryIdentifier = new CustomData();
        secondaryIdentifier.setKey(SECONDARY_IDENTIFIER_NAME);
        secondaryIdentifier.setValue(phoneNumber);

        // Mapping custom data
        CustomData transactionId = new CustomData();
        transactionId.setKey(TRANSACTION_ID);
        transactionId.setValue(generateTransactionId());

        CustomData currencyObj = new CustomData();
        currencyObj.setKey(CURRENCY);
        currencyObj.setValue(currency);

        CustomData getAccountDetails = new CustomData();
        getAccountDetails.setKey(GET_ACCOUNT_DETAILS_FLAG);
        getAccountDetails.setValue(getAccountDetailsFlag);

        ChannelValidationRequestDTO validationRequestDTO = new ChannelValidationRequestDTO();
        validationRequestDTO.setPrimaryIdentifier(primaryIdentifier);
        validationRequestDTO.setSecondaryIdentifier(secondaryIdentifier);
        validationRequestDTO.setCustomData(List.of(transactionId, currencyObj, getAccountDetails));

        return validationRequestDTO;
    }

    /**
     * Convert {@link PayBillValidationResponseDTO} to {@link GsmaTransfer}
     *
     * @param paybillValidationResponseDTO
     *            {@link PayBillValidationResponseDTO}
     * @param clientCorrelationId
     *            the correlation id
     * @return {@link ChannelValidationRequestDTO}
     */
    public static GsmaTransfer createGsmaTransferDTO(PayBillValidationResponseDTO paybillValidationResponseDTO,
            String clientCorrelationId, int tnmpayRequestWaitPeriod) {
        GsmaTransfer gsmaTransfer = new GsmaTransfer();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        List<CustomData> customData = setCustomData(paybillValidationResponseDTO, clientCorrelationId,
                tnmpayRequestWaitPeriod);
        String currentDateTime = formatter.format(new Date());

        Party payer = new Party();
        payer.setPartyIdIdentifier(paybillValidationResponseDTO.getMsisdn());
        payer.setPartyIdType("MSISDN");
        List<Party> payerObj = new ArrayList<>();
        payerObj.add(payer);

        Party payee = new Party();
        payee.setPartyIdIdentifier(paybillValidationResponseDTO.getMsisdn());
        payee.setPartyIdType("accountId");
        List<Party> payeeObj = new ArrayList<>();
        payeeObj.add(payee);

        gsmaTransfer.setCustomData(customData);
        gsmaTransfer.setRequestDate(currentDateTime);
        gsmaTransfer.setPayee(payeeObj);
        gsmaTransfer.setPayer(payerObj);
        gsmaTransfer.setSubType("inbound");
        gsmaTransfer.setType("tnm");
        gsmaTransfer.setDescriptionText("description");
        gsmaTransfer.setRequestingOrganisationTransactionReference(paybillValidationResponseDTO.getTransactionId());
        gsmaTransfer.setAmount(paybillValidationResponseDTO.getAmount());
        gsmaTransfer.setCurrency(paybillValidationResponseDTO.getCurrency());

        return gsmaTransfer;
    }

    private static List<CustomData> setCustomData(PayBillValidationResponseDTO paybillValidationResponseDTO,
            String clientCorrelationId, int tnmpayRequestWaitPeriod) {
        CustomData reconciled = new CustomData();
        reconciled.setKey(PARTY_LOOKUP_FAILED);
        reconciled.setValue(!paybillValidationResponseDTO.isReconciled());
        CustomData confirmationReceived = new CustomData();
        confirmationReceived.setKey("confirmationReceived");
        confirmationReceived.setValue(false);
        // This customData will be set as Zeebe variable and be used in Clearing workflow route
        CustomData txnId = new CustomData();
        txnId.setKey(TNM_TRX_ID);
        txnId.setValue(paybillValidationResponseDTO.getTransactionId());
        CustomData ams = new CustomData();
        ams.setKey("ams");
        ams.setValue(paybillValidationResponseDTO.getAmsName());
        CustomData tenantId = new CustomData();
        tenantId.setKey("tenantId");
        tenantId.setValue(paybillValidationResponseDTO.getAccountHoldingInstitutionId());
        CustomData clientCorrelation = new CustomData();
        clientCorrelation.setKey("clientCorrelationId");
        clientCorrelation.setValue(clientCorrelationId);
        CustomData currency = new CustomData();
        currency.setKey("currency");
        currency.setValue(paybillValidationResponseDTO.getCurrency());

        CustomData tmmPayRequestWaitPeriod = new CustomData();
        tmmPayRequestWaitPeriod.setKey(TNM_PAY_REQUEST_PAY_WAIT_PERIOD);
        tmmPayRequestWaitPeriod.setValue(String.format("PT%dS", tnmpayRequestWaitPeriod));

        List<CustomData> customData = new ArrayList<>();
        customData.add(reconciled);
        customData.add(confirmationReceived);
        customData.add(txnId);
        customData.add(ams);
        customData.add(tenantId);
        customData.add(clientCorrelation);
        customData.add(currency);
        customData.add(tmmPayRequestWaitPeriod);
        return customData;
    }

    public static ChannelRequestDto convertPayBillToChannelPayload(
            TNMPayBillPayRequestDTO payBillConfirmationRequestDTO, String amsName, String currency) {
        JSONObject payer = new JSONObject();

        JSONObject partyIdInfoPayer = new JSONObject();
        partyIdInfoPayer.put("partyIdType", SECONDARY_IDENTIFIER_NAME);
        partyIdInfoPayer.put("partyIdentifier", payBillConfirmationRequestDTO.getMsisdn());

        payer.put("partyIdInfo", partyIdInfoPayer);

        JSONObject payee = new JSONObject();
        JSONObject partyIdInfoPayee = new JSONObject();

        partyIdInfoPayee.put("partyIdType", getPrimaryIdentifierName(amsName));
        partyIdInfoPayee.put("partyIdentifier", payBillConfirmationRequestDTO.getAccountNumber());

        payee.put("partyIdInfo", partyIdInfoPayee);

        JSONObject amount = new JSONObject();
        amount.put("amount", payBillConfirmationRequestDTO.getTransactionAmount());
        amount.put("currency", currency);

        Party payeeDto = new Party();
        payeeDto.setPartyIdType(getPrimaryIdentifierName(amsName));
        payeeDto.setPartyIdIdentifier(payBillConfirmationRequestDTO.getAccountNumber());
        Party payerDto = new Party();
        payerDto.setPartyIdType(SECONDARY_IDENTIFIER_NAME);
        payerDto.setPartyIdIdentifier(payBillConfirmationRequestDTO.getMsisdn());
        List<Party> payerList = List.of(payerDto);
        List<Party> payeeList = List.of(payeeDto);

        return new ChannelRequestDto(payerList, payeeList, payBillConfirmationRequestDTO.getTransactionAmount(),
                currency, payBillConfirmationRequestDTO.getTransactionID());
    }

    public static String getWorkflowId(String type, String subtype, String amsName,
            String accountHoldingInstitutionId) {
        return new StringBuilder().append(subtype).append("_").append(type).append("_").append(amsName).append("-")
                .append(accountHoldingInstitutionId).toString();

    }

    public static String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    public static JSONObject buildPayBillValidationResponse(Boolean reconciled, String transactionId,
            String clientName) {
        JSONObject responseObject = new JSONObject();
        boolean isReconciled = Boolean.TRUE.equals(reconciled);
        responseObject.put("status", isReconciled ? 200 : 404);
        responseObject.put("message",
                isReconciled ? "Account exists" : "Account does not exists or payment not allowed");
        responseObject.put("oafTransactionReference", transactionId);
        if (isReconciled) {
            responseObject.put("clientName", clientName);
        }
        return responseObject;
    }

    public static JSONObject buildPayBillTransactionResponseResponse(boolean successful, Object response) {
        JSONObject responseObject = new JSONObject();
        int status = 404;
        String message = "Transaction not found";
        if (successful && response != null && response instanceof TransactionStatusResponseDTO responseDTO) {
            TransferState transferState = responseDTO.getTransferState();
            log.debug("Transfer state: {}", transferState);
            if (TransferState.COMMITTED.equals(transferState)) {
                status = 200;
                message = "Payment successful";
            }
            responseObject.put("receipt_number", responseDTO.getTransactionId());
        }

        responseObject.put("status", status);
        responseObject.put("message", message);

        return responseObject;
    }

    public static String getPrimaryIdentifierName(String amsName) {
        String primaryIdentifierName = null;
        if (amsName.equalsIgnoreCase("roster")) {
            primaryIdentifierName = ROSTER_PRIMARY_IDENTIFIER_NAME;
        } else if (amsName.equalsIgnoreCase("fineract")) {
            primaryIdentifierName = FINERACT_PRIMARY_IDENTIFIER_NAME;
        }
        return primaryIdentifierName;
    }

}
