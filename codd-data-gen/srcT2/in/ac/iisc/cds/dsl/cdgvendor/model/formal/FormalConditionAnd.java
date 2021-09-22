package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

import org.apache.commons.lang3.StringUtils;

import in.ac.iisc.cds.dsl.cdgvendor.constants.Constants;

public class FormalConditionAnd extends FormalConditionComposite {

    private static final long serialVersionUID = -4437727352513679355L;

    public FormalConditionAnd() {
        super();
    }

    public FormalConditionAnd(FormalConditionAnd another) {
        super(another);
    }

    @Override
    public String asString() {

        StringBuilder sb = new StringBuilder();
        for (FormalCondition condition : conditionList) {
            sb.append("[" + condition.asString() + "]").append(Constants.AND_OPERATOR);
        }
        return StringUtils.removeEnd(sb.toString(), Constants.AND_OPERATOR);
    }

    @Override
    public String asQueryString() {

        StringBuilder sb = new StringBuilder();
        for (FormalCondition condition : conditionList) {
            sb.append("(" + condition.asQueryString() + ")").append(" AND ");
        }
        return StringUtils.removeEnd(sb.toString(), " AND ");
    }

    @Override
    public FormalConditionAnd getDeepCopy() {
        FormalConditionAnd another = new FormalConditionAnd();
        for (FormalCondition condition : conditionList) {
            another.addCondition(condition.getDeepCopy());
        }
        another.outputCardinality = outputCardinality;
        another.viewname = viewname;
        return another;
    }
}
