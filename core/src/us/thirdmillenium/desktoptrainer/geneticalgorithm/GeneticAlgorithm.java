package us.thirdmillenium.desktoptrainer.geneticalgorithm;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.neuroph.core.NeuralNetwork;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import us.thirdmillenium.desktoptrainer.TrainingParams;
import us.thirdmillenium.desktoptrainer.neuralnetwork.ModifyNeuralNetwork;


public class GeneticAlgorithm extends ApplicationAdapter {
	private Random random;
	private File BaseNN;
	
	
	@Override
	public void create()
	{
		// Turn of rendering
		Gdx.graphics.setContinuousRendering(false);
		
		// Setup Output Folders
		buildOutputFolders();
		
		// Setup Test Environment
		//this.BaseEnviro = new NoGraphicsEnvironment("MyCrappyMap.tmx", 32, null);		
		this.random = new Random();
		
		// Setup BaseNN
		this.BaseNN = new File(TrainingParams.PathToBaseNN);
		
		if( !this.BaseNN.exists() ) {
			System.err.println("Could not find file at: " + TrainingParams.PathToBaseNN);
			return;
		}
		
		// Compute!
		runGA();
		
		// Render Once
		//Gdx.graphics.requestRendering();
	}
	
	@SuppressWarnings("unchecked")
	private void runGA() {
		// Initial Genome Population (with error check)
		//ArrayList<Genome> GenomePopulation = generateInitialGenomePopulation();
		ArrayList<Genome> GenomePopulation = generateInitialGenomePopulation2(this.BaseNN);
		ArrayList<Long[]> ScoreList = new ArrayList<Long[]>(TrainingParams.NumGenerations + 2); 
		if( GenomePopulation == null ) { return; }
		
		// Calculate GA
		for( int i = 1; i <= TrainingParams.NumGenerations; i++ ) {
			System.out.print("Beginning Generation " + i + " ... ");
			
			// Step 1 : Calculate Fitness for each Genome *** MAKE PARALLEL!!! ***
			FitnessScorer fitScore = new FitnessScorer(GenomePopulation);
			fitScore.runFitnessScorer();
			
			// Step 2 : Sort Genomes so Elite on top
			Collections.sort(GenomePopulation);
			
			// Step 3 : Save NNs, ranked by score
			writeGenomesToFile(GenomePopulation, ScoreList, i);
			
			// Step 4 : Crossover & Mutation
			if( i < TrainingParams.NumGenerations ) {
				GenomePopulation = crossoverAndMutate(GenomePopulation);
			}
			
			
			System.out.println("Done   " + (ScoreList.get(ScoreList.size()-1))[0]);
		}
		
		System.out.println("GA Complete");
	}
	
	@Override
	public void render() { 
		
	}
	
	
	
