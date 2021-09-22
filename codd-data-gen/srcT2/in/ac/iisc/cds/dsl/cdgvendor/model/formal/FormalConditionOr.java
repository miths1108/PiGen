package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

import org.apache.commons.lang3.StringUtils;

import in.ac.iisc.cds.dsl.cdgvendor.constants.Constants;

public class FormalConditionOr extends FormalConditionComposite {

    private static final long serialVersionUID = -7992237468345472426L;

    public FormalConditionOr() {
        super();
    }

    public FormalConditionOr(FormalConditionOr another) {
        super(another);
    }

    @Override
    public String asString() {

        StringBuilder sb = new StringBuilder();
        for (FormalCondition condition : conditionList) {
            sb.append("[" + condition.asString() + "]").append(Constants.OR_OPERATOR);
        }
        return StringUtils.removeEnd(sb.toString(), Constants.OR_OPERATOR);
    }

    @Override
    public String asQueryString() {

        StringBuilder sb = new StringBuilder();
        for (FormalCondition condition : conditionList) {
            sb.append("(" + condition.asQueryString() + ")").append(" OR ");
        }
        return StringUtils.removeEnd(sb.toString(), " OR ");
    }

    @Override
    public FormalConditionOr getDeepCopy() {
        FormalConditionOr another = new FormalConditionOr();
        for (FormalCondition condition : conditionList) {
            another.addCondition(condition.getDeepCopy());
        }
        another.outputCardinality = outputCardinality;
        another.viewname = viewname;
        return another;
    }
}
