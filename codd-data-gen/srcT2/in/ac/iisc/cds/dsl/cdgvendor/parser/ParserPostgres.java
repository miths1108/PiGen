package in.ac.iisc.cds.dsl.cdgvendor.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import in.ac.iisc.cds.dsl.cdgvendor.model.Alqp;
import in.ac.iisc.cds.dsl.cdgvendor.model.AlqpAggregateNode;
import in.ac.iisc.cds.dsl.cdgvendor.model.AlqpInternalNode;
import in.ac.iisc.cds.dsl.cdgvendor.model.AlqpLeafIndexNode;
import in.ac.iisc.cds.dsl.cdgvendor.model.AlqpLeafNode;
import in.ac.iisc.cds.dsl.cdgvendor.model.AlqpNode;
import in.ac.iisc.cds.dsl.cdgvendor.utils.StringUtils;

public class ParserPostgres extends Parser {

    @Override
    public Alqp parse(String ea) throws ParseException {

        try {

            JSONArray root = new JSONArray(ea);
            JSONObject obj = (JSONObject) root.get(0);
            obj = obj.getJSONObject("Plan");
            

            /*
            {
              "Node Type": "Limit",

              "Hash Cond": "(store_returns.sr_returned_date_sk = date_dim.d_date_sk)",
              "Join Filter": "(ctr1.ctr_store_sk = store.s_store_sk)",
              "Filter": "(s_state = 'SD'::bpchar)",

              "Plan Rows": 100,

                  < optionally>

              "Relation Name": "customer",
              "Alias": "customer",

              "Plans": [ < recurse > ]

                  < other fields like >

              "Startup Cost": 435415570.71,
              "Total Cost": 435415570.96,
              "Plan Width": 17,
            }
            */
            
            Alqp alqp = new Alqp(parseAsAlqpNode(obj));
            return alqp;

        } catch (Exception ex) {
            throw new ParseException("Unable to parse JSON: " + ea, ex);
        }
    }

    private static String getIfAtmostOne(JSONObject obj, String... keys) throws ParseException {

        int count, i;
        String ans = null;
        for (i = count = 0; i < keys.length; i++) {
            if (obj.has(keys[i])) {
                ans = obj.getString(keys[i]);
                count++;
            }
        }

        if (count > 1)
            throw new ParseException("Found multiple attributes for keys: " + Arrays.asList(keys) + " in obj: " + obj + " while expected atmost 1");

        return ans;
    }

