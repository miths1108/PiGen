package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;

public class CCInfo implements Serializable {

    private static final long                  serialVersionUID = -4937664668953589226L;

    private Map<String, List<FormalCondition>> viewnameToCCMap;
    

    public Map<String, List<FormalCondition>> getViewnameToCCMap() {
        return viewnameToCCMap;
    }
    
	public void setViewnameToCCMap(Map<String, List<FormalCondition>> viewnameToCCMap) {
		this.viewnameToCCMap = viewnameToCCMap;
		
	}

}
