package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;

public class ConditionJoin extends Condition {

    public ConditionJoin(Condition cond1, Condition cond2, long outputCardinality, String conditionStr) {
        super(outputCardinality);
        
       
        //PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(tablename);
       
        this.relnames.addAll(cond1.getRelnames());
        this.relnames.addAll(cond2.getRelnames());

        this.predicates.addAll(cond1.getPredicates());
        this.predicates.addAll(cond2.getPredicates());
        
        
        this.pkfkJoinList.addAll(cond1.pkfkJoinList);
        this.pkfkJoinList.addAll(cond2.pkfkJoinList);
        
        // A.a = 10  // C.c  // D.d  // B,D  D.fk B.pk   
         // pkfkList = [A,B]    Leaf node : colPath [[A]] |  Join node: predicates [a] relnames[A,B], pkfkList =[A,B] colPath=[[A,B]]
        // C join D  : Join node: predicates [c,d] relnames[C,D], pkfkList =[C,D] colPath=[[C,D],[D]]
        // (AjoinB) join (CjoinD) : join Node : predicates [a,c,d] relnames[A,B, C,D], pkfkList =[A,B,C,D,B,D] colPath=[[A,B,D],[C,D],[D]]
        
        
        //  [A,B,D]    [A,B,D]
        // [A,B,C,D,B,D]
        
        
        
        /*
         *    A:pk
         *    B:fk  B->A, D->C, D->B // DAG 
         *    
         *    
         *    A : a : 1,2
         *    B ; b_a : 1,1,1,1,2,2
         *    
         *    A join B : 6 tuples : 1->4  2->2
         * 
         * 
         */
        
        
        /*
        System.out.println("this.cond : " + this.col_path);
        System.out.println("Cond1 : " + cond1.col_path);
        System.out.println("Cond2 : " + cond2.col_path);
        */
        
        // get both relation names in conditionStr
        // Exp:- (store_sales.ss_sold_time_sk = time_dim.t_time_sk)
        
        
        
        //System.out.println("conditionStr :" + conditionStr);
        conditionStr.strip();
        String[] rels = conditionStr.split(" = ");
        List<String> relations =  new ArrayList<String>();
        for(String s : rels)
        {
        	String r = s.split("\\.")[0].replace('(', ' ').strip();
        	for(RelnameAlias ra : this.relnames)
        	{
        		if(ra.getAlias().contentEquals(r))
        		{
        			relations.add(ra.getRelname());
        		}
        	}
        	
        	
        }
       // System.out.println("relations" + relations);
        if(relations.size() != 2)
        {
        	System.out.println("Error in conditionStr");
        }
        
        int relname1TopoSeqno = PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(relations.get(0)).getTopoSeqno();
    	int relname2TopoSeqno = PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(relations.get(1)).getTopoSeqno();
    	if(relname1TopoSeqno > relname2TopoSeqno)
    	{
    		String pkTable = new String(relations.get(0));
    		String fkTable = new String(relations.get(1));
    		System.out.println("pkTable" + pkTable);
    		System.out.println("fkTable" + fkTable);
    		this.pkfkJoinList.add(pkTable);
    		this.pkfkJoinList.add(fkTable);
    	}
    	else if(relname1TopoSeqno < relname2TopoSeqno)
    	{
    		
    		String pkTable = new String(relations.get(1));
    		String fkTable = new String(relations.get(0));
    		//System.out.println("pkTable" + pkTable);
    		//System.out.println("fkTable" + fkTable);
    		
    		this.pkfkJoinList.add(pkTable);
    		this.pkfkJoinList.add(fkTable);
    	}
    	else
    	{
    		System.out.println("Should not be reaching here");
    		System.out.println("Reason : Not a pk-fk join type");
    	}
    		
    	//System.out.println("pkfkJoinList : " + this.pkfkJoinList);
       
        
        
        if(cond1.colPath.isEmpty() && cond2.colPath.isEmpty())
        {
        	//System.out.println("empty both : " + cond1 + " v " + cond2.col_path.isEmpty());
        }
        else if(cond1.colPath.isEmpty())
        {
        	
        	String relname1 = getParentView(cond1.getRelnames());   // left subtree inverntory
        	String arbitrary_pred = cond2.getPredicates().get(0); //  [[cata_sales], [custmer,cata_sales]]
        	List<String> rel_list = cond2.colPath.get(0);
        	String relname2 = rel_list.get(rel_list.size() - 1);    	
        	
        	int topo1 = PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(relname1).getTopoSeqno();
        	int topo2 = PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(relname2).getTopoSeqno();
        	String topView = relname1;
        	if(topo1 < topo2)
        	{
        		topView = relname1;
        	}
        	else if(topo1 > topo2)
        	{
        		topView = relname2;
        	}
        	else
        	{
        		System.out.println("not a pk-fk join");
        	}
        	/*
        	for(Entry<String,List<String>> e : cond2.col_path.entrySet())
    		{
    			List<String> temp = new ArrayList<String>();
    			temp.addAll(e.getValue());
    			if(!temp.get(temp.size()-1).contentEquals(topView))
    				temp.add(topView);
    			this.col_path.put(e.getKey(), temp);
    		}
        	*/
        	for(List<String> l : cond2.colPath)
        	{
        		ArrayList<String> temp = new ArrayList<String>();
    			temp.addAll(l);
    			if(!temp.get(temp.size()-1).contentEquals(topView))
    				temp.add(topView);
        		this.colPath.add(temp);
        	}
        	
        	 //[ [cata_sales,inventory], [custmer,cata_sales, inventory]]
        	//  [ [cata_sales], [custmer,cata_sales]]
        	
        	
        	
        	
        	
        }
        else if(cond2.colPath.isEmpty())
        {
        	        	
        	String relname2 = getParentView(cond2.getRelnames());
        	String arbitrary_pred = cond1.getPredicates().get(0);
        	List<String> rel_list = cond1.colPath.get(0);
        	String relname1 = rel_list.get(rel_list.size() - 1);
        	
        	int topo1 = PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(relname1).getTopoSeqno();
        	int topo2 = PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(relname2).getTopoSeqno();
        	String topView = relname1;
        	if(topo1 < topo2)
        	{
        		topView = relname1;
        	}
        	else if(topo1 > topo2)
        	{
        		topView = relname2;
        	}
        	else
        	{
        		System.out.println("not a pk-fk join");
        	}
        	/*
        	for(Entry<String,List<String>> e : cond1.col_path.entrySet())
    		{
    			List<String> temp = new ArrayList<String>();
    			temp.addAll(e.getValue());
    			if(!temp.get(temp.size()-1).contentEquals(topView))
    				temp.add(topView);
    			this.col_path.put(e.getKey(), temp);
    		}
    		*/
        	for(List<String> l : cond1.colPath)
        	{
        		ArrayList<String> temp = new ArrayList<String>();
    			temp.addAll(l);
    			if(!temp.get(temp.size()-1).contentEquals(topView))
    				temp.add(topView);
        		this.colPath.add(temp);
        	}
        	
        	
        	        	
        }
        else
        {
        	        	
        	// [ [item, inventory] , [ date_dim,inventory], [customer, inv] , [cu_addr, customer, inv] ]
        	String arbitrary_pred = cond1.getPredicates().get(0);      
        	List<String> rel_list1 = cond1.colPath.get(0);     //  [ [item, inventory] , [ date_dim,inventory] ]
        	String relname1 = rel_list1.get(rel_list1.size() - 1);   //   inventory
        	arbitrary_pred = cond2.getPredicates().get(0);   // [ [customer] , [cu_addr, customer]]
        	List<String> rel_list2 = cond2.colPath.get(0);              
        	String relname2 = rel_list2.get(rel_list2.size() - 1);  //  customer
        	
        	int topo1 = PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(relname1).getTopoSeqno();
        	int topo2 = PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(relname2).getTopoSeqno();
        	String topView = relname1;
        	if(topo1 < topo2)
        	{
        		topView = relname1;
        	}
        	else if(topo1 > topo2)
        	{
        		topView = relname2;
        	}
        	else
        	{
        		System.out.println("not a pk-fk join");
        	}
        	/*
        	for(Entry<String,List<String>> e : cond1.col_path.entrySet())
    		{
    			List<String> temp = new ArrayList<String>();
    			temp.addAll(e.getValue());
    			if(!temp.get(temp.size()-1).contentEquals(topView))
    				temp.add(topView);
    			this.col_path.put(e.getKey(), temp);
    		}
        	for(Entry<String,List<String>> e : cond2.col_path.entrySet())
    		{
    			List<String> temp = new ArrayList<String>();
    			temp.addAll(e.getValue());
    			if(!temp.get(temp.size()-1).contentEquals(topView))
    				temp.add(topView);
    			this.col_path.put(e.getKey(), temp);
    		}
        	*/
        	for(List<String> l : cond1.colPath)
        	{
        		ArrayList<String> temp = new ArrayList<String>();
    			temp.addAll(l);
    			if(!temp.get(temp.size()-1).contentEquals(topView))
    				temp.add(topView);
        		this.colPath.add(temp);
        	}
        	for(List<String> l : cond2.colPath)
        	{
        		ArrayList<String> temp = new ArrayList<String>();
    			temp.addAll(l);
    			if(!temp.get(temp.size()-1).contentEquals(topView))
    				temp.add(topView);
        		this.colPath.add(temp);
        	}
        	
        	      	      	
       }
       
       checkColPathsCorrectness(cond1,cond2); // for list of list
       //checkColPathsCorrectness1(cond1,cond2); // for dictionay
        
    }
    


	

/*
    private void checkColPathsCorrectness1(Condition cond1, Condition cond2) {
		
    	
    	//String parent = getParentView(this.getRelnames());
    	// Iterate over each predicate
      	
    	for(String pred : this.getPredicates())
    	{
    		   		
    		List<String> colPath = this.col_path.get(pred);
    		String destRelation = colPath.get(colPath.size()-1); 
    		String sourceRelation = colPath.get(0); 
    		String currentRelation = new String(destRelation);
    		int currentIndex = colPath.size()-1;
    		System.out.println("colPath : " + colPath);
    		
    		if (colPath.size() == 1)
    		{
    			continue;
    		}
    		
    		while(currentRelation != sourceRelation)
    		{
    			String prevRelation = colPath.get(currentIndex-1);
    			boolean flag = true;
    			int pk = -1;
    			int pkFlag = 0;
    			for(int i=0;i<this.pkfkJoinList.size()-1;i+=2)
    			{
    				if(this.pkfkJoinList.get(i+1).contentEquals(currentRelation) && this.pkfkJoinList.get(i).contentEquals(prevRelation))
    				{
    					currentIndex = currentIndex -1;
    					currentRelation = colPath.get(currentIndex);
    					flag = false;
    					break;
    				}
    				
    				System.out.println("prevRelation : " + prevRelation);
    				System.out.println("currentRelation : " + currentRelation);
    				System.out.println("this.pkfkJoinList : " + this.pkfkJoinList);
    				System.out.println("colPath : " + colPath);
    				if(this.pkfkJoinList.get(i).contentEquals(prevRelation))
    				{
    					pk = i+1;
    					pkFlag++;
    				}
    				
    			}
    			
    			if(flag)
    			{
    				//System.out.println("Not a correct path ");
    				if(pk == -1)
        			{
        				System.out.println("fk-fk join query");
        			}
    				colPath.add(currentIndex, this.pkfkJoinList.get(pk));
    				currentRelation = this.pkfkJoinList.get(pk);
    				
    				
    			}
    			if(pkFlag > 1)
    			{
    				
    				System.out.println(" Concept issue !! ");
    				break;
    			}
 
    			
    		}
    		
    	}
    	
		
	}
*/

