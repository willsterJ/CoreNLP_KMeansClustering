
/*
 * Class that handles the n-gram procedure. Given inputs integer and list in the constructor,
 *  generate a hash map that contains <key, val> = (word, word_count)
 */
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class NGram {
	// "global" static hash map that is shared by all documents
	// used later to filter words with smaller values
	public static HashMap<String, Integer> hashTable = new HashMap<String, Integer>();
	private HashMap<String, Integer> documentHash; // local hash for each document

	public NGram(int n, ArrayList<String> inputlist, boolean writeToTopics) {
		this.documentHash = new HashMap<String, Integer>();
		if (writeToTopics == true)
			generateMap(n, inputlist, hashTable);
		generateMap(n, inputlist, documentHash);
	}

	// method to generate n-gram hash map given n and a list. Implements a Sliding
	// Window approach.
	// It uses recursion to iterate through the last remaining n elements of the
	// list
	private void generateMap(int n, ArrayList<String> list, HashMap<String, Integer> hash) {
		// error check
		if (list.size() < n) {
			System.out.println("ERROR: n-gram is bigger than the array list of tokens");
			return;
		}

		String s = "";
		// base case
		if (n == 1) {
			s = list.get(list.size() - 1); // insert the last remaining element to hash
			insertToHash(s, hash);
			return;
		}

		// insert all words at index i ... i+n to hash
		for (int i = 0; i < list.size() - n + 1; i++) {
			s = "";
			for (int j = 0; j < n; j++) {
				s = s + " " + list.get(i + j);
				insertToHash(s, hash);
			}
		}

		// Now take care of the n remainder of the list
		int begin = list.size() - n + 1;
		int end = list.size();
		ArrayList<String> subList = new ArrayList<String>(list.subList(begin, end));
		this.generateMap(n - 1, subList, hash); // recurse

	}

	// method to insert an element into the hash map
	private void insertToHash(String instring, HashMap<String, Integer> hash) {

		String s = instring;
		// get rid of plurals (i.e. banks and bank are the same. We only need one)
		if (instring.substring(instring.length() - 1).equals("s"))
			s = instring.substring(0, instring.length() - 1);

		if (hash.containsKey(s)) { // if key already exists, increment its val by 1
			int curr = hash.get(s);
			hash.put(s, curr + 1);
		} else {
			hash.put(s, 1);
		}
	}

	// method to eliminate entries whose value < n
	public static void eliminateByValueLessThan(int n) {

		Iterator<Map.Entry<String, Integer>> iter = hashTable.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Integer> entry = iter.next();
			// System.out.println(entry.getKey() + " = " + entry.getValue());
			if (entry.getValue() < n)
				iter.remove();
		}
	}

	// method that sorts the entire hash map of the entire dataset by each element's
	// value
	public static void sortAllByValue() {
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(hashTable.entrySet());

		// Sort the list
		hashComparator hashCompare = new hashComparator();
		Collections.sort(list, hashCompare);

		// put data from sorted list to hashmap
		HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
		for (Map.Entry<String, Integer> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		hashTable = temp;
	}

	// method that sorts local document hash map
	public HashMap<String, Integer> sortByValue(HashMap<String, Integer> hash) {
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(hash.entrySet());
		// Sort the list
		hashComparator hashCompare = new hashComparator();
		Collections.sort(list, hashCompare);

		// put data from sorted list to hashmap
		HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
		for (Map.Entry<String, Integer> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		return temp;
	}

	// method to print topics in hashTable
	public static void printTopics() {
		System.out.println("----------------------------------Topic List------------------------------------");
		Iterator<Map.Entry<String, Integer>> itr = hashTable.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, Integer >pair = itr.next();
			System.out.println(pair.getKey() + " = " + pair.getValue());
		}
		System.out.println("----------------------------------END-----------------------------------");

	}

	// Getters and Setters
	public HashMap<String, Integer> getDocumentHash() {
		return documentHash;
	}

	public void setDocumentHash(HashMap<String, Integer> o) {
		this.documentHash = o;
	}

}

// implement a comparator class
class hashComparator implements Comparator<Map.Entry<String, Integer>> {
	public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
		return o2.getValue().compareTo(o1.getValue());
	}
}
