package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import in.ac.iisc.cds.dsl.cdgvendor.constants.Constants;

public class Constraint {
    private final String       viewname;
    private final List<String> predicates;
    private final long         outputCardinality;

    public Constraint(String viewname, List<String> predicates, long outputCardinality) {
        this.viewname = viewname;
        this.outputCardinality = outputCardinality;
        this.predicates = predicates;
    }

    public String getViewname() {
        return viewname;
    }

    public List<String> getPredicates() {
        return predicates;
    }

    public long getOutputCardinality() {
        return outputCardinality;
    }

    @Override
    public String toString() {
        StringBuilder sb;

        sb = new StringBuilder();
        for (String predicate : predicates) {
            sb.append(predicate + Constants.AND_OPERATOR);
        }
        String predtsr = StringUtils.removeEnd(sb.toString(), Constants.AND_OPERATOR);

        return "|" + Constants.SEL_OPERATOR + "( " + predtsr + " ) (" + viewname + ") | = " + getOutputCardinality();
    }

}