    private AlqpNode parseAsAlqpNode(JSONObject obj) throws ParseException {

        String nodetype = obj.getString("Node Type");
        if (nodetype.equals("Limit") || nodetype.equals("Sort")) {
        	if (!obj.has("Plans"))
        		throw new RuntimeException("Was not expecting this!");
        	JSONArray arr = obj.getJSONArray("Plans");
        	if (arr.length() != 1)
        		throw new RuntimeException("Was not expecting this!");
        	return parseAsAlqpNode(arr.getJSONObject(0));
        }
        	
        String conditionStr = getIfAtmostOne(obj, "Hash Cond", "Join Filter", "Filter", "Merge Cond");
        //System.out.println("conditionStr" + conditionStr);
        long outputCardinality = obj.getLong("Actual Rows");

        if (obj.has("Plans")) {
            JSONArray arr = obj.getJSONArray("Plans");
            if (arr.length() == 1 || arr.length() == 2) {
            	
            	if(nodetype.equals("Aggregate") || nodetype.equals("Group")) {	// TODO : Dirty edit!
            		
            		AlqpAggregateNode ans = new AlqpAggregateNode();
            		ans.setChild(parseAsAlqpNode(arr.getJSONObject(0)));
            		obj.getJSONArray("Group Key");
            		StringUtils.toStringList(obj.getJSONArray("Group Key").toList());
            		ans.setGroupKey(StringUtils.toStringList(obj.getJSONArray("Group Key").toList()));
            		ans.setOutputCardinality(outputCardinality);
            		ans.setNodetype(nodetype);
            		return ans;
            	}
            	if(nodetype.equals("Unique")) {	// TODO : Dirty edit!
            		
            		AlqpAggregateNode ans = new AlqpAggregateNode();
            		JSONObject objTemp=arr.getJSONObject(0);
            		ans.setChild(parseAsAlqpNode((objTemp.getJSONArray("Plans").getJSONObject(0))));
            		//StringUtils.toStringList(obj.getJSONArray("Sort Key");
            		//Object groupkeysString=objTemp.get("Sort Key");
            		
//            		String str=objTemp.get("Sort Key").toString();
//            		String substr=str.substring(str.indexOf("ROW")+4,str.indexOf(")"));
//            		//ans.setChild(parseAsAlqpNode(arr.getJSONObject(0)));
//            		//String[]y=;
//            		
//            		List<String> groupkeys=Arrays.asList(substr.split(","));
            		//t.addAll(y);
            		//obj.getJSONArray("Group Key");
            		
            		//temp.addAll(groupkeysString);
            		//StringUtils.toStringList(obj.getJSONArray("Group Key").toList());
            		ans.setGroupKey(StringUtils.toStringList(objTemp.getJSONArray("Sort Key").toList()));
            		ans.setOutputCardinality(outputCardinality);
            		ans.setNodetype(nodetype);
            		return ans;
            	}

                AlqpNode leftChild = parseAsAlqpNode(arr.getJSONObject(0));
                AlqpNode rightChild = null;
                if (arr.length() == 2) {
                    rightChild = parseAsAlqpNode(arr.getJSONObject(1));
                    //TODO: put automated check for PK-FK join
                    System.out.println("MANUAL FARZI CHECK PK-FK join " + nodetype + " " + conditionStr);
                }

                AlqpInternalNode ans = new AlqpInternalNode();
                ans.setNodetype(nodetype);
                ans.setConditionStr(conditionStr);
                ans.setOutputCardinality(outputCardinality);
                ans.setLeftChild(leftChild);
                ans.setRightChild(rightChild);

                if ("Bitmap Heap Scan".equals(nodetype)) {
                    ans.setRelname(obj.getString("Relation Name"));
                    ans.setAlias(obj.getString("Alias"));
                }

                return ans;
            } else
                throw new ParseException("Found zero/multiple plans in JSONObject: " + obj + " while expected 1 or 2");
        } else if ("CTE Scan".equals(nodetype)) {
            AlqpLeafNode ans = new AlqpLeafNode();
            ans.setNodetype(nodetype);
            ans.setRelname(obj.getString("CTE Name"));
            ans.setAlias(obj.getString("Alias"));
            ans.setConditionStr(conditionStr);
            ans.setOutputCardinality(outputCardinality);
            return ans;

        } else if ("Bitmap Index Scan".equals(nodetype)) {
            AlqpLeafIndexNode ans = new AlqpLeafIndexNode();
            ans.setNodetype(nodetype);
            ans.setConditionStr(conditionStr);
            ans.setOutputCardinality(outputCardinality);
            ans.setIndexConditionStr(obj.getString("Index Cond"));
            return ans;

        } else if ("Index Scan".equals(nodetype)) {
            AlqpLeafIndexNode ans = new AlqpLeafIndexNode();
            ans.setNodetype(nodetype);
            ans.setRelname(obj.getString("Relation Name"));
            ans.setAlias(obj.getString("Alias"));
            ans.setConditionStr(conditionStr);
            ans.setOutputCardinality(outputCardinality);
            if (obj.has("Index Cond")) {
                ans.setIndexConditionStr(obj.getString("Index Cond"));
            }
            return ans;

        } else if (!"Seq Scan".equals(nodetype) && nodetype.contains("Scan"))
            throw new RuntimeException("Found a new type of scan: " + nodetype + ". Check if its outputCardinality is infact outputCardinality");
        else {
            //} else if("Seq Scan".equals(nodetype) {

            AlqpLeafNode ans = new AlqpLeafNode();
            ans.setNodetype(nodetype);
            ans.setRelname(obj.getString("Relation Name"));
            ans.setAlias(obj.getString("Alias"));
            ans.setConditionStr(conditionStr);
            ans.setOutputCardinality(outputCardinality);
            return ans;
        }
    }

}
