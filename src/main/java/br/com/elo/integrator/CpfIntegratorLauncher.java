package br.com.elo.integrator;

import com.bnpparibas.cardif.job.commons.close.AbstractPimsJobCloseLauncherCommons;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;


@Slf4j
@Component
public class CpfIntegratorLauncher extends AbstractPimsJobCloseLauncherCommons {

    @Inject
    @Named("policyCloseForAgeLegacyJob")
    private Job job;

    @Override
    public Job getJob() {
        return job;
    }
}
