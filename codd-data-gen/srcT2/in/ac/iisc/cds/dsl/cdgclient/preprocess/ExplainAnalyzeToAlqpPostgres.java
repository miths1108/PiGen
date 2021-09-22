package in.ac.iisc.cds.dsl.cdgclient.preprocess;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import in.ac.iisc.cds.dsl.cdgvendor.model.Alqp;
import in.ac.iisc.cds.dsl.cdgvendor.model.TableInfo;
import in.ac.iisc.cds.dsl.cdgvendor.parser.Parser;
import in.ac.iisc.cds.dsl.cdgvendor.parser.ParserPostgres;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;

/**
 * Reads the ExplainAnalyze jsons and creates ALQPs using PostgresParser
 * @author raghav
 *
 */
public class ExplainAnalyzeToAlqpPostgres extends ExplainAnalyzeToAlqp {

    @Override
    public List<Alqp> createAllAlqp(List<String> eas, List<String> queryNames, Map<String, TableInfo> tableInfoMap) {

        DebugHelper.printInfo("-------- Creating ALQPs from " + eas.size() + " Postgres EAs ------------");
        Map<String, Object> resultStatus = new LinkedHashMap<>();
        int failCount = 0;
        for (int e = 0; e < eas.size(); e++) {
            String ea = eas.get(e);
            String queryName = queryNames.get(e);

            DebugHelper.printDebug("\tDoing " + queryName);
            Alqp alqp;
            try {
                alqp = readAlqpFromString(ea, queryName, tableInfoMap);
            } catch (Exception ex) {
                failCount++;
                resultStatus.put(queryName, ex);
                ex.printStackTrace(); //TODO: do something with the stack trace
                continue;
            }
            resultStatus.put(queryName, alqp);
        }

        DebugHelper.printInfo("-------- Summarizing CreateALQPs. SUCCESS: " + (eas.size() - failCount) + " FAILED: " + failCount + " ------------");

        //Step 3: Dump the summary
        List<Alqp> alqps = new ArrayList<>();
        for (Entry<String, Object> entry : resultStatus.entrySet()) {
            String queryName = entry.getKey();
            Object status = entry.getValue();

            if (status instanceof Throwable) {
                DebugHelper.printInfo("\tEA " + queryName + ": FAILED");
            } else if (status instanceof Alqp) {
                DebugHelper.printInfo("\tEA " + queryName + ": SUCCEEDED");
                alqps.add((Alqp) status);
            } else {
                DebugHelper.printInfo("\tEA " + queryName + ": " + status.toString());
            }
        }

        if (failCount != 0)
            throw new RuntimeException("Not all queries SUCCEEDED");

        DebugHelper.printInfo("-------- Done Creating ALQPs from " + eas.size() + " Postgres EAs ------------");

        return alqps;
    }

    public static Alqp readAlqpFromString(String ea, String queryName, Map<String, TableInfo> tableInfoMap) {
        //Step 1: parse EA and create Alqp
        Parser parser = new ParserPostgres();
        Alqp alqp = parser.parse(ea);
        DebugHelper.printDebug("\n\nALQP " + queryName + ": ");
        DebugHelper.printDebug(alqp.toString());

        //Step 2: transform
        //        NestedLoopsTransformer.transformIfApplicable(alqp.getRoot());
        //        DebugHelper.printDebug("\nALQP after Transform: ");
        //        DebugHelper.printDebug(alqp.toString());

        //Step 3: compress
        alqp.compress();
        DebugHelper.printDebug("\nALQP after Compress: ");
        DebugHelper.printDebug(alqp.toString());

        DebugHelper.printDebug("\nALQP conditions: ");
        DebugHelper.printConditions(alqp.getAllConditions(tableInfoMap));

        return alqp;
    }
}
