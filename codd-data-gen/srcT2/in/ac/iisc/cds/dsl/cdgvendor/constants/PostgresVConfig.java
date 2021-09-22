package in.ac.iisc.cds.dsl.cdgvendor.constants;

import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONObject;

import in.ac.iisc.cds.dsl.cdgvendor.model.CCInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ColumnPathInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.SchemaInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.TableInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewInfo;
import in.ac.iisc.cds.dsl.cdgvendor.utils.ConfigProvider;
import in.ac.iisc.cds.dsl.cdgvendor.utils.FileUtils;
import in.ac.iisc.cds.dsl.cdgvendor.utils.SerializationHelper;

// newViewNonKeys : [t10_c001, t09_c002, t10_c002, t09_c004, t09_c000]
// newViewNonKeys : [t10_c001, t10_c002, t09_c004, t09_c002_3617663, t09_c000]

/**
 * PostgresVendorConfig
 * @author dsladmin
 *
 */
public class PostgresVConfig {

    public static final Charset     CHARSET               = Charset.forName("UTF-8");

    private static final String     DEFAULTPROPS_FILENAME = "cdgvendor/postgres.properties";
    private static Map<Key, String> PROPS_MAP             = null;

    public static void initDefaultConfig() {
        PROPS_MAP = new HashMap<>();
        Map<String, String> temp = ConfigProvider.INSTANCE.getPropsFromFile(PostgresVConfig.DEFAULTPROPS_FILENAME);
        for (Key key : Key.values()) {
            PROPS_MAP.put(key, temp.get(key.configFileKey));
        }
    }

    public static void overlayOnDefaultConfig(Map<Key, String> overlayConfig) {
        for (Entry<Key, String> entry : overlayConfig.entrySet()) {
            PROPS_MAP.put(entry.getKey(), entry.getValue());
        }
    }
    
    public enum LPSolvingType {
    	ALL,
    	SINGLE;
    }

    public enum Key {

        ANONYMIZEDCCS_INFO("postgres.anonymizedcc.info"),
        ANONYMIZEDSCHEMA_FILENAME("postgres.anonymizedschema.info"),

        DATABASESUMMARY_LOCATION("postgres.databasesummary.location"),
        DATABASESTATICDUMP_LOCATION("postgres.databasesummary.staticdumplocation"),
    	
        // added for columnIDMap
    	COLUMNIDMAP_LOCATION("postgres.columnidmap.targetlocation");

        String configFileKey;

        private Key(String configFileKey) {
            this.configFileKey = configFileKey;
        }
    }

    public static String getProp(Key key) {
        return PROPS_MAP.get(key);
    }

    public static final String DATABASESUMMARY_INDEX = "index";

    public static CCInfo       CC_INFO               = null;

    public static void loadCCInfo() {
        if (CC_INFO == null) {
            CC_INFO = SerializationHelper.deserializeCCInfo(getProp(Key.ANONYMIZEDCCS_INFO));
        }
    }

    public static Map<String, ViewInfo> ANONYMIZED_VIEWINFOs = null;
    public static List<String>          VIEWNAMES_TOPO       = null;
    public static List<String>          VIEWNAMES_REVTOPO    = null;
    public static SchemaInfo			ANONYMIZED_SCHEMAINFO = null;
    // adding COLUMN_ID_MAP for column identifier mapping to colname and colPath
    public static Map<String,ColumnPathInfo>   COLUMN_ID_MAP = null; // Comes from client side, maps column id with (colName and path of it)
    public static Map<String,List<String>> ColNameToColumnIDsMap = null;  // t0_c023 : [ t0_c023_123, t0_c023_432]
    
