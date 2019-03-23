import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
/*
 * Class that handles stop word processing. It takes in a filename path in string
 * of the Stop Words text file.  
 * methods include checking if a word belongs to a hash set of stop words and
 * reading the words from a text file specified by the constructor. 
 * 
 * Only checkWord() would be used.
 * 
 */
public class STOPWORDS {
	private ArrayList<String> stopList;
	private Set<String> mySet;


	public STOPWORDS(String filename) {
		stopList = readStopWords(filename);
	    mySet = new HashSet<String>(stopList);	// store mySet as a HashSet
	}
	
	// returns true if input belongs in mySet
    public boolean checkWord(String input) { 
        return mySet.contains(input.toLowerCase());
    } 

    // opens a path file and adds all the words to stopList
	private ArrayList<String> readStopWords(String filename) {
		ArrayList<String> arr = new ArrayList<String>();
		Scanner input;
		
		try {
			input = new Scanner(new FileReader(filename));
			while (input.hasNext()) {
				arr.add(input.next());
			}
		}
		catch(IOException e){
			System.out.println("File does not exist!!");
			System.exit(0);
		}
		return arr;
	}
	
	
	// Getters and Setters
	public ArrayList<String> getStopList() {
		return stopList;
	}

	public void setStopList(ArrayList<String> stopList) {
		this.stopList = stopList;
	}
}
