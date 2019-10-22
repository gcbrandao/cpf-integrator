package br.com.elo.integrator;

import com.bnpparibas.cardif.contractservice.dto.job.policyclose.PolicyCloseRequestTO;
import com.bnpparibas.cardif.job.commons.controller.JobSchedulerGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.jms.Jms;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.naming.NamingException;
import java.util.concurrent.Executor;

@Slf4j
@Configuration
public class CpfIntegratorIntegration {
    private static final long JMSQUEUE_READ_TIMEOUT = 1_000L;
    public static final String QUEUE_CONNECTION_FACTORY = "QueueConnectionFactory";

    public static final String PROFILES_TOMCAT = "tomcat,test";

    @Value("${task.executor}")
    private String taskExecutor;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${job.queue.name}")
    private String jobQueueName;

    @Value("${job.scheduler.update.job.queue}")
    private String jobSchedulerUpdate;

    @Value("${job.scheduler.notification.job.queue}")
    private String jobSchedulerNotification;

    @Value("${job.scheduler.reply.job.queue}")
    private String jobSchedulerReply;

    @Value("${job.scheduler.update-process.job.queue}")
    private String jobSchedulerUpdateProcess;

    @Value("${job.scheduler.error.job.queue}")
    private String jobSchedulerError;

    @Bean
    public IntegrationFlow processFlow(final CpfIntegratorLauncher launcher) throws NamingException {
        return IntegrationFlows.from(Jms.messageDriverChannelAdapter(queueConnectionFactory())
                .destination(this.jobQueueName))
                .transform(new JsonToObjectTransformer(PolicyCloseRequestTO.class))
                .handle(launcher)
                .get();
    }

    @Bean
    public IntegrationFlow updateFlow() throws NamingException {
        return IntegrationFlows.from(JobSchedulerGateway.UPDATE_JOB_CHANNEL)
                .transform(new ObjectToJsonTransformer())
                .handle(Jms.outboundAdapter(createJmsTemplate(this.jobSchedulerUpdate)).destination(this.jobSchedulerUpdate))
                .get();
    }

    @Bean
    public IntegrationFlow replyFlow() throws NamingException {
        return IntegrationFlows.from(JobSchedulerGateway.REPLY_JOB_CHANNEL)
                .transform(new ObjectToJsonTransformer())
                .handle(Jms.outboundAdapter(createJmsTemplate(this.jobSchedulerReply)).destination(this.jobSchedulerReply))
                .get();
    }

    @Bean
    public IntegrationFlow errorFlow() throws NamingException {
        return IntegrationFlows.from(JobSchedulerGateway.ERROR_JOB_CHANNEL)
                .transform(new ObjectToJsonTransformer())
                .handle(Jms.outboundAdapter(createJmsTemplate(this.jobSchedulerError)).destination(this.jobSchedulerError))
                .get();
    }

    @Bean
    public IntegrationFlow updateProcessFlow() throws NamingException {
        return IntegrationFlows.from(JobSchedulerGateway.UPDATE_PROCESS_JOB_CHANNEL)
                .transform(new ObjectToJsonTransformer())
                .handle(Jms.outboundAdapter(createJmsTemplate(this.jobSchedulerUpdateProcess)).destination(this.jobSchedulerUpdateProcess))
                .get();
    }

    @Bean
    public IntegrationFlow notificationFlow() throws NamingException {
        return IntegrationFlows.from(JobSchedulerGateway.NOTIFICATION_JOB_CHANNEL)
                .handle(Jms.outboundAdapter(createJmsTemplate(this.jobSchedulerNotification)))
                .get();
    }

    @Bean
    public ConnectionFactory queueConnectionFactory() throws NamingException {
        if (PROFILES_TOMCAT.contains(activeProfile)) {
            final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
            activeMQConnectionFactory.setBrokerURL("tcp://localhost:61616");
            return activeMQConnectionFactory;
        }

        return (ConnectionFactory) new JndiTemplate().lookup(QUEUE_CONNECTION_FACTORY);
    }


    @Bean
    public Executor executor() throws NamingException {
        if (PROFILES_TOMCAT.contains(activeProfile)) {
            return new SimpleAsyncTaskExecutor("pims_job_");
        } else {
            return (Executor) new JndiTemplate().lookup(this.taskExecutor);
        }
    }


    @Bean
    public JndiDestinationResolver destinationResolver() {
        return new JndiDestinationResolver();
    }

    private JmsTemplate createJmsTemplate(final String destination) throws NamingException {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestinationName(destination);
        jmsTemplate.setConnectionFactory(queueConnectionFactory());
        jmsTemplate.setReceiveTimeout(JMSQUEUE_READ_TIMEOUT);
        jmsTemplate.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        return jmsTemplate;
    }

    @Bean
    public Jackson2JsonObjectMapper objectMapperTransformer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return new Jackson2JsonObjectMapper(mapper);
    }
}
