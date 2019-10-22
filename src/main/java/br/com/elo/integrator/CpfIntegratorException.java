package br.com.elo.integrator;

import com.bnpparibas.cardif.job.commons.controller.JobSchedulerGateway;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.exception.SimpleLimitExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class CpfIntegratorException extends SimpleLimitExceptionHandler {

    @Autowired
    private JobSchedulerGateway jobSchedulerGateway;

    @Override
    public void handleException(RepeatContext context, Throwable throwable) throws Throwable {
        super.handleException(context, throwable);
    }
  
}
