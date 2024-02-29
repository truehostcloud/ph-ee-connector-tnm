package org.mifos.connector.tnm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mifos.connector.common.gsma.dto.Party;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRequestDto {

    @JsonProperty("payer")
    List<Party> payer;

    @JsonProperty("payee")
    List<Party> payee;

    @JsonProperty("amount")
    String amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("externalId")
    private String externalId;
}
