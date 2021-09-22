package in.ac.iisc.cds.dsl.cdgclient.preprocess;

import java.util.List;

/**
 * Sends the sql queries to fetch the explain analyse jsons
 * @author raghav
 *
 */
public abstract class QueryToExplainAnalyze {

    /**
     * Expects 2 corresponding lists with queries and their names (like [query1.sql, query2.sql, ... ])
     * @param queries
     * @param queryNames
     */
    public abstract void explainAnalyzeQueries(List<String> queries, List<String> queryNames);
}