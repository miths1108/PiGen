package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.List;



public class ViewSolutionRegion {
	public ViewSolution viewSolution;
	public List<VariableValuePair> viewSolutionRegion;
	public ViewSolutionRegion(ViewSolution viewSolution,List<VariableValuePair> viewSolutionRegions) {
		this.viewSolution=viewSolution;
		this.viewSolutionRegion=viewSolutionRegions;
	}
}
