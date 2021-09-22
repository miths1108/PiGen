package in.ac.iisc.cds.dsl.cdgclient.anonymizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig.Key;
import in.ac.iisc.cds.dsl.cdgvendor.model.Alqp;
import in.ac.iisc.cds.dsl.cdgvendor.model.ColumnInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ColumnPathInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.HistogramMappingInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.RelnameAlias;
import in.ac.iisc.cds.dsl.cdgvendor.model.TableInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAnd;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAndAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionBaseAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionComposite;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionOr;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionOrAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimple;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleDate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleDateAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleInt;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleIntAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleJoin;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleNumber;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleNumberAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleString;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleStringAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.utils.Converter;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import in.ac.iisc.cds.dsl.cdgvendor.utils.FileUtils;
import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * ASSUMPTION: Columnnames are unique in the schema
 * @author raghav
 *
 */

public class Anonymizer {

    private static final String          NEWLINE      = "\n";
    private static final String          TAB          = "    ";
    private static final String          COMMANEWLINE = "," + NEWLINE + TAB;
    private static final String          NEWLINEAND   = NEWLINE + TAB + "AND ";

    private final Map<String, TableInfo> origTableInfoMap;
    private Map<String, String>          tablenameAnonymMap;                   //old tablename to anonymized tablename
    private Map<String, String>          columnnameAnonymMap;                  //old columnname to anonymized columnname
    private Map<String, TableInfo>       anonymTableInfoMap;                   //anonymized tablename to anonymized table info
    /*
    public class ColumnPathInfo {
    	String colName;			// anonymized column name
    	List<String> colPath;  // list of relations
    	
    	ColumnPathInfo(String colName, List<String> colPath)
    	{
    		this.colName = colName;
    		this.colPath = colPath;
    	}
    }
    public Map<String, ColumnPathInfo>     columnPathInfoMap;  // key : anonymizedcolumnname + hashcode   value: object of columnpath  --> added by tarun
	*/
    /************************************************************
     * Constructor with initializers
     ************************************************************/

    public Anonymizer(Map<String, TableInfo> tableInfoMap) {

        origTableInfoMap = tableInfoMap;
        initTablenameAnonymMap();
        initColumnnameAnonymMap();
        initAnonymTableInfoMap();
       // columnPathInfoMap = new HashMap<>();
    }

    private void initTablenameAnonymMap() {
        Set<String> tablenames = origTableInfoMap.keySet();
        List<String> tablenameList = new ArrayList<>(tablenames);
        Collections.sort(tablenameList);

        tablenameAnonymMap = new HashMap<>();
        for (int i = 0; i < tablenameList.size(); i++) {
            String tablename = tablenameList.get(i);
            String anonymTablename = "t" + String.format("%02d", i);
            //anonymTablename += "_" + tablename; //TODO: remove this line

            tablenameAnonymMap.put(tablename, anonymTablename);
            DebugHelper.printInfo("tablename: " + tablename + " codedname: " + anonymTablename);
        }
    }

    private void initColumnnameAnonymMap() {

        columnnameAnonymMap = new HashMap<>();
        for (Entry<String, TableInfo> entry : origTableInfoMap.entrySet()) {
            String tablename = entry.getKey();
            TableInfo tinfo = entry.getValue();

            //columns
            List<String> columns = new ArrayList<>(tinfo.columnsNames());
            Collections.sort(columns);
            String anonymTablename = tablenameAnonymMap.get(tablename);
            for (int i = 0; i < columns.size(); i++) {
                String columnname = columns.get(i);
                String anonymColumnname = anonymTablename + "_" + "c" + String.format("%03d", i);
                //anonymColumnname += "_" + columnname; //TODO: remove this line

                columnnameAnonymMap.put(columnname, anonymColumnname);
                DebugHelper.printInfo("columnname: " + columnname + " codedname: " + anonymColumnname);

            }
        }
    }

    private void initAnonymTableInfoMap() {

        anonymTableInfoMap = new HashMap<>();
        for (Entry<String, TableInfo> entry : origTableInfoMap.entrySet()) {
            String tablename = entry.getKey();
            TableInfo tinfo = entry.getValue();

            String anonymTablename = tablenameAnonymMap.get(tablename);
            TableInfo anonymTinfo = new TableInfo();

            //revTopoSeqno
            anonymTinfo.setTopoSeqno(tinfo.getTopoSeqno());

            //columns
            for (Entry<String, ColumnInfo> column : tinfo.getColumns().entrySet()) {
                String anonymColumn = columnnameAnonymMap.get(column.getKey());
//                anonymTinfo.getColumns().add(anonymColumn);
                anonymTinfo.getColumns().put(anonymColumn, column.getValue());
            }

            //keys
            for (String key : tinfo.getKeys()) {
                String anonymKey = columnnameAnonymMap.get(key);
                anonymTinfo.getKeys().add(anonymKey);
            }

            //fkeyTables
            for (String fkeyTable : tinfo.getFkeyTables()) {
                String anonymFKeyTable = tablenameAnonymMap.get(fkeyTable);
                anonymTinfo.getFkeyTables().add(anonymFKeyTable);
            }

            //rowcount
            anonymTinfo.setRowcount(tinfo.getRowcount());
            
            //columnPositionMapping
            anonymTinfo.getColumnPositionMapping().addAll(tinfo.getColumnPositionMapping());

            anonymTableInfoMap.put(anonymTablename, anonymTinfo);
        }
    }

