package org.mifos.connector.tnm.zeebe;

import static org.mifos.connector.tnm.camel.config.CamelProperties.TNM_TRX_ID;
import static org.mifos.connector.tnm.camel.routes.PayBillRouteProcessor.workflowInstanceStore;

import io.camunda.zeebe.client.ZeebeClient;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Contains workers that will run based on the BPMN flow.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ZeebeWorkers {

    private final ZeebeClient zeebeClient;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @PostConstruct
    void setupWorkers() {

        zeebeClient.newWorker().jobType("delete-tnm-workflow-instancekey").handler(((client, job) -> {
            log.info("Removing Workflow Instance key and Tnm Txn Id from store");
            Map<String, Object> variables = job.getVariablesAsMap();
            String tnmTxnId = variables.get(TNM_TRX_ID).toString();
            log.debug("Txn Id Removed :{}", tnmTxnId);
            workflowInstanceStore.remove(tnmTxnId);
            client.newCompleteCommand(job.getKey()).send().join();
        })).name("Cleanup").maxJobsActive(workerMaxJobs).open();
    }

}
