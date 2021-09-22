package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.Iterator;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntList;

public class ViewSolutionWithSolverStats implements ViewSolution {
    //Shadab-All made public for easy access as a quick fix
	public final ViewSolution    viewSolution;
    public final ViewSolutionRegion viewSolutionRegion;//Added by Shadab to keep regionwise solution
    public final SolverViewStats solverStats;

    public ViewSolutionWithSolverStats(ViewSolution viewSolution, SolverViewStats solverStats) {
        this.viewSolution = viewSolution;
        this.viewSolutionRegion=null;
        this.solverStats = solverStats;
    }
    public ViewSolutionWithSolverStats(ViewSolution viewSolution,ViewSolutionRegion viewSolutionRegion, SolverViewStats solverStats) {
        this.viewSolution = viewSolution;
        this.viewSolutionRegion=viewSolutionRegion;
        this.solverStats = solverStats;
    }


    public SolverViewStats getSolverStats() {
        return solverStats;
    }

    @Override
    public void addValueCombination(ValueCombination valueCombination) {
        viewSolution.addValueCombination(valueCombination);
    }

    @Override
    public int getCountOfValueCombinations() {
        return viewSolution.getCountOfValueCombinations();
    }

    @Override
    public List<ValueCombination> getValueCombinations() {
        return viewSolution.getValueCombinations();
    }

    @Override
    public void close() {
        viewSolution.close();
    }

    @Override
    public Iterator<ValueCombination> iterator() {
        return viewSolution.iterator();
    }

    @Override
    public void prepareForSearch() {
        viewSolution.prepareForSearch();
    }

    @Override
    public boolean contains(IntList valuesInCombination) {
        return viewSolution.contains(valuesInCombination);
    }

    @Override
    public long getPK(IntList valuesInCombination) {
        return viewSolution.getPK(valuesInCombination);
    }

    @Override
    public ViewSolution clone() {
        return viewSolution.clone();
    }
}