    /************************************************************
     * Getters
     ************************************************************/

    public Map<String, TableInfo> getAnonymTableInfoMap() {
        return new HashMap<>(anonymTableInfoMap);
    }

    public Map<String, String> getTablenameAnonymMap() {
        return new HashMap<>(tablenameAnonymMap);
    }
    
    public Map<String, String> getColumnnameAnonymMap() {
        return new HashMap<>(columnnameAnonymMap);
    }

    public String getParentView(List<RelnameAlias> relnameAliasList) {
        String parentViewname = null;
        int minSeqno = Integer.MAX_VALUE;
        for (RelnameAlias relnameAlias : relnameAliasList) {
            String viewname = relnameAlias.getRelname();
            int seqno = origTableInfoMap.get(viewname).getTopoSeqno();
            if (minSeqno > seqno) {
                minSeqno = seqno;
                parentViewname = viewname;
            }
        }

        if (parentViewname == null)
            throw new RuntimeException("Should not be reaching here");

        return parentViewname;
    }
    
    public String getParentViewFromRelList(List<String> allRelnames) {
        String parentViewname = null;
        int minSeqno = Integer.MAX_VALUE;
        for (String viewname : allRelnames) {
            int seqno = origTableInfoMap.get(viewname).getTopoSeqno();
            if (minSeqno > seqno) {
                minSeqno = seqno;
                parentViewname = viewname;
            }
        }

        if (parentViewname == null)
            throw new RuntimeException("Should not be reaching here");

        return parentViewname;
    }
    
    /************************************************************
     * Important public methods
     ************************************************************/

    /**
     * Inplace anonymizes the tablename and columnname in all formal conditions
     * @param allFormalConditions
     */
    public void anonymizeAllFormalConditions(List<? extends FormalCondition> allFormalConditions) {

        for (FormalCondition formalCondition : allFormalConditions) {

            String viewname = formalCondition.getViewname();
            String anonymViewname = tablenameAnonymMap.get(viewname);
            formalCondition.setViewname(anonymViewname);
                                 
            
            if (formalCondition instanceof FormalConditionComposite) {
            	
            	anonymizeAllFormalConditions(((FormalConditionComposite) formalCondition).getConditionList());
            	
            } else if (formalCondition instanceof FormalConditionSimpleJoin) {
                
            	FormalConditionSimpleJoin formalConditionSimpleJoin = (FormalConditionSimpleJoin) formalCondition;
                formalConditionSimpleJoin.setColumnname(columnnameAnonymMap.get(formalConditionSimpleJoin.getColumnname()));
                formalConditionSimpleJoin.setColumnname2(columnnameAnonymMap.get(formalConditionSimpleJoin.getColumnname2()));
                
            } else if (formalCondition instanceof FormalConditionSimple) {
            	
                FormalConditionSimple formalConditionSimple = (FormalConditionSimple) formalCondition;
                
                // colPath anonymization done here
                List<String> colPath = formalConditionSimple.getColPath();                
                List<String> anonymColPath = new ArrayList<String>();
                for(String c : colPath)
                {
                	anonymColPath.add(tablenameAnonymMap.get(c));
                	
                }
                             
                formalConditionSimple.setColPath(anonymColPath);
                formalConditionSimple.setColumnname(columnnameAnonymMap.get(formalConditionSimple.getColumnname()));
               
            } else if (formalCondition instanceof FormalConditionBaseAggregate) {
            } else
                throw new RuntimeException("Unrecognized type of FormalCondition " + formalCondition.getClass());
            
            if (formalCondition instanceof FormalConditionAggregate) {
            	FormalConditionAggregate formalConditionAggregate = (FormalConditionAggregate) formalCondition;
            	List<String> anonyGroupKey = new ArrayList<>();
            	for (String key : formalConditionAggregate.getGroupKey()) {
					anonyGroupKey.add(columnnameAnonymMap.get(key));
				}
            	formalConditionAggregate.setGroupKey(anonyGroupKey);

            	Set<String> anonyGroupTables = new HashSet<>();
            	for (String tablename : formalConditionAggregate.getGroupTables()) {
					anonyGroupTables.add(tablenameAnonymMap.get(tablename));
				}
            	formalConditionAggregate.setGroupTables(anonyGroupTables);
            } 
        }
    }

