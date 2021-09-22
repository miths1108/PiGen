package in.ac.iisc.cds.dsl.cdgclient.preprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import in.ac.iisc.cds.dsl.cdgclient.anonymizer.Anonymizer;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig.Key;
import in.ac.iisc.cds.dsl.cdgvendor.model.ColumnInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.SchemaInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.TableInfo;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import in.ac.iisc.cds.dsl.cdgvendor.utils.FileUtils;

public class DDLGenerator {

	// dk have ruined this file for demo. Earlier it used to generate anonymized DDL and I modified it so as to generate normal DDL (with column ordering accroding to summary generator) as anonymization was being removed for demo.
	// But then I changed summary order and it works with original DDL. So this file must not be required but I don't remember why it is required now! But you'll get it :D
	// UPDATE: file needed for columnPositionMapping. Other things may be removed from this file and name of class could be changed because it is not used for generating DDL.
	
    private static final String COMMA   = ",";
    private static final String NEWLINE = "\n";

    public void generateDDL(Anonymizer anonymizer) {
        Map<String, TableInfo> anonyTableInfoMap = anonymizer.getAnonymTableInfoMap();
        Map<String, TableInfo> origTableInfoMap = PostgresCConfig.BASICSCHEMA_INFO.getTableInfos();

        StringBuilder sbCreateTable = new StringBuilder();
        StringBuilder sbAlterTableP = new StringBuilder();
        StringBuilder sbAlterTableF = new StringBuilder();
        
        Map<String, String> mapAnonyTableToOrigTable = new HashMap<>();
        for (Entry<String, String> entry : anonymizer.getTablenameAnonymMap().entrySet()) {
        	mapAnonyTableToOrigTable.put(entry.getValue(), entry.getKey());
		}
        
        Map<String, String> mapAnonyColumnToOrigColumn = new HashMap<>();
        for (Entry<String, String> entry : anonymizer.getColumnnameAnonymMap().entrySet()) {
        	mapAnonyColumnToOrigColumn.put(entry.getValue(), entry.getKey());
		}

        //CREATE TABLE
        //ALTER TABLE ADD PK
        //ALTER TABLE ADD FK
        for (Entry<String, TableInfo> entry : anonyTableInfoMap.entrySet()) {
            String anonyTableName = entry.getKey();
            TableInfo anonyTableInfo = entry.getValue();
            List<Integer> columnPositionMapping = anonyTableInfo.getColumnPositionMapping();
            
            String origTableName = mapAnonyTableToOrigTable.get(anonyTableName);
            TableInfo origTableInfo = origTableInfoMap.get(origTableName);
            HashMap<String, ColumnInfo> origColumnsInfo = origTableInfo.getColumns();

//            columnnames are ordered with first being a single pkey followed by fkeys (to other tables in sorted order of table name) followed by normal columnnames of this table..
//            followed by other columns of pk follwed by other columns of fk..A fk table may have multiple fks to same pk table, so one fk is initially put and others are at last
            List<String> columnnames = new ArrayList<>();
            List<String> otherColumnNames = new ArrayList<>();
            List<String> otherColumnOnlyNames = new ArrayList<>();
            
            DebugHelper.printInfo(origTableName + " : " + anonyTableName + " : " + origTableInfo.pkList());
            String firstPk = origTableInfo.pkList().get(0);
            
            columnnames.add(firstPk + " int");
            columnPositionMapping.add(origColumnsInfo.get(firstPk).getOrdinalPosition()-1);
            
            List<String> otherPks = new ArrayList<>();
            for(int i = 1; i < origTableInfo.pkList().size(); ++i)
            	otherPks.add(origTableInfo.pkList().get(i));

            for (String pkColumn : otherPks) {
            	otherColumnNames.add(pkColumn + " int");
            	otherColumnOnlyNames.add(pkColumn);
			}
            
            String columnnamesP;
            if(otherPks.size() > 0)
            	columnnamesP = firstPk + "," + StringUtils.join(otherPks, COMMA);
            else
            	columnnamesP = firstPk;

            String sqlQueryP = FileUtils.readFileToString(PostgresCConfig.getProp(Key.MQ_DDLGENERATED_ADDPKQUERY));
            sqlQueryP = constructQueryFromTemplate(sqlQueryP, PostgresCConfig.PH_DDLGENERATED_TABLENAMEP, origTableName);
            sqlQueryP = constructQueryFromTemplate(sqlQueryP, PostgresCConfig.PH_DDLGENERATED_COLUMNNAMEP, columnnamesP);
            sbAlterTableP.append(sqlQueryP).append(NEWLINE);

        	Map<String, List<String>> mapFkTargetTableToSourceColumns = origTableInfo.fkTargetTableToSourceColumns();
            List<String> fkeyAnonyTablenames = new ArrayList<>(anonyTableInfo.getFkeyTables());
            Collections.sort(fkeyAnonyTablenames);
            for (String fkeyAnonyTablename : fkeyAnonyTablenames) {
            	String fkeyOrigTablename = mapAnonyTableToOrigTable.get(fkeyAnonyTablename);
            	List<String> fks = mapFkTargetTableToSourceColumns.get(fkeyOrigTablename);
            	columnnames.add(fks.get(0) + " int");
            	columnPositionMapping.add(origColumnsInfo.get(fks.get(0)).getOrdinalPosition()-1);
            	
            	for(int i = 1; i < fks.size(); ++i) {
                	otherColumnNames.add(fks.get(i) + " int");
                	otherColumnOnlyNames.add(fks.get(i));
            	}
            	
            	for (String fkColumn : fks) {
                    String sqlQueryF = FileUtils.readFileToString(PostgresCConfig.getProp(Key.MQ_DDLGENERATED_ADDFKQUERY));
                    sqlQueryF = constructQueryFromTemplate(sqlQueryF, PostgresCConfig.PH_DDLGENERATED_TABLENAMEF, origTableName);
                    sqlQueryF = constructQueryFromTemplate(sqlQueryF, PostgresCConfig.PH_DDLGENERATED_COLUMNNAMEF, fkColumn);
                    sqlQueryF = constructQueryFromTemplate(sqlQueryF, PostgresCConfig.PH_DDLGENERATED_FKCONSTRAINTNAME, "cn_" + origTableName + "_" + fkColumn + "_F_" + fkeyOrigTablename);
                    sqlQueryF = constructQueryFromTemplate(sqlQueryF, PostgresCConfig.PH_DDLGENERATED_TABLENAMEP, fkeyOrigTablename);
                    sqlQueryF = constructQueryFromTemplate(sqlQueryF, PostgresCConfig.PH_DDLGENERATED_COLUMNNAMEP, origTableInfoMap.get(fkeyOrigTablename).pkList().get(0));
                    sbAlterTableF.append(sqlQueryF).append(NEWLINE);
				}
            }

            List<String> nonKeyColumnnames = new ArrayList<>(anonyTableInfo.columnsNames());
            nonKeyColumnnames.removeAll(anonyTableInfo.getKeys());
            Collections.sort(nonKeyColumnnames);
            for (String nonKeyAnonyColumnname : nonKeyColumnnames) {
                columnnames.add(mapAnonyColumnToOrigColumn.get(nonKeyAnonyColumnname) + " int");
                columnPositionMapping.add(origColumnsInfo.get(mapAnonyColumnToOrigColumn.get(nonKeyAnonyColumnname)).getOrdinalPosition()-1);
            }
            
            for (String colName : otherColumnOnlyNames) {
            	otherColumnNames.add(colName);
            	columnPositionMapping.add(origColumnsInfo.get(colName).getOrdinalPosition()-1);
			}
            
            if(columnPositionMapping.size() != new HashSet<>(columnPositionMapping).size())
            	throw new RuntimeException("Problem with column position mapping");
            
            String columnListStr = StringUtils.join(columnnames, COMMA) + COMMA + StringUtils.join(otherColumnNames, COMMA);

            String sqlQueryC = FileUtils.readFileToString(PostgresCConfig.getProp(Key.MQ_DDLGENERATED_CREATETABLEQUERY));
            sqlQueryC = constructQueryFromTemplate(sqlQueryC, PostgresCConfig.PH_DDLGENERATED_TABLENAME, origTableName);
            sqlQueryC = constructQueryFromTemplate(sqlQueryC, PostgresCConfig.PH_DDLGENERATED_COLUMNLIST, columnListStr);
            sbCreateTable.append(sqlQueryC).append(NEWLINE);

        }
        
        JSONObject result = new JSONObject();
        for (Entry<String, TableInfo> entry : anonyTableInfoMap.entrySet()) {
            String tablename = entry.getKey();
            TableInfo tinfo = entry.getValue();
            result.put(tablename, new JSONObject(tinfo));
        }
        FileUtils.writeStringToFile(PostgresCConfig.getProp(Key.ANONYMIZEDSCHEMA_TARGETFILE), result.toString());
//        cheating end

        String ddlStr = sbCreateTable.toString() + sbAlterTableP.toString() + sbAlterTableF.toString();
        ddlStr = StringUtils.removeEnd(ddlStr, NEWLINE);
        try {
            FileUtils.writeStringToFile(PostgresCConfig.getProp(Key.DDLGENERATED_TARGETFILE), ddlStr);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to write generate ddl", ex);
        }

    }

    private static String constructQueryFromTemplate(String queryTemplate, String key, String replacement) {
        return queryTemplate.replace("<" + key + ">", replacement);
    }

}
