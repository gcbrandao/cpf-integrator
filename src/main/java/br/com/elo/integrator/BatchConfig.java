package br.com.elo.integrator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.scope.JobScope;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.concurrent.Executor;

@Slf4j
@Configuration
public class BatchConfig {

    @Bean
    public JobRepositoryFactoryBean jobRepositoryFactoryBean(
            final PlatformTransactionManager transactionManager,
            final DataSource dataSource) {
        final JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setTransactionManager(transactionManager);
        jobRepositoryFactoryBean.setDataSource(dataSource);
        jobRepositoryFactoryBean.setValidateTransactionState(false);
        jobRepositoryFactoryBean.setLobHandler(lobHandler());
        return jobRepositoryFactoryBean;
    }

    @Bean
    public JobRepository jobRepository(final JobRepositoryFactoryBean jobRepositoryFactoryBean) throws Exception {
        return jobRepositoryFactoryBean.getObject();
    }

    @Bean
    public JobScope jobScope() {
        return new JobScope();
    }

    @Bean
    public JobExplorerFactoryBean jobExplorer(final DataSource dataSource) {
        final JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
        jobExplorerFactoryBean.setDataSource(dataSource);
        return jobExplorerFactoryBean;
    }

    @Bean
    public org.springframework.batch.core.configuration.support.MapJobRegistry jobRegistry() {
        return new org.springframework.batch.core.configuration.support.MapJobRegistry();
    }

    @Bean
    public SimpleJobLauncher jobLauncher(final JobRepository jobRepository,
                                         final @Qualifier("executor") Executor taskExecutor) {
        final SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(jobRepository);
        simpleJobLauncher.setTaskExecutor(new TaskExecutorAdapter(taskExecutor));
        return simpleJobLauncher;
    }

    @Bean
    public JobBuilderFactory jobBuilderFactory(JobRepository jobRepository) {
        return new JobBuilderFactory(jobRepository);
    }

    @Bean("stepBuilderFactory")
    public StepBuilderFactory stepBuilderFactory(JobRepository jobRepository,
                                                 PlatformTransactionManager transactionManager) {
        log.trace("stepBuilderFactory");
        return new StepBuilderFactory(jobRepository, transactionManager);
    }


    @Bean
    public DefaultLobHandler lobHandler() {
        return new DefaultLobHandler();
    }

}