    public static void loadColumnIdMap() {
    	
    	if(COLUMN_ID_MAP == null) {
    		
    		try {
    			 
    			JSONObject obj = new JSONObject(FileUtils.readFileToString(getProp(Key.COLUMNIDMAP_LOCATION)));
    			COLUMN_ID_MAP = new HashMap<String,ColumnPathInfo>();
    			ColNameToColumnIDsMap = new HashMap<String,List<String>>();
    			for (String key : obj.keySet()) {
    				
    				// String manipulation is done here, 
    				// will need to change if toString() function of columnPath is changed
    				String tempStr = obj.getString(key);  // tempStr = (Colname, Colpath)
    				String replace1 = tempStr.replace('(',' ').strip();
    				String replace2 = replace1.replace(')',' ').strip();
    				String[] tempList = replace2.split(": ");
    				String colName = tempList[0];
    				String pathStr = tempList[1].replace('[',' ').strip();
    				String pathStr1 = pathStr.replace(']',' ').strip();
    				
    				String[] colPath = pathStr1.split(", "); 
    				List<String> colPaths = Arrays.asList(colPath);
    				
    				ColumnPathInfo cp = new ColumnPathInfo(colName, colPaths);
    				
    				if(ColNameToColumnIDsMap.containsKey(colName))
    				{
    					ColNameToColumnIDsMap.get(colName).add(key);
    				}
    				else
    				{
    					List<String> columnIdList =  new ArrayList<>();
    					columnIdList.add(key);
    					ColNameToColumnIDsMap.put(colName, columnIdList);
    				}
    				
    				COLUMN_ID_MAP.put(key,cp);
    			}
    			
    			System.out.println("COLUMN_ID_MAP : " + COLUMN_ID_MAP);
    			
    			
    			 
    		} catch (Exception ex) {
    			System.out.print("Exception : " +  ex);;
                throw new ExceptionInInitializerError(ex);
            }
    		
    	}
    	//System.out.println("ColNameToColumn : " + ColNameToColumnIDsMap);
    }

    public static void loadAnonymizedViewInfos() {
        if (ANONYMIZED_VIEWINFOs == null) {
            try {
                JSONObject obj = new JSONObject(FileUtils.readFileToString(getProp(Key.ANONYMIZEDSCHEMA_FILENAME)));
                
                ANONYMIZED_SCHEMAINFO = new SchemaInfo();
                for (String key : obj.keySet()) {
                	obj.getJSONObject(key);
                	ANONYMIZED_SCHEMAINFO.putTableInfo(key, new TableInfo(obj.getJSONObject(key)));
                }
                ANONYMIZED_SCHEMAINFO.validate();

                //TODO: Use some ViewInfo and ViewGen instead of SchemaInfo and TableInfo
                //Populate nonKeys attributes bottom-up
                
                VIEWNAMES_REVTOPO = getRevTopoList(ANONYMIZED_SCHEMAINFO.getTableInfos());    // Return in descending order of topological number 
                //System.out.println("tarun : " + VIEWNAMES_REVTOPO);
                VIEWNAMES_TOPO = new ArrayList<>(VIEWNAMES_REVTOPO);
                Collections.reverse(VIEWNAMES_TOPO);  // VIEWNAMES_TOPO is now in ascending order of topological number 

                int k = -1;
                ANONYMIZED_VIEWINFOs = new LinkedHashMap<>();
                for (String tablename : VIEWNAMES_REVTOPO) {
                    TableInfo tableInfo = ANONYMIZED_SCHEMAINFO.getTableInfo(tablename);

                    Set<String> tableNonkeys = new HashSet<>();
                    tableNonkeys.addAll(tableInfo.columnsNames());
                    tableNonkeys.removeAll(tableInfo.getKeys());

                    Set<String> viewNonkeys = new HashSet<>();
                    viewNonkeys.addAll(tableNonkeys);
                    for (String immediateTarget : tableInfo.getFkeyTables()) {
                        viewNonkeys.addAll(ANONYMIZED_VIEWINFOs.get(immediateTarget).getViewNonkeys());
                    }
                    ViewInfo viewInfo = new ViewInfo(++k, tableInfo.getRowcount(), tableInfo.getFkeyTables());
                    viewInfo.getTableNonkeys().addAll(tableNonkeys);
                    viewInfo.getViewNonkeys().addAll(viewNonkeys);
                    ANONYMIZED_VIEWINFOs.put(tablename, viewInfo);
                }
                

                //Setting isNeverFKeyed field in viewInfo
                for (String tablename : VIEWNAMES_REVTOPO) {
                    ViewInfo viewInfo = ANONYMIZED_VIEWINFOs.get(tablename);
                    viewInfo.setIsNeverFKeyed(true);
                }
                for (String tablename : VIEWNAMES_REVTOPO) {
                    ViewInfo viewInfo = ANONYMIZED_VIEWINFOs.get(tablename);
                    for (String fkeyedViewname : viewInfo.getFkeyViews()) {
                        ViewInfo fkeyedViewInfo = ANONYMIZED_VIEWINFOs.get(fkeyedViewname);
                        fkeyedViewInfo.setIsNeverFKeyed(false);
                    }
                }
                
                
                
                // View A(a,b,c)  if attribute b have two path then  View A(a,b1,b2,c) 
                // Replacing some column name with columnids associated with it  in ViewInfo.viewNonKeys
                // Reason of replacement is different column path could be there for same column name,
                // So, to have different columns(columnIDs) for the column coming from different paths.
                //System.out.println("ColNameToColumnIDsMap : " + PostgresVConfig.ColNameToColumnIDsMap);
//                for(String tableName : VIEWNAMES_REVTOPO)
//                {
//                	ViewInfo viewInfo = ANONYMIZED_VIEWINFOs.get(tableName);
//                	Set<String> viewNonKeys = viewInfo.getViewNonkeys();
//                	Set<String> newViewNonKeys = new HashSet<>(viewNonKeys);
//                	//System.out.println("newViewNonKeys : " + newViewNonKeys);
//                	for(String colName : viewNonKeys)
//                	{
//                		if(!PostgresVConfig.ColNameToColumnIDsMap.containsKey(colName))
//                		{
//                			continue;
//                		}
//                		List<String> columnIdList = PostgresVConfig.ColNameToColumnIDsMap.get(colName);
//                		
//                		for(String columnId : columnIdList)
//                		{
//                			String targetView = PostgresVConfig.COLUMN_ID_MAP.get(columnId).getTargetView();
//                			
//                			if(targetView.contentEquals(tableName))
//                			{
//                				newViewNonKeys.remove(colName);
//                				newViewNonKeys.add(columnId);
//                			}
//                		}
//                		
//                	}
//                	                	
//                	// Assigning new viewNonKeys
//                	viewInfo.getViewNonkeys().clear();
//                	viewInfo.getViewNonkeys().addAll(newViewNonKeys);
//                	
//                	
//                
//                }
                
                
                
                
                
            } catch (Exception ex) {
            	System.out.println(ex);
                throw new ExceptionInInitializerError(ex);
            }
        }
        
        //checkPath();
    }
    
