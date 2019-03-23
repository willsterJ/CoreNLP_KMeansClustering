import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.linear.*;

public class MatrixHandler {
	private List<HashMap<String, Integer>> documentTopics; // list that stores topics for each document
	private List<String> datasetTopics; // list that stores entire data set topics

	public static int[][] matrix; // document-term matrix
	public static double[][] tf_idfMatrix;	// tf-idf matrix
	public static double[][] PCAMatrix;

	// constructor with input = n-gram-level, generate matrix
	public MatrixHandler(int ngram_level, int relevancy_Cutoff) {
		documentTopics = read_NLP_WriteTopics(ngram_level, relevancy_Cutoff); // list to store topics of each document
		// read ngramTopic.txt and get list of topics
		datasetTopics = readListTopics();
		matrix = generateMatrix(datasetTopics, documentTopics);
	}

	// overloaded constructor with input = matrix, output = tf_idf matrix
	public MatrixHandler(int[][] matrix) {
		tf_idfMatrix = performtTf_idf(matrix);
	}
	
	public MatrixHandler(double[][] matrix, String command) {
		if (command.equals("applyPCA")) {
			PCAMatrix = applyPCA(matrix);
		}
	}

	// method that reads input files, apply NLP, and write the topics on a file
	// ngramTopics.txt
	public static List<HashMap<String, Integer>> read_NLP_WriteTopics(int n, int relevancy_Cutoff) {
		String dataDirectory = "./data"; // starting directory data folder
		FileHandler inputFiles = new FileHandler(dataDirectory, "readFiles"); // scours the folder to find all files
		List<String> fileNames = inputFiles.getFileNameList(); // get the file paths and store in arraylist

		// apply NLP, find and sort the local and dataset n-grams, and store dataset
		// topics
		CoreNLPHandler nlphandler;
		List<HashMap<String, Integer>> documentTopics = new ArrayList<HashMap<String, Integer>>();
		for (String file : fileNames) {
			nlphandler = new CoreNLPHandler(file); // call CoreNLPHandler
			NGram ngram = new NGram(n, nlphandler.getOutputList()); // generate nGram
			ngram.setDocumentHash(ngram.sortByValue(ngram.getDocumentHash())); // sort the local hashmap
			HashMap<String, Integer> localHash = ngram.getDocumentHash();
			documentTopics.add(localHash); // add local hashmap to Topics arrayList
		}
		NGram.eliminateByValueLessThan(relevancy_Cutoff); // eliminate topics with low frequency from entire dataset
		NGram.sortAllByValue(); // sort the hash map of entire dataset
		NGram.printTopics();

		FileHandler writeTopics = new FileHandler(NGram.hashTable, "topics.txt"); // write entire dataset topics to file

		return documentTopics;
	}

	// method to read ngram topics file and return the array list of topics sorted
	public static ArrayList<String> readListTopics() {
		FileHandler topicList = new FileHandler("topics.txt", "readTopics");
		return (ArrayList<String>) topicList.getTopicList();
	}

	// method to generate a document-term matrix and returns it
	public static int[][] generateMatrix(List<String> dataSetTopics, List<HashMap<String, Integer>> documentTopics) {
		// turn this into a matrix
		int rowSize = documentTopics.size();
		int columnSize = dataSetTopics.size();
		int[][] matrix = new int[rowSize][columnSize]; // initialize matrix
		for (int i = 0; i < rowSize; i++) {
			for (int j = 0; j < columnSize; j++) {
				String topic = dataSetTopics.get(j);
				if (documentTopics.get(i).containsKey(topic)) // if topic exists in the document
					matrix[i][j] = documentTopics.get(i).get(topic); // ... set the value in the matrix entry
				else // if topic does not exist in the document
					matrix[i][j] = 0;
			}
		}
		return matrix;
	}

	// method that takes in document-term matrix and outputs tf-idf variant of it
	private double[][] performtTf_idf(int[][] matrix) {
		// count total words per document and store in array
		double[] documentwordcountArray = new double[matrix.length]; // word count for each document
		for (int i = 0; i < matrix.length; i++) {

			int documentwordcount = 0;
			for (int j = 0; j < matrix[i].length; j++) {
				documentwordcount += matrix[i][j];
			}
			documentwordcountArray[i] = documentwordcount;
		}

		// count occurrence in all documents per word.
		double[] inverseDocumentFrequency = new double[matrix[0].length];
		for (int j = 0; j < matrix[0].length; j++) {
			int appearance = 0;
			for (int i = 0; i < matrix.length; i++) {
				if (matrix[i][j] != 0) {
					appearance++;
				}
			}
			inverseDocumentFrequency[j] = (double) Math.log(matrix.length / appearance);
		}

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
	
	
	// method to apply PCA. Used to reduce dimensionality
	private double[][] applyPCA(double[][] matrix) {
		RealMatrix M = new Array2DRowRealMatrix(matrix);  // convert 2d array to RealMatrix (Apache Math)
		RealMatrix Mt = M.transpose();
		RealMatrix SquareM = Mt.multiply(M);  // obtain the square matrix
				
		EigenDecomposition E  = new EigenDecomposition(SquareM);  // prepare eigenvalue decomposition
		//System.out.println(E.getRealEigenvalue(0));

		// get first 2 eigenvectors. They are sorted by their respective eigenvalues by default
		RealVector e1 = E.getEigenvector(0);
		RealVector e2 = E.getEigenvector(1);
		
		// create EigenMatrix out of the eigenvectors
		RealMatrix EMatrix = new Array2DRowRealMatrix(e1.getDimension(), 2);
		EMatrix.setColumnVector(0, e1);
		EMatrix.setColumnVector(1, e2);

		// multiply original matrix to the EigenMatrix
		RealMatrix result = M.multiply(EMatrix);
				
		return result.getData();	// return result
		
	}

	// Getters and Setters
	public List<HashMap<String, Integer>> getDocumentTopics() {
		return documentTopics;
	}

	public void setDocumentTopics(List<HashMap<String, Integer>> documentTopics) {
		this.documentTopics = documentTopics;
	}

	public List<String> getDatasetTopics() {
		return datasetTopics;
	}

	public void setDatasetTopics(List<String> datasetTopics) {
		this.datasetTopics = datasetTopics;
	}
}
