package us.thirdmillenium.desktoptrainer.geneticalgorithm;

import java.util.Random;
import java.util.Set;

import us.thirdmillenium.desktoptrainer.environment.Environment;
import us.thirdmillenium.desktoptrainer.environment.TestEnvironment;



public class FitnessWorkerThread implements Runnable {
	private int index;
	private long score;
	private Random random;
	private Environment testEnviro1, testEnviro2, testEnviro3, testEnviro4, testEnviro5;
	
	
	public FitnessWorkerThread(int index, Genome genome, Set<Integer> mapsToUse) {
		this.index = index;
		this.score = 0;
		this.random = new Random();

        this.testEnviro1 = mapsToUse.contains(1) ? new TestEnvironment(genome.getNN(), this.random, 1,  16,   16) : null;   // Bottom Left
        this.testEnviro2 = mapsToUse.contains(2) ? new TestEnvironment(genome.getNN(), this.random, 2, 784,   16) : null;   // Bottom Right
        this.testEnviro3 = mapsToUse.contains(3) ? new TestEnvironment(genome.getNN(), this.random, 3, 784, 1200) : null;   // Top Right
        this.testEnviro4 = mapsToUse.contains(4) ? new TestEnvironment(genome.getNN(), this.random, 4,  16, 1200) : null;   // Top Left
        this.testEnviro5 = mapsToUse.contains(5) ? new TestEnvironment(genome.getNN(), this.random, 5,  16,   16) : null;   // Bottom Left
	}
	
	
	@Override
	public void run() {
        if(this.testEnviro1 != null) {
            this.testEnviro1.simulate();
            this.score += this.testEnviro1.getScore();
            this.testEnviro1 = null;
        }

        if(this.testEnviro2 != null) {
            this.testEnviro2.simulate();
            this.score += this.testEnviro2.getScore();
            this.testEnviro2 = null;
        }

        if(this.testEnviro3 != null) {
            this.testEnviro3.simulate();
            this.score += this.testEnviro3.getScore();
            this.testEnviro3 = null;
        }

        if(this.testEnviro4 != null) {
            this.testEnviro4.simulate();
            this.score += this.testEnviro4.getScore();
            this.testEnviro4 = null;
        }

        if(this.testEnviro5 != null) {
            this.testEnviro5.simulate();
            this.score += this.testEnviro5.getScore();
            this.testEnviro5 = null;
        }
	}
	
	public long getScore() {
        return this.score;
	}

	@Override
	public String toString() {
		return "Genome at index " + String.valueOf(this.index) + ".";
	}
}
