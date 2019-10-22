package br.com.elo.integrator;

import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.AbstractSqlPagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryUtils;

import java.util.Iterator;
import java.util.Map;

public class IntegratorSqlPagingQueryProvider extends AbstractSqlPagingQueryProvider {

    @Override
    public String generateFirstPageQuery(int pageSize) {
        String strQuery = SqlPagingQueryUtils.generateRowNumSqlQuery(this, false, this.buildRowNumClause(pageSize));
        return getQuery(strQuery);
    }

    private String getQuery(String strQuery){
        String keyWord = "(SELECT ";

        if(strQuery.toLowerCase().contains(keyWord.toLowerCase())){
            return strQuery.replace(keyWord,"(");
        }
        return strQuery;
    }


    @Override
    public String generateRemainingPagesQuery(int pageSize) {
        String strQuery = SqlPagingQueryUtils.generateRowNumSqlQuery(this, true, this.buildRowNumClause(pageSize));
        return getQuery(strQuery);
    }
    @Override
    public String generateJumpToItemQuery(int itemIndex, int pageSize) {
        int page = itemIndex / pageSize;
        int offset = page * pageSize;
        offset = offset == 0 ? 1 : offset;
        String sortKeySelect = this.getSortKeySelect();
        return SqlPagingQueryUtils.generateRowNumSqlQueryWithNesting(this, sortKeySelect, sortKeySelect, false, "TMP_ROW_NUM = " + offset);
    }

    private String getSortKeySelect() {
        StringBuilder sql = new StringBuilder();
        String prefix = "";
        Iterator var3 = this.getSortKeys().entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, Order> sortKey = (Map.Entry)var3.next();
            sql.append(prefix);
            prefix = ", ";
            sql.append((String)sortKey.getKey());
        }

        return sql.toString();
    }

    private String buildRowNumClause(int pageSize) {
        return "ROWNUM <= " + pageSize;
    }
}
