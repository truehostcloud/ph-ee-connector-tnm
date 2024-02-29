package org.mifos.connector.tnm.camel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Class to hold properties for Zeebe.
 *
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "zeebe")
public class ZeebeProperties {

    private Client client;
    private int waitTnmPayRequestPeriod = 30;

    @Getter
    @Setter
    public class Client {

        private int maxExecutionThreads;
        private int numberOfWorkers;
        private int evenlyAllocatedMaxJobs;
    }
}
