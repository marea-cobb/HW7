import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class AlignmentReader {

	private String path;

	public AlignmentReader(String path) throws IOException{
		this.path = path;
	}

	
	public String[] readAlignment() throws IOException{

		StringBuilder human_sequence = new StringBuilder();
		StringBuilder dog_sequence = new StringBuilder();
		StringBuilder mouse_sequence = new StringBuilder();

		String line;
		FileReader fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
			
		//Adds sequences from multiple sequence alignment to individual strings.
		while ((line = br.readLine()) != null) {			
	
			if (line.startsWith(">")) {
				String header = line;
				System.out.println(header);
			}
			else if (line.startsWith("hg18")) {				
				human_sequence.append(line.split("\\s+")[1].trim());
			}
			else if (line.startsWith("dog")) {
				dog_sequence.append(line.split("\\s+")[1].trim());
			}
			else if (line.startsWith("mouse")) {
				mouse_sequence.append(line.split("\\s+")[1].trim());
			}
			else {
				continue;
			}
		}
		
		
		br.close();
		fr.close();

		String[] sequences = {human_sequence.toString(), dog_sequence.toString(), mouse_sequence.toString()};

		return sequences;
	}
	
	
	public static String getSequenceWithIndex(String[] sequences, int index) {
		String emission_seq = "";
		
		for (String seq : sequences) {
			emission_seq += seq.charAt(index);
		}		
		return emission_seq;		
	}
	
}
