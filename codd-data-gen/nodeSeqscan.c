/*-------------------------------------------------------------------------
 *
 * nodeSeqscan.c
 *	  Support routines for sequential scans of relations.
 *
 * Portions Copyright (c) 1996-2013, PostgreSQL Global Development Group
 * Portions Copyright (c) 1994, Regents of the University of California
 *
 *
 * IDENTIFICATION
 *	  src/backend/executor/nodeSeqscan.c
 *
 *-------------------------------------------------------------------------
 */
/*
 * INTERFACE ROUTINES
 *		ExecSeqScan				sequentially scans a relation.
 *		ExecSeqNext				retrieve next tuple in sequential order.
 *		ExecInitSeqScan			creates and initializes a seqscan node.
 *		ExecEndSeqScan			releases any storage allocated.
 *		ExecReScanSeqScan		rescans the relation
 *		ExecSeqMarkPos			marks scan position
 *		ExecSeqRestrPos			restores scan position
 */
#include "postgres.h"

#include "access/relscan.h"
#include "executor/execdebug.h"
#include "executor/nodeSeqscan.h"
#include "utils/rel.h"
#include "utils/lsyscache.h"
#include "string.h"
#include "funcapi.h"

static void InitScanRelation(SeqScanState *node, EState *estate, int eflags);
static TupleTableSlot *SeqNext(SeqScanState *node);

/*Added for CODD begin*/
extern char* get_rel_name(Oid relid);


struct RelationSummary {
	char relname[500];
	int relsize;
	int *noOfCopies;
	char ***valuecombination;
	int nc;
	int nr;
	int posPK;
	long velocity;
	AttInMetadata *attrInMeta;
};

struct DatabaseSummary {
	int relcount;
	struct RelationSummary relsummary[1000];
};

struct VelocityControl {
	time_t controlTime;
	long count;
};

static struct VelocityControl velocityControl = {.count = 0};

static struct DatabaseSummary dbsummary = {.relcount = 0};

static char* toUpper(char* origStr) {
	int len = strlen(origStr);
	int i;
	char* newStr = (char *)palloc(len + 1);
	for (i=0; i<len; ++i) {
		newStr[i] = toupper(origStr[i]);
	}
	newStr[i] = '\0';
	return newStr;
}

static struct RelationSummary *get_relation_summary(char *relname, SeqScanState *node) {
	int i, loopIndex, vcIndex, posPK;
	FILE *fp;
	struct RelationSummary *relsptr;
	char *filename;
	
	for(i=0; i<dbsummary.relcount; i++){
		if(strcmp(dbsummary.relsummary[i].relname, relname) == 0) {
			return &dbsummary.relsummary[i];
		}
	}
	
	printf("Reading relation summary %s\n", relname);
	
	filename = (char *)palloc(500*sizeof(char));
	strncpy(filename, "/home/dsladmin/Documents/dataSummary/", 499); //Add folder name where the relation summaries
	char* relnameUpper = toUpper(relname);
	strncat(filename, relnameUpper, 499 - strlen(filename));
	printf("Reading relation summary %s\n", filename);
	fp = fopen(filename, "r");
	pfree(filename);
	pfree(relnameUpper);
	
	relsptr = (struct RelationSummary *)palloc(sizeof(struct RelationSummary));
	
	strcpy(relsptr->relname, relname);
	
	fscanf(fp, "%d,", &relsptr->nr);
	fscanf(fp, "%d,", &relsptr->nc);
	fscanf(fp, "%d,", &relsptr->relsize);
	fscanf(fp, "%d,", &relsptr->posPK);
	fscanf(fp, "%ld\n", &relsptr->velocity);
	printf("FIRST_ROW: %d %d %d %d %ld\n",relsptr->nr,relsptr->nc, relsptr->relsize, relsptr->posPK, relsptr->velocity);

