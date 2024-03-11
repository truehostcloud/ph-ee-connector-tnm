package org.mifos.connector.tnm.camel.routes;

import static org.mifos.connector.tnm.util.TnmUtils.buildPayBillTransactionResponseResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.mifos.connector.common.channel.dto.TransactionStatusResponseDTO;
import org.springframework.stereotype.Component;

/**
 * Route for handling PayBill response.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayBillResponseHandlingRoute extends ErrorHandlerRouteBuilder {

    private final PayBillRouteProcessor payBillRouteProcessor;

    @Override
    public void configure() {

        // Response handling routes

        from("direct:paybill-validation-response-success").id("paybill-validation-response-success")
                .log(LoggingLevel.INFO, "Sending PayBill Validation success response")
                .process(payBillRouteProcessor::processResponseForPayBillValidationResponseSuccess);

        from("direct:paybill-validation-response-failure").id("paybill-validation-response-failure")
                .log(LoggingLevel.INFO, "Sending PayBill Validation Failure response")
                .process(payBillRouteProcessor::processResponseForPayBillValidationResponseError);

        from("direct:paybill-transaction-status-response-success").id("paybill-transaction-status-response-success")
                .log(LoggingLevel.INFO, "## Paybill Transaction status success response route").process(e -> {

                    TransactionStatusResponseDTO channelResponse = e.getIn()
                            .getBody(TransactionStatusResponseDTO.class);
                    e.getIn().setBody(buildPayBillTransactionResponseResponse(true, channelResponse).toString());

                });

        from("direct:paybill-transaction-status-response-failure").id("paybill-transaction-status-response-failure")
                .log(LoggingLevel.INFO, "## Paybill Transaction status failure response route").process(e -> {
                    TransactionStatusResponseDTO channelResponse = e.getIn()
                            .getBody(TransactionStatusResponseDTO.class);

                    e.getIn().setBody(buildPayBillTransactionResponseResponse(false, channelResponse).toString());
                });

    }
}