    /**
     * Inplace anonymizes the tablename and columnname in the formal condition
     * @param formalCondition
     */
    public void anonymizeFormalConditionSingle(FormalCondition formalCondition) {

        List<FormalCondition> formalConditionsDummyList = new ArrayList<>(1);
        formalConditionsDummyList.add(formalCondition);
        anonymizeAllFormalConditions(formalConditionsDummyList);
    }

    /**
     * Returns a fresh list of domainmapped FormalConditions each element of it if not a composite or a join condition is a SimpleInt condition.
     * Also generates anonymized and domainmapped queries
     * @param allFormalConditions
     * @param histogramMappingInfo uses boundary values from this structure and also side effects domain mapped values in this
     * @param alqps
     * @return
     */
    private class Value121 {
        final Map<String, DoubleList>            seenValuesByNumberColumns;
        final Map<String, List<String>>          seenValuesByStringColumns;
        final Map<String, List<Date>>            seenValuesByDateColumns;

        final Map<String, Double2IntMap>         shownValuesByNumberColumns;
        final Map<String, Object2IntMap<String>> shownValuesByStringColumns;
        final Map<String, Object2IntMap<Date>>   shownValuesByDateColumns;

        public Value121() {
            seenValuesByNumberColumns = new HashMap<>();
            seenValuesByStringColumns = new HashMap<>();
            seenValuesByDateColumns = new HashMap<>();
            
            shownValuesByNumberColumns = new HashMap<>();
            shownValuesByStringColumns = new HashMap<>();
            shownValuesByDateColumns = new HashMap<>();
        }
    }

    private Value121 value121;

    public List<FormalCondition> domainmapAllFormalConditionsAndGenerateQueries(List<FormalCondition> allFormalConditions,
            HistogramMappingInfo histogramMappingInfo, List<Alqp> alqps, List<String> anonymQueries) {

        value121 = new Value121();

        collectSeenValues(allFormalConditions, value121.seenValuesByNumberColumns, value121.seenValuesByStringColumns, value121.seenValuesByDateColumns);
        // currently this function is of no use
        collectMetaValues(histogramMappingInfo, value121.seenValuesByNumberColumns, value121.seenValuesByStringColumns, value121.seenValuesByDateColumns); //	TODO : is it used?
        /*
        System.out.println("value121.seenValuesByNumberColumns " + value121.seenValuesByNumberColumns);
        System.out.println("value121.seenValuesByStringColumns " + value121.seenValuesByStringColumns);
        System.out.println("value121.seenValuesByDateColumns " + value121.seenValuesByDateColumns);
        */
        
        for (Entry<String, DoubleList> entry : value121.seenValuesByNumberColumns.entrySet()) {
            String columnname = entry.getKey();
            DoubleList doubleList = entry.getValue();

            DoubleList freshDoubleList = new DoubleArrayList(new DoubleOpenHashSet(doubleList));
            Collections.sort(freshDoubleList);

            Double2IntMap doubleMap = new Double2IntOpenHashMap();
            for (int i = 0; i < freshDoubleList.size(); i++) {
                doubleMap.put(freshDoubleList.getDouble(i), 2 * (i + 1)); //Domain mapping starts from 2, inc by 2
            }
            value121.shownValuesByNumberColumns.put(columnname, doubleMap);

        }
        for (Entry<String, List<String>> entry : value121.seenValuesByStringColumns.entrySet()) {
            String columnname = entry.getKey();
            List<String> stringList = entry.getValue();

            List<String> freshStringList = new ArrayList<>(new HashSet<>(stringList));
            Collections.sort(freshStringList);

            Object2IntMap<String> stringMap = new Object2IntOpenHashMap<>();
            for (int i = 0; i < freshStringList.size(); i++) {
                stringMap.put(freshStringList.get(i), 2 * (i + 1)); //Domain mapping starts from 2, inc by 2
            }
            value121.shownValuesByStringColumns.put(columnname, stringMap);
        }
        for (Entry<String, List<Date>> entry : value121.seenValuesByDateColumns.entrySet()) {
            String columnname = entry.getKey();
            List<Date> dateList = entry.getValue();

            List<Date> freshDateList = new ArrayList<>(new HashSet<>(dateList));
            Collections.sort(freshDateList);

            Object2IntMap<Date> dateMap = new Object2IntOpenHashMap<>();
            for (int i = 0; i < freshDateList.size(); i++) {
                dateMap.put(freshDateList.get(i), 2 * (i + 1)); //Domain mapping starts from 2, inc by 2
            }
            value121.shownValuesByDateColumns.put(columnname, dateMap);
        }
        // generates anonymized queries but currently not in use
        //System.out.println(" value121.shownValuesByNumberColumns : " + value121.shownValuesByNumberColumns);
        generateQueries(alqps, value121.shownValuesByNumberColumns, value121.shownValuesByStringColumns, value121.shownValuesByDateColumns, anonymQueries);
        // below function is also of no use
        
        distributeShownValues(histogramMappingInfo, value121.shownValuesByNumberColumns, value121.shownValuesByStringColumns,
                value121.shownValuesByDateColumns);
        return distributeShownValues(allFormalConditions, value121.shownValuesByNumberColumns, value121.shownValuesByStringColumns,
                value121.shownValuesByDateColumns);
    }

