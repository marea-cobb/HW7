import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HMM {
	
	private String[] states;
	private double[] initialization;
	private List<Map<String, Double>> emission;
	private double[][] transition;
	
	
	//Constructor for only maintaining states
	public HMM(String[] states, double[] initialization, List<Map<String, Double>> emission, double[][] transition) {
		this.states = states;
		this.initialization = initialization;
		this.transition = transition;
		this.emission = emission;
	}

	public static double log2(double a)
	{
		double value = Math.log(a) / Math.log(2);
	return value;
	}

	
	public int[] getMostLikelySequence(String[] sequences, double[][] transition) {
		int seq_length = sequences[0].length();
		
		double[][] table = new double[states.length][seq_length];
		int[][] path = new int[states.length][seq_length];
		
		// Initialize all our prior values
		String starter = AlignmentReader.getSequenceWithIndex(sequences, 0);
//		System.out.println(starter);
		for (int i = 0; i < states.length; i++) {
			table[i][0] = log2(initialization[i]) + log2(emission.get(i).get(starter));
			path[i][0] = 0;
		}
		
		// Run forward
		for (int i = 1; i < seq_length; i++) {
			for (int j = 0; j < states.length; j++) {
				MaxWithIndex max = getMaxForwardWithIndex(i, j, sequences, table, transition);
				table[j][i] = max.getMax();
				path[j][i] = max.getIndex();
			} 
		}
		
		// Run backwards
		int[] z = new int[seq_length];
		int T = seq_length - 1;
		
		MaxWithIndex max = getMaxWithIndex(T, table);
		z[T] = max.getIndex();
		
		for (int i = T; i >= 1; i--) {
			z[i-1] = path[z[i]][i];
		}
		
		return z;
	}
	
	
	public String[] convertPathToStateNames(int[] path) {
		String[] stateNames = new String[path.length];
		for (int i = 0; i < path.length; i++) {
			stateNames[i] = this.states[path[i]];
		}
		return stateNames;
	}
	
	private class MaxWithIndex {
		private double max;
		private int index;
		
		public MaxWithIndex(double max, int index) {
			this.max = max;
			this.index = index;
		}
		
		public double getMax() {
			return max;
		}
		
		public int getIndex() {
			return index;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("(").append(max).append(", ").append(index).append(")");
			return sb.toString();
		}
	}
	
	private MaxWithIndex getMaxForwardWithIndex(int i, int j, String[] sequences, double[][] table, double[][] transition){
		double max = Double.NEGATIVE_INFINITY;
		int index = -1;
		
		String c = AlignmentReader.getSequenceWithIndex(sequences, i);
		for (int k = 0; k < table.length; k++) {
			double current = table[k][i-1] + log2(transition[k][j]) + log2(emission.get(j).get(c));
			
			if (current >= max) {
				max = current;
				index = k;
			}
		}
		
		return new MaxWithIndex(max, index);
	}
	
	private MaxWithIndex getMaxWithIndex(int i, double[][] table) {
		double max = Double.NEGATIVE_INFINITY;
		int index = -1;
		
		for (int k = 0; k < table.length; k++) {
			double current = table[k][i];
			
			if (current >= max) {
				max = current;
				index = k;
			}
		}
		
		return new MaxWithIndex(max, index);
	}
	
	public static String printTable(double[][] table) {
		return printTable(table, new DecimalFormat());
	}
	
	public static String printTable(double[][] table, DecimalFormat df) {
		if (table == null) {
			return "null";
		}
		
		int iMax = table.length - 1;
        if (iMax == -1) {
            return "[]";
        }

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(printArray(table[i], df));
            if (i == iMax) {
                return b.append(']').toString();
            }
            b.append(", ");
        }
	}
	
	private static String printArray(double[] array, DecimalFormat df) {
        if (array == null) {
            return "null";
        }
        
        int iMax = array.length - 1;
        if (iMax == -1) {
            return "[]";
        }

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(df.format(array[i]));
            if (i == iMax) {
                return b.append(']').toString();
            }
            b.append(", ");
        }
	}
	
	//Counts the number of times the states our found
	public int[] getStateCounts(int[] path) {
		int state1_count = 0;
		int state2_count = 0;
		
		for (int i: path) {
			if (i==0) {
				state1_count++;
			} else if (i==1) {
				state2_count++;
			}
		}
		
		int[] count = {state1_count, state2_count};
		return count;
	}
	
	//Counts the number of state segments
	public int[] getSegmentCounts(int[] path) {
		ArrayList<Segments> longest_segments = new ArrayList<Segments>();
		
		int segment1_count = 0;
		int segment2_count = 0;
		int index = 1;
		int start = 0;
		int stop;
		
		boolean seg1 = false;
		boolean seg2 = false;
		while (index < path.length - 1) {
			if (path[index] == 0) {
				if(path[index+1] == 0) {
					if (seg1 == false) {
						segment1_count++;
						seg1 = true;
						index++;
					} else {
					seg1 =  true;
					index++;
					} 
				} else {
					seg1 = false;
					index++;
				}	
			} else if (path[index] == 1) {
				if(path[index+1] == 1) {
					if (seg2 == false) {
						start = index+131284313;
//						System.out.println(index+1);
						segment2_count++;
						seg2 = true;
						index++;
					} else {
					seg2 =  true;
					index++;
					} 
				} else {
					seg2 = false;
					stop = index+131284313;

					Segments segment = new Segments(start, stop);

					longest_segments.add(segment);
					index++;
				}	
			} else {
				index++;
			}
		}
		
		int[] count = {segment1_count, segment2_count};
		printLongestSegments(longest_segments);
		return count;
	}
	
	
	private void printLongestSegments(ArrayList<Segments> longest_segments) {
//		ArrayList<Map, Integer, Integer[]>

		Collections.sort(longest_segments, new Comparator<Segments>() {
			@Override public int compare(Segments s1, Segments s2) {
				return s1.length - s2.length;
			}
		});
		Collections.reverse(longest_segments);
		System.out.println(longest_segments.toString());
	}
	
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		//Reads in sequences
		AlignmentReader alignment = new AlignmentReader("/Users/mareac/Documents/Graduate/GENOME540/HW7/ENm002.maf.aln");
