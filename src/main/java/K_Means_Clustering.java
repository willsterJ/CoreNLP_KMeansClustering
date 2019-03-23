/*
 * Class that applies the k-means clustering algorithm.
 * Inputs: k number of clusters, and tf-idf variant document-term matrix 
 * The matrix is then translated into Class:DataPoints objects to be used in the k-mean algorithm
 */
import java.util.*;
import java.util.Map.Entry;

public class K_Means_Clustering {
	
	private int k;	// k cluster groups to be specified
	private double[][] centroids;	// matrix storing centroids
	public DataPoint[] points;		// points to be obtained from document-term matrix
	private int document_size;
	private int component_size;
	
	public K_Means_Clustering(int k, double[][] matrix, String similarity) {
		this.k = k;
		this.document_size = matrix.length;
		this.component_size = matrix[0].length;
		
		selectCentroids(matrix);	// first select k centroids from rows of documents
		points = new DataPoint[document_size];	// initialize DataPoints array for each row
		turnRowsIntoDataPoints(matrix);	// translate matrix rows into points
		k_means_cluster_algorithm(similarity);	// apply k-means clustering
		
	}
	
	// method to find k centroids from data matrix
	private void selectCentroids(double[][] matrix) {
		/*
		// use random points
		centroids = new double[k][component_size]; 	// initialize centroids matrix
		Set<double[]> prevChosen = new HashSet<double[]>(); // store previously chosen points so that they don't repeat
		Random rand = new Random();
		
		int n=0;
		while(n<k) {
			int randnum = rand.nextInt(document_size); // generate random num [0... num of docs]
			if (!prevChosen.contains(matrix[randnum])) {	// if not previously chosen, add to Set
				prevChosen.add(matrix[randnum]);	
				centroids[n] = matrix[randnum];		// add to centroids
				System.out.println(randnum);
				n++;
			}
		}
		*/
		
		centroids = new double[k][component_size];
		centroids[0] = matrix[14];
		centroids[1] = matrix[20];
		centroids[2] = matrix[2];
		
	}
	// method to convert 2-D matrix into datapoints array (i.e. for better legibility)
	private void turnRowsIntoDataPoints(double[][] matrix) {
		for (int i=0; i<matrix.length; i++) {
			points[i] = new DataPoint(matrix[i]);
		}
	}
	
	// method that implements the k-means clustering algorithm
	public void k_means_cluster_algorithm(String similarity){
		double n=100; // arbitrary value
		double threshold = 0.5;
		
		while (n > threshold) {
			double[][]prevCentroids = deepCopyArray(centroids);	// make a deep copy of centroids matrix
			
			switch (similarity.toLowerCase()) {
			// calculate Euclidean distance between all points and all centroids.
			// the distance is stored in each DataPoint's centroid hash map.
			case "euclidean": EuclideanDistance_algorithm(); break;
			// calculate cosine similarity between all points and all centroids
			case "cosine": CosineSimilarity_algorithm(); threshold = 0.9; break;
			default: System.out.println("wrong similarity"); return;
			}
			// create a hash map for the clusters (i.e. key = 0 represents all points in the first cluster).
			// each point will be grouped according to their closest cluster group
			HashMap<Integer, List<DataPoint>> clusterHash = updatePointsClosestCentroids();
			// after new k group for each point has been determined, calculate new centroids
			// by finding the average of all points in each k group
			findNewCentroids(clusterHash);
			// set the convergence value (to be maximum distance between previous and current centroid locations).
			n = getConvergence(prevCentroids, centroids);
		}
	}
	
	// method to deep copy. Used to make a copy of centroids
	private double[][] deepCopyArray(double[][] centroidarr){
		double[][]temp = new double[k][component_size];
		for (int i=0; i<k; i++) {
			for (int j=0; j<component_size; j++) {
				temp[i][j] = centroidarr[i][j];
			}
		}
		return temp;
	}
	
	// method to calculate the Euclidean distance between all points and all centroids
	private void EuclideanDistance_algorithm() {
		for (int i=0; i<centroids.length; i++) {	// for each centroid...
			for (int j=0; j<points.length; j++) {	// for each point...
				// add the distance to centroidHash in each DataPoint
				points[j].centroidHash.put(i, CalculateEuclideanDistance(centroids[i], points[j]));
			}
		}
	}
	// method to calculate Euclidean distance between 2 points
	private double CalculateEuclideanDistance(double[] vectorA, DataPoint vectorB) {
		double sum = 0;
		for (int k=0; k<component_size; k++) {
			sum += Math.pow(vectorA[k] - vectorB.components[k], 2);
		}
		sum = Math.sqrt(sum);
		
		return sum;
	}
	
