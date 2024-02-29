package org.mifos.connector.tnm.camel.routes;

import static org.mifos.connector.tnm.camel.config.CamelProperties.ACCOUNT_HOLDING_INSTITUTION_ID;
import static org.mifos.connector.tnm.camel.config.CamelProperties.AMS_NAME;
import static org.mifos.connector.tnm.camel.config.CamelProperties.BUSINESS_SHORT_CODE;
import static org.mifos.connector.tnm.camel.config.CamelProperties.CHANNEL_REQUEST;
import static org.mifos.connector.tnm.camel.config.CamelProperties.CHANNEL_URL;
import static org.mifos.connector.tnm.camel.config.CamelProperties.CLIENT_ACCOUNT_NUMBER;
import static org.mifos.connector.tnm.camel.config.CamelProperties.CLIENT_CORRELATION_ID;
import static org.mifos.connector.tnm.camel.config.CamelProperties.CLIENT_NAME;
import static org.mifos.connector.tnm.camel.config.CamelProperties.CONTENT_TYPE;
import static org.mifos.connector.tnm.camel.config.CamelProperties.CONTENT_TYPE_VAL;
import static org.mifos.connector.tnm.camel.config.CamelProperties.GET_ACCOUNT_DETAILS_FLAG;
import static org.mifos.connector.tnm.camel.config.CamelProperties.PAYBILL_TRANSACTION_ID_URL_PARAM;
import static org.mifos.connector.tnm.camel.config.CamelProperties.SECONDARY_IDENTIFIER_NAME;
import static org.mifos.connector.tnm.camel.config.CamelProperties.TENANT_ID;
import static org.mifos.connector.tnm.camel.config.CamelProperties.TNM_PAYBILL_WORKFLOW_SUBTYPE;
import static org.mifos.connector.tnm.camel.config.CamelProperties.TNM_PAYBILL_WORKFLOW_TYPE;
import static org.mifos.connector.tnm.util.TNMUtils.buildPayBillValidationResponse;
import static org.mifos.connector.tnm.util.TNMUtils.generateTransactionId;
import static org.mifos.connector.tnm.util.TNMUtils.getPrimaryIdentifierName;
import static org.mifos.connector.tnm.util.TNMUtils.getWorkflowId;
import static org.mifos.connector.tnm.zeebe.ZeebeVariables.CURRENCY;
import static org.mifos.connector.tnm.zeebe.ZeebeVariables.EXTERNAL_ID;
import static org.mifos.connector.tnm.zeebe.ZeebeVariables.IS_VALIDATION_REFERENCE_PRESENT;
import static org.mifos.connector.tnm.zeebe.ZeebeVariables.SERVER_TRANSACTION_ID;
import static org.mifos.connector.tnm.zeebe.ZeebeVariables.TRANSACTION_ID;
import static org.mifos.connector.tnm.zeebe.ZeebeVariables.TRANSFER_CREATE_FAILED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.camunda.zeebe.client.ZeebeClient;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.json.JSONObject;
import org.mifos.connector.common.gsma.dto.GsmaTransfer;
import org.mifos.connector.tnm.camel.config.AMSPayBillProperties;
import org.mifos.connector.tnm.camel.config.AMSProperties;
import org.mifos.connector.tnm.camel.config.ZeebeProperties;
import org.mifos.connector.tnm.dto.ChannelRequestDto;
import org.mifos.connector.tnm.dto.ChannelValidationRequestDTO;
import org.mifos.connector.tnm.dto.PayBillValidationResponseDTO;
import org.mifos.connector.tnm.dto.TNMPayBillPayRequestDTO;
import org.mifos.connector.tnm.util.TNMUtils;
import org.mifos.connector.tnm.zeebe.ZeebeVariables;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class to process the paybill route.
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayBillRouteProcessor {

    private final ZeebeClient zeebeClient;
    private final AMSPayBillProperties amsPayBillProps;

    private final ZeebeProperties zeebeProperties;

    @Value("${channel.host}")
    private String channelUrl;

    private ObjectMapper objectMapper = TNMUtils.getObjectMapper();
    public static Map<String, Boolean> reconciledStore = new HashMap<>();
    public static Map<String, String> workflowInstanceStore = new HashMap<>();

    /**
     * Build the request body for get account status request
     *
     * @param exchange
     *            {@link Exchange}
     * @return
     */
    public String buildBodyForAccountStatus(Exchange exchange) {
        log.debug("## PayBill Validation Payload request");
        String clientAccountNumber = exchange.getIn().getHeader(CLIENT_ACCOUNT_NUMBER).toString();
        Object currencyFromHeaders = exchange.getIn().getHeader(CURRENCY);
        Object shortCodeFromReq = exchange.getIn().getHeader(BUSINESS_SHORT_CODE);
        Object msisdn = exchange.getIn().getHeader(SECONDARY_IDENTIFIER_NAME);
        Object getAccountDetails = exchange.getIn().getHeader(GET_ACCOUNT_DETAILS_FLAG);
        boolean getAccountDetailsFlag = !Objects.nonNull(getAccountDetails)
                || Boolean.parseBoolean(getAccountDetails.toString());

        if (Objects.isNull(msisdn)) {
            throw new RuntimeException("MSISDN is required for PayBill validation");
        }

        AMSProperties amsProperties = amsPayBillProps
                .getAMSPropertiesFromShortCode(Objects.nonNull(shortCodeFromReq) ? shortCodeFromReq.toString()
                        : amsPayBillProps.getDefaultAmsShortCode());
        String amsName = amsProperties.getAms();
        String currency = Objects.nonNull(currencyFromHeaders) ? currencyFromHeaders.toString()
                : amsProperties.getCurrency();
        exchange.getIn().removeHeaders("*");
        exchange.getIn().setHeader("amsUrl", amsProperties.getBaseUrl());
        exchange.getIn().setHeader(CONTENT_TYPE, CONTENT_TYPE_VAL);
        exchange.getIn().setHeader("amsName", amsName);
        exchange.getIn().setHeader("accountHoldingInstitutionId", amsPayBillProps.getAccountHoldingInstitutionId());
        exchange.setProperty("channelUrl", channelUrl);
        exchange.setProperty("primaryIdentifier", getPrimaryIdentifierName(amsName));
        exchange.setProperty("primaryIdentifierValue", clientAccountNumber);
        exchange.setProperty("secondaryIdentifier", SECONDARY_IDENTIFIER_NAME);
        exchange.setProperty("secondaryIdentifierValue", msisdn);
        ChannelValidationRequestDTO obj = TNMUtils.createValidationRequest(clientAccountNumber, amsName, currency,
                msisdn.toString(), getAccountDetailsFlag);
        log.debug("Header:{}", exchange.getIn().getHeaders());
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String buildBodyForStartPayBillWorkflow(Exchange e) {

        PayBillValidationResponseDTO validationResponseDTO = e.getIn().getBody(PayBillValidationResponseDTO.class);
        boolean isReconciled = validationResponseDTO.isReconciled();
        String validationTransactionId = validationResponseDTO.getTransactionId();
        log.debug("PayBill Response: {}", validationResponseDTO);
        log.debug("Validation IsReconciled: {}", isReconciled);
        log.debug("Validation Transaction ID present: {}", validationTransactionId);

        // Add the validation transactionID as clientCorrelationId in the reconciledStore
        String clientCorrelationId = validationTransactionId;
        reconciledStore.put(clientCorrelationId, isReconciled);

        GsmaTransfer gsmaTransfer = TNMUtils.createGsmaTransferDTO(validationResponseDTO, clientCorrelationId,
                zeebeProperties.getWaitTnmPayRequestPeriod());

        e.getIn().removeHeaders("*");
        e.getIn().setHeader(ACCOUNT_HOLDING_INSTITUTION_ID, validationResponseDTO.getAccountHoldingInstitutionId());
        e.getIn().setHeader(AMS_NAME, validationResponseDTO.getAmsName());
        e.getIn().setHeader(TENANT_ID, validationResponseDTO.getAccountHoldingInstitutionId());
        e.getIn().setHeader(CLIENT_CORRELATION_ID, clientCorrelationId);
        e.getIn().setHeader(CONTENT_TYPE, CONTENT_TYPE_VAL);
        e.getIn().setHeader(CLIENT_NAME, validationResponseDTO.getClientName());

        e.setProperty("isValidationReferencePresent", isReconciled);
        e.setProperty("channelUrl", channelUrl);
        try {
            return objectMapper.writeValueAsString(gsmaTransfer);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void processRequestForPayBillPayRoute(Exchange e) {
        TNMPayBillPayRequestDTO requestDTO = e.getIn().getBody(TNMPayBillPayRequestDTO.class);
        log.debug("PayBill Response: {}", requestDTO);
        log.debug("Is oafTransactionReference present: {}", requestDTO.getOafValidationRef());

        String shortCodeFromReq = e.getIn().getHeader(BUSINESS_SHORT_CODE).toString();
        String currencyFromHeaders = e.getIn().getHeader(CURRENCY).toString();

        AMSProperties amsProperties = amsPayBillProps.getAMSPropertiesFromShortCode(
                Objects.nonNull(shortCodeFromReq) ? shortCodeFromReq : amsPayBillProps.getDefaultAmsShortCode());
        String amsName = amsProperties.getAms();
        String currency = Objects.nonNull(currencyFromHeaders) ? currencyFromHeaders : amsProperties.getCurrency();
        String amsUrl = amsProperties.getBaseUrl();

        Boolean isReconciled = Objects.nonNull(requestDTO.getOafValidationRef());
        String oafTransactionReference = requestDTO.getOafValidationRef();
        requestDTO.setValidationReferencePresent(isReconciled);
        requestDTO.setAmsName(amsProperties.getAms());

        e.setProperty("amsUrl", amsUrl);
        e.setProperty("secondaryIdentifier", "MSISDN");
        e.setProperty("secondaryIdentifierValue", requestDTO.getMsisdn());

        Gson gson = new Gson();
        ChannelRequestDto channelRequestDTO = TNMUtils.convertPayBillToChannelPayload(requestDTO, amsName, currency);
        String channelRequestDTOString = gson.toJson(channelRequestDTO);
        e.setProperty("PAY_REQUEST", channelRequestDTOString);

        // Getting TNM workflow transaction id and removing key
        String transactionId = workflowInstanceStore.get(oafTransactionReference);

        e.setProperty(CHANNEL_REQUEST, channelRequestDTO);
        e.setProperty(EXTERNAL_ID, requestDTO.getTransactionID());

        Map<String, Object> variables = new HashMap<>();
        variables.put("confirmationReceived", true);
        variables.put(CHANNEL_REQUEST, channelRequestDTOString);
        variables.put("amount", requestDTO.getTransactionAmount());
        variables.put("accountId", requestDTO.getAccountNumber());
        variables.put("originDate",
                Long.parseLong(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));
        variables.put("phoneNumber", requestDTO.getMsisdn());
        variables.put(SERVER_TRANSACTION_ID, requestDTO.getTransactionID());
        variables.put(IS_VALIDATION_REFERENCE_PRESENT, isReconciled && transactionId != null);
        variables.put(TRANSACTION_ID, oafTransactionReference);
        variables.put(TRANSFER_CREATE_FAILED, false);
        log.info("Workflow transaction id : {}", transactionId);

        if (transactionId != null) {
            zeebeClient.newPublishMessageCommand().messageName("pendingPayRequest").correlationKey(transactionId)
                    .timeToLive(Duration.ofMillis(300)).variables(variables).send();
            log.debug("Published Variables");
        } else {
            log.debug("No workflow of such transaction ID exists");
            transactionId = generateTransactionId();
            variables.put(ZeebeVariables.TRANSACTION_ID, transactionId);
            variables.put(ZeebeVariables.ORIGIN_DATE, Instant.now().toEpochMilli());
            zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId(getWorkflowId(TNM_PAYBILL_WORKFLOW_TYPE, TNM_PAYBILL_WORKFLOW_SUBTYPE, amsName,
                            amsPayBillProps.getAccountHoldingInstitutionId()))
                    .latestVersion().variables(variables).send().join();
            zeebeClient.newPublishMessageCommand().messageName("pendingPayRequest").correlationKey(transactionId)
                    .timeToLive(Duration.ofMillis(300)).variables(variables).send();
        }
    }

    public void processRequestForTransactionStatusCheck(Exchange exchange) {
        log.debug("## PayBill Transaction status check route");
        String tnmTransactionId = exchange.getIn().getHeader(PAYBILL_TRANSACTION_ID_URL_PARAM).toString();

        exchange.getIn().removeHeaders("*");
        exchange.getIn().setHeader(CONTENT_TYPE, CONTENT_TYPE_VAL);
        exchange.getIn().setHeader("requestType", "transfers");
        exchange.getIn().setHeader(TENANT_ID, "oaf");
        exchange.setProperty(PAYBILL_TRANSACTION_ID_URL_PARAM, tnmTransactionId);
        exchange.setProperty(CHANNEL_URL, channelUrl);
    }

    public void processResponseForPayBillValidationResponseSuccess(Exchange e) {
        String channelResponseBodyString = e.getIn().getBody(String.class);
        JSONObject channelResponse = new JSONObject(channelResponseBodyString);
        log.debug("channelResponse:{}", channelResponse);
        String workflowInstanceKey = channelResponse.getString("transactionId");

        // Retrieving client correlation ID added to the header in --- route
        String clientCorrelationId = e.getIn().getHeader(CLIENT_CORRELATION_ID).toString();
        Object clientName = e.getIn().getHeader(CLIENT_NAME);
        Boolean reconciled = reconciledStore.get(clientCorrelationId);
        // Storing the key value
        workflowInstanceStore.put(clientCorrelationId, workflowInstanceKey);
        reconciledStore.remove(clientCorrelationId);

        e.getIn().setBody(buildPayBillValidationResponse(reconciled, clientCorrelationId,
                Objects.nonNull(clientName) ? clientName.toString() : null).toString());
    }

    public void processResponseForPayBillValidationResponseError(Exchange e) {
        JSONObject channelResponse = new JSONObject(e.getIn().getBody());
        log.debug("channelResponse:{}", channelResponse);

        e.getIn().setBody(buildPayBillValidationResponse(false, null, null).toString());
    }

}