    /************************************************************
     * Private supporting methods
     ************************************************************/

    /**
     * Looks at each of the constraints and fills in the seenValueBy____Column maps
     * @param allFormalConditions
     * @param seenValuesByNumberColumns
     * @param seenValuesByStringColumns
     * @param seenValuesByDateColumns
     */
    private void collectSeenValues(List<FormalCondition> allFormalConditions, Map<String, DoubleList> seenValuesByNumberColumns,
            Map<String, List<String>> seenValuesByStringColumns, Map<String, List<Date>> seenValuesByDateColumns) {

        for (FormalCondition formalCondition : allFormalConditions) {
        	collectSeenValues(formalCondition, seenValuesByNumberColumns, seenValuesByStringColumns, seenValuesByDateColumns);
        }
    }
    
    private void collectSeenValues(FormalCondition formalCondition, Map<String, DoubleList> seenValuesByNumberColumns,
            Map<String, List<String>> seenValuesByStringColumns, Map<String, List<Date>> seenValuesByDateColumns) {

        if (formalCondition instanceof FormalConditionComposite) {
            collectSeenValues(((FormalConditionComposite) formalCondition).getConditionList(), seenValuesByNumberColumns, seenValuesByStringColumns,
                    seenValuesByDateColumns);

        } else if (formalCondition instanceof FormalConditionSimpleJoin) {

        } else if (formalCondition instanceof FormalConditionSimpleNumber) {
            FormalConditionSimpleNumber formalConditionSimpleNumber = (FormalConditionSimpleNumber) formalCondition;
            String columnname = formalConditionSimpleNumber.getColumnname();
            DoubleList doubleList = seenValuesByNumberColumns.get(columnname);
            if (doubleList == null) {
                doubleList = new DoubleArrayList();
                seenValuesByNumberColumns.put(columnname, doubleList);
            }
            doubleList.add(formalConditionSimpleNumber.getValue());

        } else if (formalCondition instanceof FormalConditionSimpleString) {
            FormalConditionSimpleString formalConditionSimpleString = (FormalConditionSimpleString) formalCondition;
            String columnname = formalConditionSimpleString.getColumnname();
            List<String> stringList = seenValuesByStringColumns.get(columnname);
            if (stringList == null) {
                stringList = new ArrayList<>();
                seenValuesByStringColumns.put(columnname, stringList);
            }
            stringList.add(formalConditionSimpleString.getValue());

        } else if (formalCondition instanceof FormalConditionSimpleDate) {
            FormalConditionSimpleDate formalConditionSimpleDate = (FormalConditionSimpleDate) formalCondition;
            String columnname = formalConditionSimpleDate.getColumnname();
            List<Date> dateList = seenValuesByDateColumns.get(columnname);
            if (dateList == null) {
                dateList = new ArrayList<>();
                seenValuesByDateColumns.put(columnname, dateList);
            }
            dateList.add(formalConditionSimpleDate.getValue());

        } else if (formalCondition instanceof FormalConditionBaseAggregate) {
        } else
            throw new RuntimeException("Unrecognized type of FormalCondition " + formalCondition.getClass());
    }

    /**
     * Looks at each of the histogram infos and fills in the seenValueBy____Column maps
     * @param histogramMappingInfo
     * @param seenValuesByNumberColumns
     * @param seenValuesByStringColumns
     * @param seenValuesByDateColumns
     */
    private void collectMetaValues(HistogramMappingInfo histogramMappingInfo, Map<String, DoubleList> seenValuesByNumberColumns,
            Map<String, List<String>> seenValuesByStringColumns, Map<String, List<Date>> seenValuesByDateColumns) {

        for (String columnname : histogramMappingInfo.shownValuesByNumberColumns.keySet()) {
            DoubleList doubleList = seenValuesByNumberColumns.get(columnname);
            if (doubleList == null) {
                doubleList = new DoubleArrayList();
                seenValuesByNumberColumns.put(columnname, doubleList);
            }
            doubleList.addAll(histogramMappingInfo.shownValuesByNumberColumns.get(columnname).keySet());
        }
        for (String columnname : histogramMappingInfo.shownValuesByStringColumns.keySet()) {
            List<String> stringList = seenValuesByStringColumns.get(columnname);
            if (stringList == null) {
                stringList = new ArrayList<>();
                seenValuesByStringColumns.put(columnname, stringList);
            }
            stringList.addAll(histogramMappingInfo.shownValuesByStringColumns.get(columnname).keySet());
        }
        for (String columnname : histogramMappingInfo.shownValuesByDateColumns.keySet()) {
            List<Date> dateList = seenValuesByDateColumns.get(columnname);
            if (dateList == null) {
                dateList = new ArrayList<>();
                seenValuesByDateColumns.put(columnname, dateList);
            }
            dateList.addAll(histogramMappingInfo.shownValuesByDateColumns.get(columnname).keySet());
        }
    }

