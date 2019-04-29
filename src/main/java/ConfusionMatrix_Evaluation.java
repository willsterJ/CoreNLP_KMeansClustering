import java.util.*;
import java.util.Map.Entry;

public class ConfusionMatrix_Evaluation {
	private DataPoint[] predictedPoints;
	private List<String> actualPointsNames;
	public List<String> predictedPointsNames;
	private List<Integer> actualIndex; // size = k. stores the index of the documents list for each folder
										// (i.e. 0-7 is C1, so index is 7)
	// cell values of the confusion matrix
	int TruePositive = 0;
	int FalseNegative = 0;
	int FalsePositive = 0;
	int TrueNegative = 0;
	double F1Score = 0;

	public ConfusionMatrix_Evaluation(int k, DataPoint[] points, String similarity) {
		this.predictedPoints = points;
		FileHandler rangeFinder = new FileHandler("./data", "extractFileIndices");
		HashMap<String, List<String>> actualDocsHash = rangeFinder.documentIndexHash;
		generateConfusionMatrix(k, actualDocsHash, similarity);
	}
	
	// overloaded constructor that takes in 2 lists. Used in the KNN algorithm
	public ConfusionMatrix_Evaluation(int k, List<String> actualPointsNames, List<String> predictedPointsNames) {
		compareActualandPredicted(actualPointsNames, predictedPointsNames);

	}

	private void generateConfusionMatrix(int k, HashMap<String, List<String>> actualDocsHash, String similarity) {
		// convert actualDocsHash into an arraylist of actual clusters
		// files that are in a folder belong to same cluster (name cluster name with
		// folder name)
		convert_actualDocsHash_to_List(actualDocsHash);
		// classify each cluster in predicted points with a topic (i.e. C1, C4, C7)
		// - convert DataPoint points into an array ot predicted topics
		convert_predictedDataPoints_to_List(k, predictedPoints);
		// compare cluster name between actual and predicted points and calculate F1
		// Score
		compareActualandPredicted(actualPointsNames, predictedPointsNames);

		printResult(similarity);
	}

