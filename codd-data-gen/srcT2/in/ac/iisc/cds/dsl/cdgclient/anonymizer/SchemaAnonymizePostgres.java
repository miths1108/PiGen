package in.ac.iisc.cds.dsl.cdgclient.anonymizer;

import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig.Key;
import in.ac.iisc.cds.dsl.cdgvendor.model.SchemaInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.TableInfo;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import in.ac.iisc.cds.dsl.cdgvendor.utils.FileUtils;

/**
 * Reads schemna and anonymizes it while creating anonymizer
 * @author raghav
 *
 */
public class SchemaAnonymizePostgres extends SchemaAnonymize {

    @Override
    public Anonymizer anonymizeSchema() {

        DebugHelper.printInfo("-------- SchemaAnonymize with Postgres ------------");

        //Step 1: Read tables and columns names
        SchemaInfo schemaInfo = PostgresCConfig.BASICSCHEMA_INFO;
        Map<String, TableInfo> tableInfoMap = schemaInfo.getTableInfos();

        //Step 2: Do anonymization of schema
        Anonymizer anonymizer = new Anonymizer(tableInfoMap);
        tableInfoMap = anonymizer.getAnonymTableInfoMap();

        //Step 3: Dump the infos
        /*
        {
            'ENCRstore_sales': {
                'columns': ['ENCRss_sold_time_sk', 'ENCRss_ticket_number', ... ],
                'pkeys': ['ENCRss_sold_time_sk', 'ENCRss_ticket_number'],
                'fkeytables': ['ENCRweb_sales', 'ENCRcustomer_address', ...],
                'rowcount': 287997024
                }
            ...
        }
        */
        JSONObject result = new JSONObject();
        for (Entry<String, TableInfo> entry : tableInfoMap.entrySet()) {
            String tablename = entry.getKey();
            TableInfo tinfo = entry.getValue();
            result.put(tablename, new JSONObject(tinfo));
        }
        FileUtils.writeStringToFile(PostgresCConfig.getProp(Key.ANONYMIZEDSCHEMA_TARGETFILE), result.toString());
        

        DebugHelper.printInfo("-------- Done SchemaAnonymize with Postgres ------------");
        return anonymizer;
    }

}
