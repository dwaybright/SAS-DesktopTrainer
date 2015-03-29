package us.thirdmillenium.desktoptrainer.geneticalgorithm;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import org.neuroph.core.NeuralNetwork;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import us.thirdmillenium.desktoptrainer.Params;
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
		this.random = new Random();
		
		// Setup BaseNN
		this.BaseNN = new File(Params.PathToBaseNN);
		
		if( !this.BaseNN.exists() ) {
			System.err.println("Could not find file at: " + Params.PathToBaseNN);
			return;
		}
		
		// Compute!
		runGA();
		
		// Render Once
		//Gdx.graphics.requestRendering();
	}
	
	@SuppressWarnings("unchecked")
	private void runGA() {
		// Initial Genome Population
		//ArrayList<Genome> GenomePopulation = generateInitialGenomePopulation();
		ArrayList<Genome> GenomePopulation = generateInitialGenomePopulationFromNN(this.BaseNN);
		ArrayList<Long[]> ScoreList = new ArrayList<Long[]>(Params.NumGenerations + 2);
		if( GenomePopulation == null ) { return; }
		
		// Calculate GA
		for( int i = 1; i <= Params.NumGenerations; i++ ) {
			System.out.print("Beginning Generation " + i + " ... ");
			
			// Step 1 : Calculate Fitness for each Genome ***  PARALLEL!!! ***
			FitnessScorer fitScore = new FitnessScorer(GenomePopulation);
			fitScore.runFitnessScorer(random, Math.max(0.30, (i / (double) Params.NumGenerations)));
			
			// Step 2 : Sort Genomes so Elite on top
			Collections.sort(GenomePopulation);
			
			// Step 3 : Save NNs, ranked by score
			writeGenomesToFile(GenomePopulation, ScoreList, i);
			
			// Step 4 : Crossover & Mutation
			if( i < Params.NumGenerations ) {
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
		File file = new File(Params.GA_OutputPath);
		
		if(!file.exists()) {
			if( file.mkdir()) {
				System.out.println("Root Directory created.");
			} else {
				System.out.println("Failed creating Root Directory.");
			}
			
			// Build SubFolders			
			for( int i = 1; i <= Params.NumGenerations; i++) {
				if( i < 10) {
					file = new File(Params.GA_OutputPath + "Gen_00" + i);
				} else if( i < 100){
					file = new File(Params.GA_OutputPath + "Gen_0" + i);
				} else {
					file = new File(Params.GA_OutputPath + "Gen_" + i);
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
		String basePath = Params.GA_OutputPath + "Gen_";
		
		// Gen_xxx folder for this NN
		String genPath;
		if( generation < 10 ) { genPath = basePath + "00" + generation + "/"; }
		else if(generation < 100) { genPath = basePath + "0" + generation + "/"; }
		else { genPath = basePath + generation + "/"; }
		
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
			if( i < Math.min(10, Params.NumEliteGenomes) ) {
				genomes.get(i).getNN().save(nnetFileName);
			}
		}
		
		scores.add(newScores);
		
		/*
		 *  If this is the last generation, output a CSV containing the score data.
		 */
		
		if( generation == Params.NumGenerations ) {
			try {
				String csvFileName = Params.GA_OutputPath + "/scores.csv";
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
	private ArrayList<Genome> generateRandomInitialGenomePopulation() {
		ArrayList<Genome> GenomePopulation = new ArrayList<Genome>(Params.NumGenomes + 2);
		
		// Generate Random Weighted Neural Networks for First Generation
		for( int i = 1; i <= Params.NumGenomes; i++ ) {
			Genome temp = new Genome(ModifyNeuralNetwork.randomizeWeights(this.BaseNN, this.random));
			GenomePopulation.add(temp);
		}
		
		return GenomePopulation;
	}
	
	
	private ArrayList<Genome> generateInitialGenomePopulationFromNN(File startNNET) {
		ArrayList<Genome> GenomePopulation = new ArrayList<Genome>(Params.NumGenomes);

        // Add Base NN without modification
        Genome base = new Genome(NeuralNetwork.createFromFile(startNNET));
        GenomePopulation.add(base);

        // Cross & Mutate a random NN with the Base
		for( int i = 1; i < Params.NumGenomes; i += 2 ) {
            Genome rando = new Genome(NeuralNetwork.createFromFile(startNNET));
            rando.getNN().randomizeWeights(-0.99, 0.99);

            GenomePopulation.addAll(crossoverAndMutateHelper(base, rando));
		}

		
		return GenomePopulation;
	}
	
	
	/**
	 * This method mutates children weights.
	 * 
	 * @param child
	 */
	private void mutateChildrenHelper(ArrayList<Double> child) {
		if( !Params.PerformMutations ) {
			return;
		}
		
		int numMutations = (int)(Params.PercentMutations * child.size());
		
		for(int i=0; i < numMutations; i++) {
			// Get index to flip
			int index = ((int)(this.random.nextDouble() * (child.size()-1)));
			
			// Get range of change
			Double weight = child.get(index);

			double high = weight * (1 + Params.MaxPercentMutationChange);  // 1.10 * weight
			double low  = weight * (1 - Params.MaxPercentMutationChange);  // 0.90 * weight

            double mutWeight = weight >= 0 ? new Double( (this.random.nextDouble() * (high - low)) + low ) :
                                             new Double( (this.random.nextDouble() * (low - high)) + high );
			
			// Check ranges (Keep between 0 and 1)
			//if( mutWeight >=  1 ) { mutWeight =  0.999999999999; }
			//if( mutWeight <= -1 ) { mutWeight = -0.999999999999; }

			// Mutate
			//Double mutWeight = new Double( Math.abs(1 - ((this.random.nextDouble() * (high - low)) + low)) );

            child.set(index, mutWeight);
		}
	}
	
	
	/**
	 * A helper method to convert from ArrayList<Double> to double[]
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
	 * This method takes two elite 'parents' and produces a two children through crossover and mutation. <br><br>
	 *
	 * @param alpha
	 * @param beta
     *
	 * @return A list containing two children.
	 */
	private ArrayList<Genome> crossoverAndMutateHelper(Genome alpha, Genome beta) {
		// Collect Parent Weights
		ArrayList<Double> alphaWeights = new ArrayList<Double>(Arrays.asList(alpha.getNN().getWeights()));
		ArrayList<Double> betaWeights  = new ArrayList<Double>(Arrays.asList(beta.getNN().getWeights()));

        // Create Children Weights
        ArrayList<Genome> children = new ArrayList<Genome>();
        ArrayList<Double> childWeights1 = new ArrayList<Double>();
        ArrayList<Double> childWeights2 = new ArrayList<Double>();


		// Collect random indexes to perform crossover at.
		HashSet<Integer> pivotPoints = new HashSet<Integer>();
		
		for(int i = 0; i < alphaWeights.size() * Params.CrossoverPercent; i++) {
			int pivot = (int) Math.floor(alphaWeights.size() * (this.random.nextDouble() - .000001));
			
			pivotPoints.add(pivot);
		}


        // Perform Crossover.  If at a pivot point, switch with elite parent gives to which child.
        for(int i = 0; i < alphaWeights.size(); i++ ) {

            if( pivotPoints.contains(i) && Params.PerformCrossovers ) {
                childWeights1.add(new Double(betaWeights.get(i).doubleValue()));
                childWeights2.add(new Double(alphaWeights.get(i).doubleValue()));
            } else {
                childWeights1.add(new Double(alphaWeights.get(i).doubleValue()));
                childWeights2.add(new Double(betaWeights.get(i).doubleValue()));
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

		
		return children;
	}
	
	/**
	 * This method controls the Crossover and Mutation, with helper methods.
	 * 
	 * @param currentGenomes
	 * @return
	 */
	private ArrayList<Genome> crossoverAndMutate(ArrayList<Genome> currentGenomes) {
		ArrayList<Genome> newGenomes = new ArrayList<Genome>(Params.NumGenomes + 2);
		
		// Copy in the elites from current generation
		for( int i = 0; i < Params.NumEliteGenomes; i ++ ) {
			newGenomes.add(currentGenomes.get(i));
		}
		
		
		// Generate enough new Genomes for next Generation
		for( int i = Params.NumEliteGenomes; i < Params.NumGenomes; /* No i++! */) {
			// Randomly pick two parents, then crossover/mutate them for children
			int index1 = (int)(Params.NumEliteGenomes * this.random.nextDouble());
			int index2 = (int)(Params.NumEliteGenomes * this.random.nextDouble());
			
			if( index1 == index2 ) {
				if( index1 > 0 ) {
					index2 --;  // Crossover with slightly better Genome
				} else {
					index2 ++;
				}
			}
			
			Genome parent1 = currentGenomes.get(index1);
			Genome parent2 = currentGenomes.get(index2);
			
			ArrayList<Genome> children = crossoverAndMutateHelper(parent1, parent2);
			
			// Add children to newGenomes
			for(int j = 0; j < children.size(); j++ ) {
				if( i < Params.NumGenomes ) {
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