	relsptr->valuecombination = (char***)palloc(relsptr->nr * sizeof(char**));
	relsptr->noOfCopies = (int*)palloc(relsptr->nr * sizeof(int));

	char *line = NULL;
	char *part;
	char *temp = NULL;
	size_t len = 0;
	ssize_t read;

	for(i=0; i<relsptr->nr; ++i) {
		relsptr->valuecombination[i] = (char**)palloc((relsptr->nc - 1) * sizeof(char*));

		read = getline(&line, &len, fp);
//		printf("Retrieved line of length %zu :\n", read);
		line[strlen(line)-1] = '\0';

		part = strtok(line,",");

		posPK = relsptr->posPK;
		vcIndex = 0;
		if(posPK == 0) {
			relsptr->noOfCopies[i] = atoi(part);
		} else {
			if(part [0] == '"') {
				temp = (char*)palloc(strlen(part) + 1);
				strcpy(temp, part);
				while(temp[strlen(temp)-1] != '"'  ||  (temp[strlen(temp)-1] == '"' && temp[strlen(temp)-2] == '\\')) {
					part = strtok (NULL, ",");
					temp = repalloc(temp, strlen(temp) + strlen(part) + 2);	// 1 for , and 1 for \0
					strcat(temp, ",");
					strcat(temp, part);
				}
				memmove(temp, temp+1, strlen(temp));
				temp[strlen(temp)-1] = '\0';
				part = temp;
			}
			relsptr->valuecombination[i][vcIndex] = (char*)palloc(strlen(part) + 1);
			strcpy(relsptr->valuecombination[i][vcIndex], part);
			if(temp != NULL) {
				pfree(temp);
				temp = NULL;
			}
			++vcIndex;
		}
		loopIndex = 1;		// 1st value already read above

		for(; loopIndex < relsptr->nc; ++loopIndex) {
			part = strtok (NULL, ",");
			if(posPK == loopIndex) {
				relsptr->noOfCopies[i] = atoi(part);
				continue;
			}
			if(part [0] == '"') {
				temp = (char*)palloc(strlen(part) + 1);
				strcpy(temp, part);
				while(temp[strlen(temp)-1] != '"'  ||  (temp[strlen(temp)-1] == '"' && temp[strlen(temp)-2] == '\\')) {
					part = strtok (NULL, ",");
					temp = repalloc(temp, strlen(temp) + strlen(part) + 2);	// 1 for , and 1 for \0
					strcat(temp, ",");
					strcat(temp, part);
				}
				memmove(temp, temp+1, strlen(temp));
				temp[strlen(temp)-1] = '\0';
				part = temp;
			}
			relsptr->valuecombination[i][vcIndex] = (char*)palloc(strlen(part) + 1);
			strcpy(relsptr->valuecombination[i][vcIndex], part);
			if(temp != NULL) {
				pfree(temp);
				temp = NULL;
			}
			++vcIndex;
		}
	}
	free(line);



//	relsptr->valuecombination = (int **)palloc(relsptr->nr*sizeof(int *));
//	for(i=0; i<relsptr->nr; i++) {
//		relsptr->valuecombination[i] = (int *)palloc(relsptr->nc*sizeof(int));
//		for(j=0; j<relsptr->nc-1; j++) {
//			fscanf(fp, "%d,", &relsptr->valuecombination[i][j]);
//		}
//		fscanf(fp, "%d\n", &relsptr->valuecombination[i][j]);
//	}
	fclose(fp);
	
	relsptr->attrInMeta = TupleDescGetAttInMetadata(((RelationData *)node->ss_currentRelation)->rd_att);

	dbsummary.relsummary[dbsummary.relcount] = *relsptr;
	pfree(relsptr);
	
	return &dbsummary.relsummary[dbsummary.relcount++];
}