    private static void checkPath()
    {
    	// ColNameToColumnIDsMap
    	// COLUMN_ID_MAP
    	for(Entry<String,List<String>> col : ColNameToColumnIDsMap.entrySet())
    	{
    		String colname = col.getKey();
    		List<String> colIdList = col.getValue();
    		
    		for(int i=0; i < colIdList.size(); i++ )
    		{
    			ColumnPathInfo c = COLUMN_ID_MAP.get(colIdList.get(i));
    			if(c.getTargetView().contentEquals("t03"))
    			{
    				System.out.print("  colname: " +  colname);
    				System.out.print("  source: " +  c.getColPath().get(0));
    				System.out.print("  Path: " +  c.getColPath());
    				System.out.println("   target :" + c.getTargetView());
    			}
    			
    			
    		}
    		
    	}
    	
    }

    private static List<String> getRevTopoList(Map<String, TableInfo> tableInfos) {

        Map<String, Boolean> done = new HashMap<>();

        for (Entry<String, TableInfo> entry : tableInfos.entrySet()) {
            String tablename = entry.getKey();
            done.put(tablename, false);
        }

        List<String> revTopoList = new ArrayList<>();
        int max;
        while (true) {
            max = Integer.MIN_VALUE;
            String toAddTablename = null;
            for (Entry<String, TableInfo> entry : tableInfos.entrySet()) {
                String tablename = entry.getKey();
                TableInfo tableInfo = entry.getValue();

                if (!done.get(tablename) && tableInfo.getTopoSeqno() > max) {
                    max = tableInfo.getTopoSeqno();
                    toAddTablename = tablename;
                }
            }
            if (max == Integer.MIN_VALUE) {
                break;
            }
            revTopoList.add(toAddTablename);
            done.put(toAddTablename, true);
        }
        return revTopoList;
    }

}