    /**
     * Generates a query (anonymized and domainmapped) for each alqp.
     *
     *   Input :
     *   
     *    select *
     *    from store_sales,household_demographics,time_dim, store
     *    where 1=1
     *        and ss_sold_time_sk = time_dim.t_time_sk
     *        and ss_hdemo_sk = household_demographics.hd_demo_sk
     *        and ss_store_sk = s_store_sk
     *        and time_dim.t_hour = 8
     *        and time_dim.t_minute >= 30
     *        and household_demographics.hd_dep_count = 5
     *        and store.s_store_name = 'ese'
     *    ;
     *    
     *    
     *    Output : 
     *    
     *    SELECT * 
     *    FROM 	t18, t19, t16, t09 
     *    WHERE 1=1
     *    AND t18_F_t09 = t09_P
     *    AND t18_F_t16 = t16_P
     *    AND t18_F_t19 = t19_P
     *    AND ((t19_c003 >= 2) 
     *    AND (t19_c001 = 2)) 
     *    AND (t16_c021 = 2) 
     *    AND (t09_c002 = 2)
;
     *    
     *
     *
     * @param alqps
     * @param shownValuesByNumberColumns
     * @param shownValuesByStringColumns
     * @param shownValuesByDateColumns
     * @param anonymQueries
     */
    @SuppressWarnings("unused") // It is suppressed since, on/off is a feature which make other part of the code as Dead Code
	private void generateQueries(List<Alqp> alqps, Map<String, Double2IntMap> shownValuesByNumberColumns,
            Map<String, Object2IntMap<String>> shownValuesByStringColumns, Map<String, Object2IntMap<Date>> shownValuesByDateColumns,
            List<String> anonymQueries) {

    	if(PostgresCConfig.ANONYMIZED_QUERY_GENERATION == "off")
    	{
    		DebugHelper.printInfo("Skipping generating anonimized queries becuase anyway not required!");
    		DebugHelper.printInfo("Anonymized Query generation can be turned on from PostgresCConfig (config file)");
    	}
    	else {
    	
	       for (int i = 0; i < alqps.size(); i++) {
	
	            Alqp alqp = alqps.get(i);
	
	            //TODO: Optimizable by combining following two tree traversals
	            List<String> relnames = alqp.getAllRelnames();
	            List<FormalConditionSimpleJoin> joinConditions = new ArrayList<>();
	            List<String> joinConditionStrs = alqp.getAllJoinConditionStrs();
	            for (String joinConditionStr : joinConditionStrs) {
	                if (joinConditionStr == null) {
	                    continue;
	                }
	                FormalCondition formalCondition = Converter.parseConditionStr(joinConditionStr, -1, null);
	                if (!(formalCondition instanceof FormalConditionSimpleJoin))
	                    throw new RuntimeException("Should not be reaching here");
	
	                FormalConditionSimpleJoin formalJoinCondition = (FormalConditionSimpleJoin) formalCondition;
	
	                String rela = null;
	                for (String relname : relnames) {
	                    TableInfo tableInfo = PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(relname);
	                    if (tableInfo.getKeys().contains(formalJoinCondition.getColumnname())) {
	                        rela = relname;
	                        break;
	                    }
	                }
	                String relb = null;
	                for (String relname : relnames) {
	                    TableInfo tableInfo = PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(relname);
	                    if (tableInfo.getKeys().contains(formalJoinCondition.getColumnname2())) {
	                        relb = relname;
	                        break;
	                    }
	                }
	
	                if (rela == null || relb == null)
	                    throw new RuntimeException("Should not be reaching here");
	
	                boolean aholdsfktob = PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(rela).getFkeyTables().contains(relb);
	
	                if (aholdsfktob) {
	                    formalJoinCondition.updateColumnnames(tablenameAnonymMap.get(rela) + "_F_" + tablenameAnonymMap.get(relb),
	                            tablenameAnonymMap.get(relb) + "_P");
	                } else {
	                    formalJoinCondition.updateColumnnames(tablenameAnonymMap.get(relb) + "_F_" + tablenameAnonymMap.get(rela),
	                            tablenameAnonymMap.get(rela) + "_P");
	                }
	
	                joinConditions.add(formalJoinCondition);
	            }
	
	            FormalCondition predicateCondition = Converter.getAsFormalCondition(alqp.getRoot().getCondition(), this);
	
	            //            List<String> baseConditionStrs = new ArrayList<>();
	            //            for (Condition condition : alqp.getAllConditions()) {
	            //                if (condition instanceof ConditionJoin) {
	            //
	            //                } else if (condition instanceof ConditionBase) {
	            //                    List<String> predicates = condition.getPredicates();
	            //                    if (!predicates.isEmpty()) {
	            //                        baseConditionStrs.add(predicates.get(0));
	            //                    }
	            //                } else
	            //                    throw new RuntimeException("Unrecognized condition " + condition);
	            //            }
	
	            StringBuilder sbFrom = new StringBuilder();
	            for (String relname : relnames) {
	                sbFrom.append(tablenameAnonymMap.get(relname)).append(COMMANEWLINE);
	            }
	            String strFrom = StringUtils.removeEnd(sbFrom.toString(), COMMANEWLINE);
	
	            StringBuilder sbWhere = new StringBuilder();
	            for (FormalConditionSimpleJoin joinCondition : joinConditions) {
	                sbWhere.append(NEWLINEAND).append(joinCondition.getColumnname() + " = " + joinCondition.getColumnname2());
	            }
	            if (predicateCondition != null) {
	                List<FormalCondition> tempList = new ArrayList<>();
	                tempList.add(predicateCondition);
	                anonymizeAllFormalConditions(tempList);
	                tempList = distributeShownValues(tempList, shownValuesByNumberColumns, shownValuesByStringColumns, shownValuesByDateColumns);
	                FormalCondition convertedCondition = tempList.get(0);
	                sbWhere.append(NEWLINEAND).append(convertedCondition.asQueryString());
	            }
	            String strWhere = sbWhere.toString();
	            String sqlQuery = FileUtils.readFileToString(PostgresCConfig.getProp(Key.MQ_ANONYMIZEDQUERIES_SELECTSTARQUERY));
	            sqlQuery = constructQueryFromTemplate(sqlQuery, PostgresCConfig.PH_ANONYMIZEDQUERIES_FROMLIST, strFrom);
	            sqlQuery = constructQueryFromTemplate(sqlQuery, PostgresCConfig.PH_ANONYMIZEDQUERIES_WHERELIST, strWhere);
	
	            String filename = "aq" + String.format("%03d", i + 1) + ".sql";
	            FileUtils.writeStringToFile(PostgresCConfig.getProp(Key.ANONYMIZEDQUERIES_TARGETLOCATION), filename, sqlQuery);
	
	            anonymQueries.add(sqlQuery);
	        }
	       
	       System.out.println("Anonym Queries : " + anonymQueries);
    	} 
	
	  }
	
