package in.ac.iisc.cds.dsl.cdgvendor.reducer;

public class ConditionLabel {
    public int conditionIdx;
    public int subconditionIdx;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + conditionIdx;
        result = prime * result + subconditionIdx;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        ConditionLabel other = (ConditionLabel) obj;
        if (conditionIdx != other.conditionIdx) {
            return false;
        }
        if (subconditionIdx != other.subconditionIdx) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return conditionIdx + "." + subconditionIdx;
    }

}