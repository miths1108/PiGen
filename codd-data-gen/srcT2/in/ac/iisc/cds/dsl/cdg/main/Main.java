package in.ac.iisc.cds.dsl.cdg.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import in.ac.iisc.cds.dsl.cdgclient.anonymizer.Anonymizer;
import in.ac.iisc.cds.dsl.cdgclient.anonymizer.SchemaAnonymize;
import in.ac.iisc.cds.dsl.cdgclient.anonymizer.SchemaAnonymizePostgres;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig.Key;
import in.ac.iisc.cds.dsl.cdgclient.preprocess.AlqpToCC;
import in.ac.iisc.cds.dsl.cdgclient.preprocess.AlqpToCCPostgres;
import in.ac.iisc.cds.dsl.cdgclient.preprocess.DDLGenerator;
import in.ac.iisc.cds.dsl.cdgclient.preprocess.ExplainAnalyzeToAlqp;
import in.ac.iisc.cds.dsl.cdgclient.preprocess.ExplainAnalyzeToAlqpPostgres;
import in.ac.iisc.cds.dsl.cdgclient.preprocess.QueryToExplainAnalyze;
import in.ac.iisc.cds.dsl.cdgclient.preprocess.QueryToExplainAnalyzePostgres;
import in.ac.iisc.cds.dsl.cdgclient.preprocess.SchemaAnalyze;
import in.ac.iisc.cds.dsl.cdgclient.preprocess.SchemaAnalyzePostgres;
import in.ac.iisc.cds.dsl.cdgclient.preprocess.WorkloadDecomposer;
import in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig;
import in.ac.iisc.cds.dsl.cdgvendor.model.Alqp;
import in.ac.iisc.cds.dsl.cdgvendor.model.CCInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.HistogramMappingInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ProjectionVariableOptimized;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolution;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionRegion;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;
import in.ac.iisc.cds.dsl.cdgvendor.solver.DatabaseSummary;
import in.ac.iisc.cds.dsl.cdgvendor.solver.DomainDecomposer;
import in.ac.iisc.cds.dsl.cdgvendor.solver.Z3Solver;
import in.ac.iisc.cds.dsl.cdgvendor.solver.Z3Solver.SolverType;
import in.ac.iisc.cds.dsl.cdgvendor.solver.Z3Solver.SpillType;
import in.ac.iisc.cds.dsl.cdgvendor.utils.ConditionsEvaluator;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import in.ac.iisc.cds.dsl.cdgvendor.utils.FileUtils;
import in.ac.iisc.cds.dsl.cdgvendor.utils.StopWatch;
import in.ac.iisc.cds.dsl.dirty.DirtyCode;
import in.ac.iisc.cds.dsl.dirty.DirtyDatabaseSummary;
import it.unimi.dsi.fastutil.ints.IntList;

public class Main {

	private static void checkHeapSize() {
		// Get current size of heap in bytes
		long heapSize = Runtime.getRuntime().totalMemory();

		// Get maximum size of heap in bytes. The heap cannot grow beyond this size.//
		// Any attempt will result in an OutOfMemoryException.
		long heapMaxSize = Runtime.getRuntime().maxMemory();

		// Get amount of free memory within the heap in bytes. This size will increase
		// // after garbage collection and decrease as new objects are created.
		long heapFreeSize = Runtime.getRuntime().freeMemory();

		System.out.println(heapSize / (1024 * 1024));
		System.out.println(heapMaxSize / (1024 * 1024));
		System.out.println(heapFreeSize / (1024 * 1024));
	}

