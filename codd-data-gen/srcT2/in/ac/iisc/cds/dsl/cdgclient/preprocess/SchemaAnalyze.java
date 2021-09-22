package in.ac.iisc.cds.dsl.cdgclient.preprocess;

import java.util.Map;

import in.ac.iisc.cds.dsl.cdgvendor.model.TableInfo;

/**
 * Sends the sql queries to fetch the basic schema info
 * @author raghav
 *
 */
public abstract class SchemaAnalyze {

    /**
     * Stores to filesystem and returns TableInfoMap
     * @return
     */
    public abstract Map<String, TableInfo> explainSchema();
}