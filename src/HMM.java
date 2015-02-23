import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HMM {
	
	private String[] states;
	private double[] initialization;
	private List<Map<Character, Double>> emission;
	private double[][] transition;
	private Character[] alphabet;
	
	
	//Constructor for only maintaining states
	public HMM(String[] states, double[] initialization, List<Map<Character, Double>> emission, double[][] transition, Character[] alphabet) {
		this.states = states;
		this.initialization = initialization;
		this.emission = emission;
		this.transition = transition;
		this.alphabet = alphabet;
	}

	public double baumWelch(String sequence, double target_delta) {
		int index = 0;
		double target_log_delta = LogMath.log(target_delta);
		double new_likelihood = baumWelch(sequence);
		double old_likelihood;
		
		System.out.println("Initial likelihood: " + new_likelihood);
		System.out.println(String.format("Target delta: %s (%s)", target_log_delta, target_delta));
		
		double change;
		do {
			index++;
			old_likelihood = new_likelihood;
			
			new_likelihood = baumWelch(sequence);
			
			change = LogMath.product(new_likelihood, -old_likelihood);
			
			System.out.println(String.format("Likelihood: %s. change: %s", new_likelihood, change));
			System.out.println("Iterations for Convergence: " + index);
		} while (change > target_delta);
		
		return new_likelihood;
	}
	
	private void updateHMM(double[] initialization, List<Map<Character, Double>> emission, double[][] transition) {
		this.initialization = initialization;
		this.emission = emission;
		this.transition = transition;
	}
	
	private double[][] forward(String sequence) {
		int T = sequence.length();
		int N = states.length;
		
		double[][] fwd = new double[N][T];
		
		//Initial score
		for (int i=0; i<N; i++) {
			fwd[i][0] = LogMath.product(logInitialization(i), logEmission(i, 0, sequence));
		}
		
		for (int t=1; t<T; t++) {
			for (int j=0; j<N; j++) {
				double logalpha = LogMath.LOGZERO;
				
				for (int i=0; i<N; i++) {
					logalpha = LogMath.sum(logalpha, LogMath.product(fwd[i][t-1], logTransition(i, j)));
				}
				
				fwd[j][t] = LogMath.product(logalpha, logEmission(j, t, sequence));
			}
		}
		
		return fwd;
	}
	
	//Stores values in log scale
	private double[][] backwards(String sequence) {
		int T = sequence.length();
		int N = states.length;
		
		double[][] bkw = new double[N][T];
		
		for (int i=0; i<N; i++) {
			bkw[i][T-1] = 0;
		}
		
		for (int t=T-2; t >= 0; t--) {
			for (int i=0; i<N; i++) {
				double logbeta = LogMath.LOGZERO;
				
				for (int j=0; j < N; j++) {
					logbeta = LogMath.sum(logbeta, LogMath.product(logTransition(i, j), logEmission(j, t+1, sequence), bkw[j][t+1]));
				}
				bkw[i][t] = logbeta;
			}
		}
		
		return bkw;
	}
	
	private double logInitialization(int i) {
		return LogMath.log(initialization[i]);
	}
	
	private double logTransition(int i, int j) {
		return LogMath.log(transition[i][j]);
	}
	
	private double logEmission(int i, int j, String sequence) {
		return LogMath.log(emission.get(i).get(sequence.charAt(j)));
	}
	
	private double baumWelch(String sequence) {		
		int T = sequence.length();
		double[][] fwd;
		double[][] bkw;
		
		double[][] gamma = new double[states.length][T];
		double[][][] xi = new double[states.length][states.length][T];
		
		
		//New states
		double[] new_initialization = new double[states.length];
		double[][] new_transition = new double[states.length][states.length];
		List<Map<Character, Double>> new_emission = new ArrayList<Map<Character, Double>>();
		
		
		//Run forward and backward
		fwd = forward(sequence);
		bkw = backwards(sequence);
		
		//Compute the gamma
		for (int t=0; t<T; t++) {
			double normalizer = LogMath.LOGZERO;
			
			for (int i=0; i<states.length; i++) {
				gamma[i][t] = LogMath.product(fwd[i][t], bkw[i][t]);
				normalizer = LogMath.sum(normalizer, gamma[i][t]);
			}
			
			for (int i=0; i<states.length; i++) {
				gamma[i][t] = LogMath.product(gamma[i][t], -normalizer);
			}
		}
		
		//Compute the xi
		for (int t=0; t<T-1; t++) {
			double normalizer = LogMath.LOGZERO;
			
			for (int i=0; i<states.length; i++) {
				for (int j=0; j<states.length; j++) {
					xi[i][j][t] = LogMath.product(fwd[i][t], logTransition(i, j), logEmission(j, t+1, sequence), bkw[j][t+1]);
					normalizer = LogMath.sum(normalizer, xi[i][j][t]);
				}
			}
			
			for (int i=0; i<states.length; i++) {
				for (int j=0; j<states.length; j++) {
					xi[i][j][t] = LogMath.product(xi[i][j][t], -normalizer);
				}
			}
		}
				
		//Re-estimate the initial probabilities
		for (int i=0; i<states.length; i++) {
			new_initialization[i] = LogMath.exp(gamma[i][0]);
		}
		System.out.println("INITIALIZATION");
		System.out.println(Arrays.toString(new_initialization));
		
		//Re-estimate the transition probabilities
		for (int i=0; i<states.length; i++) {
			for (int j=0; j<states.length; j++) {
				double numerator = LogMath.LOGZERO;
				double denominator = LogMath.LOGZERO;
				
				for (int t=0; t < T-1; t++) {
					numerator = LogMath.sum(numerator, xi[i][j][t]);
					denominator = LogMath.sum(denominator, gamma[i][t]);
				}
				new_transition[i][j] = LogMath.exp(LogMath.product(numerator, -denominator));
			}
		}
		System.out.println("TRANSITION");
		System.out.println(Arrays.toString(new_transition[0]) + " " + Arrays.toString(new_transition[1]));
		
		
		//Re-estimate the emission probabilities
		for (int j=0; j<states.length; j++) {
			Map<Character, Double> emissionState = new HashMap<Character, Double>();
			for (int k=0; k<alphabet.length; k++) {
				double numerator = LogMath.LOGZERO;
				double denominator = LogMath.LOGZERO;
				
				for (int t=0; t<T; t++) {
					
					double g = gamma[j][t];
					if (alphabet[k].equals(sequence.charAt(t))) {
						numerator = LogMath.sum(numerator, g);
					}
					
					denominator = LogMath.sum(denominator, g);
				}
				emissionState.put(alphabet[k], LogMath.exp(LogMath.product(numerator, -denominator)));
			}
			new_emission.add(j, emissionState);	
		}
		
		updateHMM(new_initialization, new_emission, new_transition);
		System.out.println("EMISSION");
		System.out.println(printTable(emission));
		
		double likelihood = LogMath.LOGZERO;
		for (int i=0; i<states.length; i++) {
			likelihood = LogMath.sum(fwd[i][T-1]);
		}
		return likelihood;
	}
	

	
	
	private static Map<Character, Double> createEmissionState(double a, double t, double g, double c) {
		Map<Character, Double> map = new HashMap<Character, Double>();
		map.put('A', a);
		map.put('T', t);
		map.put('G', g);
		map.put('C', c);
		return map;
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
	
	
	private static String printTable(List<Map<Character, Double>> mapList) {
        StringBuilder sb = new StringBuilder();
        
        for (Map<Character, Double> map : mapList) {
            sb.append("[");
	        for (Character key :  map.keySet()) {
	        	sb.append(key + ": " + map.get(key));
		        sb.append(", ");
	        }
	        sb.append("]");
        }
        return sb.toString();

	}
	//Counts the number of times the states our found


	public static void main(String[] args) throws IOException, InterruptedException {
		String[] states = { "NEUTRAL", "CONSERVED" };
		double[] initialization = { 0.95, 0.05 };
//		Character[] alphabet = { 'A', 'T', 'G', 'C' };
		double[][] transition = { { 0.95, 0.05 }, { 0.10, 0.90 } };

//		List<Map<Character, Double>> emission = new ArrayList<Map<Character, Double>>();
//		emission.add(createEmissionState(0.291, 0.291, 0.209, 0.209));
//		emission.add(createEmissionState(0.169, 0.169, 0.331, 0.331));
		

		
		
//		String[] states = { "S1", "S2" };
//		double[] initialization = { 0.2, 0.8 };
//		Character[] alphabet = { 'E', 'N' };
//		double[][] transition = { { 0.5, 0.5 }, { 0.3, 0.7 } };
//		
//		List<Map<Character, Double>> emission = new ArrayList<Map<Character, Double>>();
//		Map<Character, Double> em1 = new HashMap<Character, Double>();
//		Map<Character, Double> em2 = new HashMap<Character, Double>();
//		em1.put('N', 0.3); em1.put('E', 0.7);
//		em2.put('N', 0.8); em2.put('E', 0.2);
//		emission.add(em1); emission.add(em2);
//		
//		String sequence = "NE";
		
//		HMM markov = new HMM(states, initialization, emission, transition, alphabet);
//		markov.baumWelch(sequence, 0.1);
	}
	
	
}