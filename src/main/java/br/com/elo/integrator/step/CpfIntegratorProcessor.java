package br.com.elo.integrator.step;

import br.com.elo.integrator.dto.CpfIntegratorDTO;
import br.com.elo.integrator.dto.ResultDTO;
import com.google.common.collect.Lists;
import jdk.nashorn.internal.runtime.regexp.joni.constants.Arguments;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.text.MessageFormat.format;


@Component
@StepScope
@Slf4j
public class CpfIntegratorProcessor implements ItemProcessor<CpfIntegratorDTO, ResultDTO> {


    private StepExecution stepExecution;

    @BeforeStep
    public void beforeStep(final StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public ResultDTO process(final CpfIntegratorDTO policyDTO) throws Exception {


        try {

        } catch (Exception ex){
            throw new Exception();
        }

        return null;
    }



}
