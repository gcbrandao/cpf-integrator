package br.com.elo.integrator;

import com.bnpparibas.cardif.contractservice.dto.job.policyclose.PolicyCloseResponseTO;
import com.bnpparibas.cardif.job.commons.controller.JobSchedulerGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static br.com.elo.integrator.CpfIntegratorApplication.ATTRIBUTE_CONTROLLER;

@Slf4j
@Component
public class CpfIntegratorListener implements JobExecutionListener {

    @Autowired
    private JobSchedulerGateway jobSchedulerGateway;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobExecution.getExecutionContext().put(ATTRIBUTE_CONTROLLER, new PolicyCloseResponseTO());
    }

    @Override
    public void afterJob(final JobExecution jobExecution) {
        final PolicyCloseResponseTO response = (PolicyCloseResponseTO)
                jobExecution.getExecutionContext().get(ATTRIBUTE_CONTROLLER);

        trace(jobExecution, response);

        final String correlationId = jobExecution.getJobParameters().getString("correlationId");
        jobExecution.getStepExecutions()
                .stream()
                .map(StepExecution::getFailureExceptions)
                .flatMap(Collection::stream)
                .forEach(cause -> {
                    jobSchedulerGateway.sendNotification(String.format("%s : %s ",  cause.getClass().getName(), cause.getMessage()), correlationId);
                    log.error(cause.getMessage());
                });


        if (response.getCountErrorProcessedItems() > 0) {
            jobSchedulerGateway.sendError(response, correlationId, response.getCountProcessedItems(), response.getCountErrorProcessedItems());
        } else {
            jobSchedulerGateway.sendReply(response, correlationId, response.getCountRequestedItems());
        }

    }

    private void trace(JobExecution jobExecution, PolicyCloseResponseTO response) {
        log.trace("########################## SUMMARY ########################## ");
        log.trace("STARTED:       " + jobExecution.getStartTime());
        log.trace("FINISHED:      " + (jobExecution.getEndTime() != null ? jobExecution.getEndTime() : jobExecution.getLastUpdated()));
        log.trace("READ COUNT:    " + response.getCountRequestedItems());
        log.trace("WRITE COUNT:   " + response.getCountProcessedItems());
        log.trace("ERROR COUNT:   " + response.getCountErrorProcessedItems());
        log.trace("SUCCESS COUNT: " + response.getCountSuccessProcessedItems());
        log.trace("########################## SUMMARY ########################## \n");
    }
}