static char **gettuple(int tuplenum, struct RelationSummary *relsptr) {
	if(relsptr->velocity > 0) {
		time_t curTime = time(NULL);
		if (time(NULL) == velocityControl.controlTime) {
			if (relsptr->velocity == velocityControl.count) {
				while(time(NULL) == velocityControl.controlTime);
				velocityControl.controlTime = time(NULL);
				velocityControl.count = 1;
			} else {
				velocityControl.count++;
			}
		} else {
			velocityControl.controlTime = curTime;
			velocityControl.count = 1;
		}
	}

	int cumsum = 0;
	int i;
	for(i=0; i<relsptr->nr; i++) {
		cumsum += relsptr->noOfCopies[i];
		if(cumsum>tuplenum)
			return relsptr->valuecombination[i];
	}
	return NULL;
}

/*Added for CODD end*/

/* ----------------------------------------------------------------
 *						Scan Support
 * ----------------------------------------------------------------
 */

/* ----------------------------------------------------------------
 *		SeqNext
 *
 *		This is a workhorse for ExecSeqScan
 * ----------------------------------------------------------------
 */
static TupleTableSlot *
SeqNext(SeqScanState *node)
{
	HeapTuple	tuple;
	HeapScanDesc scandesc;
	EState	   *estate;
	ScanDirection direction;
	TupleTableSlot *slot;

	/*Added for CODD begin*/
//	Datum gen_values[node->ss_currentRelation->rd_att->natts];
	char *gen_vals[node->ss_currentRelation->rd_att->natts];
//	bool isNull[node->ss_currentRelation->rd_att->natts];
//	int *raw_tuple;
	char **raw_tuple;
	int i;
	char *relname;
	struct RelationSummary *relsptr;
	bool shouldFree;
	/*Added for CODD end*/

	/*
	 * get information from the estate and scan state
	 */
	scandesc = node->ss_currentScanDesc;
	estate = node->ps.state;
	direction = estate->es_direction;
	slot = node->ss_ScanTupleSlot;
	
	/*Added for CODD begin*/
	shouldFree = false;
#ifdef CODD_ENABLED
	if (RelationGetSpecialExec(node->ss_currentRelation) != 0) {
	
		relname = RelationGetRelationName(node->ss_currentRelation);
//		relname = get_rel_name(node->ss_currentRelation->rd_node.relNode);
//		SPI_getrelname(node->ss_currentRelation->rd_node.relNode);
		if(relname == NULL)
			printf("NULL relation name\n");
		struct RelationSummary *relsptr = get_relation_summary(relname, node);
//		pfree(relname);	TODO

		if(node->tuple_count >= relsptr->relsize) {
			return NULL;
		}
		
//		gen_values[0] = Int32GetDatum(node->tuple_count);
//		isNull[0] = false;

		int posPK = relsptr->posPK;

//		gen_vals[0] = (char*)palloc(11 * sizeof(char));
		gen_vals[posPK] = (char*)palloc(11 * sizeof(char));
//		sprintf(gen_vals[0], "%d", node->tuple_count);
		sprintf(gen_vals[posPK], "%d", node->tuple_count);

		raw_tuple = gettuple(node->tuple_count, relsptr);
		int index_raw_tuple;
		int index_gen_vals;

		for(index_raw_tuple = 0, index_gen_vals = 0; index_gen_vals < relsptr->nc; ++index_raw_tuple, ++index_gen_vals) {
			if(index_gen_vals == posPK) {
				++index_gen_vals;
				if(!(index_gen_vals < relsptr->nc))
					break;
			}
			gen_vals[index_gen_vals] = (char*)palloc(strlen(raw_tuple[index_raw_tuple]) + 1);
			strcpy(gen_vals[index_gen_vals], raw_tuple[index_raw_tuple]);
		}

		/*for(i=1; i < relsptr->nc; i++) {
			gen_vals[i] = (char*)palloc(strlen(raw_tuple[i-1]) + 1);
			strcpy(gen_vals[i], raw_tuple[i-1]);

//			gen_values[i] = Int32GetDatum(raw_tuple[i]);
//			isNull[i] = false;
		}*/

//		char *gen_vals[] = { "1", "hello", "a", "Fri Feb 01 22:56:33 IST 1991"};

//		tuple = BuildTupleFromCStrings(TupleDescGetAttInMetadata(((RelationData *)node->ss_currentRelation)->rd_att), gen_vals);
		tuple = BuildTupleFromCStrings(relsptr->attrInMeta, gen_vals);

		for(i = 0; i < node->ss_currentRelation->rd_att->natts; ++i)
			pfree(gen_vals[i]);
//		tuple = heap_form_tuple(((RelationData *)node->ss_currentRelation)->rd_att, gen_values, isNull);
		shouldFree = true;
		node->tuple_count = node->tuple_count + 1;
	}
	else {
		tuple = heap_getnext(scandesc, direction);
	}
#else
	/*Added for CODD end*/

	/*
	 * get the next tuple from the table
	 */
	tuple = heap_getnext(scandesc, direction);

#endif //Added for CODD

	/*
	 * save the tuple and the buffer returned to us by the access methods in
	 * our scan tuple slot and return the slot.  Note: we pass 'false' because
	 * tuples returned by heap_getnext() are pointers onto disk pages and were
	 * not created with palloc() and so should not be pfree()'d.  Note also
	 * that ExecStoreTuple will increment the refcount of the buffer; the
	 * refcount will not be dropped until the tuple table slot is cleared.
	 */
	if (tuple)
		ExecStoreTuple(tuple,	/* tuple to store */
					   slot,	/* slot to store in */
					   scandesc->rs_cbuf,		/* buffer associated with this
												 * tuple */
					   shouldFree);	/* don't pfree this pointer */
	else
		ExecClearTuple(slot);

	return slot;
}

