package in.ac.iisc.cds.dsl.cdgclient.preprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig.Key;
import in.ac.iisc.cds.dsl.cdgclient.dao.PostgressDao;
import in.ac.iisc.cds.dsl.cdgvendor.model.ColumnInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.TableInfo;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import in.ac.iisc.cds.dsl.cdgvendor.utils.FileUtils;
import in.ac.iisc.cds.dsl.cdgvendor.utils.Pair;

/**
 * Sends the sql queries to fetch the basic schema info
 * @author raghav
 *
 */
public class SchemaAnalyzePostgres extends SchemaAnalyze {

    @Override
    public Map<String, TableInfo> explainSchema() {

        DebugHelper.printInfo("-------- SchemaAnalyze with Postgres ------------");

        //Step 1: Read tables and columns names
        List<PostgressDao.SchemaInfoResultSetRow> schemaInfo;

        String sqlQuery = FileUtils.readFileToString(PostgresCConfig.getProp(Key.MQ_BASICSCHEMA_SCHEMAQUERY));
        sqlQuery = constructQueryFromTemplate(sqlQuery, PostgresCConfig.PH_BASICSCHEMA_DBNAME, PostgresCConfig.getProp(Key.BASICSCHEMA_DBNAME));
        sqlQuery = constructQueryFromTemplate(sqlQuery, PostgresCConfig.PH_BASICSCHEMA_SCHEMANAME, PostgresCConfig.getProp(Key.BASICSCHEMA_SCHEMANAME));
        
        schemaInfo = PostgressDao.execSchemaQuery(sqlQuery);

        Map<String, TableInfo> tableInfoMap = new HashMap<>();
        for (PostgressDao.SchemaInfoResultSetRow sinfo : schemaInfo) {
            TableInfo tinfo = tableInfoMap.get(sinfo.tablename);
            if (tinfo == null) {
                tinfo = new TableInfo();
                tableInfoMap.put(sinfo.tablename, tinfo);
            }
            tinfo.getColumns().put(sinfo.columnname, new ColumnInfo(sinfo.ordinalPosition , sinfo.columntype));
//            tinfo.getColumns().add(sinfo.columnname);
            if (sinfo.is_key) {
                tinfo.getKeys().add(sinfo.columnname);
            }
        }

        //Step 2: Read fkeyTables
        sqlQuery = FileUtils.readFileToString(PostgresCConfig.getProp(Key.MQ_BASICSCHEMA_FKEYSQUERY));
        sqlQuery = constructQueryFromTemplate(sqlQuery, PostgresCConfig.PH_BASICSCHEMA_DBNAME, PostgresCConfig.getProp(Key.BASICSCHEMA_DBNAME));
        sqlQuery = constructQueryFromTemplate(sqlQuery, PostgresCConfig.PH_BASICSCHEMA_SCHEMANAME, PostgresCConfig.getProp(Key.BASICSCHEMA_SCHEMANAME));
       
        Map<String, Map<String, List<String>>> mapSorceTableToTargetTableToSourceColumns = PostgressDao.execFkeyTablesQuery(sqlQuery);

        for (Entry<String, TableInfo> entry : tableInfoMap.entrySet()) {
            String tablename = entry.getKey();
            TableInfo tinfo = entry.getValue();
            if (mapSorceTableToTargetTableToSourceColumns.get(tablename) != null) {
                Map<String, List<String>> mapTargetTableToSourceColumns = mapSorceTableToTargetTableToSourceColumns.get(tablename);
                tinfo.setFkTargetTableToSourceColumns(mapTargetTableToSourceColumns);
                tinfo.getFkeyTables().addAll(mapTargetTableToSourceColumns.keySet());
                
            }
        }

        //Step 3: For each table, get rowcount
        for (Entry<String, TableInfo> entry : tableInfoMap.entrySet()) {
            String tablename = entry.getKey();
            TableInfo tinfo = entry.getValue();

            sqlQuery = FileUtils.readFileToString(PostgresCConfig.getProp(Key.MQ_BASICSCHEMA_ROWCOUNTQUERY));
            sqlQuery = constructQueryFromTemplate(sqlQuery, PostgresCConfig.PH_BASICSCHEMA_TABLENAME, tablename);

            tinfo.setRowcount(PostgressDao.execRowCountQuery(sqlQuery));
        }
        
//        Step 4: Get primary keys
        sqlQuery = FileUtils.readFileToString(PostgresCConfig.getProp(Key.MQ_BASICSCHEMA_PKQUERY));
        
        for (Pair<String, String> entry : PostgressDao.execPrimaryKeyQuery(sqlQuery)) {
            String tablename = entry.getFirst();
            String pkName = entry.getSecond();
            tableInfoMap.get(tablename).pkList().add(pkName);
        }
        DebugHelper.printAllTableInfos(tableInfoMap);

        //        Step 4: For domained columns
        //        Map<String, List<String>> domainvalues = new HashMap<>();
        //        List<String> domainedcolumns = FileUtils.readLines(PostgresConstants.getProp(PostgresConstants.BASICSCHEMA_DOMAINEDCOLUMNS));
        //        for (String domaincolumn : domainedcolumns) {
        //            String tablename = domaincolumn.split("#")[0];
        //            String columnname = domaincolumn.split("#")[1];
        //            sqlQuery = FileUtils.readFileToString(PostgresConstants.getProp(PostgresConstants.BASICSCHEMA_DOMAINEDQUERY));
        //            sqlQuery = constructQueryFromTemplate(sqlQuery, PostgresConstants.BASICSCHEMA_DOMAINEDCOLUMNNAME, columnname);
        //            sqlQuery = constructQueryFromTemplate(sqlQuery, PostgresConstants.BASICSCHEMA_DOMAINEDTABLENAME, tablename);
        //
        //            domainvalues.put(columnname, PostgressDao.execDomainedQuery(sqlQuery));
        //        }
        //        result = new JSONObject();
        //        for (Entry<String, List<String>> entry : domainvalues.entrySet()) {
        //            String columnname = entry.getKey();
        //            List<String> values = entry.getValue();
        //            result.put(columnname, new JSONArray(values));
        //        }
        //        FileUtils.writeStringToFile(PostgresConstants.getProp(PostgresConstants.BASICSCHEMA_DOMAINEDTARGET), result.toString());

        //Step 4: Dump the summary
        /*
        {
            'store_sales': {
                'columns': ['ss_sold_time_sk', 'ss_ticket_number', ... ],
                'pkeys': ['ss_sold_time_sk', 'ss_ticket_number'],
                'fkeytables': ['web_sales', 'customer_address', ...],
                'rowcount': 287997024
                }
            ...generateEAsPostgres();
        }
        */

        JSONObject result = new JSONObject();
        for (Entry<String, TableInfo> entry : tableInfoMap.entrySet()) {
            String tablename = entry.getKey();
            TableInfo tinfo = entry.getValue();
            result.put(tablename, new JSONObject(tinfo));
        }
        FileUtils.writeStringToFile(PostgresCConfig.getProp(Key.BASICSCHEMA_TARGETFILE), result.toString());

        DebugHelper.printInfo("-------- Done SchemaAnalyze with Postgres ------------");

        return tableInfoMap;
    }

    private static String constructQueryFromTemplate(String queryTemplate, String key, String replacement) {
        return queryTemplate.replace("<" + key + ">", replacement);
    }

}