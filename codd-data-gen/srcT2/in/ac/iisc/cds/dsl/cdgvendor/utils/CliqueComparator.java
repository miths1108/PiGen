package in.ac.iisc.cds.dsl.cdgvendor.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jgrapht.graph.SimpleDirectedGraph;

import in.ac.iisc.cds.dsl.cdgvendor.reducer.Bucket;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructure;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Partition;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class CliqueComparator {
	
	String viewname;
	public CliqueComparator(String viewname) {
		this.viewname = viewname; 
	}

	public void comparePartitions(List<Partition> cliqueIdxtoPList) {
		try {
        	List<Partition> cliqueIdxtoPListCopy = new ArrayList<>();
        	for (Partition partition : cliqueIdxtoPList) {
        		Partition newPartition = new Partition();
        		for (Region region : partition.getAll()) {
					newPartition.add(region.getDeepCopy());
				}
        		cliqueIdxtoPListCopy.add(newPartition);
			}
        	
			FileInputStream fs = new FileInputStream("cliqueIdxtoPList" + viewname);
			ObjectInputStream os = new ObjectInputStream(fs);
			@SuppressWarnings("unchecked")
			List<Partition> original = (List<Partition>)os.readObject();
			os.close();
			
			int noOfThreads = 12;
			
			for (int i = 0; i < original.size(); i++) {
				System.err.println("\nFor " + i);
				IntList toRemoveFromOld = new IntArrayList();
				Partition oldP = original.get(i);
				Partition newP = cliqueIdxtoPListCopy.get(i);
				
//				for every region in newP there should be a region in oldP
//				for every point in oldRegion there should be a point in newRegion
				
				if(oldP.getAll().size() != newP.getAll().size())
					throw new RuntimeException("Number of regions not equal");
				
				HashMap<Integer, SimpleDirectedGraph<Node, MyCustomEdge>> newRToGraph = new HashMap<>();
				
				ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);
				
				HashSet<Integer> toRemoveFromNew = new HashSet<>();
				Object lockMap = new Object();
				Object lockList = new Object();
				
				for(int j = 0; j < oldP.getAll().size(); ++j) {
					Region oldR = oldP.getAll().get(j);
					Runner task = new Runner(newRToGraph, i, j, oldP.getAll().size(), oldR, newP, toRemoveFromOld, toRemoveFromNew, lockMap, lockList);
					executor.submit(task);
				}
				executor.shutdown();
				try {
					executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
				} catch (InterruptedException e) {
					throw new RuntimeException(e.toString());
				}
				if(toRemoveFromNew.size() != oldP.size()) {
					System.err.println("old");
					for(int g = 0; g < oldP.size(); g++)
						if(!toRemoveFromOld.contains(g))
							System.err.println(oldP.getAll().get(g));
				    System.err.println("new");
				    for(int g = 0; g < newP.size(); g++)
						if(!toRemoveFromNew.contains(g))
							System.err.println(newP.getAll().get(g));
					throw new RuntimeException("not equal");
				}
			}
			os.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("failed");
		}
	}
}

class Runner implements Runnable {

	int partition;
	int region;
	int size;
	HashMap<Integer, SimpleDirectedGraph<Node, MyCustomEdge>> newRToGraph;
	Region oldR;
	Partition newP;
	IntList toRemoveFromOld;
	HashSet<Integer> toRemoveFromNew;
	Object lockMap;
	Object lockList;
	
	public Runner(HashMap<Integer, SimpleDirectedGraph<Node, MyCustomEdge>> newRToGraph2, int i, int j, int size, Region oldR, Partition newP, IntList toRemoveFromOld, HashSet<Integer> toRemoveFromNew, Object lockMap, Object lockList) {
		this.partition = i;
		this.region = j;
		this.size = size;
		this.newRToGraph = newRToGraph2;
		this.oldR = oldR;
		this.newP = newP;
		this.toRemoveFromOld = toRemoveFromOld;
		this.toRemoveFromNew = toRemoveFromNew;
		this.lockMap = lockMap;
		this.lockList = lockList;
	}
	
