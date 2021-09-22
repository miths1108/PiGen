package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.apache.commons.lang3.StringUtils;

import in.ac.iisc.cds.dsl.cdgvendor.constants.Constants;


public class Condition {

    private final long                 outputCardinality;
    protected final List<RelnameAlias> relnames;
    protected final List<String>       predicates;       //TODO: Model as composite predicates instead of List<String> of predicates
    //protected HashMap<String,List<String>>  col_path;  // key : predicate, value : List of relations
    protected List<String> pkfkJoinList; 
    protected List<ArrayList<String>> colPath;      // each row will represent the path(list of string) in terms of relations
    
 

    public Condition(long outputCardinality) {
        
		this.outputCardinality = outputCardinality;
        this.relnames = new ArrayList<>();
        this.predicates = new ArrayList<>(); 
        // List of list 
       // this.col_path = new HashMap<String, List<String>>();  // key : predicate,  value : 
        this.pkfkJoinList = new ArrayList<>();
        this.colPath = new ArrayList<ArrayList<String>>();
        
    }

    @Override
    public String toString() {
        StringBuilder sb;

        sb = new StringBuilder();
        for (String predicate : predicates) {
            sb.append(predicate).append(Constants.AND_OPERATOR);
        }
        String predtsr = StringUtils.removeEnd(sb.toString(), Constants.AND_OPERATOR);

        sb = new StringBuilder();
        for (RelnameAlias relname : relnames) {
            sb.append(relname.toString() + Constants.JOIN_OPERATOR);
        }
        /*
         *  relstr : represents the relation(or join of relations)'
         *  Exp:-  store_sales≡store_sales ⋈ time_dim≡time_dim ⋈ store≡store
         *  
         *  predstr : represents the condition(or SOP of conditions) 
         *  Exp :- ((t_minute >= 30) AND (t_hour = 8)) ∧ ((s_store_name)::text = 'ese'::text)
         *   
         */
        
        
        String relstr = StringUtils.removeEnd(sb.toString(), Constants.JOIN_OPERATOR);

        return "|" + Constants.SEL_OPERATOR + "( " + predtsr + " ) (" + relstr + ") | = " + outputCardinality +  "\n colPath : " + colPath;
    }

    public long getOutputCardinality() {
        return outputCardinality;
    }

    public List<String> getPredicates() {
        return predicates;
    }

    public List<RelnameAlias> getRelnames() {
        return relnames;
    }

    /*
	public HashMap<String, List<String>> getColPath() {
		
		return col_path;
	}

	public List<String> getColPath(String conditionStr) {
		
		return col_path.get(conditionStr);
	}
	*/
    
	public List<String> getColPath(int index) {
		return colPath.get(index);
	}
 
    
    

}
