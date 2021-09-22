package in.ac.iisc.cds.dsl.cdgclient.preprocess;

import java.util.List;
import java.util.Map;

import in.ac.iisc.cds.dsl.cdgvendor.model.Alqp;
import in.ac.iisc.cds.dsl.cdgvendor.model.TableInfo;

/**
* Reads the ExplainAnalyze jsons and creates ALQPs using PostgresParser
* @author raghav
*
*/
public abstract class ExplainAnalyzeToAlqp {

    /**
     * Expects 2 corresponding lists with execution plans and their query names (like [query1.sql, query2.sql, ... ])
     * @param eas
     * @param queryNames
     * @param tableInfoMap 
     */
    public abstract List<Alqp> createAllAlqp(List<String> eas, List<String> queryNames, Map<String, TableInfo> tableInfoMap);
}
