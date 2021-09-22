package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.microsoft.z3.IntExpr;

public class ProjectionStuffInSSPRegion {	// Stuff Inside Single Split Point Region
	final Set<IntExpr> aggVars;
	final Set<IntExpr> nonAggVars;
	final HashMap<IntExpr, List<IntExpr>> aggVarsToProjVars;
	final List<Set<IntExpr>> connectedSets;
	
	public ProjectionStuffInSSPRegion(Set<IntExpr> aggVars, Set<IntExpr> nonAggVars) {
		this.aggVars = aggVars;
		this.nonAggVars = nonAggVars;
		aggVarsToProjVars = new HashMap<>();
		connectedSets = new ArrayList<>();
	}
	
	public Set<IntExpr> getAggVars() {
		return aggVars;
	}
	
	public Set<IntExpr> getNonAggVars() {
		return nonAggVars;
	}
	
	public HashMap<IntExpr, List<IntExpr>> getAggVarsToProjVars() {
		return aggVarsToProjVars;
	}
	
	public Set<IntExpr> getAllProjVars() {
		Set<IntExpr> result = new HashSet<IntExpr>();
		for (List<IntExpr> projVars : aggVarsToProjVars.values()) {
			result.addAll(projVars);
		}
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(aggVars).append("\n");
		sb.append(nonAggVars).append("\n");
		return sb.toString();
	}
}
