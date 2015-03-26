package us.thirdmillenium.desktoptrainer.geneticalgorithm;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import us.thirdmillenium.desktoptrainer.TrainingParams;


public class FitnessScorer implements TrainingParams {
	private ArrayList<Genome> GenomePopulation;
	
	
	public FitnessScorer(ArrayList<Genome> genomes) {
		this.GenomePopulation = genomes;
	}
	
	/**
	 * A threaded method which will compute scores for all Genomes in the population.
	 */
	public void runFitnessScorer() {
		// Create array of objects to run
		ArrayList<FitnessWorkerThread> workers = new ArrayList<FitnessWorkerThread>();
		
		for( int i = 0; i < this.GenomePopulation.size(); i++ ) {
			workers.add(new FitnessWorkerThread(i, this.GenomePopulation.get(i)));
		}
		
		
		// Create ThreadPool to compute each genome in parallel
		ExecutorService executor = Executors.newFixedThreadPool(TrainingParams.NumThreads);
		
		for(int i = 0; i < workers.size(); i++) {
			executor.execute(workers.get(i));
		}
		
		executor.shutdown();
		while(!executor.isTerminated()) { }
		
		
		// Collect Scores
		for( int i = 0; i < workers.size(); i++ ) {
			this.GenomePopulation.get(i).setScore(workers.get(i).getScore());
		}
		
		System.gc();
	}
}