	/**
	 * Builds Output Folders
	 */
	private void buildOutputFolders() {
		File file = new File(TrainingParams.runPath);
		
		if(!file.exists()) {
			if( file.mkdir()) {
				System.out.println("Root Directory created.");
			} else {
				System.out.println("Failed creating Root Directory.");
			}
			
			// Build SubFolders			
			for( int i = 1; i <= TrainingParams.NumGenerations; i++) {
				if( i < 10) {
					file = new File(TrainingParams.runPath + "\\Gen_00" + i);
				} else if( i < 100){
					file = new File(TrainingParams.runPath + "\\Gen_0" + i);
				} else {
					file = new File(TrainingParams.runPath + "\\Gen_" + i);
				}

				if(!file.exists()) {
					if( file.mkdir()) {
						System.out.println("Sub Directory created.");
					} else {
						System.out.println("Failed creating Sub Directory.");
					}
				}
				
			}
		}
	}
	
	
	private void writeGenomesToFile(ArrayList<Genome> genomes, ArrayList<Long[]> scores, int generation) {
		Long[] newScores = new Long[genomes.size()];
		String basePath = TrainingParams.runPath + "\\Gen_";
		
		// Gen_xxx folder for this NN
		String genPath;
		if( generation < 10 ) { genPath = basePath + "00" + generation + "\\"; }
		else if(generation < 100) { genPath = basePath + "0" + generation + "\\"; }
		else { genPath = basePath + generation + "\\"; }
		
		// Write out NNET files
		for(int i=0; i < genomes.size(); i++) {
			// Collect Score Array
			newScores[i] = genomes.get(i).getScore();

			// Generate FileName for this NN
			String nnetFileName;
			if( (i+1) < 10 ) { 
				nnetFileName = genPath + "nnet_00" + (i+1) + ".nnet"; 
			} else if((i+1) < 100) { 
				nnetFileName = genPath + "nnet_0"  + (i+1) + ".nnet"; 
			} else { 
				nnetFileName = genPath + "nnet_"   + (i+1) + ".nnet"; 
			}
			
			// Save Elite NNs
			if( i < Math.min(10, TrainingParams.NumEliteGenomes) ) {
				genomes.get(i).getNN().save(nnetFileName);
			}
		}
		
		scores.add(newScores);
		
		/*
		 *  If this is the last generation, output a CSV containing the score data.
		 */
		
		if( generation == TrainingParams.NumGenerations ) {
			try {
				String csvFileName = TrainingParams.runPath + "\\scores.csv";
				File csvFile = new File(csvFileName);
				
				// Delete if currently exists, then create fresh file
				if( csvFile.exists() ) { 
					csvFile.delete(); 
				} 
				
				csvFile.createNewFile();			
				
				// Write out to file
				PrintWriter csvWriter = new PrintWriter(csvFile);
			
				for(int i = 0; i < scores.size(); i++ ) {
					Long[] temp = scores.get(i);
					
					for(int j = 0; j < temp.length; j++ ) {
						csvWriter.print( temp[j] );
						csvWriter.print( "," );
					}
					
					csvWriter.println("");
				}
				
				csvWriter.close();
				
			} catch(IOException ex) {
				System.out.println("Error in writing scores to CSV file.");
			}
		}
	}
	
	
	/**
	 * Generates random weights for each Genome in the initial Population.
	 * 
	 * @return ArrayList of created Genomes.
	 */
	private ArrayList<Genome> generateInitialGenomePopulation() {
		ArrayList<Genome> GenomePopulation = new ArrayList<Genome>(TrainingParams.NumGenomes + 2);
		
		// Generate Random Weighted Neural Networks for First Generation
		for( int i = 1; i <= TrainingParams.NumGenomes; i++ ) {
			Genome temp = new Genome(ModifyNeuralNetwork.randomizeWeights(this.BaseNN, this.random));
			GenomePopulation.add(temp);
		}
		
		return GenomePopulation;
	}
	
	
	private ArrayList<Genome> generateInitialGenomePopulation2(File startNNET) {
		ArrayList<Genome> GenomePopulation = new ArrayList<Genome>(TrainingParams.NumGenomes + 2);
		
		for( int i = 0; i < TrainingParams.NumGenomes - 1; i++ ) {
			GenomePopulation.add(mutateOnlyHelper2(new Genome(NeuralNetwork.createFromFile(startNNET))));
		}
		
		GenomePopulation.add(new Genome(NeuralNetwork.createFromFile(startNNET)));		
		
		return GenomePopulation;
	}
	
	
	/**
	 * This method mutates children weights.
	 * 
	 * @param children
	 */
	private void mutateChildrenHelper(ArrayList<Double> children) {
		if( !TrainingParams.PerformMutations ) {
			return;
		}
		
		int numMutations = (int)(TrainingParams.PercentMutations * children.size());
		
		for(int i=0; i < numMutations; i++) {
			// Get index to flip
			int index = ((int)(this.random.nextDouble() * (children.size()-1)));
			
			// Get range of change
			Double weight = children.get(index);
			double high = weight * (1 + TrainingParams.MaxPercentMutationChange);
			double low  = weight * (1 - TrainingParams.MaxPercentMutationChange);
			
			// Check ranges (Keep between 0 and 1)
			if( high >= 1 ) { high = 0.999999999999; }
			if( low  <= 0 ) { low  = 0.000000000001; }
			
			// Mutate
			//Double mutWeight = new Double((this.random.nextDouble() * (high - low)) + low);
			Double mutWeight = new Double( Math.abs(1 - ((this.random.nextDouble() * (high - low)) + low)) );
			children.set(index, mutWeight);
		}
	}
	
	
	/**
	 * A helper method to convert from Double to double
	 * 
	 * @param list
	 * @return
	 */
	private double[] doubleConverter(ArrayList<Double> list) {
		double[] temp = new double[list.size()];
		
		for(int i = 0; i < list.size(); i++ ){
			temp[i] = list.get(i).doubleValue();
		}
		
		return temp;
	}
	
	
	/**
	 * This method takes two elite parents, mutates both, and returns 2 children.
	 * 
	 * @param alpha
	 * @param beta
	 * @return
	 */
	private ArrayList<Genome> mutateOnlyHelper(Genome alpha, Genome beta) {
		ArrayList<Genome> children = new ArrayList<Genome>();
		
		// Collect parent weights to go to child
		ArrayList<Double> childWeights1 = new ArrayList<Double>(Arrays.asList(alpha.getNN().getWeights()));
		ArrayList<Double> childWeights2  = new ArrayList<Double>(Arrays.asList(beta.getNN().getWeights()));
		
		// Mutate Children
		mutateChildrenHelper(childWeights1);
		mutateChildrenHelper(childWeights2);
		
		// Store in new Genomes
		Genome child1 = new Genome(ModifyNeuralNetwork.randomizeWeights(this.BaseNN, this.random));
		child1.getNN().setWeights(doubleConverter(childWeights1));
		children.add(child1);
		
		Genome child2 = new Genome(ModifyNeuralNetwork.randomizeWeights(this.BaseNN, this.random));
		child2.getNN().setWeights(doubleConverter(childWeights2));
		children.add(child2);
		
		return children;
	}
	
