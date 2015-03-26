package us.thirdmillenium.desktoptrainer.neuralnetwork;

import java.io.File;
import java.util.Random;

import org.neuroph.core.NeuralNetwork;


public class ModifyNeuralNetwork {
	
	@SuppressWarnings("rawtypes")
	public static NeuralNetwork randomizeWeights(File file, Random random) {
		NeuralNetwork rando = NeuralNetwork.createFromFile(file);
		
		Double[] weights = rando.getWeights();
		double[] newWeights = new double[weights.length];
		
		for( int i = 0; i < weights.length; i ++) {
			newWeights[i] = random.nextDouble();
		}
		
		rando.setWeights(newWeights);
		
		return rando;
	}
}