	public static void main(String[] args) {
//    	UndirectedGraph<String, DefaultEdge> projectionConditionGraph = new SimpleGraph<>(DefaultEdge.class);
//		
//		projectionConditionGraph.addVertex("1");
//		projectionConditionGraph.addVertex("2");
//		projectionConditionGraph.addVertex("3");
//		projectionConditionGraph.addVertex("4");
//	//projectionConditionGraph.addVertex("5");
//		projectionConditionGraph.addEdge("1", "2");
//		projectionConditionGraph.addEdge("2", "3");
//		projectionConditionGraph.addEdge("3", "4");
//		projectionConditionGraph.addEdge("4", "1");
//		projectionConditionGraph.addEdge("1", "3");
////		projectionConditionGraph.addEdge("1", "3");
//		
//		List<ProjectionVariableOptimized> varList = getProjectionVariablesOptimized(projectionConditionGraph);

		/************************************************************
		 * This call produces explain-analyze from given queries. Takes a lot of time.
		 * Keep it commented until you have changed the queries
		 ************************************************************/

		StopWatch preprocessingSW = new StopWatch("Tot TIme");
		PostgresCConfig.initDefaultConfig();
		// PostgresCConfig.overlayOnDefaultConfig(overlapConfig);

		 generateEAsPostgres();

		/************************************************************
		 * This call produces cardinality constraints from given EAs. EAs are traversed
		 * to extract join and filter conditions. Then tablenames and columnnames are
		 * anonymized. Also all literal values appearing in the constraints (other than
		 * output cardinalities) are mapped to integer domains
		 ************************************************************/

		generateAnonymizedCCsPostgres();

		/************************************************************
		 * This call triggers the data generation module which takes anonymized CCs as
		 * input and produces database summary
		 ************************************************************/
		PostgresVConfig.initDefaultConfig();
		// Projection workload decomposition
//		decomposeWorkload();
		coddgenPostgres();
		preprocessingSW.displayTimeAndDispose();

	}

	private static List<ProjectionVariableOptimized> getProjectionVariablesOptimized(
			UndirectedGraph<String, DefaultEdge> projectionConditionGraph) {
		List<String> nodes = new ArrayList<>(projectionConditionGraph.vertexSet());
		Collections.sort(nodes);
		List<ProjectionVariableOptimized> varList = new ArrayList<>();

		List<String> visited = new ArrayList<>();
		for (String node : nodes) {
//			Region currNodeRegion = cliqueIdxtoPList.get(cliqueIndex).getAll().get(Integer.parseInt(node));
//			Region currRegionProjection = projectRegion(currNodeRegion, groupkey);
			List<String> intersectionNeighbourVisited = Graphs.neighborListOf(projectionConditionGraph, node);

			intersectionNeighbourVisited.retainAll(visited);
			ProjectionVariableOptimized var = new ProjectionVariableOptimized();
			var.positive.add(Integer.parseInt(node));
			// add the visited region in negative region only when it has an intersection
			for (String neg : intersectionNeighbourVisited) {
				// Region currNeg =
				// cliqueIdxtoPList.get(cliqueIndex).getAll().get(Integer.parseInt(neg));
//				if(currRegionProjection.intersection(projectRegion(currNeg,groupkey)).isEmpty())
//					continue;
				var.negative.add(Integer.parseInt(neg));
			}
			// var.addStringNegative(intersection);
			// var.intersection = currRegionProjection;
			varList.addAll(getProjectionVariablesOptimizedHelper(var, visited, projectionConditionGraph));
			// varList.addAll(getProjectionVariablesOptimizedHelper(var,new ArrayList<>(),
			// projectionConditionGraph));
			visited.add(node);

		}
		return varList;

	}

	private static List<ProjectionVariableOptimized> getProjectionVariablesOptimizedHelper(
			ProjectionVariableOptimized var, List<String> visited,
			UndirectedGraph<String, DefaultEdge> projectionConditionGraph) {
		List<ProjectionVariableOptimized> ret = new ArrayList<>();

		String vertex = null;

		for (int i = 0; i < var.positive.size(); i++) {
			String currVertex = (var.positive.get(i)).toString();
			if (!visited.contains(currVertex)) {
				vertex = currVertex;
				break;
			}
		}
		if (vertex == null) {

			Set<String> tempNeighbours = new HashSet<>();
			for (Integer pos : var.positive)
				tempNeighbours.addAll(Graphs.neighborListOf(projectionConditionGraph, pos.toString()));
//			tempNeighbours.removeAll(var.positive);
//			tempNeighbours.removeAll(var.negative);
			for (Integer neg : var.negative)
				tempNeighbours.remove(neg.toString());
			for (Integer pos : var.positive)
				tempNeighbours.remove(pos.toString());

			for (String neg : tempNeighbours) {
				var.negative.add(Integer.parseInt(neg));
			}
			ret.add(var);

			// System.out.println(countVarOptimized++);
			return ret;
		}
		List<String> neighbours = Graphs.neighborListOf(projectionConditionGraph, vertex);
		neighbours.removeAll(visited);
		for (Integer neg : var.negative)
			neighbours.remove(neg.toString());
		for (Integer pos : var.positive)
			neighbours.remove(pos.toString());

		List<ProjectionVariableOptimized> powerSet = getPowerSetOptimized(var, neighbours);

		for (ProjectionVariableOptimized set : powerSet) {
			List<String> currVisited = new ArrayList<>();
			currVisited.addAll(visited);
			currVisited.add(vertex);
			ret.addAll(getProjectionVariablesOptimizedHelper(set, currVisited, projectionConditionGraph));
		}
		// TODO Auto-generated method stub
		return ret;
	}

