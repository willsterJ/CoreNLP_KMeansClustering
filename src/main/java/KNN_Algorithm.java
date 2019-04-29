
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Consumer;

public class KNN_Algorithm {
	private List<String> datasetTopics; // list that stores entire data set topics
	private int[][] matrix;
	public static double[][] unknown_tf_idfMatrix;
	
	public KNN_Algorithm(int k, int knn, double[][] tf_idfMatrix, List<String> euclideanClusterNames, int ngram_level, int relevancy_Cutoff, String source) {
		// apply nlp, ngram, yaddy yadda to unknowns
		new MatrixHandler(ngram_level, relevancy_Cutoff, source, false);
		int[][] unknown_matrix = MatrixHandler.matrix; // create document-term matrix for the unknowns
		double[] idf_array = MatrixHandler.idf_array; // inverse document frequency generated from training data set matrix
		
		unknown_tf_idfMatrix = performtTf_idf(unknown_matrix, idf_array); // convert matrix to tf-idf
		
		// Use euclidean distance between unknowns and dataset
		double[][] distanceMatrix = Euclidean_Distance_Matrix(tf_idfMatrix, unknown_tf_idfMatrix);
		
		// find nearest neighbors
		// class KNN_Distance_Element is defined below, and stores value and index for distance element of distance matrix
		KNN_Distance_Element[][] neighbor = chooseNearestNeighbor(knn, distanceMatrix);
		
		// obtain name using predicted names from dataset
		String[][] namesMatrix = category_Name_Matrix(neighbor, euclideanClusterNames);
		
		// obtain category frequency hashmap from name matrix
		List<HashMap<String, Integer>> name_frequency_hash = convert_matrix_to_hash(namesMatrix);
		
		// select most frequent name
		String[] names = selectName(name_frequency_hash);
		
		// accuracy score
		List<String> predicted = Arrays.asList(names);
		List<String> actual = obtainActualList();
		ConfusionMatrix_Evaluation conf = new ConfusionMatrix_Evaluation(k, predicted, actual);
		
		// write to .txt and print
		printOutput(predicted, conf);
		
		// FUZZY LOGIC
		print_fuzzy_logic(name_frequency_hash);
		
	}
	
	// method that turns document-term matrix into tf-idf
	private double[][] performtTf_idf(int[][] matrix, double[] idf_array) {
		// count total words per document and store in array
		double[] documentwordcountArray = new double[matrix.length]; // word count for each document
		for (int i = 0; i < matrix.length; i++) {

			int documentwordcount = 0;
			for (int j = 0; j < matrix[i].length; j++) {
				documentwordcount += matrix[i][j];
			}
			documentwordcountArray[i] = documentwordcount;
		}
		
		double[] inverseDocumentFrequency = idf_array;
		
		// copy matrix to tf_idfMatrix
		double tf_idfMatrix[][] = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < tf_idfMatrix.length; i++) {
			for (int j = 0; j < tf_idfMatrix[i].length; j++) {
				tf_idfMatrix[i][j] = matrix[i][j];
			}
		}
		// perform tf_idf
		for (int i = 0; i < tf_idfMatrix.length; i++) {
			for (int j = 0; j < tf_idfMatrix[i].length; j++) {
				double tf_idf;
				if (tf_idfMatrix[i][j] != 0) {
					tf_idf = tf_idfMatrix[i][j] / documentwordcountArray[i] * inverseDocumentFrequency[j];
					tf_idfMatrix[i][j] = tf_idf;
				}
			}
		}

