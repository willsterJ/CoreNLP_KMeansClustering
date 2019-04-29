import java.util.*;

import javax.swing.*;
import org.apache.log4j.BasicConfigurator;

import edu.stanford.nlp.util.logging.RedwoodConfiguration;

public class Default {

	public static void main(String[] args) {
		BasicConfigurator.configure(); // setup for Stanford CoreNLP to work
		RedwoodConfiguration.current().clear().apply();
		
		System.out.println("------------------------------------Kmeans Clustering-------------------------------");
		
		// choose t he max n-gram level (i.e. if n-gram=3, it will process n-grams
		// 3,2,1)
		int ngram_level = 4;
		int relevancy_Cutoff = 10; // cutoff point for each topic's frequency (i.e. if 5, remove all topics that
									// occur fewer than 5 times)

		// generates document-term matrix with parameters ngram and cutoff
		// MatrixHandler will use coreNLP and NGram handlers to process the data
		new MatrixHandler(ngram_level, relevancy_Cutoff, "./data", true);
		int[][] matrix = MatrixHandler.matrix;

		// overloaded MatrixHandler will take in document-term matrix and generate
		// tf-idf matrix
		new MatrixHandler(matrix);
		double[][] tf_idfMatrix = MatrixHandler.tf_idfMatrix;

		int k = 3;
		// use K-Means Algorithm for both euclidean and cosine similarity
		K_Means_Clustering kmeans_euclidean = new K_Means_Clustering(k, tf_idfMatrix, "euclidean");
		K_Means_Clustering kmeans_cosine = new K_Means_Clustering(k, tf_idfMatrix, "cosine");

		// extract DataPoints[] which holds cluster data in the form on index (i.e.
		// clusters are labeled 0,1,2)
		DataPoint[] euclideanpoints = kmeans_euclidean.points;
		DataPoint[] cosinepoints = kmeans_cosine.points;
		// use the confusion matrix to obtain the F1-score and label cluster index with
		// their respective folder names
		ConfusionMatrix_Evaluation confusionEuclidean = new ConfusionMatrix_Evaluation(k, euclideanpoints, "euclidean");
		ConfusionMatrix_Evaluation confusionCosine = new ConfusionMatrix_Evaluation(k, cosinepoints, "cosine");

		MatrixHandler PCA = new MatrixHandler(tf_idfMatrix, "applyPCA");
		double[][] PCAMatrix = PCA.PCAMatrix;

		List<String> euclideanClusterNames = confusionEuclidean.predictedPointsNames;
		List<String> cosineClusterNames = confusionCosine.predictedPointsNames;

		/*
		// Visualizations...
		Visualization plot1 = new Visualization("Euclidean Scatter Chart", PCAMatrix, k, euclideanClusterNames);
		plot1.setSize(1000, 1000);
		plot1.setLocationRelativeTo(null);
		plot1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		plot1.setVisible(true);

		Visualization plot2 = new Visualization("Cosine Scatter Chart", PCAMatrix, k, cosineClusterNames);
		plot2.setSize(1000, 1000);
		plot2.setLocationRelativeTo(null);
		plot2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		plot2.setVisible(true);
		*/
		
		/* KNN ALGORITHM -------------------------------------------------
		 * Given an unknown dataset, find k nearest neighbor and label that data appropriately
		*/
		System.out.println("------------------------------------END-------------------------------------");
		System.out.println("------------------------------KNN ALGORITHM--------------------------------");
		
		int knn = 3;
		KNN_Algorithm knn_algorithm = new KNN_Algorithm(k, knn, tf_idfMatrix, euclideanClusterNames, ngram_level, relevancy_Cutoff, "./data_unknowns");
		
		System.out.println("----------------------------------END---------------------------------------");
		
	}

}