	    /**
	     * Fills in histogramMappingInfo's shownValueBy____Column maps using shownValueBy____Column maps
	     * @param histogramMappingInfo
	     * @param shownValuesByNumberColumns
	     * @param shownValuesByStringColumns
	     * @param shownValuesByDateColumns
	     */
	    private void distributeShownValues(HistogramMappingInfo histogramMappingInfo, Map<String, Double2IntMap> shownValuesByNumberColumns,
	            Map<String, Object2IntMap<String>> shownValuesByStringColumns, Map<String, Object2IntMap<Date>> shownValuesByDateColumns) {
	
	    	//System.out.println("histogramMappingInfo : " + histogramMappingInfo );
	        for (Entry<String, Map<Double, Integer>> entry : histogramMappingInfo.shownValuesByNumberColumns.entrySet()) {
	            String columnname = entry.getKey();
	            Double2IntMap sourceMapping = shownValuesByNumberColumns.get(columnname);
	            Map<Double, Integer> destMapping = entry.getValue();
	            for (Double key : destMapping.keySet()) {
	                destMapping.put(key, sourceMapping.get(key));
	            }
	        }
	        for (Entry<String, Map<String, Integer>> entry : histogramMappingInfo.shownValuesByStringColumns.entrySet()) {
	            String columnname = entry.getKey();
	            Object2IntMap<String> sourceMapping = shownValuesByStringColumns.get(columnname);
	            Map<String, Integer> destMapping = entry.getValue();
	            for (String d : destMapping.keySet()) {
	                destMapping.put(d, sourceMapping.get(d));
	            }
	        }
	        for (Entry<String, Map<Date, Integer>> entry : histogramMappingInfo.shownValuesByDateColumns.entrySet()) {
	            String columnname = entry.getKey();
	            Object2IntMap<Date> sourceMapping = shownValuesByDateColumns.get(columnname);
	            Map<Date, Integer> destMapping = entry.getValue();
	            for (Date key : destMapping.keySet()) {
	                destMapping.put(key, sourceMapping.get(key));
	            }
	        }
	    }
	
