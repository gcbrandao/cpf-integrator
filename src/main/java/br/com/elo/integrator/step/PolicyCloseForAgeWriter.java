package br.com.elo.integrator.step;

import br.com.elo.integrator.dto.ResultDTO;
import com.bnpparibas.cardif.contractservice.dto.job.policyclose.PolicyCloseResponseTO;
import com.bnpparibas.cardif.job.commons.ProcessOperationJobDAO;
import com.bnpparibas.cardif.job.commons.controller.JobSchedulerGateway;
import com.bnpparibas.cardif.legacy.business.ChangePolicyBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static br.com.elo.integrator.CpfIntegratorApplication.ATTRIBUTE_CONTROLLER;

@Slf4j
@StepScope
@Component
public class PolicyCloseForAgeWriter implements ItemWriter<ResultDTO> {

    @Autowired
    private ChangePolicyBO changePolicyBO;

    @Autowired
    private JobSchedulerGateway jobSchedulerGateway;

    @Autowired
    private ProcessOperationJobDAO processOperationJobDAO;

    @Value("#{jobParameters['ProcessID']}")
    private String correlationId;

    @Value("${job.max.page}")
    private int maxPage;

    private PolicyCloseResponseTO response;

    @Override
    public void write(List<? extends ResultDTO> list) throws Exception {
        list.forEach(p -> {
            log.trace(" policy number: " + p.getPolicyNumber());
            response.addCountProcessedItems();
            if (!p.getPolicyApplyVOS().isEmpty()) {
                p.getPolicyApplyVOS().forEach(apply -> {
                            changePolicyBO.doPersistWithPolicyLog(p.getSessionId(), new ArrayList<>(), apply);
                            apply.getPolicies()
                                    .forEach(policy -> processOperationJobDAO.save(Long.parseLong(correlationId),
                                            policy.getId() == null ?
                                                    policy.getAgregatedPolicy().getId().longValue() :
                                                    policy.getId(),
                                            policy.getContextOperation().getId() == null ?
                                                    policy.getAgregatedPolicy()
                                                            .getContextOperation()
                                                            .getId()
                                                            .longValue() :
                                                    policy.getContextOperation().getId().longValue()));

                        response.addCountSuccessProcessedItems();
                        response.addCountMovementItems();
                        response.addSuccessProcessedItem(p.getPolicyNumber());
                });
            } else {
                response.addCountErrorProcessedItems();
                response.addErrorProcessedItems(p.getPolicyNumber(), p.getExceptions().stream().findFirst().get().getMessage());
            }
        });

        updateJobStatus();
    }

    @BeforeStep
    public void beforeStep(final StepExecution stepExecution) {
        response = (PolicyCloseResponseTO) stepExecution.getJobExecution().getExecutionContext().get(ATTRIBUTE_CONTROLLER);
    }

    private void updateJobStatus() {
        if (response.getCountProcessedItems() % this.maxPage == 0) {
            jobSchedulerGateway.sendUpdateProcess(correlationId, response.getCountProcessedItems());
        }
    }

}