		return tf_idfMatrix;
	}
	
	// method that applies euclidean distance and ouputs distance array between unknowns(rows) and dataset points(columns)
	private double[][] Euclidean_Distance_Matrix(double[][] tf_idfMatrix, double[][] unknown_matrix) {
		// rows = # of unknowns, rows = # data points in training set
		int rowSize = unknown_matrix.length;
		int columnSize = tf_idfMatrix.length;
		double[][] distanceMatrix = new double[rowSize][columnSize];
		
		for (int i=0; i<rowSize; i++) {
			for (int j=0; j<columnSize; j++) {
				distanceMatrix[i][j] = calculate_Euclidean_Distance(unknown_matrix[i], tf_idfMatrix[j]);
			}
		}
		
		return distanceMatrix;
	}
	
	// method that calculates euclidean distance between 2 vectors
	private double calculate_Euclidean_Distance(double[] A, double[] B) {
		double sum=0;
		for (int i=0; i<A.length; i++) {
			sum += Math.pow(B[i] - A[i],2);
		}
		
		return Math.sqrt(sum);
	}
	
	// method that chooses knn nearest neighbors
	private KNN_Distance_Element[][] chooseNearestNeighbor(int knn, double[][] distanceMatrix) {
		
		// convert distance matrix into object
		List<KNN_Distance_Element>[] temp = new ArrayList[distanceMatrix.length];
		// init matrix
		for (int i=0; i<distanceMatrix.length; i++) {
			temp[i] = new ArrayList<KNN_Distance_Element>();
			for (int j=0; j<distanceMatrix[0].length; j++) {
				temp[i].add(new KNN_Distance_Element(distanceMatrix[i][j], j));
			}
		}
		
		// implement sorting algorithm to sort unknowns
		for (int i=0; i<temp.length; i++) {
			Collections.sort(temp[i], new Comparator<KNN_Distance_Element>() {
				public int compare(KNN_Distance_Element o1, KNN_Distance_Element o2) {
					if (o1.element > o2.element)
						return 1;
					else if (o1.element == o2.element)
						return 0;
					else
						return -1;
				}
			});
		}
		
		// obtain nearest neighbor matrix with knn neighbors
		KNN_Distance_Element[][] nearestNeighbor = new KNN_Distance_Element[temp.length][knn];
		for (int i=0; i<temp.length; i++) {
			for (int j=0; j<knn; j++) {
				nearestNeighbor[i][j] = temp[i].get(j);
			}
		}
		
		return nearestNeighbor;
	}
	
	// method that obtains the category name from the neighbors. It uses the predicted cluster names derived from K_means_clustering
	private String[][] category_Name_Matrix(KNN_Distance_Element[][] neighbor, List<String> clusterNames) {
		String[][] names = new String[neighbor.length][neighbor[0].length];
		
		for (int i=0; i<neighbor.length; i++) {
			for (int j=0; j<neighbor[0].length; j++) {
				names[i][j] = clusterNames.get(neighbor[i][j].index);
			}
		}
		return names;
	}
	
	// method that converts the names matrix to a hashmap
	private List<HashMap<String, Integer>> convert_matrix_to_hash (String[][] nameMatrix){
		List<HashMap<String, Integer>> tempList = new ArrayList<HashMap<String, Integer>>();
		
		// iterate through matrix to find most frequent name
		for (int i=0; i<nameMatrix.length; i++) {
			// use hashmap to store name and its frequency
			HashMap<String, Integer> temp = new HashMap<String, Integer>();
			for (int j=0; j<nameMatrix[0].length; j++) {
				String key = nameMatrix[i][j];
				if (temp.containsKey(key)) {
					int val = temp.get(key);
					temp.put(key, val+1);
				}
				else
					temp.put(key, 1);
			}
			
			tempList.add(temp);
		}
		return tempList;
	}
	
	// method that selects most frequent category
	private String[] selectName(List<HashMap<String, Integer>> name_frequency_hash) {
		String[]names = new String[name_frequency_hash.size()];
		
		// iterate through matrix to find most frequent name
		for (int i=0; i<name_frequency_hash.size(); i++) {
			
			HashMap<String, Integer> temp = name_frequency_hash.get(i);
			
			// find max
			Map.Entry<String, Integer> max = null;
			for (Map.Entry<String, Integer> entry : temp.entrySet())
			{
			    if (max == null || entry.getValue().compareTo(max.getValue()) > 0)
			    {
			    	max = entry;
			    }
			}
			names[i] = max.getKey();
		}
		return names;
	}
	
	// initialize the actual names list. Obtained using the reader's diligence in reading the unknown set
	private List<String> obtainActualList(){
		List<String> temp = new ArrayList<String>();
		
		temp.add("C1");
		temp.add("C1");
		temp.add("C1");
		temp.add("C1");
		temp.add("C4");
		temp.add("C4");
		temp.add("C7");
		temp.add("C7");
		temp.add("C1");
		temp.add("C4");
		
		return temp;
	}
	
	// print result
	public void printOutput(List<String> predicted, ConfusionMatrix_Evaluation conf) {
		FileHandler files = new FileHandler("./data_unknowns", "readFiles");
		List<String> filenames = files.getFileNameList();
		
		String destination = "./results/knn.txt";
		
		try {
			PrintWriter writer = new PrintWriter(destination,"UTF-8");
			writer.println("----------------KNN RESULTS---------------\n");
			writer.println("predicted categories:");
			for (int i=0; i<filenames.size(); i++) {
				writer.println(filenames.get(i) + ": " + predicted.get(i));
			}
			writer.println();
			
			writer.println("accuracy: " + conf.F1Score);
			
			writer.close();
			
		} catch(Exception e) {System.out.println("ERROR at KNN_Algorithm.printOutput");}
	}
	
	// print fuzzy logic
	private void print_fuzzy_logic(List<HashMap<String, Integer>> name_frequency_hash) {
		
		FileHandler files = new FileHandler("./data_unknowns", "readFiles");
		List<String> filenames = files.getFileNameList();
		
		String destination = "./results/knn_fuzzy.txt";
		
		try {
			PrintWriter writer = new PrintWriter(destination, "UTF-8");
			writer.println("------------FUZZY KNN--------------\n");
			writer.println("fuzzy predicted values:");
			for (int i=0; i<filenames.size(); i++) {
				double sum = 0;
				HashMap<String, Integer> temp = name_frequency_hash.get(i);
				
				// obtain sum of all category frequencies
				for (Map.Entry<String, Integer> entry : temp.entrySet()) {
					sum += entry.getValue();
				}
				
				writer.print(filenames.get(i) + ": ");
				for (Map.Entry<String, Integer> entry : temp.entrySet()) {
					String percent = String.format("%.2f", entry.getValue()/sum*100);
					writer.print(percent + "% " + entry.getKey() + ", ");
				}
				writer.println();
				
			}
			
			writer.close();
		}catch(Exception e) {System.out.println("ERROR - KNN_Algorithm.fuzzy_logic");}
	}
	
	
	// inner class that represents distance element in distance matrix
	private class KNN_Distance_Element{
		public double element;
		public int index;
		
		public KNN_Distance_Element(double element, int index) {
			this.element = element;
			this.index = index;
		}
	}

}