	private static List<ProjectionVariableOptimized> getPowerSetOptimized(ProjectionVariableOptimized var,
			List<String> neighbours) {
		// TODO Auto-generated method stub
//		if(var.positive.contains(0)&&var.positive.contains(1))
//		{
//			System.out.print("hi");
//		}
		List<ProjectionVariableOptimized> ret = new ArrayList<>();
		if (neighbours.isEmpty()) {
			ret.add(var);
			return ret;
		}
		List<Integer> projectionColumnsIdx = new ArrayList<>();

		// converting col names to index
//		for (String projectionColumn : groupkey) {
//			projectionColumnsIdx.add(sortedViewColumns.indexOf(projectionColumn));
//		}

		// check if var1 is possible
//		Region currRegion = cliqueIdxtoPList.get(cliqueIndex).getAll().get(Integer.parseInt(neighbours.get(0)));
//		Region projectedRegion = projectRegion(currRegion, groupkey);

		// if there is intersection, only then the new variables must be formed
//		if (!projectedRegion.intersection(var.intersection).isEmpty()) {

		ProjectionVariableOptimized var1 = new ProjectionVariableOptimized();

		var1.positive.addAll(var.positive);
		var1.negative.addAll(var.negative);

		ProjectionVariableOptimized var2 = new ProjectionVariableOptimized();
		var2.positive.addAll(var.positive);
		var2.negative.addAll(var.negative);

		var1.addPositive(Integer.parseInt(neighbours.get(0)));
		// var1.intersection = projectedRegion;bug
		// var1.intersection=projectedRegion.intersection(var.intersection);

		var2.addNegative(Integer.parseInt(neighbours.get(0)));
		// var2.intersection = var.intersection;

		ret.addAll(getPowerSetOptimized(var1, neighbours.subList(1, neighbours.size())));
		ret.addAll(getPowerSetOptimized(var2, neighbours.subList(1, neighbours.size())));
//		} else {
//			ret.addAll(getPowerSetOptimized(var, neighbours.subList(1, neighbours.size()), groupkey, cliqueIndex));
//		}
		return ret;
	}

	private static void decomposeWorkload() {
		// TODO Auto-generated method stub

		// StopWatch readInputSW = new StopWatch("Coddgen-ReadInputSW");
		// Step 1: Read SchemaInfo and ColumnIdMap
		// in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig.loadColumnIdMap();
		in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig.loadAnonymizedViewInfos();

		// Step 1: Read CCInfo
		in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig.loadCCInfo();
		CCInfo ccInfo = in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig.CC_INFO;
		Map<String, List<FormalCondition>> viewnameToCCMap = ccInfo.getViewnameToCCMap();
		List<String> sortedViewnames = new ArrayList<>(viewnameToCCMap.keySet());
		Collections.sort(sortedViewnames);
		int i = 0;
		WorkloadDecomposer wd = new WorkloadDecomposer();
		for (String viewname : sortedViewnames) {
			// if (viewname.equals("t03"))
			// continue;
			// if (viewname.equals("t05"))
			// continue;
			// int x;
			// i++;
			// if (i<=2)
			// continue;

			List<FormalCondition> conditions = viewnameToCCMap.get(viewname);
//            for(ListIterator<FormalCondition>iter=conditions.listIterator();iter.hasNext();){
//        		FormalCondition condition=iter.next();
//        		if( !((FormalConditionAggregate)condition).isTop())
//        			iter.remove();
//    		}

			List<FormalCondition> conditionsPercent = new ArrayList<>();
			Collections.shuffle(conditions);
			for (ListIterator<FormalCondition> iter = conditions.listIterator(); iter.hasNext();) {
				FormalCondition condition = iter.next();
				if (!(condition instanceof FormalConditionAggregate) || !((FormalConditionAggregate) condition).isTop())
					iter.remove();
			}

			// temp
			int n = conditions.size();
			for (int j = 0; j < n; j++) {
				conditionsPercent.add(conditions.get(j));
				System.out.println(conditions.get(j).getQueryName());
			}
			ViewInfo viewInfo = PostgresVConfig.ANONYMIZED_VIEWINFOs.get(viewname);

			DebugHelper.printInfo("\nSolving View: " + viewname + " (" + viewInfo.getViewNonkeys().size()
					+ " dimensions | " + viewInfo.getRowcount() + " rows)");
			DebugHelper.printInfo("View Name: " + viewname + " Columns: " + viewInfo.getViewNonkeys());

			// wd.addConditions(conditions,viewInfo);
			wd.addConditions(conditionsPercent, viewInfo);

		}
		List<List<String>> workloads = wd.decompose();
		int tot = 0;
		for (int j = 0; j < workloads.size(); j++) {

			System.out.println("Workload" + j);
			for (String file : workloads.get(j)) {
				tot++;
				System.out.println(file);
			}
		}
		// PostgresCConfig.initDefaultConfig();
//		generateAnonymizedCCsPostgresDecomposed(workloads.get(0));
		coddgenPostgres();
	}

