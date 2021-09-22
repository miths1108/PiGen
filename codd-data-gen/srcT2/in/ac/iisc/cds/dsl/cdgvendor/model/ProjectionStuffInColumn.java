package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;

import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;

public class ProjectionStuffInColumn {
	final HashMap<Region, ProjectionStuffInSSPRegion> sSPRegionToProjectionStuff = new HashMap<>();
	final HashMap<IntExpr, Set<List<IntExpr>>> aggVarsToContainerSets = new HashMap<>();
	final HashMap<List<IntExpr>, IntExpr> containerSetsToProjVar = new HashMap<>();
	final HashMap<IntExpr, List<IntExpr>> aggVarsToProjVars = new HashMap<>();
	boolean aggVarsToProjVarsComputationDone = false;
	
	final Set<Set<IntExpr>> consistencyConstraintSetWiseNonAggVars = new HashSet<>();
	final List<Set<IntExpr>> listOfAtomicSetOfNonAggVars = new LinkedList<>();
	
	public HashMap<Region, ProjectionStuffInSSPRegion> getMapSSPRegionToProjectionStuff() {
		return sSPRegionToProjectionStuff;
	}
	
	public ProjectionStuffInSSPRegion getProjectionStuffInSSPRegion(Region region) {
		return sSPRegionToProjectionStuff.get(region);
	}
	
	public void putProjectionStuffInSSPRegion(Region region, ProjectionStuffInSSPRegion projectionStuffInSSPRegion) {
		sSPRegionToProjectionStuff.put(region, projectionStuffInSSPRegion);
	}
	
	public HashMap<IntExpr, Set<List<IntExpr>>> getMapAggVarsToContainerSets() {
		return aggVarsToContainerSets;
	}

	public HashMap<List<IntExpr>, IntExpr> getMapContainerSetsToProjVar() {
		return containerSetsToProjVar;
	}
	
	public HashMap<IntExpr, List<IntExpr>> getAggVarsToProjVars() {
		if(!aggVarsToProjVarsComputationDone) {
			for (Entry<IntExpr, Set<List<IntExpr>>> entry : aggVarsToContainerSets.entrySet()) {
				List<IntExpr> projectionVars = new ArrayList<>();
				for (List<IntExpr> containerSet : entry.getValue()) {
					projectionVars.add(containerSetsToProjVar.get(containerSet));
				}
				aggVarsToProjVars.put(entry.getKey(), projectionVars);
			}
			aggVarsToProjVarsComputationDone = true;
		}
		return aggVarsToProjVars;
	}

	public void updateAggVarToProjVarOfProjectionStuffInSSPRegion() {
		HashMap<IntExpr, List<IntExpr>> allAggVarsToProjVars = getAggVarsToProjVars();
		for (ProjectionStuffInSSPRegion projectionStuffInSSPRegion : sSPRegionToProjectionStuff.values()) {
			HashMap<IntExpr, List<IntExpr>> aggVarsToProjVars = projectionStuffInSSPRegion.getAggVarsToProjVars();
			for (IntExpr aggVar : projectionStuffInSSPRegion.getAggVars()) {
				aggVarsToProjVars.put(aggVar, allAggVarsToProjVars.get(aggVar));
			}
		}
	}
	
	public Set<Set<IntExpr>> getConsistencyConstraintSetWNonAggVars() {
		return consistencyConstraintSetWiseNonAggVars;
	}

	public List<Set<IntExpr>> doPartition_ConsistencyConstraintSetWiseNonAggVars() {
		int conditionSetIndex = 0;
		final HashMap<Set<Integer>, Set<IntExpr>> setOfConditionSetIndexToSetOfNonAggVars = new HashMap<>();
		HashMap<IntExpr, Set<Integer>> nonAggVarToSetOfConditionSetIndex = new HashMap<>();
		for (Set<IntExpr> nonAggVars : consistencyConstraintSetWiseNonAggVars) {
			for (IntExpr nonAggVar : nonAggVars) {
				Set<Integer> setOfConditionSetIndex = nonAggVarToSetOfConditionSetIndex.get(nonAggVar);
				if (setOfConditionSetIndex == null) {
					setOfConditionSetIndex = new HashSet<>();
					nonAggVarToSetOfConditionSetIndex.put(nonAggVar, setOfConditionSetIndex);
				}
				setOfConditionSetIndex.add(conditionSetIndex);
			}
			conditionSetIndex++;
		}
		
		for (Entry<IntExpr, Set<Integer>> entry : nonAggVarToSetOfConditionSetIndex.entrySet()) {
			IntExpr nonAggVar = entry.getKey();
			Set<Integer> setOfConditionSetIndex = entry.getValue();
			Set<IntExpr> setOfNonAggVars = setOfConditionSetIndexToSetOfNonAggVars.get(setOfConditionSetIndex);
			if (setOfNonAggVars == null) {
				setOfNonAggVars = new HashSet<>();
				setOfConditionSetIndexToSetOfNonAggVars.put(setOfConditionSetIndex, setOfNonAggVars);
			}
			setOfNonAggVars.add(nonAggVar);
		}
		listOfAtomicSetOfNonAggVars.addAll(setOfConditionSetIndexToSetOfNonAggVars.values());
		return listOfAtomicSetOfNonAggVars;
	}
	
	public List<Integer> getSlackVarsIndexsContainedInNonAggVars(Set<IntExpr> nonAggVars) {
		List<Integer> result = new LinkedList<>();
		int index = 0;
		for (Set<IntExpr> atomicSetOfNonAggVars : listOfAtomicSetOfNonAggVars) {
			if (nonAggVars.containsAll(atomicSetOfNonAggVars))
				result.add(index);
			// sanity check
			else {
				for (IntExpr intExpr : atomicSetOfNonAggVars) {
					if (nonAggVars.contains(intExpr))
						throw new RuntimeException("Wrong assumption!");
				}
			}
			index++;
		}
		return result;
	}

	public List<Set<IntExpr>> getListOfAtomicSetOfNonAggVars() {
		return listOfAtomicSetOfNonAggVars;
	}
}
