package org.mifos.connector.tnm.camel.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AMSProperties {

    String businessShortCode;
    String ams;
    String currency;
    String baseUrl;
}