    private void checkColPathsCorrectness(Condition cond1, Condition cond2) {
		
    	
    	//String parent = getParentView(this.getRelnames());
    	// Iterate over each predicate
    	// this.colPath =  [ [customer_address, catalog_sales], [item, catalog_sales] ]
    	System.out.println("colPath : " + colPath);

    	for(List<String> currentColPath : this.colPath)
    	{
    		   		
    		    		
    		String destRelation = currentColPath.get(currentColPath.size()-1);    // catalog_sales
    		String sourceRelation = currentColPath.get(0);    // customer_address
    		String currentRelation = new String(destRelation);  // catalog_sales
    		int currentIndex = currentColPath.size()-1;         // 1
    		System.out.println("current colPath : " + currentColPath);
    		
    		if (currentColPath.size() == 1)
    		{
    			continue;
    		}
    		
    		while(currentRelation != sourceRelation)
    		{
    			String prevRelation = currentColPath.get(currentIndex-1);  // `customer_address
    			boolean flag = true;
    			int pk = -1;
    			int pkFlag = 0;
    			// colPath : [customer_address, customer]
    			// colPath : [customer_address, customer, catalog_sales]
    			// pkfkJoinList : [customer_address, catalog_sales, customer_address, customer, customer, catalog_sales]
    			// pkfkJoinList : [customer, catalog_sales, customer_addr, customer]
    			// currentRelation : catalog_sales
    			for(int i=0;i<this.pkfkJoinList.size()-1;i+=2)
    			{
    				if(this.pkfkJoinList.get(i+1).contentEquals(currentRelation) && this.pkfkJoinList.get(i).contentEquals(prevRelation))
    				{
    					currentIndex = currentIndex -1;
    					currentRelation = currentColPath.get(currentIndex);
    					flag = false;
    					break;
    				}
    				
    				System.out.println("prevRelation : " + prevRelation);
    				System.out.println("currentRelation : " + currentRelation);
    				System.out.println("this.pkfkJoinList : " + this.pkfkJoinList);
    				System.out.println("colPath : " + currentColPath);
    				if(this.pkfkJoinList.get(i).contentEquals(prevRelation))
    				{
    					pk = i+1;
    					pkFlag++;
    				}
    				
    			}
    			
    			if(flag)
    			{
    				//System.out.println("Not a correct path ");
    				if(pk == -1)
        			{
        				System.out.println("fk-fk join query");
        			}
    				currentColPath.add(currentIndex, this.pkfkJoinList.get(pk));
    				currentRelation = this.pkfkJoinList.get(pk);
    				
    				
    			}
    			if(pkFlag > 1)
    			{
    				
    				System.out.println(" Concept issue !! ");
    				break;
    			}
 
    			
    		}
    		
    	}
    	
		
	}

   
    


	public String getParentView(List<RelnameAlias> relnameAliasList) {
        String parentViewname = null;
        int minSeqno = Integer.MAX_VALUE;
        for (RelnameAlias relnameAlias : relnameAliasList) {
            String viewname = relnameAlias.getRelname();
            int seqno = PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(viewname).getTopoSeqno();
            if (minSeqno > seqno) {
                minSeqno = seqno;
                parentViewname = viewname;
            }
        }

        if (parentViewname == null)
            throw new RuntimeException("Should not be reaching here");

        return parentViewname;
    }
    
        
}
