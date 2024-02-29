package org.mifos.connector.tnm.camel.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Class to hold properties for AMS Paybill.
 *
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "paybill")
public class AMSPayBillProperties {

    private String accountHoldingInstitutionId;
    private String defaultAms;
    private String defaultAmsShortCode;
    private List<AMSProperties> groups = new ArrayList<>();

    /**
     * Fetches the AMS properties from the business short code.
     *
     * @param businessShortCode
     *            the AMS short code
     * @return the AMS properties
     */
    public AMSProperties getAMSPropertiesFromShortCode(String businessShortCode) {
        return getGroups().stream().filter(p -> p.getBusinessShortCode().equalsIgnoreCase(businessShortCode))
                .findFirst().get();
    }

}
