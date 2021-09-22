package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class TableInfo {
    private int               topoSeqno;
    private final HashMap<String, ColumnInfo> columns;    //all columns in the table
    private final Set<String> keys;       //all pkey / fkey columns in the table
    private final Set<String> fkeyTables; //list of tables to which current table holds an fkey
    private int               rowcount;
    private final List<String>	_pkList;
    private Map<String, List<String>> _fkTargetTableToSourceColumns;
    private final List<Integer> columnPositionMapping;

    public TableInfo() {
        topoSeqno = -1;
        columns = new HashMap<>();
        keys = new HashSet<>();
        fkeyTables = new HashSet<>();
        rowcount = -1;
        _pkList = new ArrayList<>();
        _fkTargetTableToSourceColumns = new HashMap<>();
        columnPositionMapping = new ArrayList<>();
    }

    public TableInfo(JSONObject obj) {
        int i;
        JSONArray arr;

        if (obj.has("topoSeqno")) {
            topoSeqno = obj.getInt("topoSeqno");
        } else {
            topoSeqno = -1;
        }

        columns = new HashMap<>();
        JSONObject temp = obj.getJSONObject("columns");
        for (String columnName : temp.keySet()) {
			JSONObject colinfo = temp.getJSONObject(columnName);
			columns.put(columnName, new ColumnInfo(colinfo.getInt("ordinalPosition"), colinfo.getString("columnType")));
		}
        
//        for (i = 0; i < arr.length(); i++) {
//        	JSONObject column = arr.getJSONObject(i);
//            columns.add(new Column(column.getString("columnName"), column.getString("columnType")));
//        }

        keys = new HashSet<>();
        arr = obj.getJSONArray("keys");
        for (i = 0; i < arr.length(); i++) {
            keys.add(arr.getString(i));
        }

        fkeyTables = new HashSet<>();
        arr = obj.getJSONArray("fkeyTables");
        for (i = 0; i < arr.length(); i++) {
            fkeyTables.add(arr.getString(i));
        }

        rowcount = obj.getInt("rowcount");
        
        columnPositionMapping = new ArrayList<>();
        arr = obj.getJSONArray("columnPositionMapping");
        for (i = 0; i < arr.length(); i++) {
        	columnPositionMapping.add(arr.getInt(i));
        }
        
        
//      TODO : not required at vendor site
        _pkList = new ArrayList<>();
        
//        TODO : not required at vendor site
        _fkTargetTableToSourceColumns = new HashMap<>();
    }
    
    public List<Integer> getColumnPositionMapping() {
    	return columnPositionMapping;
    }
    
    public void setFkTargetTableToSourceColumns(Map<String, List<String>> map) {
    	_fkTargetTableToSourceColumns = map;
    }
    
    public Map<String, List<String>> fkTargetTableToSourceColumns() {
    	return _fkTargetTableToSourceColumns;
    }

    public int getTopoSeqno() {
        if (topoSeqno == -1) {
            throw new RuntimeException("Trying to get topoSeqno when it is still not set");
        }
        return topoSeqno;
    }

    public void setTopoSeqno(int topoSeqno) {
        this.topoSeqno = topoSeqno;
    }
    
//    public void addColumn(String cName, String cType) {
//    	columns.add(new Column(cName, cType));
//    }

    public HashMap<String, ColumnInfo> getColumns() {
    	return columns;
    }
    
//    public String getColumn(String columnName) {
//    	return columns.get(columnName).getColumnType();
//    }
//    
//    public String getColumnOrdinal(String columnName) {
//    	return columns.get(columnName).getColumnType();
//    }
    
    public Set<String> columnsNames() {
    	return columns.keySet();
    }

    public Set<String> getKeys() {
        return keys;
    }

    public Set<String> getFkeyTables() {
        return fkeyTables;
    }

    public int getRowcount() {
        return rowcount;
    }

    public void setRowcount(int rowcount) {
        this.rowcount = rowcount;
    }
    
    public List<String> pkList() {
    	return _pkList;
    }

    @Override
    public String toString() {
        return rowcount + ") columns=" + columns + " keys=" + keys + " fkeys to: " + fkeyTables;
    }
}