	private static void generateEAsPostgres() {
		DebugHelper.printInfo("-------- Gateway: Generating EAs for Postgres ------------");

//        Step 1: Fetch the database schema
		SchemaAnalyze sa = new SchemaAnalyzePostgres();
		sa.explainSchema();

//        Step 2: Fetch explain-analyze for all given queries
		List<String> queryIndex = FileUtils.readLines(PostgresCConfig.getProp(Key.SQLQUERIES_LOCATION),
				PostgresCConfig.SQLQUERIES_INDEX);
		List<String> queriesFromFiles = new ArrayList<>();
		List<String> queryNames = new ArrayList<>();
		for (String queryFilename : queryIndex) {
			String sqlQuery = FileUtils.readFileToString(PostgresCConfig.getProp(Key.SQLQUERIES_LOCATION),
					queryFilename);
			if (sqlQuery.startsWith("#"))
				continue;
			queriesFromFiles.add(sqlQuery);
			queryNames.add(queryFilename);
		}
		QueryToExplainAnalyze ea = new QueryToExplainAnalyzePostgres();
		ea.explainAnalyzeQueries(queriesFromFiles, queryNames);

		DebugHelper.printInfo("-------- Gateway: Done generating EAs for Postgres ------------");
	}

	private static void generateAnonymizedCCsPostgres() {
		DebugHelper.printInfo("-------- Gateway: Generating anonymized CCs for Postgres ------------");

		// Step 1: Read schema
		PostgresCConfig.loadBasicSchemaInfo();

		// Step 2: Anonymize it while creating anonymizer
		SchemaAnonymize san = new SchemaAnonymizePostgres();
		Anonymizer anonymizer = san.anonymizeSchema();

//        No need of the anonymized schema and queries since the plan is not same. Instead, performing deanonymizing later so that original schema and queries can be used 
		// Step 3: Generate ddl for anonymized schema. Also contains primary key/foreign
		// key constraints
//        DDLGenerator dg = new DDLGenerator();
//        dg.generateDDL(anonymizer);

		// Step 4: Read all given explain-analyze as ALQPs
		List<String> eaIndex = FileUtils.readLines(PostgresCConfig.getProp(Key.EXPANALYZE_LOCATION),
				PostgresCConfig.EXPANALYZE_INDEX);
		List<String> easFromFiles = new ArrayList<>(); // EA, Plan in Json String
		List<String> queryNames = new ArrayList<>();
		for (String eaFilename : eaIndex) {
			if (eaFilename.startsWith("#"))
				continue;
			String eaStr = FileUtils.readFileToString(PostgresCConfig.getProp(Key.EXPANALYZE_LOCATION), eaFilename);
			easFromFiles.add(eaStr);
			queryNames.add(eaFilename);
		}

		ExplainAnalyzeToAlqp ea = new ExplainAnalyzeToAlqpPostgres();
//        List<Alqp> alqps = ea.createAllAlqp(easFromFiles, queryNames, anonymizer);
		List<Alqp> alqps = ea.createAllAlqp(easFromFiles, queryNames, PostgresCConfig.BASICSCHEMA_INFO.getTableInfos());

		// Step 5: Generate anonymized CCs and queries with domain mapping
		HistogramMappingInfo emptyHistogramMappingInfo = new HistogramMappingInfo(); // TODO : for metadata fetching
		AlqpToCC ac = new AlqpToCCPostgres(anonymizer);
		ac.anonymizeAlqpsAndGenerateCCsAndQueries(alqps, emptyHistogramMappingInfo);

		DebugHelper.printInfo("-------- Gateway: Done generating anonymized CCs for Postgres ------------");
	}

