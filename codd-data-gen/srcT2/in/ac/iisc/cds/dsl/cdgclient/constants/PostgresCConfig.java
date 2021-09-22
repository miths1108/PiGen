package in.ac.iisc.cds.dsl.cdgclient.constants;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import in.ac.iisc.cds.dsl.cdgvendor.model.SchemaInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.TableInfo;
import in.ac.iisc.cds.dsl.cdgvendor.utils.ConfigProvider;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import in.ac.iisc.cds.dsl.cdgvendor.utils.FileUtils;

/**
 * PostgresClientConfig
 * @author dsladmin
 *
 */
public class PostgresCConfig {

    public static final Charset     CHARSET               = Charset.forName("UTF-8");

    private static final String     DEFAULTPROPS_FILENAME = "cdgclient/postgres.properties";
    private static Map<Key, String> PROPS_MAP             = null;

    public static void initDefaultConfig() {
        PROPS_MAP = new HashMap<>();
        Map<String, String> temp = ConfigProvider.INSTANCE.getPropsFromFile(PostgresCConfig.DEFAULTPROPS_FILENAME);
        for (Key key : Key.values()) {
            PROPS_MAP.put(key, temp.get(key.configFileKey));
        }
    }

    public static void overlayOnDefaultConfig(Map<Key, String> overlayConfig) {
        for (Entry<Key, String> entry : overlayConfig.entrySet()) {
            PROPS_MAP.put(entry.getKey(), entry.getValue());
        }
    }

    public enum Key {

        //Conn details
        CONN_DRIVERCLASS("postgres.conn.driverclass"),
        CONN_CONNSTRING("postgres.conn.connstring"),
        CONN_USERNAME("postgres.conn.username"),
        CONN_PASSWD("postgres.conn.passwd"),

        BASICSCHEMA_DBNAME("postgres.basicschema.dbname"),
        BASICSCHEMA_SCHEMANAME("postgres.basicschema.schemaname"),

        //Locations
        SQLQUERIES_LOCATION("postgres.sqlquery.location"),
        EXPANALYZE_LOCATION("postgres.explainanalyze.location"),

        //Target files
        BASICSCHEMA_TARGETFILE("postgres.basicschema.targetfile"),

        DDLGENERATED_TARGETFILE("postgres.ddlgenerated.targetfile"),

        ANONYMIZEDSCHEMA_TARGETFILE("postgres.anonymizedschema.targetfile"),
        ANONYMIZEDCCS_TARGETFILE("postgres.anonymizedccs.targetfile"),
        ANONYMIZEDQUERIES_TARGETLOCATION("postgres.anonymizedqueries.targetlocation"),
        COLUMNIDMAP_TARGETLOCATION("postgres.columnidmap.targetlocation"),

        //Metaqueries
        MQ_BASICSCHEMA_SCHEMAQUERY("postgres.basicschema.schemaquery"),
        MQ_BASICSCHEMA_FKEYSQUERY("postgres.basicschema.fkeysquery"),
        MQ_BASICSCHEMA_ROWCOUNTQUERY("postgres.basicschema.rowcountquery"),
        MQ_BASICSCHEMA_PKQUERY("postgres.basicschema.pkeysquery"),

        MQ_DDLGENERATED_CREATETABLEQUERY("postgres.ddlgenerated.createtablequery"),
        MQ_DDLGENERATED_ADDPKQUERY("postgres.ddlgenerated.altertableaddpkquery"),
        MQ_DDLGENERATED_ADDFKQUERY("postgres.ddlgenerated.altertableaddfkquery"),

        MQ_ANONYMIZEDQUERIES_SELECTSTARQUERY("postgres.anonymizedqueries.selectstarquery");

        String configFileKey;

        private Key(String configFileKey) {
            this.configFileKey = configFileKey;
        }
    }

    public static String getProp(Key key) {
        return PROPS_MAP.get(key);
    }

    //Hardcoded Constants
    public static final String SQLQUERIES_INDEX                 = "index";
    public static final String EXPANALYZE_INDEX                 = "index";
    public static final String SQLQUERIES_TERMINATOR            = ";";

    //Placeholders
    public static final String PH_BASICSCHEMA_DBNAME            = "postgres-basicschema-dbname";
    public static final String PH_BASICSCHEMA_SCHEMANAME        = "postgres-basicschema-schemaname";
    public static final String PH_BASICSCHEMA_TABLENAME         = "postgres-basicschema-tablename";

    public static final String PH_DDLGENERATED_TABLENAME        = "postgres-ddlgenerated-tablename";
    public static final String PH_DDLGENERATED_COLUMNLIST       = "postgres-ddlgenerated-columnlist";
    public static final String PH_DDLGENERATED_COLUMNNAME       = "postgres-ddlgenerated-columnname";
    public static final String PH_DDLGENERATED_FKCONSTRAINTNAME = "postgres-ddlgenerated-fkconstaintname";
    public static final String PH_DDLGENERATED_TABLENAMEP       = "postgres-ddlgenerated-tablenamep";
    public static final String PH_DDLGENERATED_TABLENAMEF       = "postgres-ddlgenerated-tablenamef";
    public static final String PH_DDLGENERATED_COLUMNNAMEP      = "postgres-ddlgenerated-columnnamep";
    public static final String PH_DDLGENERATED_COLUMNNAMEF      = "postgres-ddlgenerated-columnnamef";

    public static final String PH_ANONYMIZEDQUERIES_FROMLIST    = "postgres-anonymizedqueries-fromlist";
    public static final String PH_ANONYMIZEDQUERIES_WHERELIST   = "postgres-anonymizedqueries-wherelist";

    public static SchemaInfo   BASICSCHEMA_INFO                 = null;

    /*
     *  Some HardCoded Constants for On-Off feature
     *  
     *  options can be either [on/off]
     */
    public static final String HISTOGRAM_MAPPING = "off";  // to be kept as turn on when metadata is to be fetched
    public static final String ANONYMIZED_QUERY_GENERATION = "off"; // to be kept on when benchmark queries need to be anonymized and needed as a client output
    
    
    public static void loadBasicSchemaInfo() {
        if (BASICSCHEMA_INFO == null) {
            try {
                JSONObject obj = new JSONObject(FileUtils.readFileToString(getProp(Key.BASICSCHEMA_TARGETFILE)));
                BASICSCHEMA_INFO = new SchemaInfo();
                for (String key : obj.keySet()) {
                    BASICSCHEMA_INFO.putTableInfo(key, new TableInfo(obj.getJSONObject(key)));
                }
                BASICSCHEMA_INFO.validateAndInit();
                DebugHelper.printSchemaInfo(BASICSCHEMA_INFO);
            } catch (Exception ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }
    }

    public static void setBASICSCHEMA_INFO(SchemaInfo BASICSCHEMA_INFO) {
        PostgresCConfig.BASICSCHEMA_INFO = BASICSCHEMA_INFO;
    }

}
