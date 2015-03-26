package us.thirdmillenium.desktoptrainer.geneticalgorithm;

import java.util.Comparator;

import org.neuroph.core.NeuralNetwork;


@SuppressWarnings("rawtypes")
public class Genome implements Comparable {
	private NeuralNetwork BaseNN;
	private long Score;
	
	
	public Genome(NeuralNetwork baseNN) {
		this.BaseNN = baseNN;
	}
	
	public void setScore(long score) {
		this.Score = score;
	}
	
	public long getScore() { return this.Score; }
	
	public NeuralNetwork getNN() { return this.BaseNN; }

	/**
	 * NOTE!!! - This is DEscending, not AScending order!
	 */
	@Override
	public int compareTo(Object arg0) {
		long score1 = this.Score;
        long score2 = ((Genome)arg0).getScore();

        if( score1 > score2 ) {
            return -1;
        } else if (score1 < score2) {
            return 1;
        } else {
            return 0;
        }
	}

	public static Comparator<Genome> compareOnScore = new Comparator<Genome>() {

		@Override
		public int compare(Genome o1, Genome o2) {
			long score1 = o1.getScore();
	        long score2 = o2.getScore();

	        if( score1 > score2 ) {
	            return 1;
	        } else if (score1 < score2) {
	            return -1;
	        } else {
	            return 0;
	        }
		}
	};

	@Override
	public String toString() {
		return String.valueOf(this.Score);
	}

}
