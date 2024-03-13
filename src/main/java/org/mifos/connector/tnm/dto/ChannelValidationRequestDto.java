package org.mifos.connector.tnm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.mifos.connector.common.gsma.dto.CustomData;

/**
 * Class representing the request to be sent to the Channel connector.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChannelValidationRequestDto {

    @JsonProperty("primaryIdentifier")
    private CustomData primaryIdentifier;
    @JsonProperty("secondaryIdentifier")
    private CustomData secondaryIdentifier;
    @JsonProperty("customData")
    private List<CustomData> customData;
}
