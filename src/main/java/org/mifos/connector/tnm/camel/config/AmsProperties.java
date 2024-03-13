package org.mifos.connector.tnm.camel.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Class to hold properties for AMS details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AmsProperties {

    String businessShortCode;
    String ams;
    String currency;
    String baseUrl;
}