	private Genome mutateOnlyHelper2(Genome alpha) {
		ArrayList<Genome> children = new ArrayList<Genome>();
		
		// Collect parent weights to go to child
		ArrayList<Double> childWeights1 = new ArrayList<Double>(Arrays.asList(alpha.getNN().getWeights()));
		
		// Mutate Children
		mutateChildrenHelper(childWeights1);
		
		// Store in new Genomes
		Genome child1 = new Genome(ModifyNeuralNetwork.randomizeWeights(this.BaseNN, this.random));
		child1.getNN().setWeights(doubleConverter(childWeights1));
		
		return child1;
	}
	
	
	/**
	 * This method takes two elite 'parents' and produces a set of children through crossover and mutation. <br><br>
	 * 
	 * <b>Crossover Algorithm</b> <br>
	 * Assume 2 crossover points, which splits weights into 3 segments <br>
	 * alphaWeights - ABC, betaWeights - XYZ <br> 
	 * Then generate children by "injecting" weights from other parent into it. <br>
	 * In this case, you would end up with: <br>
	 * XBC, AYC, ABZ, AYZ, XBZ, XYC <br> <br>
	 * This generates (2 * NumCrossovers) children.
	 * 
	 * @param alpha
	 * @param beta
	 * @return
	 */
	private ArrayList<Genome> crossoverAndMutateHelper(Genome alpha, Genome beta) {
		ArrayList<Genome> children = new ArrayList<Genome>((TrainingParams.NumCrossovers * 2) + 2);
		
		// Collect Parent Weights
		ArrayList<Double> alphaWeights = new ArrayList<Double>(Arrays.asList(alpha.getNN().getWeights()));
		ArrayList<Double> betaWeights  = new ArrayList<Double>(Arrays.asList(beta.getNN().getWeights()));
		
		/* 
		 * Collect Random Pivot points for Crossover, 
		 * making sure each segment will have at least 1 weight in it
		 */
		ArrayList<Integer> pivotPoints = new ArrayList<Integer>();
		pivotPoints.add(0);
		pivotPoints.add(alphaWeights.size());
		
		for(int i = 0; i < TrainingParams.NumCrossovers; i++) {
			int pivot = (int)(alphaWeights.size() * this.random.nextDouble());
			
			if( pivot == 0 || 
				pivot == alphaWeights.size() ||
				pivotPoints.contains(new Integer(pivot)) ||
				pivotPoints.contains(new Integer(pivot-1)) ||
				pivotPoints.contains(new Integer(pivot+1)) ) {
				i--;
			} else {
				pivotPoints.add(pivot);
			}
		}
		
		Collections.sort(pivotPoints);
		
		
		/*
		 * Perform crossover and mutation.  Each loop makes two children.
		 */
		for(int i=0; i < pivotPoints.size(); i++ ) {
			// Splice beta into alpha, and vice versa
			ArrayList<Double> childWeights1 = new ArrayList<Double>();
			ArrayList<Double> childWeights2 = new ArrayList<Double>();
			
			for(int j=0; j < pivotPoints.size() - 1; j++) {
				if( i == j ) {
					childWeights1.addAll( betaWeights.subList(  pivotPoints.get(j), pivotPoints.get(j+1)) );
					childWeights2.addAll( alphaWeights.subList( pivotPoints.get(j), pivotPoints.get(j+1)) );
				} else {
					childWeights1.addAll( alphaWeights.subList( pivotPoints.get(j), pivotPoints.get(j+1)) );
					childWeights2.addAll( betaWeights.subList(  pivotPoints.get(j), pivotPoints.get(j+1)) );
				}
			}
			
			// Mutate Children
			mutateChildrenHelper(childWeights1);
			mutateChildrenHelper(childWeights2);
			
			// Store in new Genomes
			Genome child1 = new Genome(ModifyNeuralNetwork.randomizeWeights(this.BaseNN, this.random));
			child1.getNN().setWeights(doubleConverter(childWeights1));
			children.add(child1);
			
			Genome child2 = new Genome(ModifyNeuralNetwork.randomizeWeights(this.BaseNN, this.random));
			child2.getNN().setWeights(doubleConverter(childWeights2));
			children.add(child2);
		}
		
		return children;
	}
	
