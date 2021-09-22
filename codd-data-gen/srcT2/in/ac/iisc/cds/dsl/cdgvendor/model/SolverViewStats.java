package in.ac.iisc.cds.dsl.cdgvendor.model;

public class SolverViewStats {
    public long relRowCount;
    public int  solverConstraintCount;
    public int  solverVarCount;
    public long millisToSolve;

    public SolverViewStats() {
        relRowCount = 0;
        solverConstraintCount = 0;
        solverVarCount = 0;
        millisToSolve = 0;
    }

}