package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

import java.util.ArrayList;
import java.util.List;

public abstract class FormalConditionComposite extends FormalCondition {
    protected final List<FormalCondition> conditionList;

    public FormalConditionComposite() {
        conditionList = new ArrayList<>();
    }

    public FormalConditionComposite(FormalConditionComposite another) {
        this.outputCardinality = another.outputCardinality;
        this.viewname = another.viewname;
        conditionList = new ArrayList<>();
        
        this.queryName = another.queryName;
    }

    public void addCondition(FormalCondition formalCondition) {
        conditionList.add(formalCondition);
    }

    public void addConditionAll(List<FormalCondition> formalConditions) {
        conditionList.addAll(formalConditions);
    }

    public List<FormalCondition> getConditionList() {
        return conditionList;
    }
    
    public int size() {
    	return conditionList.size();
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (conditionList == null ? 0 : conditionList.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FormalConditionComposite other = (FormalConditionComposite) obj;
        if (conditionList == null) {
            if (other.conditionList != null) {
                return false;
            }
        } else if (!conditionList.equals(other.conditionList)) {
            return false;
        }
        return true;
    }
}
