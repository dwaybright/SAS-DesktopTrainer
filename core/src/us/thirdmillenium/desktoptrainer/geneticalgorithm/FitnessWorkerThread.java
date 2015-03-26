package us.thirdmillenium.desktoptrainer.geneticalgorithm;

import java.util.Random;

import us.thirdmillenium.desktoptrainer.environment.TestEnvironment;



public class FitnessWorkerThread implements Runnable {
	private int Index;
	private long score;
	private Random random;
	private Genome genome;
	private TestEnvironment testEnviro1, testEnviro2, testEnviro3, testEnviro4, testEnviro5;	
	
	
	public FitnessWorkerThread(int index, Genome genome) {
		this.Index = index;
		this.score = 0;
		this.random = new Random();
		this.genome = genome;
		
		//testEnviro1 = new TestEnvironment(this.genome.getNN(), this.random, 1, 16, 16);	// Bottom Left
		testEnviro2 = new TestEnvironment(this.genome.getNN(), this.random, 2, 784, 16);	// Bottom Right
		testEnviro3 = new TestEnvironment(this.genome.getNN(), this.random, 3, 784, 1200);	// Top Right
		testEnviro4 = new TestEnvironment(this.genome.getNN(), this.random, 4, 16, 1200);	// Top Left
		testEnviro5 = new TestEnvironment(this.genome.getNN(), this.random, 5, 16, 16);		// Bottom Left
	}
	
	
	@Override
	public void run() {	
		//this.score += testEnviro1.simulate();
		//testEnviro1 = null;
		
		this.score += testEnviro2.simulate();
		testEnviro2 = null;
		
		this.score += testEnviro3.simulate();
		testEnviro3 = null;
		
		this.score += testEnviro4.simulate();
		testEnviro4 = null;
		
		this.score += testEnviro5.simulate();
		testEnviro5 = null;
	}
	
	public long getScore() {
		return this.score;
	}

	@Override
	public String toString() {
		return "Genome at index " + String.valueOf(this.Index) + ".";
	}
}
