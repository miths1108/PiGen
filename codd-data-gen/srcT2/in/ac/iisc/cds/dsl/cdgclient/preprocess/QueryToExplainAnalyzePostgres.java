package in.ac.iisc.cds.dsl.cdgclient.preprocess;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig.Key;
import in.ac.iisc.cds.dsl.cdgclient.dao.PostgressDao;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import in.ac.iisc.cds.dsl.cdgvendor.utils.FileUtils;

/**
 * Sends the sql queries to postgres to fetch the explain analyse jsons
 * @author raghav
 *
 */
public class QueryToExplainAnalyzePostgres extends QueryToExplainAnalyze {

    private static final String NEWLINE = "\n";

    @Override
    public void explainAnalyzeQueries(List<String> sqlQueries, List<String> queryNames) {

        DebugHelper.printInfo("-------- ExplainAnalyzing " + sqlQueries.size() + " queries with Postgres ------------");
        Map<String, Object> resultStatus = new LinkedHashMap<>();
        int failCount = 0;
        for (int q = 0; q < sqlQueries.size(); q++) {
            String sqlQuery = sqlQueries.get(q);
            String queryName = queryNames.get(q);

            DebugHelper.printDebug("\tDoing query " + q);

            String ea;
            try {
                //Step 1: Read Query
                ea = fetchAlqpFromDB(sqlQuery);
            } catch (Exception ex) {
                failCount++;
                resultStatus.put(queryName, ex);
                ex.printStackTrace(); //TODO: do something with the stack trace
                continue;
            }

            //Step 2: Dump each query response to filesystem
            try {
                FileUtils.writeStringToFile(PostgresCConfig.getProp(Key.EXPANALYZE_LOCATION), getAsJsonFilename(queryName), ea.toString());
                resultStatus.put(queryName, ea);
            } catch (Exception ex) {
                failCount++;
                resultStatus.put(queryName, ex);
                ex.printStackTrace();
            }
        }
        DebugHelper.printInfo(
                "-------- Summarizing ExplainAnalyze results. SUCCESS: " + (sqlQueries.size() - failCount) + " FAILED: " + failCount + " ------------");

        if (failCount != 0)
            throw new RuntimeException("Not all queries SUCCEEDED");

        //Step 3: Dump the summary
        //        StringBuilder sb = new StringBuilder();
        //        for (Entry<String, Object> entry : resultStatus.entrySet()) {
        //            String jsonFilename = entry.getKey();
        //            Object status = entry.getValue();
        //
        //            if (status instanceof Throwable) {
        //                DebugHelper.printInfo("\tQuery " + jsonFilename + ": FAILED");
        //            } else if (status instanceof Alqp) {
        //                DebugHelper.printInfo("\tQuery " + jsonFilename + ": SUCCEEDED" + " " + status.toString());
        //                sb.append(jsonFilename).append(NEWLINE);
        //            } else {
        //                DebugHelper.printInfo("\tQuery " + jsonFilename + ": " + status.toString());
        //            }
        //        }
        //        String str = StringUtils.chomp(sb.toString());
        //        FileUtils.writeStringToFile(PostgresConstants.getProp(PostgresConstants.EXPANALYZE_LOCATION),
        //                PostgresConstants.getProp(PostgresConstants.EXPANALYZE_INDEX), str);

        DebugHelper.printInfo("-------- Done ExplainAnalyzing " + sqlQueries.size() + " queries with Postgres ------------");
    }

    public static String fetchAlqpFromDB(String sqlQuery) {
        sqlQuery = getAsExplainQuery(sqlQuery, PostgresCConfig.SQLQUERIES_TERMINATOR);
        String ea = PostgressDao.execExplainAnalyzeQuery(sqlQuery);
        return ea;
    }

    private static String getAsExplainQuery(String sqlQuery, String queryTerminator) {

        sqlQuery = sqlQuery.substring(0, sqlQuery.lastIndexOf(queryTerminator));
        if (sqlQuery.contains(queryTerminator))
            throw new RuntimeException("Found multiple Queries but expected one");

        return "EXPLAIN (ANALYZE, FORMAT JSON) " + sqlQuery;
        //return "EXPLAIN ANALYZE " + sqlQuery;
    }

    private static String getAsJsonFilename(String filename) {
        return filename.replaceAll(".sql", ".json");
    }
}