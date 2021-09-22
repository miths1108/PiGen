package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.List;

import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructure;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;

import java.util.Collections;
import it.unimi.dsi.fastutil.ints.IntList;

public class ProjectionVariable {
	public List<Integer> regionList;
	public long rowCount;
	public List<String>groupkey;
	public Region intersectionRegion;
	public ProjectionVariable() {
		this.regionList=new ArrayList<>();
		
		
	}
	
	public ProjectionVariable(int i) {
		// TODO Auto-generated constructor stub
		this.regionList=new ArrayList<>();
		this.regionList.add(i);
	}
	public void setRegionList(List<Integer>regionList) {
		Collections.sort(regionList);
		this.regionList=regionList;
	}
	public void setIntersectionRegion(Region reg) {
		//TODO Handle when BS overlapping in the projected space
		this.intersectionRegion=reg;
	}
	
	
}
