package br.com.elo.integrator.step;

import br.com.elo.integrator.AbstractIntegratorReader;
import br.com.elo.integrator.IntegratorSqlPagingQueryProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@StepScope
@Slf4j
@Profile("!test")
public class CpfIntegratorReader extends AbstractIntegratorReader {


        // Aqui vc vai colocar a consulta SQL

        private final String SQL_SELECT = "WITH " +
                "  tab_benef_BIRTHDATE AS ( " +
                "   SELECT NP.BIRTHDATEINPUT, PVI.AGREGATEDPARENTID AS AGREGATEDINSURANCEOBJECTID" +
                "   FROM PARTICIPATIONVERSIONINSURANCE PVI, NATURALPERSON NP" +
                "   WHERE PVI.ROL_ID = 110" +
                "   AND NP.STATIC = PVI.THIRDPARTYID" +
                "   ), " +
                "  tab_max_coverage AS ( " +
                "   SELECT COV.INSUREDRENEWALMAXAGEINPUT, ECOV.AGREGATEDINSURANCEOBJECTID" +
                "   FROM EVALUATEDCOVERAGE ECOV, CCOXTPCOVERAGE COV" +
                "   WHERE COV.STATIC = ECOV.EVALUATEDCOVERAGEID" +
                "  ) " +
                "  SELECT DISTINCT AP.AGREGATEDPOLICYID AS policyId, PDCO.POD_POLICYNUMBER policyNumber ";

        private final String SQL_FROM = " FROM AGREGATEDPOLICY AP " +
                "  INNER JOIN POLICYDCO PDCO " +
                "  ON PDCO.AGREGATEDOBJECTID = AP.AGREGATEDPOLICYID AND PDCO.OPERATIONPK = AP.OPERATIONPK " +
                "  INNER JOIN CCOPTPPOLICY CCPO " +
                "  ON CCPO.PK = PDCO.DCOID " +
                "  INNER JOIN AGREGATEDRISKUNIT ARU" +
                "  ON ARU.AGREGATEDPOLICYID = AP.AGREGATEDPOLICYID" +
                "  INNER JOIN AGREGATEDINSURANCEOBJECT AIO " +
                "  ON AIO.AGREGATEDRISKUNITID = ARU.AGREGATEDRISKUNITID" +
                "  INNER JOIN tab_benef_BIRTHDATE b " +
                "  ON b.AGREGATEDINSURANCEOBJECTID = AIO.AGREGATEDINSURANCEOBJECTID " +
                "  INNER JOIN tab_max_coverage c " +
                "  ON c.AGREGATEDINSURANCEOBJECTID = AIO.AGREGATEDINSURANCEOBJECTID  ";

        private final String SQL_WHERE = "WHERE AP.PRODUCT_NAME IS NOT NULL" +
                "  AND AP.PRODUCT_NAME = :product" +
                "  AND PDCO.status =  2" +
                "  AND PDCO.policy_state = 8" +
                "  AND PDCO.FINISHDATE >= :initial" +
                "  AND PDCO.FINISHDATE <= :final" +
                "  AND CCPO.PK = PDCO.DCOID";


        @Override
        protected PagingQueryProvider createQueryProvider() {
            final IntegratorSqlPagingQueryProvider queryProvider = new IntegratorSqlPagingQueryProvider();
            queryProvider.setSelectClause(SQL_SELECT);
            queryProvider.setFromClause(SQL_FROM);
            queryProvider.setWhereClause(SQL_WHERE);

            return queryProvider;
        }

}