	private static void coddgenPostgres() {

		SolverType solverType = SolverType.DOUBLE;
		SpillType spillType = SpillType.FILEBACKED_FKeyedBased;

		DebugHelper.printInfo("-------- CODDGEN: Started Postgres ------------");
		DebugHelper.printInfo("solverType: " + solverType + " spillType:" + spillType);

		StopWatch readInputSW = new StopWatch("Coddgen-ReadInputSW");
		// Step 1: Read SchemaInfo and ColumnIdMap
		// in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig.loadColumnIdMap();
		in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig.loadAnonymizedViewInfos();

		// Step 1: Read CCInfo
		in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig.loadCCInfo();
		CCInfo ccInfo = in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig.CC_INFO;
		readInputSW.displayTimeAndDispose();

		// Step 2: Solver using z3
		Z3Solver solver = new Z3Solver(solverType, spillType);
		// Map<String, ViewSolution> viewSolutions = new HashMap<>();
		Map<String, ViewSolutionRegion> viewSolutions = new HashMap<>();
//        if(false) {
//        	viewSolutions = solveEachView(solver, ccInfo);  
//        }
//        else
//        {
//        	viewSolutions = solveAllViewInStages(solver, ccInfo);
//        }
		// viewSolutions = solveEachView(solver, ccInfo);
		solveEachView(solver, ccInfo);

		// Step 3: Create DatabaseSummary
//        StopWatch databaseSummarySW = new StopWatch("Database-Summary");
//        DatabaseSummary databaseSummary = new DatabaseSummary(databaseSummarySW, spillType, viewSolutions);
//        
//        DirtyCode.postMakingSummary();
//        
//        databaseSummary.makeFKeyConsistency();
//        debugTotalErrorPerCondition(databaseSummarySW, databaseSummary, ccInfo);
//        databaseSummary.compressSummaryByAddingFkeys();
//        if (solverType == SolverType.DOUBLE) {
//            databaseSummary.dumpDatabaseSummary();
//        }
//        databaseSummarySW.displayTimeAndDispose();
//        
////        if (solverType == SolverType.DOUBLE) {
////            StopWatch doubleStaticDumpSW = new StopWatch("DOUBLE-Static-Dump");
////            databaseSummary.dumpAllStaticRelations();
////            doubleStaticDumpSW.displayTimeAndDispose();
////        }

		DebugHelper.printInfo("-------- CODDGEN: Finished Postgres ------------");

	}

