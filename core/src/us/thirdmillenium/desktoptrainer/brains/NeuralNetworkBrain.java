package us.thirdmillenium.desktoptrainer.brains;

import java.util.Random;

import org.neuroph.core.NeuralNetwork;

@SuppressWarnings("rawtypes")
public class NeuralNetworkBrain extends Brain {
	private NeuralNetwork myNN;
	
	
	public NeuralNetworkBrain(NeuralNetwork nnet) {
        this.myNN = nnet;
	}

    public NeuralNetworkBrain(String nnetPath) {
        this.myNN = NeuralNetwork.createFromFile(nnetPath);
    }
	
	@Override
	public double[] brainCrunch(double[] inputs) {
		// Set Inputs
		this.myNN.setInput(inputs);
		
		// Calculate Network
		this.myNN.calculate();
		
		// Return Output
		return this.myNN.getOutput();		
	}
	
	@Override
	public int getNumInputs() { return this.myNN.getInputsCount(); }
	
	@Override
	public int getOutputCounts() { return this.myNN.getOutputsCount(); }

	@Override
	public void randomWeights(Random random) {
		this.myNN.randomizeWeights(random);
	}
}
