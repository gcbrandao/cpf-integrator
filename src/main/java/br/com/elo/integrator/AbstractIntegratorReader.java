package br.com.elo.integrator;

import br.com.elo.integrator.dto.CpfIntegratorDTO;
import br.com.elo.integrator.dto.ResultDTO;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

public abstract class AbstractIntegratorReader extends JdbcPagingItemReader<CpfIntegratorDTO> {

    @Autowired
    private DataSource dataSource;


    @Value("${job.max.page}")
    private int maxPage;

    private ResultDTO response;

    @PostConstruct
    public void initialize() {
        this.setPageSize(this.maxPage);
        final PagingQueryProvider queryProvider = createQueryProvider();
        this.setQueryProvider(queryProvider);
        this.setRowMapper(new BeanPropertyRowMapper<>(CpfIntegratorDTO.class));
        this.setDataSource(this.dataSource);
    }

    @BeforeStep
    public void beforeStep(final StepExecution stepExecution) {
    }


    @Override
    public CpfIntegratorDTO read() throws Exception {
        final CpfIntegratorDTO read = super.read();
        if (read != null) {
            //
        }
        return read;
    }

    protected abstract PagingQueryProvider createQueryProvider();

}