	    /**
	     * Returns a fresh list of FormalCondtions formed by replacing all simple conditions present in the given list by the
	     * corresponding int conditions with order-preserving appropriate int values
	     * @param allFormalConditions
	     * @param shownValuesByNumberColumns
	     * @param shownValuesByStringColumns
	     * @param shownValuesByDateColumns
	     * @return
	     */
	    private static List<FormalCondition> distributeShownValues(List<FormalCondition> allFormalConditions, Map<String, Double2IntMap> shownValuesByNumberColumns,
	            Map<String, Object2IntMap<String>> shownValuesByStringColumns, Map<String, Object2IntMap<Date>> shownValuesByDateColumns) {
	    	//System.out.println("shownValuesByStringColumns : " + shownValuesByStringColumns);
	        List<FormalCondition> domainmappedFormalConditions = new ArrayList<>();
	        for (FormalCondition formalCondition : allFormalConditions) {
	
	        	if(formalCondition instanceof FormalConditionAggregate) {
	        		if(formalCondition instanceof FormalConditionAndAggregate || formalCondition instanceof FormalConditionOrAggregate || formalCondition instanceof FormalConditionSimpleStringAggregate
	        				|| formalCondition instanceof FormalConditionSimpleNumberAggregate || formalCondition instanceof FormalConditionSimpleDateAggregate 
	        				|| formalCondition instanceof FormalConditionBaseAggregate)		// Have handled aggregate versions of these classes in following code
	        			;
	        		else
	        			throw new RuntimeException("Not handled this FormalCondition type! " + formalCondition.getClass());
	        	}
	            if (formalCondition instanceof FormalConditionComposite) {
	                if (formalCondition instanceof FormalConditionAnd) {
	                    FormalConditionAnd formalConditionAnd = (FormalConditionAnd) formalCondition;
	                    FormalConditionAnd freshFormalConditionAnd;
	                    if (formalConditionAnd instanceof FormalConditionAndAggregate) {
	                    	freshFormalConditionAnd = new FormalConditionAndAggregate((FormalConditionAndAggregate) formalConditionAnd);
	                    } else 
	                    	freshFormalConditionAnd = new FormalConditionAnd(formalConditionAnd);
	                    freshFormalConditionAnd.addConditionAll(distributeShownValues(formalConditionAnd.getConditionList(), shownValuesByNumberColumns,
	                            shownValuesByStringColumns, shownValuesByDateColumns));
	
	                    domainmappedFormalConditions.add(freshFormalConditionAnd);
	                } else if (formalCondition instanceof FormalConditionOr) {
	                    FormalConditionOr formalConditionOr = (FormalConditionOr) formalCondition;
	                    FormalConditionOr freshFormalConditionOr;
	                    if (formalConditionOr instanceof FormalConditionOrAggregate) {
	                    	freshFormalConditionOr = new FormalConditionOrAggregate((FormalConditionOrAggregate) formalConditionOr);
	                    } else {
	                    	freshFormalConditionOr = new FormalConditionOr(formalConditionOr);
	                    }
	                    freshFormalConditionOr.addConditionAll(distributeShownValues(formalConditionOr.getConditionList(), shownValuesByNumberColumns,
	                            shownValuesByStringColumns, shownValuesByDateColumns));
	
	                    domainmappedFormalConditions.add(freshFormalConditionOr);
	                } else
	                    throw new RuntimeException("Unrecognized FormalConditionComposite " + formalCondition + " of class " + formalCondition.getClass());
	
	            } else if (formalCondition instanceof FormalConditionSimpleJoin) {
	                domainmappedFormalConditions.add(formalCondition);
	
	            } else if (formalCondition instanceof FormalConditionSimpleNumber) {
	                FormalConditionSimpleNumber formalConditionSimpleNumber = (FormalConditionSimpleNumber) formalCondition;
	                String columnname = formalConditionSimpleNumber.getColumnname();
	                double value = formalConditionSimpleNumber.getValue();
	
	                FormalConditionSimpleInt formalConditionSimpleInt;
	                if (formalConditionSimpleNumber instanceof FormalConditionSimpleNumberAggregate)
	                	formalConditionSimpleInt = new FormalConditionSimpleIntAggregate((FormalConditionSimpleNumberAggregate) formalConditionSimpleNumber, shownValuesByNumberColumns.get(columnname).get(value));
	                else
	                	formalConditionSimpleInt = new FormalConditionSimpleInt(formalConditionSimpleNumber, shownValuesByNumberColumns.get(columnname).get(value));
	                domainmappedFormalConditions.add(formalConditionSimpleInt);
	
	            } else if (formalCondition instanceof FormalConditionSimpleString) {
	                FormalConditionSimpleString formalConditionSimpleString = (FormalConditionSimpleString) formalCondition;
	                String columnname = formalConditionSimpleString.getColumnname();
	                String value = formalConditionSimpleString.getValue();
	
	                FormalConditionSimpleInt formalConditionSimpleInt;
	                if (formalConditionSimpleString instanceof FormalConditionSimpleStringAggregate)
	                	formalConditionSimpleInt = new FormalConditionSimpleIntAggregate((FormalConditionSimpleStringAggregate) formalConditionSimpleString, shownValuesByStringColumns.get(columnname).getInt(value));
	                else
	                	formalConditionSimpleInt = new FormalConditionSimpleInt(formalConditionSimpleString, shownValuesByStringColumns.get(columnname).getInt(value));
	                domainmappedFormalConditions.add(formalConditionSimpleInt);
	
	            } else if (formalCondition instanceof FormalConditionSimpleDate) {
	                FormalConditionSimpleDate formalConditionSimpleDate = (FormalConditionSimpleDate) formalCondition;
	                String columnname = formalConditionSimpleDate.getColumnname();
	                Date value = formalConditionSimpleDate.getValue();
	
	                FormalConditionSimpleInt formalConditionSimpleInt;
	                if (formalConditionSimpleDate instanceof FormalConditionSimpleDateAggregate)
	                	formalConditionSimpleInt = new FormalConditionSimpleIntAggregate((FormalConditionSimpleDateAggregate) formalConditionSimpleDate, shownValuesByDateColumns.get(columnname).getInt(value));
	                else
	                	formalConditionSimpleInt = new FormalConditionSimpleInt(formalConditionSimpleDate, shownValuesByDateColumns.get(columnname).getInt(value));
	                domainmappedFormalConditions.add(formalConditionSimpleInt);
	
	            } else if (formalCondition instanceof FormalConditionBaseAggregate) {
	                domainmappedFormalConditions.add(formalCondition);
	            } else
	                throw new RuntimeException("Unrecognized type of FormalCondition " + formalCondition.getClass());
	        }
	        
	        
	        return domainmappedFormalConditions;

    }

