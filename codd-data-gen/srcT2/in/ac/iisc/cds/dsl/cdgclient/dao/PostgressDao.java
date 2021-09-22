package in.ac.iisc.cds.dsl.cdgclient.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;

import in.ac.iisc.cds.dsl.cdgclient.connectors.PostgresConn;
import in.ac.iisc.cds.dsl.cdgvendor.utils.Pair;

public class PostgressDao {

    private static final String NEWLINE = "\n";

    public static String execExplainAnalyzeQuery(String sql) {
        try {
            Statement st = PostgresConn.INSTANCE.createStatement();

            DebugHelper.printDebug("Query\n" + sql);
            ResultSet rs = st.executeQuery(sql);
            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                result.append(rs.getString(1)).append(NEWLINE);
            }
            //DebugHelper.printDebug("result\n" + result);
            try {
                rs.close();
                st.close();
                PostgresConn.INSTANCE.rollback();
            } catch (Exception ex) {}
            return result.toString();
        } catch (SQLException ex) {
            throw new RuntimeException("Exception executing\n" + sql, ex);
        }
    }

    public static List<SchemaInfoResultSetRow> execSchemaQuery(String sql) {
        try {
            Statement st = PostgresConn.INSTANCE.createStatement();

            //DebugHelper.printDebug("Query\n" + sql);
            ResultSet rs = st.executeQuery(sql);

            List<SchemaInfoResultSetRow> result = new ArrayList<>();
            
            while (rs.next()) {
                SchemaInfoResultSetRow info = new SchemaInfoResultSetRow();
                info.tablename = rs.getString(1);
                info.columnname = rs.getString(2);
                info.is_key = rs.getBoolean(4);
                info.columntype = rs.getString(5);
                info.ordinalPosition = rs.getInt(6);
                result.add(info);
                //DebugHelper.printDebug(info.toString());
            }
           // DebugHelper.printDebug("result\n" + result);
            try {
                rs.close();
                st.close();
            } catch (Exception ex) {}

            return result;
        } catch (SQLException ex) {
            throw new RuntimeException("Exception executing\n" + sql, ex);
        }
    }

    public static class SchemaInfoResultSetRow {
        public String  tablename;
        public String  columnname;
        public boolean is_key;
        public String columntype;
        public int ordinalPosition;
    }

    public static Map<String, Map<String, List<String>>> execFkeyTablesQuery(String sql) {
        try {
            Statement st = PostgresConn.INSTANCE.createStatement();

            //DebugHelper.printDebug("Query\n" + sql);
            ResultSet rs = st.executeQuery(sql);

            Map<String, Map<String, List<String>>> mapSorceTableToTargetTableToSourceColumns = new HashMap<>();
            while (rs.next()) {
                String table_source = rs.getString(1);
                String table_target = rs.getString(2);
                String column_source = rs.getString(3);

                Map<String, List<String>> mapTargetTableToSourceColumns = mapSorceTableToTargetTableToSourceColumns.get(table_source);
                if (mapTargetTableToSourceColumns == null) {
                    mapTargetTableToSourceColumns = new HashMap<>();
                	mapSorceTableToTargetTableToSourceColumns.put(table_source, mapTargetTableToSourceColumns);
                }
                
                List<String> sourceColumns = mapTargetTableToSourceColumns.get(table_target);
                if(sourceColumns == null) {
                	sourceColumns = new ArrayList<>();
                	mapTargetTableToSourceColumns.put(table_target, sourceColumns);
                }
                sourceColumns.add(column_source);
                
//                	throw new RuntimeException("Multiple foreign key columns found\n Source table: "+table_source+" Target table: "+table_target+" Column source: "+column_source);
//                DebugHelper.printInfo("Multiple foreign key columns found\n Source table: "+table_source+" Target table: "+table_target+" Column source: "+mapTargetTableToSourceColumn.get(table_target)+" and "+column_source);
            }

            //DebugHelper.printDebug("result\n" + result);
            try {
                rs.close();
                st.close();
                PostgresConn.INSTANCE.rollback();
            } catch (Exception ex) {}
            return mapSorceTableToTargetTableToSourceColumns;
        } catch (SQLException ex) {
            throw new RuntimeException("Exception executing\n" + sql, ex);
        }
    }

    public static int execRowCountQuery(String sql) {
        try {
            Statement st = PostgresConn.INSTANCE.createStatement();

            //DebugHelper.printDebug("Query\n" + sql);
            ResultSet rs = st.executeQuery(sql);
            rs.next();
            int result = rs.getInt(1);
            //DebugHelper.printDebug("result\n" + result);
            try {
                rs.close();
                st.close();
                PostgresConn.INSTANCE.rollback();
            } catch (Exception ex) {}
            return result;
        } catch (SQLException ex) {
            throw new RuntimeException("Exception executing\n" + sql, ex);
        }
    }

    public static List<String> execDomainedQuery(String sql) {
        try {
            Statement st = PostgresConn.INSTANCE.createStatement();

            DebugHelper.printDebug("Query\n" + sql);
            ResultSet rs = st.executeQuery(sql);
            rs.next();
            List<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getString(1).trim());
            }
            //DebugHelper.printDebug("result\n" + result);
            try {
                rs.close();
                st.close();
                PostgresConn.INSTANCE.rollback();
            } catch (Exception ex) {}
            return result;
        } catch (SQLException ex) {
            throw new RuntimeException("Exception executing\n" + sql, ex);
        }
    }

	public static List<Pair<String, String>> execPrimaryKeyQuery(String sql) {
		try {
            Statement st = PostgresConn.INSTANCE.createStatement();

            //DebugHelper.printDebug("Query\n" + sql);
            ResultSet rs = st.executeQuery(sql);
            List<Pair<String, String>> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new Pair<String, String>(rs.getString(1).trim(), rs.getString(2).trim()));
            }
            //DebugHelper.printDebug("result\n" + result);
            try {
                rs.close();
                st.close();
                PostgresConn.INSTANCE.rollback();
            } catch (Exception ex) {}
            return result;
        } catch (SQLException ex) {
            throw new RuntimeException("Exception executing\n" + sql, ex);
        }
	}
}
