package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.atn.ParseInfo;

import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.RegionF;

public class ProjectionVariableOptimized {
	public List<Integer>positive;
	public List<Integer>negative;
	public Region intersection;
	public RegionF interval;
	public long rowcount;
	public ProjectionVariableOptimized() {
		this.positive=new ArrayList<>();
		this.negative=new ArrayList<>();
		this.interval=null;
		//this.region.
	}
	
	public void addStringNegative(List<String>negList) {
		for (String neg:negList) {
			this.negative.add(Integer.parseInt(neg));
		}
	}
	public void addPositive(Integer x)
	{
		this.positive.add(x);
		Collections.sort(this.positive);
	}
	public void addNegative(Integer x)
	{
		this.negative.add(x);
		Collections.sort(this.negative);
	}
	public String toString() {
		Collections.sort(this.positive);
		Collections.sort(this.negative);
		String ret="";
		int i=0,j=0;
		while(i<this.positive.size()&&j<this.negative.size()) {
			if(positive.get(i)<negative.get(j)) {
				ret+=positive.get(i)+"+";
				i++;
			}
			else {
				ret+=negative.get(j)+"-";
				j++;
			}
		}
		for(;i<this.positive.size();i++)
			ret+=positive.get(i)+"+";
		for(;j<this.negative.size();j++)
			ret+=negative.get(j)+"-";
		return ret;
		
	}
	
}
