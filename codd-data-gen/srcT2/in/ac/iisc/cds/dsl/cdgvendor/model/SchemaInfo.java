package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class SchemaInfo {
    private final Map<String, TableInfo> tableInfos;
    private boolean                      validated;

    public SchemaInfo() {
        tableInfos = new HashMap<>();
        validated = false;
    }

    /************************************************************
     * Setters
     ************************************************************/

    public void putTableInfo(String tablename, TableInfo tableInfo) {
        tableInfos.put(tablename, tableInfo);
    }

    /************************************************************
     * Validate And Initialize
     ************************************************************/
    public void validate() {
        //Validate
        if (!hasUniqueColumns())
            throw new RuntimeException("Expected schema with unique column names accross tables");

        if (hasCycle())
            throw new RuntimeException("Expected schema with no FK->PK cycle");
        validated = true;
    }

    public void validateAndInit() {
        validate();
        initTopoSeqnos();
    }

    private boolean hasCycle() {
        DefaultDirectedGraph<String, DefaultEdge> jgraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        for (String tablename : getTablenames()) {
            jgraph.addVertex(tablename);
        }
        for (String tablename_source : getTablenames()) {
            Set<String> tablename_targets = tableInfos.get(tablename_source).getFkeyTables();

            for (String tablename_target : tablename_targets) {
                jgraph.addEdge(tablename_source, tablename_target);
            }
        }
        CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<>(jgraph);
//        for (String iterable_element : cycleDetector.findCycles()) {
//			System.err.println(iterable_element);
//		}
        return cycleDetector.detectCycles();
    }

    private boolean hasUniqueColumns() {
        Set<String> allcolumns = new HashSet<>();
        for (TableInfo tableInfo : tableInfos.values()) {
            Set<String> columns = tableInfo.columnsNames();
            Set<String> temp = new HashSet<>(allcolumns);
            temp.retainAll(columns);
            if (!temp.isEmpty())
                return false;
            allcolumns.addAll(columns);
        }
        return true;
    }

    private void initTopoSeqnos() {
        Map<String, Integer> tablenameTopoSeqnoMap = getTablenameTopoSeqnoMap();
        for (Entry<String, TableInfo> entry : tableInfos.entrySet()) {
            String tablename = entry.getKey();
            TableInfo tableInfo = entry.getValue();

            tableInfo.setTopoSeqno(tablenameTopoSeqnoMap.get(tablename));
        }

    }

    private Map<String, Integer> getTablenameTopoSeqnoMap() {
        //Creates jGrapht and generates views by traversing in topological order

        //Step 1: Create jGrapht with given tables as vertices and table1 -> table2 as edge if table1 has a FKey for which table2 has a unique key
        DefaultDirectedGraph<String, DefaultEdge> jgraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        for (String tablename : tableInfos.keySet()) {
            jgraph.addVertex(tablename);
        }
        for (String tablename_source : tableInfos.keySet()) {
            Set<String> tablename_targets = tableInfos.get(tablename_source).getFkeyTables();

            for (String tablename_target : tablename_targets) {
                jgraph.addEdge(tablename_source, tablename_target);
            }
        }

        //Step 2: Assign topoSeqno to tablenames
        List<String> topoTablenames = getTopoList(jgraph);

        //Step 3: Return tablenameTopoSeqnoMap
        Map<String, Integer> tablenameTopoSeqnoMap = new HashMap<>();
        for (int i = 0; i < topoTablenames.size(); i++) {
            tablenameTopoSeqnoMap.put(topoTablenames.get(i), i);
        }
        return tablenameTopoSeqnoMap;
    }

    private List<String> getTopoList(DirectedGraph<String, DefaultEdge> jgraph) {

        Iterator<String> iter = new TopologicalOrderIterator<>(jgraph);
        List<String> topoList = new ArrayList<>();
        while (iter.hasNext()) {
            String v = iter.next();
            topoList.add(v);
        }
        return topoList;
    }

    /************************************************************
     * Getters
     ************************************************************/
    public Map<String, TableInfo> getTableInfos() {
        handleUnvalidate();
        return tableInfos;
    }

    public TableInfo getTableInfo(String tablename) {
        handleUnvalidate();
        if (tableInfos.get(tablename) == null)
            throw new RuntimeException("No tableInfo found for tablename " + tablename);
        return tableInfos.get(tablename);
    }

    public List<String> getTablenames() {
        return new ArrayList<>(tableInfos.keySet());
    }

    private void handleUnvalidate() {
        if (!validated) {
            //throw new RuntimeException("trying to get parts of TableInfo before it is validated");
        }
    }

    /************************************************************
     * toString
     ************************************************************/
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        for (Entry<String, TableInfo> entry : tableInfos.entrySet()) {
            String tablename = entry.getKey();
            TableInfo tableInfo = entry.getValue();

            sb.append("\t" + tablename + ": " + tableInfo + "\n");
        }

        String str = sb.toString();
        StringUtils.chomp(str);
        return str;
    }
}