	@Override
	public void run() {
		BigInteger numberOfPoints = getNumberOfPoints(oldR);
		int sizeOfMap;
		synchronized(lockMap) {
			sizeOfMap = newRToGraph.size();
		}
		System.err.println("Partition " + partition + "  region "+region+" of " + size +" Total points : " + numberOfPoints + " ..Map size = " + sizeOfMap);
		
		SimpleDirectedGraph<Node, MyCustomEdge> oldGraph = new SimpleDirectedGraph<>(MyCustomEdge.class);
		oldGraph.addVertex(new Node(-1, -1));
		for (BucketStructure oldBS : oldR.getAll()) {
			addToGraph(oldBS, oldGraph);
		}
		
		for (int newRIndex = 0; newRIndex < newP.size(); newRIndex++) {
			synchronized (lockList) {
				if(toRemoveFromNew.contains(newRIndex))
					continue;
			}
			Region newR = newP.getAll().get(newRIndex).getDeepCopy();
			if(numberOfPoints.compareTo(getNumberOfPoints(newR)) != 0)
				continue;
			SimpleDirectedGraph<Node, MyCustomEdge> newGraph = null;
			synchronized (lockMap) {
				if(newRToGraph.containsKey(newRIndex))
					newGraph = newRToGraph.get(newRIndex);
			}
			if(newGraph == null) {
				newGraph = new SimpleDirectedGraph<>(MyCustomEdge.class);
				newGraph.addVertex(new Node(-1, -1));
				for (BucketStructure newBS : newR.getAll()) {
					addToGraph(newBS, newGraph);
				}
			}
			
			if(oldGraph.equals(newGraph)) {
				synchronized (lockList) {
//					System.err.println(region+":"+newRIndex);
					toRemoveFromNew.add(newRIndex);
					toRemoveFromOld.add(region);
				}
				synchronized (lockMap) {
					newRToGraph.remove(newRIndex);
				}
				break;
			}
			else {
				synchronized (lockList) {
					synchronized (lockMap) {
						if (newRToGraph.size() < 2000 && !newRToGraph.containsKey(newRIndex) && !toRemoveFromNew.contains(newRIndex)) {
							newRToGraph.put(newRIndex, newGraph);
						}
					}
				}
			}
				
		}
	}
	
	private void addToGraph(BucketStructure oldBS, SimpleDirectedGraph<Node, MyCustomEdge> newGraph) {
		List<Node> parents = new ArrayList<>();
		parents.add(new Node(-1, -1));
		List<Node> current = new ArrayList<>();
		for (int level = 0; level < oldBS.getAll().size(); ++level) {
			Bucket b = oldBS.getAll().get(level);
			for (Integer nodeVal : b.getAll()) {
				Node curNode = new Node(level, nodeVal);
				newGraph.addVertex(curNode);
				current.add(curNode);
				for (Node parent : parents) {
					newGraph.addEdge(parent, curNode, new MyCustomEdge(parent, curNode));
				}
			}
			parents = current;
			current = new ArrayList<>();
		}
	}
	
	private BigInteger getNumberOfPoints(Region region) {
		BigInteger numberOfPoints = new BigInteger("0");
		for (BucketStructure bs : region.getAll()) {
			numberOfPoints = numberOfPoints.add(getNumberOfPoints(bs));
		}
		return numberOfPoints;
	}
	
	private BigInteger getNumberOfPoints(BucketStructure bs) {
    	BigInteger numberOfPoints = new BigInteger("1");
		for (Bucket b: bs.getAll()) {
			numberOfPoints = numberOfPoints.multiply(new BigInteger(String.valueOf(b.size())));
		}
		return numberOfPoints;
	}
}

class Node {
	final int level;
	final int value;
	
	public Node(int l, int v) {
		level = l;
		value = v;
	}
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = result + prime * level;
        result = result + prime * value;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (level != other.level || value != other.value)
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "("+level+"."+value+")";
    }
}

class MyCounter {
	public int count = 0;
}
class MyCustomEdge {
	Node source;
	Node target;
	public MyCustomEdge(Node s, Node t) {
		source = s;
		target = t;
	}
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = prime * source.hashCode();
        result += prime * target.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MyCustomEdge other = (MyCustomEdge) obj;
        if (!source.equals(other.source) || !target.equals(other.target))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "("+source+":"+target+")";
    }
}