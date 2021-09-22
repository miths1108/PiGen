package in.ac.iisc.cds.dsl.cdgvendor.utils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import in.ac.iisc.cds.dsl.cdgclient.anonymizer.Anonymizer;
import in.ac.iisc.cds.dsl.cdgvendor.model.Condition;
import in.ac.iisc.cds.dsl.cdgvendor.model.ConditionAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.antlr.PredicateLexer;
import in.ac.iisc.cds.dsl.cdgvendor.model.antlr.PredicateParser;
import in.ac.iisc.cds.dsl.cdgvendor.model.antlr.PredicateParser.AndedConditionContext;
import in.ac.iisc.cds.dsl.cdgvendor.model.antlr.PredicateParser.ConditionStrContext;
import in.ac.iisc.cds.dsl.cdgvendor.model.antlr.PredicateParser.JoinConditionContext;
import in.ac.iisc.cds.dsl.cdgvendor.model.antlr.PredicateParser.OredConditionContext;
import in.ac.iisc.cds.dsl.cdgvendor.model.antlr.PredicateParser.PredicateContext;
import in.ac.iisc.cds.dsl.cdgvendor.model.antlr.PredicateParser.SimpleConditionContext;
import in.ac.iisc.cds.dsl.cdgvendor.model.antlr.PredicateParser.SimpleDateConditionContext;
import in.ac.iisc.cds.dsl.cdgvendor.model.antlr.PredicateParser.SimpleINConditionContext;
import in.ac.iisc.cds.dsl.cdgvendor.model.antlr.PredicateParser.SimpleNumConditionContext;
import in.ac.iisc.cds.dsl.cdgvendor.model.antlr.PredicateParser.SimpleStrConditionContext;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAnd;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAndAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionBaseAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionOr;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionOrAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleDate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleDateAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleJoin;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleNumber;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleNumberAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleString;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleStringAggregate;

public class Converter {

    public static List<FormalCondition> getAsFormalConditions(List<Condition> conditions, Anonymizer anonymizer) {

        List<FormalCondition> formalConditions = new ArrayList<>();
        for (Condition condition : conditions) {
        	
//            long outputCardinality = condition.getOutputCardinality();
//            String viewname = anonymizer.getParentView(condition.getRelnames());
//            List<String> conditionStrs = condition.getPredicates();
//
//            List<FormalCondition> currConditions = new ArrayList<>();
//            for (String conditionStr : conditionStrs) {
//                currConditions.add(parseConditionStr(conditionStr, outputCardinality, viewname));
//            }
//
//            FormalCondition toAdd = null;
//            if (currConditions.size() == 1) {
//                toAdd = currConditions.get(0);
//            } else if (currConditions.size() > 1) {
//                FormalConditionAnd formalConditionAnd = new FormalConditionAnd();
//                for (FormalCondition tempCurrCondition : currConditions) {
//                    formalConditionAnd.addCondition(tempCurrCondition);
//                }
//                formalConditionAnd.setOutputCardinality(outputCardinality);
//                formalConditionAnd.setViewname(viewname);
//                toAdd = formalConditionAnd;
//            } else {
//                continue;
//                //Skipping predicate less conditions which are nothing but relation rowcount constraints
//            }
        	
        	FormalCondition toAdd = getAsFormalCondition(condition, anonymizer);
        	
        	if(toAdd != null)
        		formalConditions.add(toAdd);
        }

        return formalConditions;
    }