	/**
	 * This method controls the Crossover and Mutation, with helper methods.
	 * 
	 * @param currentGenomes
	 * @return
	 */
	private ArrayList<Genome> crossoverAndMutate(ArrayList<Genome> currentGenomes) {
		ArrayList<Genome> newGenomes = new ArrayList<Genome>(TrainingParams.NumGenomes + 2);
		
		// Copy in the elites from current generation
		for( int i = 0; i < TrainingParams.NumEliteGenomes; i ++ ) {
			newGenomes.add(currentGenomes.get(i));
		}
		
		
		// Generate enough new Genomes for next Generation
		for( int i = TrainingParams.NumEliteGenomes; i < TrainingParams.NumGenomes; /* No i++! */) {
			// Randomly pick two parents, then crossover/mutate them for children
			int index1 = (int)(TrainingParams.NumEliteGenomes * this.random.nextDouble());
			int index2 = (int)(TrainingParams.NumEliteGenomes * this.random.nextDouble());
			
			if( index1 == index2 ) {
				if( index1 > 0 ) {
					index2 --;  // Crossover with slightly better Genome
				} else {
					index2 ++;
				}
			}
			
			Genome parent1 = currentGenomes.get(index1);
			Genome parent2 = currentGenomes.get(index2);
			
			ArrayList<Genome> children = (TrainingParams.PerformCrossovers) ?  
						crossoverAndMutateHelper(parent1, parent2) : mutateOnlyHelper(parent1, parent2);
			
			// Add children to newGenomes
			for(int j = 0; j < children.size(); j++ ) {
				if( i < TrainingParams.NumGenomes ) {
					newGenomes.add(children.get(j));
					i++;
				}
			}
		}
		
		// Clear scores
		for(int i = 0; i < newGenomes.size(); i++ ) {
			newGenomes.get(i).setScore(0);			
		}
		
		currentGenomes = null;
		
		System.gc();
		
		return newGenomes;
	}

}
