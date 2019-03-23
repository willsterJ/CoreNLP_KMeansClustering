/*
 * Class that handles files. It takes in the directory in which the data files are stored
 * and spits out the path address in string in an array list.
 * It also handles writing to topics.txt
 */
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

public class FileHandler {
	private File[] files;
	private String dataDirectory;
	private List<String> fileNameList = new ArrayList<String>();	// list of file names to be processed
	private List<String> topicList = new ArrayList<String>();	// list of topics in ngramTopic.txt
	HashMap<String, List<String>> documentIndexHash = new LinkedHashMap<String,List<String>>(); // <foldername, file list> to be used for document index range

	
	// constructor to handle input files
	public FileHandler(String source, String command) {
		if (command.equals("readFiles")) {
			this.files = new File(source).listFiles();
			this.dataDirectory = source;
			showFilesHelper();
			filterFiles();
		}
		if (command.equals("readTopics")) {
			processTopicsFile(source);
		}
		if (command.equals("extractFileIndices")) {
			this.files = new File(source).listFiles();
			this.dataDirectory = source;
			obtainDocumentIndicesInDirectory_Helper();
		}
	}
	// overloaded constructor to handle writing topics to a topics.txt
	public FileHandler(HashMap<String, Integer> hash, String destination) {
		writeTopicsToFile(hash, destination);
	}
	
	// function wrapper for the recursion
	public void showFilesHelper() {
		showFiles(this.files, dataDirectory);
	}
	// method to iterate through all the files in a directory, and adds them to arraylist
	private void showFiles(File[] files, String path) {
	    for (File file : files) {
	        if (file.isDirectory()) {
	        	String current;
	        	current = path + "/" + file.getName();
	            //System.out.println("Directory: " + file.getName());
	            showFiles(file.listFiles(),current); // Calls same method again.
	        } else {
	        	String completePath = path+"/"+file.getName();
	            //System.out.println(completePath);
	            fileNameList.add(completePath);
	        }
	    }
	}
	// method to remove unwanted files
	public void filterFiles() {
		for (int i=0; i< fileNameList.size(); i++) {
			if (!fileNameList.get(i).contains(".txt")) {
				fileNameList.remove(i);
			}
		}
	}
	
	// method to write the reverse sorted hash to a file
	public void writeTopicsToFile(HashMap<String, Integer> hash, String destination) {
		try {
		PrintWriter writer = new PrintWriter(destination, "UTF-8");
		Iterator<Map.Entry<String, Integer>> itr = hash.entrySet().iterator();
		while(itr.hasNext()) {
			Map.Entry<String, Integer> entry = itr.next();
			writer.println(entry.getKey() + " " + entry.getValue());
		}
		writer.close();
		} catch(Exception e) {}
	}
	
	// method to read ngramTopic.txt and add topics to a list (ignoring number values)
	public void processTopicsFile(String source) {
		Scanner input;
		String topic = "";
		try {
			input = new Scanner (new FileReader(source));
			while (input.hasNext()) {
				String inputtok = input.next();
				
				if (isNumeric(inputtok) == true) {	// add the string that precedes the number
					topicList.add(topic);
					topic = "";
				}
				else	// if not a num, concatenate string
					topic = topic + " " + inputtok;
			}
			input.close();
		} catch(Exception e) {}
	}

	// method to check if a string is a number
	public static boolean isNumeric(String str) {
		  return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	
	// method to obtain the range indices of article files in each directory
	// FileHandler will process files indicriminately from folder, so this method extracts the range
	// of indices of the files in each folder
	public void obtainDocumentIndicesInDirectory_Helper() {
		obtainDocumentIndicesInDirectory(this.files, dataDirectory, documentIndexHash);
	}
	public void obtainDocumentIndicesInDirectory(File[]files,String path, HashMap<String, List<String>> hash) {
    	String directoryName = path;
		
	    for (File file : files) {
	        if (file.isDirectory()) {
	        	directoryName = file.getName();
	        	hash.put(file.getName(), new ArrayList<String>());
	        	obtainDocumentIndicesInDirectory(file.listFiles(),directoryName,hash); // Calls same method again.
	        } else {
	        	if (!file.getName().contains(".txt")) {	// if not a .txt, skip
	        		continue;
	        	}
	        	// add documents to hash map
	        	List<String> temp = hash.get(directoryName);
	        	temp.add(file.getName());
	        	hash.put(directoryName, temp);
	        }
	    }
	}
	
	// Getters and Setters
	public List<String> getFileNameList() {
		return fileNameList;
	}

	public void setFileNameList(List<String> fileNameList) {
		this.fileNameList = fileNameList;
	}
	public List<String> getTopicList() {
		return topicList;
	}
	public void setTopicList(List<String> topicList) {
		this.topicList = topicList;
	}

	
}
