import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


public class FrequencyCounts {

	
	public static Map<String, Double> getNeutralFrequencyCounts(String neutral_path) throws IOException {
		String line;
		RandomAccessFile fr = new RandomAccessFile(neutral_path, "r");
		
		int neutral_total = 0;
		while ((line = fr.readLine()) != null) {		
			
			String[] data = line.split("\\s+");
			int frequency = Integer.valueOf(data[1]);
			neutral_total += frequency;
		}
		
		fr.seek(0);
		
		DecimalFormat df = new DecimalFormat("0.000E0");
		Map<String, Double> neutral_frequency_counts = new HashMap<String, Double>();
		while ((line = fr.readLine()) != null) {		
			String[] data = line.split("\\s+");
			String alignment = data[0];
			int frequency = Integer.valueOf(data[1]);
			System.out.println("1," + alignment + "=" + df.format((double) frequency/neutral_total));

			neutral_frequency_counts.put(alignment, (double) frequency/neutral_total);
		}
		
		fr.close();
		return neutral_frequency_counts;
	}
	
	
	public static Map<String, Double> getConservedFrequencyCounts(String conserved_path) throws IOException {
		String line;
		RandomAccessFile fr = new RandomAccessFile(conserved_path, "r");
		
		int conserved_total = 0;
		while ((line = fr.readLine()) != null) {		
			
			String[] data = line.split("\\s+");
			int frequency = Integer.valueOf(data[1]);
			conserved_total += frequency;	
		}
		
		fr.seek(0);
		
		DecimalFormat df = new DecimalFormat("0.000E0");
		Map<String, Double> conserved_frequency_counts = new HashMap<String, Double>();
		while ((line = fr.readLine()) != null) {		
			String[] data = line.split("\\s+");
			String alignment = data[0];
			int frequency = Integer.valueOf(data[1]);
			System.out.println("2," + alignment + "=" + df.format((double) frequency/conserved_total));

			conserved_frequency_counts.put(alignment, (double) frequency/conserved_total);
		}
		
		fr.close();	
		return conserved_frequency_counts;
	}

}