//		AlignmentReader alignment = new AlignmentReader("/Users/mareac/Documents/Graduate/GENOME540/HW7/ENm006_short.aln");
		String[] sequences = alignment.readAlignment();
		
		String[] states = { "NEUTRAL", "CONSERVED" };
		double[] initialization = { 0.95, 0.05 };
		double[][] transition = { { 0.95, 0.05 }, { 0.10, 0.90 } };
		
		//Reads in frequency counts for both states.
		Map<String, Double> neutral_emissions = FrequencyCounts.getNeutralFrequencyCounts("/Users/mareac/Documents/Graduate/GENOME540/HW7/NeutralFrequencyCounts");
		Map<String, Double> conserved_emissions = FrequencyCounts.getConservedFrequencyCounts("/Users/mareac/Documents/Graduate/GENOME540/HW7/ConservedFrequencyCounts");
		
		List<Map<String, Double>> emission = new ArrayList<Map<String, Double>>();
		emission.add(neutral_emissions);
		emission.add(conserved_emissions);

		
		HMM markov = new HMM(states, initialization, emission, transition);
		int[] path = markov.getMostLikelySequence(sequences, transition);
//		int[] state_numbers = markov.getStateCounts(path);
		int[] segment_numbers = markov.getSegmentCounts(path);
		
//		System.out.println("State numbers: " + state_numbers[0] + " " + state_numbers[1]);
//		System.out.println("Segment numbers: " + segment_numbers[0] + " " + segment_numbers[1]);
		
	}
	
}