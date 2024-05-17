package org.mifos.connector.tnm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;

/**
 * Class representing the request to be sent to the Channel connector.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRequestDto {

    JSONObject payer;
    JSONObject payee;
    JSONObject amount;

    Boolean useWorkflowIdAsTransactionId;

    String workflowId;

    @Override
    public String toString() {
        return "{" + "payer:" + payer + ", payee:" + payee + ", amount:" + amount + ", useWorkflowIdAsTransactionId:"
                + useWorkflowIdAsTransactionId + ", workflowId:" + workflowId + "}";
    }
}