    public static FormalCondition getAsFormalCondition(Condition condition, Anonymizer anonymizer) {

        long outputCardinality = condition.getOutputCardinality();
        String viewname = anonymizer.getParentView(condition.getRelnames());
        
        
        /* written if else just have check in whether view naming in CCs is having same value */
       /*
        if(condition.getPredicates().isEmpty())
        {
        	String viewname2 = anonymizer.getParentView(condition.getRelnames());
        	  //System.out.println("Viewname comparison " + viewname + "  " + viewname2);
        	  if(!viewname.contentEquals(viewname2))
        	  {
        		  System.out.println("error in path code for Condition class"); // will change to throw exception
        	  }
        }
        
        else
        {
        	HashMap<String, List<String>> col_path = condition.getColPath();
        	List<String> predicates = condition.getPredicates();
        	List<String> temp = col_path.get(predicates.get(0));
        	//System.out.println("Viewname comparison " + viewname + "  " + temp.get(temp.size()-1));
        	if(!viewname.contentEquals(temp.get(temp.size()-1)))
      	  {
      		  System.out.println("error in path code for Condition class");
      	  }
        }
        */
        
        
       
        List<String> conditionStrs = condition.getPredicates();

        List<FormalCondition> currConditions = new ArrayList<>();
        // iterate with i 
        /*
        for (String conditionStr : conditionStrs) {
        	List<String> col_path = condition.getColPath(conditionStr);
        	
            currConditions.add(parseConditionStr(conditionStr, outputCardinality, viewname,col_path));
        }
        */
        for(int condIndex=0; condIndex < conditionStrs.size(); condIndex++)
        {
        	String conditionStr = conditionStrs.get(condIndex);
        	List<String> colPath = condition.getColPath(condIndex);
        	currConditions.add(parseConditionStr(conditionStr, outputCardinality, viewname,colPath));
        }
        
        FormalCondition toAdd = null;
        if (currConditions.size() == 1) {
            toAdd = currConditions.get(0);
        } else if (currConditions.size() > 1) {
            FormalConditionAnd formalConditionAnd = new FormalConditionAnd();
            for (FormalCondition tempCurrCondition : currConditions) {
                formalConditionAnd.addCondition(tempCurrCondition);
            }
            formalConditionAnd.setOutputCardinality(outputCardinality);
            formalConditionAnd.setViewname(viewname);
            toAdd = formalConditionAnd;
        } else {
    //    	if(condition instanceof ConditionAggregate) {
////        		ConditionAggregate origCondition = (ConditionAggregate) condition;
////        		return new FormalConditionBaseAggregate(origCondition, viewname, outputCardinality);
//        		throw new RuntimeException("Not implemented: Query with GROUP BY but no filter (WHERE) not allowed");	// Can create FormalConditionBaseAggregate
//        	}
            return null;
        }
        
        if(condition instanceof ConditionAggregate) {
        	ConditionAggregate origCondition = (ConditionAggregate) condition;
        	FormalCondition temp = null;
        	if (toAdd instanceof FormalConditionAnd) {
            	temp = new FormalConditionAndAggregate(origCondition, (FormalConditionAnd) toAdd);
        	} else if (toAdd instanceof FormalConditionOr) {
            	temp = new FormalConditionOrAggregate(origCondition, (FormalConditionOr) toAdd);
        	} else if (toAdd instanceof FormalConditionSimpleString) {
            	temp = new FormalConditionSimpleStringAggregate(origCondition, (FormalConditionSimpleString) toAdd);
        	} else if (toAdd instanceof FormalConditionSimpleNumber) {
            	temp = new FormalConditionSimpleNumberAggregate(origCondition, (FormalConditionSimpleNumber) toAdd);
        	} else if (toAdd instanceof FormalConditionSimpleDate) {
            	temp = new FormalConditionSimpleDateAggregate(origCondition, (FormalConditionSimpleDate) toAdd);
        	} else {
        		throw new RuntimeException("Not implemented for " + toAdd.getClass().toString());
        	}
        	toAdd = temp;
        }
        
       

        return toAdd;
    }