	// method that converts actual document hashmap (key=actual group, val = list of
	// documents)
	// into actual list of groups in sequence. List looks like this:
	// [C1,C1,...C4,C4,...C7,C7,C7]
	private void convert_actualDocsHash_to_List(HashMap<String, List<String>> actualDocsHash) {
		List<String> actualClusterList = new ArrayList<String>();

		int index = 0;
		actualIndex = new ArrayList<Integer>();

		Iterator<Map.Entry<String, List<String>>> itr = actualDocsHash.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, List<String>> pair = itr.next();
			List<String> docList = pair.getValue();
			for (String s : docList) {
				actualClusterList.add(pair.getKey());
				index++;
			}
			actualIndex.add(index);
		}
		actualPointsNames = actualClusterList;
	}

	// method to convert predicted points to folder name. It achieves this by assigning cluster index with the cluster that has the most count
	private void convert_predictedDataPoints_to_List(int k, DataPoint[] predictedPoints) {
		int actualIndexCount = 0;
		// key = [0...k], value = C1,C4, or C7
		HashMap<Integer, String> clusterIndexToFolderName = new HashMap<Integer, String>();

		int nextStartingIndex = 0;
		// while there is next actual index...
		while (actualIndexCount < actualIndex.size()) {
			int[] clusterCount = new int[k]; // stores the count for each folder name

			// iterate over range dictated by actual index
			for (int i = nextStartingIndex; i < actualIndex.get(actualIndexCount); i++) {
				clusterCount[predictedPoints[i].belongsToWhichCentroid]++; // increment occurrence
			}
			nextStartingIndex = actualIndex.get(actualIndexCount); // starting index for the next loop

			// find max, which will dictate the folder name of the clusters
			int max = Integer.MIN_VALUE;
			int max_index = 0;
			for (int i = 0; i < clusterCount.length; i++) {
				if (clusterCount[i] > max) {
					max = clusterCount[i];
					max_index = i;
				}
			}
			// key = cluster index, value = folder name
			clusterIndexToFolderName.put(max_index, actualPointsNames.get(actualIndex.get(actualIndexCount) - 1));

			actualIndexCount++; // go to next actual Index count
		}
		
		clusterIndexToFolderName = ensureAllFoldersIndexed(clusterIndexToFolderName);

		predictedPointsNames = new ArrayList<String>();
		for (int i = 0; i < predictedPoints.length; i++) {
			// assign folder names to the predicted point
			predictedPointsNames.add(clusterIndexToFolderName.get(predictedPoints[i].belongsToWhichCentroid));
		}

	}

	// ensures all cluster indices are assigned a folder name
	private HashMap<Integer, String> ensureAllFoldersIndexed(HashMap<Integer, String> clusterIndexToFolderName) {
		HashSet<String> Used = new HashSet<String>();
		HashSet<String> Unused = new HashSet<String>();

		for (int i = 0; i < actualIndex.size(); i++) {
			if (clusterIndexToFolderName.containsKey(i))
				Used.add(clusterIndexToFolderName.get(i));
		}
		if (!Used.contains("C1"))
			Unused.add("C1");
		if (!Used.contains("C4"))
			Unused.add("C4");
		if (!Used.contains("C4"))
			Unused.add("C4");

		for (int i = 0; i < actualIndex.size(); i++) {
			if (!clusterIndexToFolderName.containsKey(i)) {
				if (Unused.contains("C1")) {
					clusterIndexToFolderName.put(i, "C1");
					Unused.remove("C1");
					Used.add("C1");
				} else if (Unused.contains("C4")) {
					clusterIndexToFolderName.put(i, "C4");
					Unused.remove("C4");
					Used.add("C4");
				} else if (Unused.contains("C4")) {
					clusterIndexToFolderName.put(i, "C4");
					Unused.remove("C4");
					Used.add("C4");
				}
			}
		}
		return clusterIndexToFolderName;
	}

	// method to find TruePositives, FalseNegatives, FalsePositives, and
	// TrueNegatives
	private void compareActualandPredicted(List<String> actualPointsNames, List<String>predictedPointsNames) {
		// prepare the confusion matrix with actual rows (C1, C4, C7) and predicted
		// column (C1 C4 C7)
		// use hashmap to count the occurrences
		HashMap<String, Integer> actualC1 = new LinkedHashMap<String, Integer>();
		HashMap<String, Integer> actualC4 = new LinkedHashMap<String, Integer>();
		HashMap<String, Integer> actualC7 = new LinkedHashMap<String, Integer>();

		// initialize hashmap
		actualC1.put("C1", 0);
		actualC1.put("C4", 0);
		actualC1.put("C7", 0);
		actualC4.put("C1", 0);
		actualC4.put("C4", 0);
		actualC4.put("C7", 0);
		actualC7.put("C1", 0);
		actualC7.put("C4", 0);
		actualC7.put("C7", 0);

		// increment occurrences
		for (int i = 0; i < actualPointsNames.size(); i++) {
			if (actualPointsNames.get(i) == null || predictedPointsNames.get(i) == null)
				continue;
			else if (actualPointsNames.get(i).equals("C1") && predictedPointsNames.get(i).equals("C1"))
				actualC1 = checkHash(actualC1, "C1");
			else if (actualPointsNames.get(i).equals("C1") && predictedPointsNames.get(i).equals("C4"))
				actualC1 = checkHash(actualC1, "C4");
			else if (actualPointsNames.get(i).equals("C1") && predictedPointsNames.get(i).equals("C7"))
				actualC1 = checkHash(actualC1, "C7");
			else if (actualPointsNames.get(i).equals("C4") && predictedPointsNames.get(i).equals("C1"))
				actualC4 = checkHash(actualC4, "C1");
			else if (actualPointsNames.get(i).equals("C4") && predictedPointsNames.get(i).equals("C4"))
				actualC4 = checkHash(actualC4, "C4");
			else if (actualPointsNames.get(i).equals("C4") && predictedPointsNames.get(i).equals("C7"))
				actualC4 = checkHash(actualC4, "C7");
			else if (actualPointsNames.get(i).equals("C7") && predictedPointsNames.get(i).equals("C1"))
				actualC7 = checkHash(actualC7, "C1");
			else if (actualPointsNames.get(i).equals("C7") && predictedPointsNames.get(i).equals("C4"))
				actualC7 = checkHash(actualC7, "C4");
			else if (actualPointsNames.get(i).equals("C7") && predictedPointsNames.get(i).equals("C7"))
				actualC7 = checkHash(actualC7, "C7");
		}

		// store the confusion hashmap as a 2D array
		int[][] arr = new int[3][3];
		arr[0][0] = actualC1.get("C1");
		arr[0][1] = actualC1.get("C4");
		arr[0][2] = actualC1.get("C7");
		arr[1][0] = actualC4.get("C1");
		arr[1][1] = actualC4.get("C4");
		arr[1][2] = actualC4.get("C7");
		arr[2][0] = actualC7.get("C1");
		arr[2][1] = actualC7.get("C4");
		arr[2][2] = actualC7.get("C7");

		// find row sums and column sums
		double[] columnSum = new double[3];
		double[] rowSum = new double[3];

		for (int i = 0; i < 3; i++) {
			int cSum = 0;
			int rSum = 0;
			for (int j = 0; j < 3; j++) {
				cSum += arr[j][i];
				rSum += arr[i][j];
			}
			columnSum[i] = cSum;
			rowSum[i] = rSum;
		}

		// calculate precision and recall
		double precisionC1 = arr[0][0] / columnSum[0];
		double precisionC4 = arr[1][1] / columnSum[1];
		double precisionC7 = arr[2][2] / columnSum[2];
		double recallC1 = arr[0][0] / rowSum[0];
		double recallC4 = arr[1][1] / rowSum[1];
		double recallC7 = arr[2][2] / rowSum[2];

		double avgprecision = (precisionC1 + precisionC4 + precisionC7) / 3;
		double avgrecall = (recallC1 + recallC4 + recallC7) / 3;

		// calculate f1score
		calculateF1Score(avgprecision, avgrecall);
	}

	private HashMap<String, Integer> checkHash(HashMap<String, Integer> hash, String s) {
		if (hash.containsKey(s)) {
			int val = hash.get(s) + 1;
			hash.put(s, val);
		}
		return hash;
	}

	// method to calculate F1 Score
	private void calculateF1Score(double precision, double recall) {

		F1Score = 2 * (precision * recall) / (precision + recall);

	}

	// method to print out the confusion matrix
	public void printResult(String similarity) {
		// System.out.println("\t\t Predicted NO" + " Predicted YES");
		// System.out.print("Actual NO" + "\t\t" + TrueNegative + "\t\t" + FalsePositive
		// + "\n");
		// System.out.print("Actual YES" + "\t\t" + FalseNegative + "\t\t" +
		// TruePositive + "\n");
		System.out.println("\n" + similarity + " F1 Score: " + F1Score);
	}

}