	private static void generateAnonymizedCCsPostgresDecomposed(List<String> workload) {
		DebugHelper.printInfo("-------- Gateway: Generating anonymized CCs for Postgres ------------");

		// Step 1: Read schema
		PostgresCConfig.loadBasicSchemaInfo();

		// Step 2: Anonymize it while creating anonymizer
		SchemaAnonymize san = new SchemaAnonymizePostgres();
		Anonymizer anonymizer = san.anonymizeSchema();

//        No need of the anonymized schema and queries since the plan is not same. Instead, performing deanonymizing later so that original schema and queries can be used 
		// Step 3: Generate ddl for anonymized schema. Also contains primary key/foreign
		// key constraints
//        DDLGenerator dg = new DDLGenerator();
//        dg.generateDDL(anonymizer);

		// Step 4: Read all given explain-analyze as ALQPs
		// List<String> eaIndex =
		// FileUtils.readLines(PostgresCConfig.getProp(Key.EXPANALYZE_LOCATION),
		// PostgresCConfig.EXPANALYZE_INDEX);
		List<String> easFromFiles = new ArrayList<>(); // EA, Plan in Json String
		List<String> queryNames = new ArrayList<>();
		for (String eaFilename : workload) {
			// if (eaFilename.startsWith("#"))
			// continue;
			String eaStr = FileUtils.readFileToString(PostgresCConfig.getProp(Key.EXPANALYZE_LOCATION), eaFilename);
			easFromFiles.add(eaStr);
			queryNames.add(eaFilename);
		}

		ExplainAnalyzeToAlqp ea = new ExplainAnalyzeToAlqpPostgres();
//        List<Alqp> alqps = ea.createAllAlqp(easFromFiles, queryNames, anonymizer);
		List<Alqp> alqps = ea.createAllAlqp(easFromFiles, queryNames, PostgresCConfig.BASICSCHEMA_INFO.getTableInfos());

		// Step 5: Generate anonymized CCs and queries with domain mapping
		HistogramMappingInfo emptyHistogramMappingInfo = new HistogramMappingInfo(); // TODO : for metadata fetching
		AlqpToCC ac = new AlqpToCCPostgres(anonymizer);
		ac.anonymizeAlqpsAndGenerateCCsAndQueries(alqps, emptyHistogramMappingInfo);

		DebugHelper.printInfo("-------- Gateway: Done generating anonymized CCs for Postgres ------------");
	}

	private static Map<String, ViewSolution> solveAllViewInStages(Z3Solver solver, CCInfo ccInfo) {

		// Map<String, ViewSolution> viewSolutions = new HashMap<>();

		Map<String, List<FormalCondition>> viewnameToCCMap = ccInfo.getViewnameToCCMap();
		List<String> sortedViewNames = new ArrayList<>(viewnameToCCMap.keySet());
		Collections.sort(sortedViewNames);
		List<List<FormalCondition>> conditionsList = new ArrayList<>();
		List<ViewInfo> viewInfoList = new ArrayList<>();
		for (String viewname : sortedViewNames) {
			conditionsList.add(viewnameToCCMap.get(viewname));
			viewInfoList.add(PostgresVConfig.ANONYMIZED_VIEWINFOs.get(viewname));
		}

		/*
		 * What we have
		 * 
		 * 1. List of viewNames in sorted order (sortedViewNames) 2. List of viewInfo at
		 * the same index of their viewName List (viewInfoList) 3. List of CCs
		 * applicable on the same views corresponding to same index in viewName list
		 * (conditionsList) 4. scaleFactor = 1
		 * 
		 */

		int scaleFactor = 1;
		// List<ViewSolution> viewSolutionList =
		// solver.solveAllView(viewInfoList,conditionsList,sortedViewNames,
		// scaleFactor);
		Map<String, ViewSolution> viewSolutions = new HashMap<>(
				solver.solveAllView(viewInfoList, conditionsList, sortedViewNames, scaleFactor));

		// Adding trivial solution for all view which didn't appear in viewnameToCCMap
		// i.e., which had no constraints
		for (String viewname : PostgresVConfig.VIEWNAMES_TOPO) {
			if (!viewSolutions.keySet().contains(viewname)) {
				ViewInfo viewInfo = PostgresVConfig.ANONYMIZED_VIEWINFOs.get(viewname);
				ViewSolution viewSolution = solver.getTrivialSolution(viewInfo);
				viewSolutions.put(viewname, viewSolution);

			}
		}

		return viewSolutions;
	}

