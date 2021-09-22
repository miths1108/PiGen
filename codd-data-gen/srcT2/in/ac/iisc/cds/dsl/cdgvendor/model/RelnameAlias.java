package in.ac.iisc.cds.dsl.cdgvendor.model;

import in.ac.iisc.cds.dsl.cdgvendor.constants.Constants;

public class RelnameAlias {
    private final String relname;
    private final String alias;

    public RelnameAlias(String relname, String alias) {

        if (relname == null) {
            throw new ExceptionInInitializerError("Unable to instantiate RelnameAlias with null relname");
        }

        this.relname = relname;
        this.alias = alias;
    }

    public String getRelname() {
        return relname;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return relname + Constants.ALIAS_OPERATOR + alias;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (alias == null ? 0 : alias.hashCode());
        result = prime * result + (relname == null ? 0 : relname.hashCode());
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
        RelnameAlias other = (RelnameAlias) obj;
        if (alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!alias.equals(other.alias)) {
            return false;
        }
        if (relname == null) {
            if (other.relname != null) {
                return false;
            }
        } else if (!relname.equals(other.relname)) {
            return false;
        }
        return true;
    }

}