    /**
     * Returns a fresh FormalCondtion formed by replacing the simple condition present by the
     * corresponding int condition with order-preservingly appropriate int values
     * @param allFormalConditions
     * @return
     */
    public FormalCondition distributeShownValuesSingle(FormalCondition formalCondition) {

        List<FormalCondition> formalConditionDummyList = new ArrayList<>(1);
        formalConditionDummyList.add(formalCondition);
        formalConditionDummyList = distributeShownValues(formalConditionDummyList, value121.shownValuesByNumberColumns, value121.shownValuesByStringColumns,
                value121.shownValuesByDateColumns);
        return formalConditionDummyList.get(0);
    }

    private static String constructQueryFromTemplate(String queryTemplate, String key, String replacement) {
        return queryTemplate.replace("<" + key + ">", replacement);
    }
    
    public Map<String, Object2IntMap<Date>> getDate121Mapping() {
    	return value121.shownValuesByDateColumns;
    }
    public Map<String, Object2IntMap<String>> getString121Mapping() {
    	return value121.shownValuesByStringColumns;
    }
    public Map<String, Double2IntMap> getNumber121Mapping() {
    	return value121.shownValuesByNumberColumns;
    }
    /*
	public void giveUniqueIdToAnonymizedCol(List<? extends FormalCondition> allFormalConditions,Map<String, ColumnPathInfo> columnPathInfoMap) {
		
		
		
		 for (FormalCondition condition : allFormalConditions) {
	            if (condition instanceof FormalConditionComposite) {
	            	giveUniqueIdToAnonymizedCol(((FormalConditionComposite) condition).getConditionList());

	            } else if (condition instanceof FormalConditionSimpleInt) {
	                FormalConditionSimpleInt formalConditionSimpleInt = (FormalConditionSimpleInt) condition;
	                String anonymColumnName = formalConditionSimpleInt.getColumnname();
	                List<String> anonymColPath = formalConditionSimpleInt.getColPath();
	                ColumnPathInfo ctemp = new ColumnPathInfo(anonymColumnName,anonymColPath);
	                columnPathInfoMap.put(anonymColumnName + "_" + ctemp.hashCode() , ctemp);
	                
	                formalConditionSimpleInt.setColumnname(anonymColumnName + "_" + anonymColPath.hashCode());
	                
	            } else if (condition instanceof FormalConditionBaseAggregate) {
	            } else
	                throw new RuntimeException("Unrecognized type of FormalCondition " + condition.getClass());
	        }
		 
		 
		 
		 
	}
	*/

	public void giveUniqueIdToAnonymizedCol(List<? extends FormalCondition> allFormalConditions,
			Map<String, ColumnPathInfo> columnIndentifierMap) {
		


		 for (FormalCondition condition : allFormalConditions) {
	            if (condition instanceof FormalConditionComposite) {
	            	giveUniqueIdToAnonymizedCol(((FormalConditionComposite) condition).getConditionList(), columnIndentifierMap);

	            } else if (condition instanceof FormalConditionSimpleInt) {
	                FormalConditionSimpleInt formalConditionSimpleInt = (FormalConditionSimpleInt) condition;
	                String anonymColumnName = formalConditionSimpleInt.getColumnname();
	                List<String> anonymColPath = formalConditionSimpleInt.getColPath();
	                ColumnPathInfo ctemp = new ColumnPathInfo(anonymColumnName,anonymColPath);
	                columnIndentifierMap.put(anonymColumnName + "_" + anonymColPath.hashCode() , ctemp);
	                
	                formalConditionSimpleInt.setColumnname(anonymColumnName + "_" + anonymColPath.hashCode());
	                
	            } else if (condition instanceof FormalConditionBaseAggregate) {
	            } else
	                throw new RuntimeException("Unrecognized type of FormalCondition " + condition.getClass());
	        }
		
	}
}