/*
 * SeqRecheck -- access method routine to recheck a tuple in EvalPlanQual
 */
static bool
SeqRecheck(SeqScanState *node, TupleTableSlot *slot)
{
	/*
	 * Note that unlike IndexScan, SeqScan never use keys in heap_beginscan
	 * (and this is very bad) - so, here we do not check are keys ok or not.
	 */
	return true;
}

/* ----------------------------------------------------------------
 *		ExecSeqScan(node)
 *
 *		Scans the relation sequentially and returns the next qualifying
 *		tuple.
 *		We call the ExecScan() routine and pass it the appropriate
 *		access method functions.
 * ----------------------------------------------------------------
 */
TupleTableSlot *
ExecSeqScan(SeqScanState *node)
{
	return ExecScan((ScanState *) node,
					(ExecScanAccessMtd) SeqNext,
					(ExecScanRecheckMtd) SeqRecheck);
}

/* ----------------------------------------------------------------
 *		InitScanRelation
 *
 *		Set up to access the scan relation.
 * ----------------------------------------------------------------
 */
static void
InitScanRelation(SeqScanState *node, EState *estate, int eflags)
{
	Relation	currentRelation;
	HeapScanDesc currentScanDesc;

	/*
	 * get the relation object id from the relid'th entry in the range table,
	 * open that relation and acquire appropriate lock on it.
	 */
	currentRelation = ExecOpenScanRelation(estate,
									  ((SeqScan *) node->ps.plan)->scanrelid,
										   eflags);

	/* initialize a heapscan */
	currentScanDesc = heap_beginscan(currentRelation,
									 estate->es_snapshot,
									 0,
									 NULL);

	node->ss_currentRelation = currentRelation;
	node->ss_currentScanDesc = currentScanDesc;

	/* and report the scan tuple slot's rowtype */
	ExecAssignScanType(node, RelationGetDescr(currentRelation));
}


/* ----------------------------------------------------------------
 *		ExecInitSeqScan
 * ----------------------------------------------------------------
 */
