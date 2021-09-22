package in.ac.iisc.cds.dsl.cdgvendor.solver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig;
import in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig.Key;
import in.ac.iisc.cds.dsl.cdgvendor.model.SchemaInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.TableInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ValueCombination;
import in.ac.iisc.cds.dsl.cdgvendor.model.VariableValuePair;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolution;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionDiskBased;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionInMemory;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionRegion;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionWithSolverStats;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructure;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;
import in.ac.iisc.cds.dsl.cdgvendor.solver.Z3Solver.SpillType;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import in.ac.iisc.cds.dsl.cdgvendor.utils.FileUtils;
import in.ac.iisc.cds.dsl.cdgvendor.utils.SharedBoolean;
import in.ac.iisc.cds.dsl.cdgvendor.utils.StopWatch;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class DatabaseSummary {

    private static final String             NEWLINE = "\n";
    private static final String             COMMA   = ",";

    private final StopWatch                 databaseSummarySW;
    private final SpillType                 spillType;
    //private final Map<String, ViewSolution> viewSolutions;
    private final Map<String, ViewSolutionRegion> viewSolutionsRegion;
    private boolean                         isConsistent;
    private boolean                         isCompressed;

    //public DatabaseSummary(StopWatch databaseSummarySW, SpillType spillType, Map<String, ViewSolution> viewSolutions) {
    public DatabaseSummary(StopWatch databaseSummarySW, SpillType spillType, Map<String, ViewSolutionRegion> viewSolutionsRegion) {
        this.databaseSummarySW = databaseSummarySW;
        this.spillType = spillType;
        isConsistent = isCompressed = false;
        this.viewSolutionsRegion = viewSolutionsRegion;
    }

    /**
     * Traverses summaryByView in topological order and adds an extra valueCombination
     * to the primary key viewSolution whenever required
     */
    public void makeFKeyConsistency() {
        if (isConsistent) {
            DebugHelper.printError("Received extra call to makeViewConsistency while it is already made consistent");
            return;
        }
        
        //Temp commented by shadab

//        for (ViewSolution viewSolution : viewSolutions.values()) {
//            if (viewSolution instanceof ViewSolutionInMemory) {
//                ((ViewSolutionInMemory) viewSolution).prepareForSearch();
//            } else if (viewSolution instanceof ViewSolutionWithSolverStats) {
//                ((ViewSolutionWithSolverStats) viewSolution).prepareForSearch();
//            }
//        }

        Map<String, ViewInfo> viewInfos = PostgresVConfig.ANONYMIZED_VIEWINFOs;
        List<String> topoViewnames = PostgresVConfig.VIEWNAMES_TOPO;

        //Checking Views in topological order and adding extra tuple to fkeyViews whenever needed
        for (int i = 0; i < topoViewnames.size() - 1; i++) { //Last one won't require addition of any extra tuple to its children because it doesn't have any!
            String fkViewname = topoViewnames.get(i);
            ViewInfo fkViewInfo = viewInfos.get(fkViewname);
            List<String> fkSortedColumns = new ArrayList<>(fkViewInfo.getViewNonkeys());
            Collections.sort(fkSortedColumns);
            //ViewSolution fkViewSolution = viewSolutions.get(fkViewname);
            ViewSolutionRegion fkViewSolution = viewSolutionsRegion.get(fkViewname);

            DebugHelper.printDebug("Ensuring view consistency for pkviews of " + fkViewname);

            for (String pkViewname : fkViewInfo.getFkeyViews()) {
                ViewInfo pkViewInfo = viewInfos.get(pkViewname);
                List<String> sortedCommonColumns = new ArrayList<>(pkViewInfo.getViewNonkeys()); //All columns of pkViewInfo should infact be the common columns between it and viewInfo
                Collections.sort(sortedCommonColumns);

                //DebugHelper.printDebug("\tEnsuring view consistency for pkview " + pkViewname);

                IntList posOfCommonColumns = new IntArrayList(sortedCommonColumns.size());
                for (int j = 0; j < fkSortedColumns.size(); j++) {
                    if (sortedCommonColumns.contains(fkSortedColumns.get(j))) {
                        posOfCommonColumns.add(j);
                    }
                }

//                ViewSolution pkViewSolution = viewSolutions.get(pkViewname);
                ViewSolutionRegion pkViewSolution = viewSolutionsRegion.get(pkViewname);
                
                //Commented by Shadab to do Ref Integrity regionwise
//                for (ValueCombination fkValueCombination : fkViewSolution) {
//                    IntList seekedValuesInCombination = new IntArrayList(posOfCommonColumns.size());
//                    for (IntIterator iter = posOfCommonColumns.iterator(); iter.hasNext();) {
//                        int pos = iter.nextInt();
//                        seekedValuesInCombination.add(fkValueCombination.getColValues().getInt(pos));
//                    }
//
//                    if (!pkViewSolution.contains(seekedValuesInCombination)) {
//                        ValueCombination toaddValueCombination = new ValueCombination(seekedValuesInCombination, 1); //Adding extra valueCombination with count 1
//                        pkViewSolution.addValueCombination(toaddValueCombination);
//                    }
//
//                }
            
                List<Region> projectedRegionsFV=new ArrayList<>();
                for (VariableValuePair fkVar : fkViewSolution.viewSolutionRegion) {
                    //IntList seekedValuesInCombination = new IntArrayList(posOfCommonColumns.size());
                	Region projectedRegion=new Region();
                	Region fkRegion=fkVar.variable;
                	for (int j =0;j<fkRegion.size();j++) {
                		
                		BucketStructure projectedBS=new BucketStructure();
                		for (IntIterator iter = posOfCommonColumns.iterator(); iter.hasNext();) {
                			int pos = iter.nextInt();
                			projectedBS.add(fkRegion.at(i).at(pos));                       
                        }
                		projectedRegion.add(projectedBS);
                    }
                	projectedRegionsFV.add(projectedRegion);                	
             }
            
            
            }
        }
        isConsistent = true;
        

//        if (DebugHelper.viewConsistencyErrorCheckNeeded()) {
//            if (databaseSummarySW != null) {
//                databaseSummarySW.pause();
//            }
//            DebugHelper.debugSummaryRowcounts(viewSolutions);
//            if (databaseSummarySW != null) {
//                databaseSummarySW.resume();
//            }
//        }
    }

    /**
     * Traverses summaryByView in topological order and replaces the fkeyColumnValueCombinations in the ValueCombinations.
     * A given foreign value combination is replaced by corresponding foreign key column
     * whose value is equal to the cumulative sum of rowCounts in the fkeyView until that value combination
     *
     * For each viewname we now have the compressed list as List<ValueCombination>
     * First few entries will be the fkey columnValues corresponding to the fKeyViews in the order returned by the viewInfo.getFkeyViews which is the sortedOrder of fkeyViewnames
     * After that there will be entries corresponding to viewInfo.getTableNonkeys in sortedOrder
     */
    public void compressSummaryByAddingFkeys() {

        Map<String, ViewInfo> viewInfos = PostgresVConfig.ANONYMIZED_VIEWINFOs;
        List<String> topoViewnames = PostgresVConfig.VIEWNAMES_TOPO;

        //Checking Views in topological order and compressing their tuples widthwise
        for (int i = 0; i < topoViewnames.size(); i++) {
            String fkViewname = topoViewnames.get(i);
            ViewInfo fkViewInfo = viewInfos.get(fkViewname);
            List<String> sortedFKViewNonkeyColumns = new ArrayList<>(fkViewInfo.getViewNonkeys());
            Collections.sort(sortedFKViewNonkeyColumns);
            List<String> sortedFKTableNonkeyColumns = new ArrayList<>(fkViewInfo.getTableNonkeys());
            Collections.sort(sortedFKTableNonkeyColumns);
            ViewSolution fkViewSolution = viewSolutions.get(fkViewname);

            DebugHelper.printDebug("Compressing ViewSolution for view " + fkViewname);
            ViewSolution fkCompressedViewSolution = getEmptyViewSolutionBasedOnSpillType(fkViewname, fkViewSolution.getCountOfValueCombinations(),
                    getCompressedValueCombinationSizeInBytes(fkViewname));

            for (ValueCombination fkValueCombination : fkViewSolution) {

                Object2IntMap<String> pkViewnameToFKValue = new Object2IntOpenHashMap<>();
                //for each childView
                for (String pkViewname : fkViewInfo.getFkeyViews()) {
                    ViewInfo pkViewInfo = viewInfos.get(pkViewname);
                    List<String> sortedCommonColumns = new ArrayList<>(pkViewInfo.getViewNonkeys()); //All columns of pkViewInfo should infact be the common columns in it and viewInfo
                    Collections.sort(sortedCommonColumns);

                    //DebugHelper.printDebug("\tCompressing using ViewSolution for pkview " + pkViewname);

                    //finding referenced commonValues
                    IntList commonValues = new IntArrayList();
                    for (int j = 0; j < sortedFKViewNonkeyColumns.size(); j++) {
                        if (sortedCommonColumns.contains(sortedFKViewNonkeyColumns.get(j))) {
                            commonValues.add(fkValueCombination.getColValues().get(j));
                        }
                    }

                    if (commonValues.isEmpty())
                        throw new RuntimeException("Should not be reaching here");

                    ViewSolution pkValueCombinations = viewSolutions.get(pkViewname);
                    //NOTE: Allowing pkValue to go negative in databaseSummary as it would impact only in cosmetically large (scaled) table sizes
                    //Fixing this requires creating a separate data structure ValueCombination2 for summary which has long keys and int colValues
                    int pkValue = (int) pkValueCombinations.getPK(commonValues);
                    pkViewnameToFKValue.put(pkViewname, pkValue);
                }

                //For originalColumns we pick the unchanged valueCombination
                //while we insert fkeyValue for each pkView referenced
                IntList compressedValuesInCombination = new IntArrayList(fkViewInfo.getFkeyViews().size());
                for (String pkViewname : fkViewInfo.getFkeyViews()) {
                    compressedValuesInCombination.add(pkViewnameToFKValue.get(pkViewname));
                }
                for (int j = 0; j < sortedFKViewNonkeyColumns.size(); j++) {
                    String viewNonkeyColumnname = sortedFKViewNonkeyColumns.get(j);
                    if (sortedFKTableNonkeyColumns.contains(viewNonkeyColumnname)) {
                        compressedValuesInCombination.add(fkValueCombination.getColValues().get(j));
                    }
                }

                ValueCombination compressedValueCombination = new ValueCombination(compressedValuesInCombination, fkValueCombination.getRowcount());
                fkCompressedViewSolution.addValueCombination(compressedValueCombination);

            }
            fkViewSolution.close();
            viewSolutions.put(fkViewname, fkCompressedViewSolution);
        }

        isCompressed = true;
    }

    //    private List<ValueCombination> getEmptyViewSolutionBasedOnSpillType(String viewname, int expectedCapacity, int entrySizeInBytes) {
    //        switch (spillType) {
    //            case INMEMORY:
    //                return new ArrayList<>(expectedCapacity);
    //            case MAPDBBACKED:
    //                return MapDBUtils.createIndexTreeList(viewname, new ValueCombinationSerializer());
    //            case FILEBACKED:
    //                return new BigArrayList<>(viewname, new ValueCombinationReaderWriter(entrySizeInBytes), expectedCapacity);
    //            default:
    //                throw new RuntimeException("Unsupported SpillType " + spillType.name());
    //        }
    //    }

    private ViewSolution getEmptyViewSolutionBasedOnSpillType(String viewname, int expectedCapacity, int entrySizeInBytes) {
        switch (spillType) {
            case INMEMORY:
                return new ViewSolutionInMemory(expectedCapacity);
            case FILEBACKED_FKeyedBased:
                if (PostgresVConfig.ANONYMIZED_VIEWINFOs.get(viewname).getIsNeverFKeyed())
                    return new ViewSolutionDiskBased(viewname + "_comp", entrySizeInBytes);
                // TODO: Is next line correct?
                return new ViewSolutionInMemory(expectedCapacity);
            default:
                throw new RuntimeException("Unsupported SpillType " + spillType.name());
        }
    }

    private int getCompressedValueCombinationSizeInBytes(String viewname) {
        ViewInfo viewInfo = PostgresVConfig.ANONYMIZED_VIEWINFOs.get(viewname);
        IntList colValues = new IntArrayList(viewInfo.getTableNonkeys().size() + viewInfo.getFkeyViews().size());
        for (int i = viewInfo.getTableNonkeys().size() + viewInfo.getFkeyViews().size() - 1; i >= 0; i--) {
            colValues.add(0);
        }
        ValueCombination compressedValueCombination = new ValueCombination(colValues, -1);
        return compressedValueCombination.getSizeInBytes();
    }

    public Map<String, ViewSolution> getSummaryByView() {
        if (!isConsistent)
            throw new RuntimeException("Trying to get summaryByView which is not yet made consistent");
        if (!isCompressed)
            throw new RuntimeException("Trying to get summaryByView which is not yet compressed");
        return viewSolutions;
    }

    public Map<String, ViewSolution> getUncompressedSummaryByView() {
        if (!isConsistent)
            throw new RuntimeException("Trying to get summaryByView which is not yet made consistent");
        if (isCompressed)
            throw new RuntimeException("Trying to get uncompressed summaryByView when it is already compressed");
        return viewSolutions;
    }

    public Map<String, ViewSolution> getDuplicateUncompressedSummaryByView() {
        Map<String, ViewSolution> uncompressedSummary = getUncompressedSummaryByView();
        return deepClone(uncompressedSummary);
    }

    private static Map<String, ViewSolution> deepClone(Map<String, ViewSolution> summary) {
        Map<String, ViewSolution> cloneSummary = new HashMap<>(summary.keySet().size());
        for (Entry<String, ViewSolution> entry : summary.entrySet()) {
            String viewname = entry.getKey();
            ViewSolution viewSolution = entry.getValue();
            cloneSummary.put(viewname, viewSolution.clone());
        }
        return cloneSummary;
    }

    public void dumpDatabaseSummary() {
        Map<String, ViewSolution> summaryByView = getSummaryByView();
        Map<String, ViewInfo> viewInfos = PostgresVConfig.ANONYMIZED_VIEWINFOs;

        StringBuilder sbindex = new StringBuilder();
        for (Entry<String, ViewSolution> entry : summaryByView.entrySet()) {
            String viewname = entry.getKey();
            ViewSolution viewSolution = entry.getValue();

            DebugHelper.printDebug("Writing database summary for view " + viewname);

            sbindex.append(viewname).append(NEWLINE);
            //Finding relation rowCount which might be different than viewInfos.get(viewname).getRowcount() because of view consistency tuples
            long rowcount = 0;
            for (ValueCombination valueCombination : viewSolution) {
                rowcount += valueCombination.getRowcount();
            }
            String firstRow = viewSolution.getCountOfValueCombinations() + COMMA + (viewInfos.get(viewname).getNonPkeyColCount() + 1) + COMMA + rowcount; //+1 as each row also has tuple count

            FileWriter fw = null;
            try {
                fw = new FileWriter(new File(PostgresVConfig.getProp(Key.DATABASESUMMARY_LOCATION), viewname));
                fw.write(firstRow + NEWLINE);
                for (ValueCombination valueCombination : viewSolution) {
                    fw.write(valueCombination.toStringFileDump() + NEWLINE);
                }
            } catch (IOException ex) {
                throw new RuntimeException("Unable to write database summary for view " + viewname, ex);
            } finally {
                try {
                    fw.close();
                } catch (Exception ex2) {}
            }
        }
        try {
            FileUtils.writeStringToFile(PostgresVConfig.getProp(Key.DATABASESUMMARY_LOCATION), PostgresVConfig.DATABASESUMMARY_INDEX, sbindex.toString());
        } catch (Exception ex) {
            throw new RuntimeException("Unable to write database summary index", ex);
        }
    }

	public void decodeAndDumpDatabaseSummary(HashMap<String, Int2DoubleOpenHashMap> reverseNumberMap, HashMap<String, Int2ObjectOpenHashMap<String>> reverseStringMap, HashMap<String, Int2ObjectOpenHashMap<Date>> reverseDateMap, HashMap<String, String> reverseTablesMap) {
		Map<String, ViewSolution> summaryByView = getSummaryByView();
        SchemaInfo schemaInfo = PostgresVConfig.ANONYMIZED_SCHEMAINFO;
        
        final String DEFAULT_STRING = " ";
        
        Calendar calender = Calendar.getInstance();
        calender.set(-1990, 1, 1);
        final Date DEFAULT_DATE = calender.getTime();

        StringBuilder sbindex = new StringBuilder();
        for (Entry<String, ViewSolution> entry : summaryByView.entrySet()) {		// For each table
            String viewname = entry.getKey();
            ViewSolution viewSolution = entry.getValue();
            TableInfo tableInfo = schemaInfo.getTableInfo(viewname);

            String origViewName = reverseTablesMap.get(viewname);
            DebugHelper.printDebug("Writing database summary for view " + origViewName);

            sbindex.append(viewname).append(NEWLINE);
            //Finding relation rowCount which might be different than viewInfos.get(viewname).getRowcount() because of view consistency tuples
            long rowcount = 0;
            for (ValueCombination valueCombination : viewSolution) {
                rowcount += valueCombination.getRowcount();
            }

            List<Integer> columnPositionMapping = tableInfo.getColumnPositionMapping();
//            int noOfGeneratedCols = viewInfos.get(viewname).getNonPkeyColCount() + 1;	 //+1 as each row also has tuple count
            int positionOfPk = columnPositionMapping.get(0);		// columnPositionMapping.get(0) is position of PK
            String firstRow = viewSolution.getCountOfValueCombinations() + COMMA + columnPositionMapping.size() + COMMA + rowcount + COMMA + positionOfPk + COMMA + "0"; // 0 is unconstrained velocity

            FileWriter fw = null;
            try {
                try {
					fw = new FileWriter(new File(PostgresVConfig.getProp(Key.DATABASESUMMARY_LOCATION), origViewName.toUpperCase()));
	                fw.write(firstRow + NEWLINE);
				} catch (IOException e) {
					e.printStackTrace();
				}
                
                int noOfPKs = 0;	// TODO
                int noOfKeys = noOfPKs + tableInfo.getFkeyTables().size();
                
                List<String> nonKeyColumnnames = new ArrayList<>(tableInfo.columnsNames());
                nonKeyColumnnames.removeAll(tableInfo.getKeys());
                Collections.sort(nonKeyColumnnames);
                
                String values[] = new String[columnPositionMapping.size()];
                
                for (ValueCombination valueCombination : viewSolution) {
                	
                	values[positionOfPk] = Long.toString(valueCombination.getRowcount());
                	
                	int i = 1;
                	String value;
                	for (IntIterator iter = valueCombination.getColValues().iterator(); iter.hasNext();) {
                		int encodedVal = iter.nextInt();
                		
                		if(i > noOfKeys) {
                			String colName = nonKeyColumnnames.get(i - noOfKeys - 1);
                    		String colType = tableInfo.getColumns().get(colName).getColumnType();
                    		
                			if(colType.equals("integer")) {
                				if(encodedVal == 0) {
                					value = Integer.toString(Integer.MIN_VALUE);
                    			} else if(reverseNumberMap.get(colName).containsKey(encodedVal)) {
                    				value = getIntFromDouble(reverseNumberMap.get(colName).get(encodedVal)).toString();
                    			} else if(reverseNumberMap.get(colName).containsKey(encodedVal - 1)) {
                    				value = getIntFromDouble(reverseNumberMap.get(colName).get(encodedVal - 1) + 1).toString();
                    			} else
                    				throw new RuntimeException("Unknown encoded value : " + encodedVal);
                    		}
                			else if(colType.equals("integer") || colType.equals("numeric")) {
                				if(encodedVal == 0) {
                					if(colType.equals("integer"))
                						value = Integer.toString(Integer.MIN_VALUE);
                					else
                						value = Double.toString(Double.MIN_VALUE);
                    			} else if(reverseNumberMap.get(colName).containsKey(encodedVal)) {
                    				value = Double.toString(reverseNumberMap.get(colName).get(encodedVal));
                    			} else if(reverseNumberMap.get(colName).containsKey(encodedVal - 1)) {
                    				if(colType.equals("integer"))
                    					value = Double.toString(reverseNumberMap.get(colName).get(encodedVal - 1) + 1);
                    				else
                    					value = Double.toString(reverseNumberMap.get(colName).get(encodedVal - 1) + 0.00001);
                    			} else
                    				throw new RuntimeException("Unknown encoded value : " + encodedVal);
                    		}
                    		
                    		else if(colType.startsWith("character")) {
                    			if(encodedVal == 0) {
                    				value = "\"" + DEFAULT_STRING + "\"";
                    			} else if(reverseStringMap.get(colName).containsKey(encodedVal)) {
                    				value = "\"" + reverseStringMap.get(colName).get(encodedVal) + "\"";
                    			} else 
                    				throw new RuntimeException("Unknown encoded value : " + encodedVal);
                    		}
                    		
                    		else if(colType.equals("date")) {
                    			if(encodedVal == 0) {
                    				value = DEFAULT_DATE.toString();
                    			} else if(reverseDateMap.get(colName).containsKey(encodedVal)) {
                    				value = reverseDateMap.get(colName).get(encodedVal).toString();
                    			} else if(reverseDateMap.get(colName).containsKey(encodedVal - 1)) {
                    				value = getNextDate(calender, reverseDateMap.get(colName).get(encodedVal - 1)).toString();
                    			} else
                    				throw new RuntimeException("Unknown encoded value : " + encodedVal);
                    		}
                    		else if(colType.startsWith("time")) {
                    			if(encodedVal == 0)
                    				value = "00:00:00";
                    			else
                    				throw new RuntimeException("Not handled");
                    		}
                    		else {
//                    			DebugHelper.printError("Unknown column type: " + colType);
                    			throw new RuntimeException("Unknown column type: " + colType);
                    		}
                		} else {
                			value = Integer.toString(encodedVal);
                		}
                		values[columnPositionMapping.get(i)] = value;
                		i++;
                	}
                	for(int j = i; j < columnPositionMapping.size(); ++j)
                		values[columnPositionMapping.get(j)] = "-1";
                	String s = StringUtils.join(values, COMMA);
					fw.write(s + NEWLINE);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Unable to write database summary for view " + origViewName + NEWLINE + ex.toString(), ex);
            } finally {
                try {
                    fw.close();
                } catch (Exception ex2) {}
            }
        }
        try {
            FileUtils.writeStringToFile(PostgresVConfig.getProp(Key.DATABASESUMMARY_LOCATION), PostgresVConfig.DATABASESUMMARY_INDEX, sbindex.toString());
        } catch (Exception ex) {
            throw new RuntimeException("Unable to write database summary index", ex);
        }
	}
	
	private Integer getIntFromDouble(Double value) {
		return Integer.valueOf((int) Math.round(value));
	}

    private Date getNextDate(Calendar calender, Date date) {
    	calender.setTime(date);
    	calender.add(Calendar.DATE, 1);  // number of days to add
		return calender.getTime();
	}

	public void dumpAllStaticRelations() {
        Map<String, ViewSolution> summaryByView = getSummaryByView();
        List<String> dumpViewnames = new ArrayList<>(summaryByView.keySet());
        Collections.sort(dumpViewnames);
        dumpStaticRelations(dumpViewnames);
    }

    public void dumpStaticRelations(List<String> dumpViewnames) {
        Map<String, ViewSolution> summaryByView = getSummaryByView();

        StringBuilder sbindex = new StringBuilder();
        for (String viewname : dumpViewnames) {
            ViewSolution viewSolution = summaryByView.get(viewname);

            DebugHelper.printDebug("Writing static database dump for view " + viewname);

            sbindex.append(viewname).append(NEWLINE);

            FileWriter fw = null;
            try {
                fw = new FileWriter(new File(PostgresVConfig.getProp(Key.DATABASESTATICDUMP_LOCATION), viewname));
                long pk = 0;
                for (ValueCombination valueCombination : viewSolution) {
                    for (long i = 0; i < valueCombination.getRowcount(); i++) {
                        fw.write(pk++ + COMMA + valueCombination.toStringStaticDump() + NEWLINE);
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException("Unable to write static database dump for view " + viewname, ex);
            } finally {
                try {
                    fw.close();
                } catch (Exception ex2) {}
            }
        }
        try {
            FileUtils.writeStringToFile(PostgresVConfig.getProp(Key.DATABASESTATICDUMP_LOCATION), PostgresVConfig.DATABASESUMMARY_INDEX, sbindex.toString());
        } catch (Exception ex) {
            throw new RuntimeException("Unable to write static database dump index", ex);
        }
    }
    
    public List<String> decodeAndDumpStaticRelations(HashMap<String, Int2DoubleOpenHashMap> reverseNumberMap, HashMap<String, Int2ObjectOpenHashMap<String>> reverseStringMap, HashMap<String, Int2ObjectOpenHashMap<Date>> reverseDateMap,
    		Map<String, String> TABLES_MAP, List<String> dumpViewnames, String outputLocation, SharedBoolean doWork) {
		Map<String, ViewSolution> summaryByView = getSummaryByView();
        SchemaInfo schemaInfo = PostgresVConfig.ANONYMIZED_SCHEMAINFO;
        
        final String DEFAULT_STRING = " ";
        
        Calendar calender = Calendar.getInstance();
        calender.set(-1990, 1, 1);
        final Date DEFAULT_DATE = calender.getTime();
        
        List<String> doneViews = new ArrayList<>();

        StringBuilder sbindex = new StringBuilder();
        for (String origViewName : dumpViewnames) {
            String viewname = TABLES_MAP.get(origViewName);
            ViewSolution viewSolution = summaryByView.get(viewname);
            TableInfo tableInfo = schemaInfo.getTableInfo(viewname);
            DebugHelper.printDebug("Writing static database dump for view " + origViewName);

            sbindex.append(origViewName).append(NEWLINE);

            List<Integer> columnPositionMapping = tableInfo.getColumnPositionMapping();
            int positionOfPk = columnPositionMapping.get(0);		// columnPositionMapping.get(0) is position of PK

            FileWriter fw = null;
            try {
				fw = new FileWriter(new File(outputLocation, origViewName.toUpperCase()));
                
                int noOfPKs = 0;	// TODO
                int noOfKeys = noOfPKs + tableInfo.getFkeyTables().size();
                
                List<String> nonKeyColumnnames = new ArrayList<>(tableInfo.columnsNames());
                nonKeyColumnnames.removeAll(tableInfo.getKeys());
                Collections.sort(nonKeyColumnnames);
                
                String values[] = new String[columnPositionMapping.size()];
                long pk = 0;

                for (ValueCombination valueCombination : viewSolution) {
//                	values[positionOfPk] = Long.toString(valueCombination.getRowcount());
                	
                	int i = 1;
                	String value;
                	for (IntIterator iter = valueCombination.getColValues().iterator(); iter.hasNext();) {
                		int encodedVal = iter.nextInt();
                		
                		if(i > noOfKeys) {
                			String colName = nonKeyColumnnames.get(i - noOfKeys - 1);
                    		String colType = tableInfo.getColumns().get(colName).getColumnType();
                    		
                			if(colType.equals("integer")) {
                				if(encodedVal == 0) {
                					value = Integer.toString(Integer.MIN_VALUE);
                    			} else if(reverseNumberMap.get(colName).containsKey(encodedVal)) {
                    				value = getIntFromDouble(reverseNumberMap.get(colName).get(encodedVal)).toString();
                    			} else if(reverseNumberMap.get(colName).containsKey(encodedVal - 1)) {
                    				value = getIntFromDouble(reverseNumberMap.get(colName).get(encodedVal - 1) + 1).toString();
                    			} else
                    				throw new RuntimeException("Unknown encoded value : " + encodedVal);
                    		}
                			else if(colType.equals("integer") || colType.equals("numeric")) {
                				if(encodedVal == 0) {
                					if(colType.equals("integer"))
                						value = Integer.toString(Integer.MIN_VALUE);
                					else
                						value = Double.toString(Double.MIN_VALUE);
                    			} else if(reverseNumberMap.get(colName).containsKey(encodedVal)) {
                    				value = Double.toString(reverseNumberMap.get(colName).get(encodedVal));
                    			} else if(reverseNumberMap.get(colName).containsKey(encodedVal - 1)) {
                    				if(colType.equals("integer"))
                    					value = Double.toString(reverseNumberMap.get(colName).get(encodedVal - 1) + 1);
                    				else
                    					value = Double.toString(reverseNumberMap.get(colName).get(encodedVal - 1) + 0.00001);
                    			} else
                    				throw new RuntimeException("Unknown encoded value : " + encodedVal);
                    		}
                    		
                    		else if(colType.startsWith("character")) {
                    			if(encodedVal == 0) {
                    				value = "\"" + DEFAULT_STRING + "\"";
                    			} else if(reverseStringMap.get(colName).containsKey(encodedVal)) {
                    				value = "\"" + reverseStringMap.get(colName).get(encodedVal) + "\"";
                    			} else 
                    				throw new RuntimeException("Unknown encoded value : " + encodedVal);
                    		}
                    		
                    		else if(colType.equals("date")) {
                    			if(encodedVal == 0) {
                    				value = DEFAULT_DATE.toString();
                    			} else if(reverseDateMap.get(colName).containsKey(encodedVal)) {
                    				value = reverseDateMap.get(colName).get(encodedVal).toString();
                    			} else if(reverseDateMap.get(colName).containsKey(encodedVal - 1)) {
                    				value = getNextDate(calender, reverseDateMap.get(colName).get(encodedVal - 1)).toString();
                    			} else
                    				throw new RuntimeException("Unknown encoded value : " + encodedVal);
                    		}
                    		else if(colType.startsWith("time")) {
                    			if(encodedVal == 0)
                    				value = "00:00:00";
                    			else
                    				throw new RuntimeException("Not handled");
                    		}
                    		else {
//                    			DebugHelper.printError("Unknown column type: " + colType);
                    			throw new RuntimeException("Unknown column type: " + colType);
                    		}
                		} else {
                			value = Integer.toString(encodedVal);
                		}
                		values[columnPositionMapping.get(i)] = value;
                		i++;
                	}
                	for(int j = i; j < columnPositionMapping.size(); ++j)
                		values[columnPositionMapping.get(j)] = "-1";
                	for (long k = 0; k < valueCombination.getRowcount(); ++k) {
                		if (!doWork.value)
                			return doneViews;
                		String curValues[] = createCopy(values);
                		curValues[positionOfPk] = Long.toString(pk++);
                		String s = StringUtils.join(curValues, COMMA);
    					fw.write(s + NEWLINE);
                	}
                }
                doneViews.add(origViewName);
            } catch (Exception ex) {
                throw new RuntimeException("Unable to write static database dump for view " + origViewName + NEWLINE + ex.toString(), ex);
            } finally {
                try {
                    fw.close();
                } catch (Exception ex2) {}
            }
        }
        try {
            FileUtils.writeStringToFile(PostgresVConfig.getProp(Key.DATABASESUMMARY_LOCATION), PostgresVConfig.DATABASESUMMARY_INDEX, sbindex.toString());
        } catch (Exception ex) {
        	throw new RuntimeException("Unable to write static database dump index", ex);
        }
        return doneViews;
	}

	private String[] createCopy(String[] values) {
		String curValues[] = new String[values.length];
		for (int i = 0; i < values.length; ++i)
			curValues[i] = values[i];
		return curValues;
	}
}