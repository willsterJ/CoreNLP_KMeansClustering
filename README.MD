This project implements NLP and K-means clustering algorithm. It first reads .txt files from a data folder,
applies Stanford CoreNLP to parse and extract named entities from the files, and apply algorithms
to cluster the documents in an attempt to identify them. 

This is a Maven project: all dependencies are handled by pom.xml

Here are the following classes and a brief summary of what they do:
1. Default.java: main class that runs the program
2. FileHandler.java: Handles all file-related tasks. These include reading from the dataset,
	reading the extracted topics, and writing to topics.txt
3. CoreNLPHandler.java: implements Stanford CoreNLP to extract the documents in the form of tokens
4. STOPWORDS.java: reads from STOPWORDS.txt to process the list of stop words. To be used by 
   CoreNLPHandler
5. NGram.java: Applies the sliding window algorithm on the tokens to find all possible combinations 
	of subsequent words of length n (3 will be used by default). 
6. MatrixHandler.java: handles all matrix-related tasks. These include generating the document-term
	matrix, the tf-idf matrix, and the PCA with dimension 2 matrix.
7. K_Means_Clustering.java: Contains both Euclidean and Cosine Similarity K-means algorithms
8. ConfusionMatrix.java: outputs the F1-score by first creating a confusion matrix
9. Visualization.java: outputs the datapoints on a scatter plot 
 

To run the project,
Instructions:
- TO RUN IN ECLIPSE:
	1. Open Eclipse
	2. Click File -> Import
	3. General -> Projects from Folder or Archive
	4. To the right of import source, select Archive
	5. select the .zip file
	6. click Finish
	7. Run
	
- TO RUN IN LINUX COMMAND:
	1. extract the zip file
	2. cd ... to the root folder core-nlp-text-processing
	3. make sure you have installed maven
	3. type the following commands:
		i. mvn clean install
		ii. mvn package
		iii. mvn exec:java