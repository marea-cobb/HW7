import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class FrequencyCounts {
	
	public Map<String, Integer> frequency_counts = new HashMap<String, Integer>();
	
	
	public FrequencyCounts(String path) throws IOException {
		String line;

		FileReader fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		
		while ((line = br.readLine()) != null) {		
			
			String[] data = line.split("\\s+");
			String alignment = data[0];
			int frequency = Integer.valueOf(data[1]);
			
			frequency_counts.put(alignment, frequency);
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		FrequencyCounts counts = new FrequencyCounts("/Users/mareac/Documents/Graduate/GENOME540/HW7/FrequencyCounts");
		
		for (String keys : counts.frequency_counts.keySet()) {
			System.out.println(keys + ": " + counts.frequency_counts.get(keys));
		}
	}

}
