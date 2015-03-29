package us.thirdmillenium.desktoptrainer.geneticalgorithm;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import us.thirdmillenium.desktoptrainer.Params;


public class FitnessScorer implements Params {
	private ArrayList<Genome> GenomePopulation;
	
	
	public FitnessScorer(ArrayList<Genome> genomes) {
		this.GenomePopulation = genomes;
	}
	
	/**
	 * A threaded method which will compute scores for all Genomes in the population.
	 */
	public void runFitnessScorer(Random random, double threshold) {
        // Randomly choose which maps to use for this generation
        Set<Integer> useMaps = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>(20));
        String mapstr = " maps [ ";

        while( useMaps.size() < 1 ) {
            for (int i = 1; i <= 5; i++) {
                if (random.nextDouble() < threshold) {
                    useMaps.add(new Integer(i));
                    mapstr = mapstr + i + " ";
                }
            }
        }

        System.out.print(useMaps.size() + mapstr + "] ... ");

		// Create array of objects to run
		ArrayList<FitnessWorkerThread> workers = new ArrayList<FitnessWorkerThread>();
		
		for( int i = 0; i < this.GenomePopulation.size(); i++ ) {
			workers.add(new FitnessWorkerThread(i, this.GenomePopulation.get(i), useMaps));
		}
		
		
		// Create ThreadPool to compute each genome in parallel
		ExecutorService executor = Executors.newFixedThreadPool(Params.NumThreads);
		
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