SeqScanState *
ExecInitSeqScan(SeqScan *node, EState *estate, int eflags)
{
	SeqScanState *scanstate;

	/*
	 * Once upon a time it was possible to have an outerPlan of a SeqScan, but
	 * not any more.
	 */
	Assert(outerPlan(node) == NULL);
	Assert(innerPlan(node) == NULL);

	/*
	 * create state structure
	 */
	scanstate = makeNode(SeqScanState);
	scanstate->ps.plan = (Plan *) node;
	scanstate->ps.state = estate;

	/*
	 * Miscellaneous initialization
	 *
	 * create expression context for node
	 */
	ExecAssignExprContext(estate, &scanstate->ps);

	/*
	 * initialize child expressions
	 */
	scanstate->ps.targetlist = (List *)
		ExecInitExpr((Expr *) node->plan.targetlist,
					 (PlanState *) scanstate);
	scanstate->ps.qual = (List *)
		ExecInitExpr((Expr *) node->plan.qual,
					 (PlanState *) scanstate);

	/*
	 * tuple table initialization
	 */
	ExecInitResultTupleSlot(estate, &scanstate->ps);
	ExecInitScanTupleSlot(estate, scanstate);

	/*
	 * initialize scan relation
	 */
	InitScanRelation(scanstate, estate, eflags);

	scanstate->ps.ps_TupFromTlist = false;

	/*
	 * Initialize result tuple type and projection info.
	 */
	ExecAssignResultTypeFromTL(&scanstate->ps);
	ExecAssignScanProjectionInfo(scanstate);

	return scanstate;
}

/* ----------------------------------------------------------------
 *		ExecEndSeqScan
 *
 *		frees any storage allocated through C routines.
 * ----------------------------------------------------------------
 */
void
ExecEndSeqScan(SeqScanState *node)
{
	Relation	relation;
	HeapScanDesc scanDesc;

	/*
	 * get information from node
	 */
	relation = node->ss_currentRelation;
	scanDesc = node->ss_currentScanDesc;

	/*
	 * Free the exprcontext
	 */
	ExecFreeExprContext(&node->ps);

	/*
	 * clean out the tuple table
	 */
	ExecClearTuple(node->ps.ps_ResultTupleSlot);
	ExecClearTuple(node->ss_ScanTupleSlot);

	/*
	 * close heap scan
	 */
	heap_endscan(scanDesc);

	/*
	 * close the heap relation.
	 */
	ExecCloseScanRelation(relation);
}

/* ----------------------------------------------------------------
 *						Join Support
 * ----------------------------------------------------------------
 */

/* ----------------------------------------------------------------
 *		ExecReScanSeqScan
 *
 *		Rescans the relation.
 * ----------------------------------------------------------------
 */
void
ExecReScanSeqScan(SeqScanState *node)
{
	HeapScanDesc scan;

	scan = node->ss_currentScanDesc;

	heap_rescan(scan,			/* scan desc */
				NULL);			/* new scan keys */

	ExecScanReScan((ScanState *) node);
}

/* ----------------------------------------------------------------
 *		ExecSeqMarkPos(node)
 *
 *		Marks scan position.
 * ----------------------------------------------------------------
 */
void
ExecSeqMarkPos(SeqScanState *node)
{
	HeapScanDesc scan = node->ss_currentScanDesc;

	heap_markpos(scan);
}

/* ----------------------------------------------------------------
 *		ExecSeqRestrPos
 *
 *		Restores scan position.
 * ----------------------------------------------------------------
 */
void
ExecSeqRestrPos(SeqScanState *node)
{
	HeapScanDesc scan = node->ss_currentScanDesc;

	/*
	 * Clear any reference to the previously returned tuple.  This is needed
	 * because the slot is simply pointing at scan->rs_cbuf, which
	 * heap_restrpos will change; we'd have an internally inconsistent slot if
	 * we didn't do this.
	 */
	ExecClearTuple(node->ss_ScanTupleSlot);

	heap_restrpos(scan);
}
