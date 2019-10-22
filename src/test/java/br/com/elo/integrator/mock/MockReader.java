package br.com.elo.integrator.mock;

import com.bnpparibas.cardif.contractservice.dto.job.policyclose.PolicyDTO;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;

import java.util.LinkedList;
import java.util.List;

public class MockReader implements ItemReader<PolicyDTO>, ItemStream {

    private static final String CURRENT_INDEX = "current.index";

    private static List<PolicyDTO> items = new LinkedList<>();

    private int currentIndex = 0;

    public MockReader() {
    }

    @Override
    public PolicyDTO read() throws Exception {
        if (currentIndex < items.size()) {
            return items.get(currentIndex++);
        }
        return null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (executionContext.containsKey(CURRENT_INDEX)) {
            currentIndex = executionContext.getInt(CURRENT_INDEX);
        } else {
            currentIndex = 0;
        }
    }

    @Override
    public void close() throws ItemStreamException {
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(CURRENT_INDEX, currentIndex);
    }

    public static void addItem(final PolicyDTO item) {
        MockReader.items.add(item);
    }
}