	private static Map<String, ViewSolutionRegion> solveEachView(Z3Solver solver, CCInfo ccInfo) {

		Map<String, ViewSolutionRegion> viewSolutions = new HashMap<>();

		Map<String, List<FormalCondition>> viewnameToCCMap = ccInfo.getViewnameToCCMap();
		List<String> sortedViewnames = new ArrayList<>(viewnameToCCMap.keySet());
		Collections.sort(sortedViewnames);
		int count = 0;
		for (String viewname : sortedViewnames) {
			System.out.println(viewname);
			List<FormalCondition> conditions = viewnameToCCMap.get(viewname);
			for (FormalCondition condition : conditions) {
				if (condition instanceof FormalConditionAggregate)
					if (((FormalConditionAggregate) condition).isTop()) {
						System.out.println(condition.getQueryName() + "\t" + condition.getOutputCardinality() + "\t"
								+ ((FormalConditionAggregate) condition).getProjectionCardinality());
						count++;

					}

				// System.out.println(Math.ceil(Math.log10(((FormalConditionAggregate)condition).getProjectionCardinality())));
			}
		}
		System.out.println("Query COunt=" + count);
		for (String viewname : sortedViewnames) {
			int j = 0;
//        	while(j++<5) {
//        	if (!viewname.equals("t17"))
//        		continue;
//        	if (!viewname.equals("t21"))
//        		continue;
			// int x;
			// i++;
			// if (i<=2)
			// continue;
			System.out.println(viewname);

			List<FormalCondition> conditions = viewnameToCCMap.get(viewname);
//			for (FormalCondition condition : conditions) {
//				if (condition instanceof FormalConditionAggregate)
//					if (((FormalConditionAggregate) condition).isTop()) {
//						Long card=((FormalConditionAggregate) condition).getProjectionCardinality();
//						if(card<100)
//							count1++;
//						else if(card<1000)
//							count2++;
//						else if(card<10000)
//							count3++;
//						else if(card<100000)
//							count2++;
//						else if(card<1000000)
//							count2++;
//						else if(card<1000)
//							count2++;
//						
//								System.out.println(condition.getQueryName());
//					}
//			}
			ViewInfo viewInfo = PostgresVConfig.ANONYMIZED_VIEWINFOs.get(viewname);
			DebugHelper.printInfo("\nSolving View: " + viewname);
			// DebugHelper
			// .printInfo("\nSolving View: " + viewname + " (" +
			// viewInfo.getViewNonkeys().size() + " dimensions | " + viewInfo.getRowcount()
			// + " rows)");
			// DebugHelper.printInfo("View Name: " + viewname + " Columns: " +
			// viewInfo.getViewNonkeys());

			/*
			 * for(FormalCondition cc: conditions){
			 * DebugHelper.printInfo(cc.getOutputCardinality()+""); }
			 */

			// Shadab
			// ViewSolution viewSolution = solver.solveView(conditions, viewInfo, viewname,
			// 1);
			// GCUtils.inviteGC();
			ViewSolutionRegion viewSolutionRegion = solver.solveView(conditions, viewInfo, viewname, 1);
		}
		// viewSolutions.put(viewname, viewSolutionRegion);
//        }
//        }
		// Commented by shadab
		// Adding trivial solution for all view which didn't appear in viewnameToCCMap
		// i.e., which had no constraints
//        for (String viewname : PostgresVConfig.VIEWNAMES_TOPO) {
//            if (!viewSolutions.keySet().contains(viewname)) {
//                ViewInfo viewInfo = PostgresVConfig.ANONYMIZED_VIEWINFOs.get(viewname);
//                ViewSolution viewSolution = solver.getTrivialSolution(viewInfo);
//                viewSolutions.put(viewname, viewSolution);
//                
//            }
//        }

//        return viewSolutions;
		return null;
	}

	private static void debugTotalErrorPerCondition(StopWatch databaseSummarySW, DatabaseSummary databaseSummary,
			CCInfo ccInfo) {
		if (!DebugHelper.totalErrorCheckNeeded())
			return;

		databaseSummarySW.pause();
		DebugHelper.printDebug("Evaluating total errors per condition");
		List<String> sortedViewnames = new ArrayList<>(databaseSummary.getUncompressedSummaryByView().keySet());
		Collections.sort(sortedViewnames);
		for (String viewname : sortedViewnames) {
			ViewInfo viewInfo = PostgresVConfig.ANONYMIZED_VIEWINFOs.get(viewname);
			List<String> sortedColumns = new ArrayList<>(viewInfo.getViewNonkeys());
			Collections.sort(sortedColumns);

			if (ccInfo.getViewnameToCCMap().containsKey(viewname)) {
				DebugHelper.printDebug("Evaluating total errors per condition for view " + viewname);
				ConditionsEvaluator.debugErrorPerCondition(ccInfo.getViewnameToCCMap().get(viewname),
						databaseSummary.getUncompressedSummaryByView().get(viewname), sortedColumns);
			}
		}
		databaseSummarySW.resume();
	}

}
