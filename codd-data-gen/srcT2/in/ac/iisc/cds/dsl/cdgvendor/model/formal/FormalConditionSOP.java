package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

import java.util.ArrayList;
import java.util.List;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

public class FormalConditionSOP extends FormalCondition {

    private static final long             serialVersionUID = -5319050660881871702L;

    protected Expression<String>          dnfExpr;

    /**
     * Each element of the list is either FormalConditionAnd or FormalConditionSimpleInt
     * TODO: Use an interface for this type
     */
    protected final List<FormalCondition> conditionList;

    public FormalConditionSOP(FormalCondition condition) {

        if (condition instanceof FormalConditionComposite) {
            FormalConditionComposite composite = (FormalConditionComposite) condition;

            outputCardinality = composite.outputCardinality;
            viewname = composite.viewname;

            dnfExpr = createJboolExpression(composite);
            dnfExpr = RuleSet.simplify(dnfExpr);
            dnfExpr = RuleSet.toDNF(dnfExpr);

            conditionList = createToFormalCondition(dnfExpr);
        } else if (condition instanceof FormalConditionSimpleInt) {
            FormalConditionSimpleInt simple = (FormalConditionSimpleInt) condition;
//           FormalCondition temp=new FormalConditionSimpleInt();
            outputCardinality = simple.outputCardinality;
            viewname = simple.viewname;
            dnfExpr = null;
            conditionList = new ArrayList<>();
            conditionList.add(simple);
        } else if (condition instanceof FormalConditionBaseAggregate) {
        	FormalConditionBaseAggregate baseCondition = (FormalConditionBaseAggregate) condition;
        	outputCardinality = baseCondition.outputCardinality;
            viewname = baseCondition.viewname;
            dnfExpr = null;
            conditionList = new ArrayList<>();
        } else
            throw new RuntimeException("Unrecognized type of FormalCondition " + condition.getClass());
    }

    private Expression<String> createJboolExpression(FormalCondition condition) {

        if (condition instanceof FormalConditionAnd) {
            FormalConditionAnd andCondition = (FormalConditionAnd) condition;
            List<Expression<String>> exprList = new ArrayList<>();
            for (FormalCondition innerCondition : andCondition.conditionList) {
                exprList.add(createJboolExpression(innerCondition));
            }
            return And.of(exprList);

        } else if (condition instanceof FormalConditionOr) {
            FormalConditionOr orCondition = (FormalConditionOr) condition;
            List<Expression<String>> exprList = new ArrayList<>();
            for (FormalCondition innerCondition : orCondition.conditionList) {
                exprList.add(createJboolExpression(innerCondition));
            }
            return Or.of(exprList);

        } else if (condition instanceof FormalConditionSimpleInt) {
            FormalConditionSimpleInt simpleCondition = (FormalConditionSimpleInt) condition;
            return Variable.of(simpleCondition.asString());

        } else
            throw new RuntimeException("Unrecognized type of CompositeCondition " + condition.getClass());

    }

    private List<FormalCondition> createToFormalCondition(Expression<String> dnfExpr) {

        if (dnfExpr instanceof And) {
            List<FormalCondition> conditionList = new ArrayList<>();
            And innerAndExpr = (And) dnfExpr;
            List<FormalCondition> andConditionList = new ArrayList<>();

            for (Expression<String> innerVarExpr : innerAndExpr.expressions) {
                FormalConditionSimpleInt simpleCondition = getAsSimpleCondition(innerVarExpr);
                andConditionList.add(simpleCondition);
            }

            FormalConditionAnd andCondition = new FormalConditionAnd();
            andCondition.addConditionAll(andConditionList);
            conditionList.add(andCondition);
            return conditionList;
        }

        if (dnfExpr instanceof Variable) {
            List<FormalCondition> conditionList = new ArrayList<>();
            FormalConditionSimpleInt simpleCondition = getAsSimpleCondition(dnfExpr);
            conditionList.add(simpleCondition);
            return conditionList;
        }

        if (!(dnfExpr instanceof Or))
            throw new RuntimeException("Expected dnfExpr but its not one " + dnfExpr);

        Or<String> orExpr = (Or<String>) dnfExpr;

        List<FormalCondition> conditionList = new ArrayList<>();
        for (Expression<String> innerExpr : orExpr.expressions) {

            if (innerExpr instanceof And) {

                And innerAndExpr = (And) innerExpr;
                List<FormalCondition> andConditionList = new ArrayList<>();

                for (Expression<String> innerVarExpr : innerAndExpr.expressions) {
                    FormalConditionSimpleInt simpleCondition = getAsSimpleCondition(innerVarExpr);
                    andConditionList.add(simpleCondition);
                }

                FormalConditionAnd andCondition = new FormalConditionAnd();
                andCondition.addConditionAll(andConditionList);
                conditionList.add(andCondition);

            } else if (innerExpr instanceof Variable) {
                FormalConditionSimpleInt simpleCondition = getAsSimpleCondition(innerExpr);
                conditionList.add(simpleCondition);
            } else
                throw new RuntimeException("Expected dnfExpr but its not one " + dnfExpr);
        }
        return conditionList;

    }

    private FormalConditionSimpleInt getAsSimpleCondition(Expression<String> variable) {
        if (!(variable instanceof Variable))
            throw new RuntimeException("Expected FormalConditionSimpleInt but its not one " + variable);

        String strRep = variable.toString();
        FormalConditionSimpleInt simpleCondition = new FormalConditionSimpleInt(strRep, outputCardinality, viewname);

        return simpleCondition;
    }

    public List<FormalCondition> getConditionList() {
        return conditionList;
    }

    @Override
    public String asString() {

        return null;
    }

    @Override
    public String asQueryString() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public FormalConditionSimpleInt getDeepCopy() {
        throw new UnsupportedOperationException("Not Implemented yet");
    }

}