	// method to calculate the cosine similarity between all points and all centroids
	private void CosineSimilarity_algorithm() {
		for (int i=0; i<centroids.length; i++) {	// for each centroid...
			for (int j=0; j<points.length; j++) {	// for each point...
				double dotProduct=0;
				double normA=0;
				double normB=0;
				for (int k=0; k<component_size; k++) {	// for each component...
					dotProduct += centroids[i][k] * points[j].components[k];
					normA += Math.pow(centroids[i][k], 2);
					normB += Math.pow(points[j].components[k], 2);
				}
				double result = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
				points[j].centroidHash.put(i, result);
			}
		}
	}
	
	// method to update each point's closest centroid. 
	// Returns a hash map of clusters each with list of points.
	private HashMap<Integer, List<DataPoint>> updatePointsClosestCentroids() {
		// set up a cluster hash with key=centroid index, value=list of points belonging to cluster
		HashMap<Integer,List<DataPoint>> clusterHash = new HashMap<Integer,List<DataPoint>>();
		// initialize hash and value lists
		for (int i=0; i<centroids.length; i++) {
			clusterHash.put(i, new ArrayList<DataPoint>());
		}
		// add each point to its respective cluster hash
		for (int i=0; i<points.length;i++) {
			points[i].choose_closest_centroid(); // resolve closest centroid with DataPoint method
			List<DataPoint> temp = clusterHash.get(points[i].belongsToWhichCentroid); // get list from hash
			temp.add(points[i]); // add to that list
			clusterHash.put(points[i].belongsToWhichCentroid, temp);	// update hash
		}
		return clusterHash;
	}
	
	// method to find new centroids after similarity or distance algorithms
	// uses the average of all points in that cluster
	private void findNewCentroids(HashMap<Integer, List<DataPoint>> clusterHash) {
		// iterate through the cluster hash
	    Iterator<Map.Entry<Integer, List<DataPoint>>> itr = clusterHash.entrySet().iterator();
	    while (itr.hasNext()) {
	        Entry<Integer, List<DataPoint>> pair = itr.next();
	        List<DataPoint> pointslist = pair.getValue(); 	// get list of points for each cluster
	        double components[] = new double[component_size];	// initialize component accumulator

	        for (int i=0; i<pointslist.size(); i++) {	// for each point in the cluster list...
	        	for (int j=0; j<component_size; j++) {	// for each component
	        		components[j] += pointslist.get(i).components[j];	// add each component
	        	}
	        }
	        
	        for (int i=0; i<component_size; i++) {
	        	components[i] = components[i]/pointslist.size();	// find the average of all points in cluster
	        }
	        
	        centroids[pair.getKey()] = components;	// set the new centroids
	        
	        // now find new centroid as one of the closest points
	        HashSet<Integer> usedPoint = new HashSet<Integer>(); // hash set to keep track of previous points that were set as centroids
	        for (int i=0; i<centroids.length; i++) {	// for each centroid...
	        	double min = Double.MAX_VALUE;
	        	int min_index = 0;
	        	for (int j=0; j<points.length; j++) {	// for each point...
	        		if (!usedPoint.contains(j)) {	// find minimum distance
	        			double distance = CalculateEuclideanDistance(centroids[i], points[j]);
	        			if (distance < min) {
	        				min = distance;
	        				min_index = j;
	        			}
	        		}
	        	}
	        	centroids[i] = points[min_index].components; // set centroid to closest point
	        	usedPoint.add(min_index); // add that point to set of used points
	        }
	        
	    }
	}
	
	// method that calculates the distance between previous and current centroids.
	// it uses Euclidean distance to find the maximum of such distance
	private double getConvergence(double[][] prev, double[][] current) {
		double max_difference = Double.MIN_VALUE;
		for (int i=0; i<k; i++) {	// ... for each centroid...
			double sum=0;
			for (int j=0; j<component_size; j++) {	// for each component
				sum += (Double)Math.pow((prev[i][j]-current[i][j]), 2);	// add the square of differences
			}
			sum = (Double)Math.sqrt(sum);
			if (sum > max_difference) {	// update maximum
				max_difference = sum;
			}
		}
		return max_difference;
	}
}

/*----------------------------------------------------------------------------------------------------
 * Class of DataPoint. It is a representation of each document that contains the component array
 * and a hash map to determine which k-group each point belongs to.
 * It has a method that updates a point's closest centroid
 */

class DataPoint{
	public double[] components;
	public HashMap<Integer, Double> centroidHash;	// hash with key = centroid index, value = distance or similarity to centroid
	public int belongsToWhichCentroid;
	
	public DataPoint(double[] coords) {
		this.components = coords;
		this.centroidHash = new HashMap<Integer,Double>();
	}
	// method to update point's closest centroid. It updates centroidHash
	public void choose_closest_centroid() {
		double min = Double.MAX_VALUE;
		int min_Index=0;
		for (int i=0; i<centroidHash.size(); i++) {	// find minimum distance to centroids
			if (centroidHash.get(i) < min) {
				min = centroidHash.get(i);
				min_Index = i;
			}
		}
		this.belongsToWhichCentroid = min_Index;	// set it to centroid index
	}
}