    // TODO : implement functionality in a single function
    public static FormalCondition parseConditionStr(String conditionStr, long outputCardinality, String viewname, List<String> col_path) {

        // Get our lexer
        PredicateLexer lexer = new PredicateLexer(new ANTLRInputStream(conditionStr));

        // Get a list of matched tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Pass the tokens to the parser
        PredicateParser parser = new PredicateParser(tokens);

        // Specify our entry point
        PredicateContext predicateContext = parser.predicate();
        return parseTreeNode(predicateContext.getChild(0), outputCardinality, viewname,col_path);
    }
    public static FormalCondition parseConditionStr(String conditionStr, long outputCardinality, String viewname) {

        // Get our lexer
        PredicateLexer lexer = new PredicateLexer(new ANTLRInputStream(conditionStr));

        // Get a list of matched tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Pass the tokens to the parser
        PredicateParser parser = new PredicateParser(tokens);

        // Specify our entry point
        PredicateContext predicateContext = parser.predicate();
        return parseTreeNode(predicateContext.getChild(0), outputCardinality, viewname);
    }
    
  //TODO: Reduce LOC in this method
    private static FormalCondition parseTreeNode(ParseTree parseTree, long outputCardinality, String viewname) {
 
    	    	
        if (parseTree instanceof AndedConditionContext) {
            AndedConditionContext andedConditionContext = (AndedConditionContext) parseTree;

            FormalConditionAnd formalConditionAnd = new FormalConditionAnd();
            for (int i = 0; i < andedConditionContext.conditionStr().size(); i++) {
                formalConditionAnd.addCondition(parseTreeNode(andedConditionContext.conditionStr().get(i), outputCardinality, viewname));
            }
            formalConditionAnd.setOutputCardinality(outputCardinality);
            formalConditionAnd.setViewname(viewname);
            return formalConditionAnd;

        } else if (parseTree instanceof OredConditionContext) {
            OredConditionContext oredConditionContext = (OredConditionContext) parseTree;

            FormalConditionOr formalConditionOr = new FormalConditionOr();
            for (int i = 0; i < oredConditionContext.conditionStr().size(); i++) {
                formalConditionOr.addCondition(parseTreeNode(oredConditionContext.conditionStr().get(i), outputCardinality, viewname));
            }
            formalConditionOr.setOutputCardinality(outputCardinality);
            formalConditionOr.setViewname(viewname);
            return formalConditionOr;

        } else if (parseTree instanceof SimpleINConditionContext) {
            SimpleINConditionContext simpleINConditionContext = (SimpleINConditionContext) parseTree;

            FormalConditionOr formalConditionOr = new FormalConditionOr();
            String columnname = simpleINConditionContext.columnname().getText();
            String symbolStr = "=";

            if (!simpleINConditionContext.stringliteral().isEmpty() && simpleINConditionContext.INTEGER().isEmpty()) {
                for (int i = 0; i < simpleINConditionContext.stringliteral().size(); i++) {
                    String valueStr = simpleINConditionContext.stringliteral().get(i).getText();
                    if(valueStr.contains(" ")) {
                    	if(valueStr.charAt(0) != '\"' || valueStr.charAt(valueStr.length() - 1) != '\"')
                    		throw new RuntimeException("String contains spaces so expecting double quotes (\") at start and end but quotes not found"); // happens either in case of ::text or ::bpchar or something I don't remember
                    	valueStr = valueStr.substring(1,  valueStr.length() - 1);
                    }
                    FormalConditionSimpleString formalConditionSimpleString = new FormalConditionSimpleString(columnname, symbolStr, valueStr);
                    formalConditionSimpleString.setOutputCardinality(outputCardinality); //TODO: Why needed to set outputcardinality in inner conditions
                    formalConditionSimpleString.setViewname(viewname);
                    formalConditionOr.addCondition(formalConditionSimpleString);
                }
                formalConditionOr.setOutputCardinality(outputCardinality);
                formalConditionOr.setViewname(viewname);
                return formalConditionOr;
            }
            if (simpleINConditionContext.stringliteral().isEmpty() && !simpleINConditionContext.INTEGER().isEmpty()) {
                for (int i = 0; i < simpleINConditionContext.INTEGER().size(); i++) {
                    String valueStr = simpleINConditionContext.INTEGER().get(i).getText();
                    FormalConditionSimpleNumber formalConditionSimpleNumber = new FormalConditionSimpleNumber(columnname, symbolStr, valueStr);
                    formalConditionSimpleNumber.setOutputCardinality(outputCardinality); //TODO: Why needed to set outputcardinality in inner conditions
                    formalConditionSimpleNumber.setViewname(viewname);
                    formalConditionOr.addCondition(formalConditionSimpleNumber);
                }
                formalConditionOr.setOutputCardinality(outputCardinality);
                formalConditionOr.setViewname(viewname);
                return formalConditionOr;
            }
            throw new RuntimeException("Unable to parse simpleINConditionContext " + simpleINConditionContext.getText());

        } else if (parseTree instanceof JoinConditionContext) {
            JoinConditionContext joinConditionContext = (JoinConditionContext) parseTree;

            String columnname1 = joinConditionContext.columnname(0).getText();
            String columnname2 = joinConditionContext.columnname(1).getText();

            FormalConditionSimpleJoin formalConditionSimpleJoin = new FormalConditionSimpleJoin(columnname1, columnname2);
            formalConditionSimpleJoin.setOutputCardinality(outputCardinality);
            formalConditionSimpleJoin.setViewname(viewname);

            return formalConditionSimpleJoin;

        } else if (parseTree instanceof SimpleNumConditionContext) {
            SimpleNumConditionContext simpleNumConditionContext = (SimpleNumConditionContext) parseTree;

            String columnname = simpleNumConditionContext.columnname().getText();
            String symbolStr = simpleNumConditionContext.symbol().getText();
            String numberStr = simpleNumConditionContext.number().getText();

            FormalConditionSimpleNumber formalConditionSimpleNumber = new FormalConditionSimpleNumber(columnname, symbolStr, numberStr);
            formalConditionSimpleNumber.setOutputCardinality(outputCardinality);
            formalConditionSimpleNumber.setViewname(viewname);
            return formalConditionSimpleNumber;

        } else if (parseTree instanceof SimpleStrConditionContext) {
            SimpleStrConditionContext simpleStrConditionContext = (SimpleStrConditionContext) parseTree;

            String columnname = simpleStrConditionContext.columnname().getText();
            String symbolStr = simpleStrConditionContext.symbol().getText();

            if (simpleStrConditionContext.stringliteral() != null && simpleStrConditionContext.INTEGER() == null && simpleStrConditionContext.DECIMAL() == null) {
                String valueStr = simpleStrConditionContext.stringliteral().getText();
                FormalConditionSimpleString formalConditionSimpleString = new FormalConditionSimpleString(columnname, symbolStr, valueStr);
                formalConditionSimpleString.setOutputCardinality(outputCardinality);
                formalConditionSimpleString.setViewname(viewname);
                return formalConditionSimpleString;
            }
            if (simpleStrConditionContext.stringliteral() == null && simpleStrConditionContext.INTEGER() != null && simpleStrConditionContext.DECIMAL() == null) {
                String valueStr = simpleStrConditionContext.INTEGER().getText();
                FormalConditionSimpleNumber formalConditionSimpleNumber = new FormalConditionSimpleNumber(columnname, symbolStr, valueStr);
                formalConditionSimpleNumber.setOutputCardinality(outputCardinality);
                formalConditionSimpleNumber.setViewname(viewname);
                return formalConditionSimpleNumber;
            }
            if (simpleStrConditionContext.stringliteral() == null && simpleStrConditionContext.INTEGER() == null && simpleStrConditionContext.DECIMAL() != null) {
                String valueStr = simpleStrConditionContext.DECIMAL().getText();
                FormalConditionSimpleNumber formalConditionSimpleNumber = new FormalConditionSimpleNumber(columnname, symbolStr, valueStr);
                formalConditionSimpleNumber.setOutputCardinality(outputCardinality);
                formalConditionSimpleNumber.setViewname(viewname);
                return formalConditionSimpleNumber;
            }
            throw new RuntimeException("Unable to parse simpleStrConditionContext " + simpleStrConditionContext.getText());

        } else if (parseTree instanceof SimpleDateConditionContext) {
            SimpleDateConditionContext simpleDateConditionContext = (SimpleDateConditionContext) parseTree;

            String columnname = simpleDateConditionContext.columnname().getText();
            String symbolStr = simpleDateConditionContext.symbol().getText();

            String dateStr;
            if (simpleDateConditionContext.DATE() != null && simpleDateConditionContext.TIMESTAMP() == null) {
                dateStr = simpleDateConditionContext.DATE().getText();
            } else if (simpleDateConditionContext.DATE() == null && simpleDateConditionContext.TIMESTAMP() != null) {
                dateStr = simpleDateConditionContext.TIMESTAMP().getText();
            } else
                throw new RuntimeException("Unable to parse SimpleDateConditionContext " + simpleDateConditionContext.getText());

            FormalConditionSimpleDate formalConditionSimpleDate = new FormalConditionSimpleDate(columnname, symbolStr, dateStr);
            formalConditionSimpleDate.setOutputCardinality(outputCardinality);
            formalConditionSimpleDate.setViewname(viewname);
            return formalConditionSimpleDate;
        } else if (parseTree instanceof ConditionStrContext)
            return parseTreeNode(parseTree.getChild(1).getChild(0), outputCardinality, viewname);
        else if (parseTree instanceof SimpleConditionContext)
            return parseTreeNode(parseTree.getChild(0), outputCardinality, viewname);
        else
            throw new RuntimeException("Should not be reaching here");

    }


  
	//TODO: Reduce LOC in this method
    private static FormalCondition parseTreeNode(ParseTree parseTree, long outputCardinality, String viewname, List<String> col_path) {
 
    	    	
        if (parseTree instanceof AndedConditionContext) {
            AndedConditionContext andedConditionContext = (AndedConditionContext) parseTree;

            FormalConditionAnd formalConditionAnd = new FormalConditionAnd();
            for (int i = 0; i < andedConditionContext.conditionStr().size(); i++) {
                formalConditionAnd.addCondition(parseTreeNode(andedConditionContext.conditionStr().get(i), outputCardinality, viewname, col_path));
            }
            formalConditionAnd.setOutputCardinality(outputCardinality);
            formalConditionAnd.setViewname(viewname);
            return formalConditionAnd;

        } else if (parseTree instanceof OredConditionContext) {
            OredConditionContext oredConditionContext = (OredConditionContext) parseTree;

            FormalConditionOr formalConditionOr = new FormalConditionOr();
            for (int i = 0; i < oredConditionContext.conditionStr().size(); i++) {
                formalConditionOr.addCondition(parseTreeNode(oredConditionContext.conditionStr().get(i), outputCardinality, viewname, col_path));
            }
            formalConditionOr.setOutputCardinality(outputCardinality);
            formalConditionOr.setViewname(viewname);
            return formalConditionOr;

        } else if (parseTree instanceof SimpleINConditionContext) {
            SimpleINConditionContext simpleINConditionContext = (SimpleINConditionContext) parseTree;

            FormalConditionOr formalConditionOr = new FormalConditionOr();
            String columnname = simpleINConditionContext.columnname().getText();
            String symbolStr = "=";

            if (!simpleINConditionContext.stringliteral().isEmpty() && simpleINConditionContext.INTEGER().isEmpty()) {
                for (int i = 0; i < simpleINConditionContext.stringliteral().size(); i++) {
                    String valueStr = simpleINConditionContext.stringliteral().get(i).getText();
                    if(valueStr.contains(" ")) {
                    	if(valueStr.charAt(0) != '\"' || valueStr.charAt(valueStr.length() - 1) != '\"')
                    		throw new RuntimeException("String contains spaces so expecting double quotes (\") at start and end but quotes not found"); // happens either in case of ::text or ::bpchar or something I don't remember
                    	valueStr = valueStr.substring(1,  valueStr.length() - 1);
                    }
                    FormalConditionSimpleString formalConditionSimpleString = new FormalConditionSimpleString(columnname, symbolStr, valueStr);
                    formalConditionSimpleString.setOutputCardinality(outputCardinality); //TODO: Why needed to set outputcardinality in inner conditions
                    formalConditionSimpleString.setViewname(viewname);
                    formalConditionSimpleString.setColPath(col_path);
                    formalConditionOr.addCondition(formalConditionSimpleString);
                }
                formalConditionOr.setOutputCardinality(outputCardinality);
                formalConditionOr.setViewname(viewname);
                return formalConditionOr;
            }
            if (simpleINConditionContext.stringliteral().isEmpty() && !simpleINConditionContext.INTEGER().isEmpty()) {
                for (int i = 0; i < simpleINConditionContext.INTEGER().size(); i++) {
                    String valueStr = simpleINConditionContext.INTEGER().get(i).getText();
                    FormalConditionSimpleNumber formalConditionSimpleNumber = new FormalConditionSimpleNumber(columnname, symbolStr, valueStr);
                    formalConditionSimpleNumber.setOutputCardinality(outputCardinality); //TODO: Why needed to set outputcardinality in inner conditions
                    formalConditionSimpleNumber.setViewname(viewname);
                    formalConditionSimpleNumber.setColPath(col_path);
                    formalConditionOr.addCondition(formalConditionSimpleNumber);
                }
                formalConditionOr.setOutputCardinality(outputCardinality);
                formalConditionOr.setViewname(viewname);
                return formalConditionOr;
            }
            throw new RuntimeException("Unable to parse simpleINConditionContext " + simpleINConditionContext.getText());

        } else if (parseTree instanceof JoinConditionContext) {
            JoinConditionContext joinConditionContext = (JoinConditionContext) parseTree;

            String columnname1 = joinConditionContext.columnname(0).getText();
            String columnname2 = joinConditionContext.columnname(1).getText();

            FormalConditionSimpleJoin formalConditionSimpleJoin = new FormalConditionSimpleJoin(columnname1, columnname2);
            formalConditionSimpleJoin.setOutputCardinality(outputCardinality);
            formalConditionSimpleJoin.setViewname(viewname);

            return formalConditionSimpleJoin;

        } else if (parseTree instanceof SimpleNumConditionContext) {
            SimpleNumConditionContext simpleNumConditionContext = (SimpleNumConditionContext) parseTree;

            String columnname = simpleNumConditionContext.columnname().getText();
            String symbolStr = simpleNumConditionContext.symbol().getText();
            String numberStr = simpleNumConditionContext.number().getText();

            FormalConditionSimpleNumber formalConditionSimpleNumber = new FormalConditionSimpleNumber(columnname, symbolStr, numberStr);
            formalConditionSimpleNumber.setOutputCardinality(outputCardinality);
            formalConditionSimpleNumber.setViewname(viewname);
            formalConditionSimpleNumber.setColPath(col_path);
            return formalConditionSimpleNumber;

        } else if (parseTree instanceof SimpleStrConditionContext) {
            SimpleStrConditionContext simpleStrConditionContext = (SimpleStrConditionContext) parseTree;

            String columnname = simpleStrConditionContext.columnname().getText();
            String symbolStr = simpleStrConditionContext.symbol().getText();

            if (simpleStrConditionContext.stringliteral() != null && simpleStrConditionContext.INTEGER() == null && simpleStrConditionContext.DECIMAL() == null) {
                String valueStr = simpleStrConditionContext.stringliteral().getText();
                FormalConditionSimpleString formalConditionSimpleString = new FormalConditionSimpleString(columnname, symbolStr, valueStr);
                formalConditionSimpleString.setOutputCardinality(outputCardinality);
                formalConditionSimpleString.setViewname(viewname);
                formalConditionSimpleString.setColPath(col_path);
                return formalConditionSimpleString;
            }
            if (simpleStrConditionContext.stringliteral() == null && simpleStrConditionContext.INTEGER() != null && simpleStrConditionContext.DECIMAL() == null) {
                String valueStr = simpleStrConditionContext.INTEGER().getText();
                FormalConditionSimpleNumber formalConditionSimpleNumber = new FormalConditionSimpleNumber(columnname, symbolStr, valueStr);
                formalConditionSimpleNumber.setOutputCardinality(outputCardinality);
                formalConditionSimpleNumber.setViewname(viewname);
                formalConditionSimpleNumber.setColPath(col_path);
                return formalConditionSimpleNumber;
            }
            if (simpleStrConditionContext.stringliteral() == null && simpleStrConditionContext.INTEGER() == null && simpleStrConditionContext.DECIMAL() != null) {
                String valueStr = simpleStrConditionContext.DECIMAL().getText();
                FormalConditionSimpleNumber formalConditionSimpleNumber = new FormalConditionSimpleNumber(columnname, symbolStr, valueStr);
                formalConditionSimpleNumber.setOutputCardinality(outputCardinality);
                formalConditionSimpleNumber.setViewname(viewname);
                formalConditionSimpleNumber.setColPath(col_path);
                return formalConditionSimpleNumber;
            }
            throw new RuntimeException("Unable to parse simpleStrConditionContext " + simpleStrConditionContext.getText());

        } else if (parseTree instanceof SimpleDateConditionContext) {
            SimpleDateConditionContext simpleDateConditionContext = (SimpleDateConditionContext) parseTree;

            String columnname = simpleDateConditionContext.columnname().getText();
            String symbolStr = simpleDateConditionContext.symbol().getText();

            String dateStr;
            if (simpleDateConditionContext.DATE() != null && simpleDateConditionContext.TIMESTAMP() == null) {
                dateStr = simpleDateConditionContext.DATE().getText();
            } else if (simpleDateConditionContext.DATE() == null && simpleDateConditionContext.TIMESTAMP() != null) {
                dateStr = simpleDateConditionContext.TIMESTAMP().getText();
            } else
                throw new RuntimeException("Unable to parse SimpleDateConditionContext " + simpleDateConditionContext.getText());

            FormalConditionSimpleDate formalConditionSimpleDate = new FormalConditionSimpleDate(columnname, symbolStr, dateStr);
            formalConditionSimpleDate.setOutputCardinality(outputCardinality);
            formalConditionSimpleDate.setViewname(viewname);
            formalConditionSimpleDate.setColPath(col_path);
            return formalConditionSimpleDate;
        } else if (parseTree instanceof ConditionStrContext)
            return parseTreeNode(parseTree.getChild(1).getChild(0), outputCardinality, viewname, col_path);
        else if (parseTree instanceof SimpleConditionContext)
            return parseTreeNode(parseTree.getChild(0), outputCardinality, viewname, col_path);
        else
            throw new RuntimeException("Should not be reaching here");

    }

    /**
     * @cite http://stackoverflow.com/a/1086092/2202712
     * @param iarr
     * @return
     */
    public static byte[] toByteArr(int[] iarr) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(iarr.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(iarr);
        return byteBuffer.array();
    }

    /**
     * @cite http://stackoverflow.com/a/1086092/2202712
     * @param barr
     * @return
     */
    public static int[] toIntArr(byte[] barr) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(barr);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        int iarr[] = new int[barr.length / 4];
        intBuffer.get(iarr);
        return iarr;
    }
}