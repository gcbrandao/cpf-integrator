package br.com.elo.integrator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@EnableTransactionManagement
@EnableBatchProcessing
@EnableIntegration
@SpringBootApplication(
        scanBasePackages = "br.com.elo.integrator")
@Import({
        PropertiesConfiguration.class})
public class CpfIntegratorApplication extends JobApplication {

    public static void main(String[] args) {
        SpringApplication.run(CpfIntegratorApplication.class, args);
    }

    @Bean
    public JndiObjectFactoryBean dataSource(@Value("${com.bnpparibas.cardif.pims.job.closeForAgeLegacy.datasource}") String jdbcJobCloseForAge) {
        final JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName(jdbcJobCloseForAge);
        return jndiObjectFactoryBean;
    }

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder applicationBuilder) {
        return applicationBuilder.sources(CpfIntegratorApplication.class);
    }
}
