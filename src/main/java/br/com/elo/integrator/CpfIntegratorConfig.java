package br.com.elo.integrator;

import com.bnpparibas.cardif.contractservice.dto.job.policyclose.PolicyDTO;
import br.com.elo.integrator.dto.ResultDTO;
import br.com.elo.integrator.step.CpfIntegratorEnd;
import br.com.elo.integrator.step.CpfIntegratorValidate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;


@Configuration
public class CpfIntegratorConfig {

    @Value("${job.throttle.limit}")
    private int throttleLimit;

    @Value("${job.chunk.size}")
    private int chunckSize;

    @Bean
    Step policyCloseForAgeStep(final ItemReader<PolicyDTO> reader,
                                          final ItemProcessor<PolicyDTO, ResultDTO> processor,
                                          final ItemWriter<ResultDTO> writer,
                                          final TaskExecutor taskExecutor,
                                          final StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("policyCloseForAgeStep")
                .<PolicyDTO, ResultDTO>chunk(this.chunckSize)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor)
                .throttleLimit(this.throttleLimit)
                .build();
    }

    @Bean
    public Step policyCloseForAgeStepValidate(StepBuilderFactory stepBuilderFactory,
                                              CpfIntegratorValidate policyCloseForAgeValidate) {
        return stepBuilderFactory.get("policyCloseForAgeStepValidate")
                .tasklet(policyCloseForAgeValidate)
                .build();
    }

    @Bean
    public Step policyCloseForAgeStepEnd(StepBuilderFactory stepBuilderFactory,
                                         CpfIntegratorEnd cpfIntegratorEnd) {
        return stepBuilderFactory.get("policyCloseForAgeStepEnd")
                .tasklet(cpfIntegratorEnd)
                .build();
    }

    @Bean
    Job policyCloseForAgeLegacyJob(JobBuilderFactory jobBuilderFactory,
                                   @Qualifier("policyCloseForAgeStep") Step main,
                                   @Qualifier("policyCloseForAgeStepValidate") Step validate,
                                   @Qualifier("policyCloseForAgeStepEnd") Step finish,
                                   CpfIntegratorListener listener
    ) {
        return jobBuilderFactory.get("policyCloseForAgeLegacyJob")
                .incrementer(new RunIdIncrementer())
                .flow(validate).on(FlowExecutionStatus.COMPLETED.getName()).to(main).next(finish)
                .from(validate).on(FlowExecutionStatus.FAILED.getName()).to(finish)
                .end()
                .listener(listener)
                .build();

    }

}
