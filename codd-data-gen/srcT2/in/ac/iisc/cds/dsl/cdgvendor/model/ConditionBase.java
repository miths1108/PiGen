package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;

public class ConditionBase extends Condition {

    public ConditionBase(String relname, String alias, String predicate, long outputCardinality) {
        super(outputCardinality);

        this.relnames.add(new RelnameAlias(relname, alias));

        //        //(sm_carrier = ANY ('{DIAMOND,AIRBORNE}'::bpchar[]))
        //        //((sm_carrier = 12) OR (sm_carrier = 13))
        //        if (predicate != null && predicate.contains(" = ANY ('{")) {
        //
        //            String column = predicate.substring(predicate.indexOf("(") + 1, predicate.indexOf("=") - 1);
        //            String[] values = predicate.substring(predicate.indexOf("{") + 1, predicate.indexOf("}")).split(",");
        //            String str = "(";
        //
        //            if (predicate.contains("::bpchar[]")) {
        //                for (String val : values) {
        //                    str += "(" + column + " = '" + val + "')" + OR;
        //                }
        //            } else if (predicate.contains("::integer[]")) {
        //                for (String val : values) {
        //                    str += "(" + column + " = " + val + ")" + OR;
        //                }
        //            } else {
        //                throw new RuntimeException("Unrecognized type of ANY clause in predicate " + predicate);
        //            }
        //
        //            str = StringUtils.removeEnd(str, OR);
        //            str += ")";
        //            this.predicates.add(str);
        //        }
        //
        //        //((d_moy = 12) AND (d_year = 1998))
        //        //(d_moy = 12), (d_year = 1998)
        //        else if (predicate != null && predicate.contains(") AND (")) {
        //            predicate = predicate.substring(1, predicate.length() - 1);
        //            String[] values = predicate.split(AND);
        //            for (String val : values) {
        //                this.predicates.add(val);
        //            }
        //        }
        //
        //        else 
        if (predicate != null) {
            this.predicates.add(predicate);
            ArrayList<String> rel_list = new ArrayList<String>(); 
            rel_list.add(relname);
            //this.col_path.put(predicate, rel_list);  
            this.colPath.add(rel_list);
          
            
        }
        

    }

}
