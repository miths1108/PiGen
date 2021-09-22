package in.ac.iisc.cds.dsl.cdgvendor.reducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class Reducer {

    private Map<IntList, Region>  P;
    private final Integer         attrCount;

    private final List<boolean[]> allTrueBS;
    private final List<Region>    conditionRegions;

    public Reducer(List<boolean[]> allTrueBS, List<Region> conditionRegions) {

        attrCount = conditionRegions.get(0).at(0).size();
        this.allTrueBS = allTrueBS;
        this.conditionRegions = conditionRegions;

        List<ConditionLabel> allConditionLabels = new ArrayList<>();
        for (int i = 0; i < conditionRegions.size(); i++) {	// For each condition + 1 (all 1's)
            for (int j = 0; j < conditionRegions.get(i).size(); j++) {	// For each sub condition in that condition. Each subcondition contains buckets for each attribute (bucket structures)
                ConditionLabel cl = new ConditionLabel();
                cl.conditionIdx = i;
                cl.subconditionIdx = j;
                allConditionLabels.add(cl);
            }
        }
        Map<List<ConditionLabel>, List<BucketStructure>> intrimP = traverseAttributeWise(allConditionLabels);	// Return condition labels (family of set of single constraints) and partitions satisfying those conditions
        //DebugHelper.printInfo(intrimP.toString());
        initP(intrimP);
        compressRegions();
        //DebugHelper.printInfo("Gotcha");
        //DebugHelper.printInfo(P.toString());
        if (DebugHelper.sanityChecksNeeded()) {
            isUniverseCheck(P.values());
            areDisjointCheck(P.values());
        }
    }

    private Map<List<ConditionLabel>, List<BucketStructure>> traverseAttributeWise(List<ConditionLabel> labels) {
        int attrIdx;

        Map<List<ConditionLabel>, List<BucketStructure>> reverseMap = new HashMap<>();

        attrIdx = 0;
        for (int i = 0; i < allTrueBS.get(attrIdx).length; i++) {	// Setting up labels initially using first attribute (dimension)
            List<ConditionLabel> satisfyingCLs = new ArrayList<>();	// Holds labels of split points which are satisfied
            for (ConditionLabel label : labels) {
                if (conditionRegions.get(label.conditionIdx).at(label.subconditionIdx).at(attrIdx).contains(i)) {
                    satisfyingCLs.add(label);
                }
            }

            List<BucketStructure> bucketStructures = reverseMap.get(satisfyingCLs);
            if (bucketStructures == null) {
                Bucket bucket = new Bucket();
                bucket.add(i);
                BucketStructure bucketStructure = new BucketStructure();
                bucketStructure.add(bucket);
                bucketStructures = new ArrayList<>();
                bucketStructures.add(bucketStructure);
                reverseMap.put(satisfyingCLs, bucketStructures);
            } else {
                for (BucketStructure bucketStructure : bucketStructures) {
                    Bucket bucket = bucketStructure.at(attrIdx);
                    if (!bucket.contains(i)) {
                        bucket.add(i);
                    }
                }
            }
        }

        for (attrIdx = 1; attrIdx < attrCount; attrIdx++) {
            Map<List<ConditionLabel>, List<BucketStructure>> reverseMapTemp = new HashMap<>();
            for (Entry<List<ConditionLabel>, List<BucketStructure>> entry : reverseMap.entrySet()) {
                List<ConditionLabel> cls = entry.getKey();
                List<BucketStructure> bucketStructures = entry.getValue();

                Map<List<ConditionLabel>, List<BucketStructure>> reverseMapInnerTemp = new HashMap<>();
                for (int i = 0; i < allTrueBS.get(attrIdx).length; i++) {
                    List<ConditionLabel> satisfyingCLs = new ArrayList<>();
                    for (ConditionLabel label : cls) {
                        if (conditionRegions.get(label.conditionIdx).at(label.subconditionIdx).at(attrIdx).contains(i)) {
                            satisfyingCLs.add(label);
                        }
                    }
                    List<BucketStructure> bucketStructuresInnerTemp = reverseMapInnerTemp.get(satisfyingCLs);
                    if (bucketStructuresInnerTemp == null) {
                        bucketStructuresInnerTemp = new ArrayList<>();
                        for (BucketStructure bucketStructure : bucketStructures) {
                            Bucket bucket = new Bucket();
                            bucket.add(i);
                            BucketStructure bucketStructureTemp = new BucketStructure(bucketStructure);
                            bucketStructureTemp.add(bucket);
                            bucketStructuresInnerTemp.add(bucketStructureTemp);
                        }
                        reverseMapInnerTemp.put(satisfyingCLs, bucketStructuresInnerTemp);
                    } else {
                        for (BucketStructure bucketStructure : bucketStructuresInnerTemp) {
                            Bucket bucket = bucketStructure.at(attrIdx);
                            if (!bucket.contains(i)) {
                                bucket.add(i);
                            }
                        }
                    }
                }

                //updating reverseMapTemp based on reverseMapInnerTemp
                for (Entry<List<ConditionLabel>, List<BucketStructure>> entry2 : reverseMapInnerTemp.entrySet()) {
                    List<ConditionLabel> cls2 = entry2.getKey();
                    List<BucketStructure> bucketStructures2 = entry2.getValue();

                    List<BucketStructure> bucketStructuresTemp = reverseMapTemp.get(cls2);
                    if (bucketStructuresTemp == null) {
                        reverseMapTemp.put(cls2, bucketStructures2);
                    } else {
                        bucketStructuresTemp.addAll(bucketStructures2);
                    }

                }
            }
            reverseMap = reverseMapTemp;
//            System.out.println(reverseMap.size());
        }
        return reverseMap;
    }

    private void initP(Map<List<ConditionLabel>, List<BucketStructure>> intrimP) {
        P = new HashMap<>();
        for (Entry<List<ConditionLabel>, List<BucketStructure>> entry : intrimP.entrySet()) {
            List<ConditionLabel> labelsKey = entry.getKey();  // label
            List<BucketStructure> bss = entry.getValue();   // region
            IntList compressedkey = getCompressedKey(labelsKey); // [0.0, 1.0] -> [0,1] keep only conditionIndex
            Region region = P.get(compressedkey);
            if (region == null) {
                region = new Region();
                P.put(compressedkey, region);
            }
            region.addAll(bss);
        }
    }

    private static IntList getCompressedKey(List<ConditionLabel> labelsKey) {
        IntSet satisfiedConditionIdxs = new IntOpenHashSet();
        for (ConditionLabel label : labelsKey) {
            satisfiedConditionIdxs.add(label.conditionIdx); //subcondition met => condition met
        }
        IntList result = new IntArrayList(satisfiedConditionIdxs);
        Collections.sort(result);
        return result;
    }

    private void isUniverseCheck(Collection<Region> regions) {
        //universe minus all regions should become empty
        Region universe = new Region();
        BucketStructure subConditionBS = new BucketStructure();
        for (int j = 0; j < allTrueBS.size(); j++) {
            Bucket bucket = new Bucket();
            for (int k = 0; k < allTrueBS.get(j).length; k++) {
                if (allTrueBS.get(j)[k]) {
                    bucket.add(k);
                }
            }
            subConditionBS.add(bucket);
        }
        universe.add(subConditionBS);

        for (Region region : regions) {
            //DebugHelper.printDebug("Minus operation " + universe.size() + " and " + region.size());
            universe = universe.minus(region);
        }

        if (!universe.isEmpty())
            throw new RuntimeException("Expected empty region but found non-empty");
    }

    private void areDisjointCheck(Collection<Region> regions) {
        //every pair of Regions are disjoint
        List<Region> regionList = new ArrayList<>(regions);
        int n = regionList.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                //DebugHelper.printDebug("Intersection operation i=" + i + " j=" + j + " n=" + n);
                if (!regionList.get(i).intersection(regionList.get(j)).isEmpty())
                    //DebugHelper.printDebug("RegionI " + regionList.get(i));
                    //DebugHelper.printDebug("RegionJ " + regionList.get(j));
                    //DebugHelper.printDebug("RegionInter " + regionList.get(i).intersection(regionList.get(j)));
                    throw new RuntimeException("Expected disjoint region but found overlapping regions");
            }
        }
    }

    /**
     * Represents the P information in a different format where the variables are returned in a list and their corresponding conditionIds are returned as another parallel list
     * @param varList empty list which get populated with the list of variables
     * @param conditionIdxsList empty list which get populated with the list of conditionIds
     */
    public void getVarsAndConditionsSimplified(List<Region> varList, List<IntList> conditionIdxsList) {

        if (!varList.isEmpty() || !conditionIdxsList.isEmpty())
            throw new RuntimeException("Expected empty lists");

        for (Entry<IntList, Region> entry : P.entrySet()) {
            IntList conditionIdxs = entry.getKey();
            Region region = entry.getValue();
            region.sort();
            varList.add(region);
            conditionIdxsList.add(conditionIdxs);
        }
    }

    public Map<IntList, Region> getMinPartition() {
        return P;
    }

	public void refineRegionsforSkewMimicking() {
		// TODO Refine the regions in the partition along attributes being considered for skew
		//DebugHelper.printInfo(P.toString());
	}
	public void compressRegions() {
    	//Code by Subhodeep
    	//Eg :Two BS having all buckets exactly same except one will be merged into 1.
    	Map<IntList, Region> partition=this.P;
    	for (Entry<IntList, Region> entry : partition.entrySet()) {
    		IntList conditionIdxs = entry.getKey();
            Region region = entry.getValue();
            Boolean change = true;
    		
    		while(change) {
    			change = false;
    			for(int i=0; i<region.size(); i++) {
	    			for(int j=i+1;j<region.size();j++) {
	    				int index = CheckBSmergeCompatibility(region.at(i), region.at(j));
	    				if( index!= -1) {
	    					mergeBS(region.at(i), region.at(j), index);
	    					//region.set(i,mergeBS(region.at(i), region.at(j), index));
	    					region.remove(j);
	    					j--;
	    					change = true;
	    				}
	    			}
    			}		
    		}
    		//if(region.size()>1)
    		//	System.out.print(region);
    	}
    }
    static void mergeBS( BucketStructure BS1, BucketStructure BS2, int index){
		// 0Bucket mergedB = new Bucket(BS1.at(index));
    	BS1.at(index).addAll(BS2.at(index));
		

		
	}
	
	
	static int CheckBSmergeCompatibility(BucketStructure BS1, BucketStructure BS2) {
		int ans_index = -1;
		int diff_count = 0;
		for(int i=0; i< BS1.size(); i++) {
			if( ! BS1.at(i).isEqual(BS2.at(i)) ) {
				diff_count++;
				ans_index = i;
			}
			if(diff_count>1) {
//				More than one dimension unaligned
				return -1;
			}
		}
		if(diff_count==1)
			return ans_index;
		return -1;
	}

}
    
