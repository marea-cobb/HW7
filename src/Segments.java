
public class Segments{
	int start;
	int stop;
	int length;

	public Segments(int start, int stop) {
		this.start = start;
		this.stop = stop;
		this.length = stop-start;
	}
	
	public int getLength() {
		return this.length;
	}
	
	public int getStart() {
		return this.start;
	}
	
	public int getStop() {
		return this.stop;
	}
	
	public String toString() {
		return "\n" + start + " " + stop;
//		return "\nStart: " + start + "\n" + "End: " + stop;
	}

 	
}
