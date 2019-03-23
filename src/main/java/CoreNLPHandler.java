/*
 * Class that handles the CoreNLP aspects of the code. The constructor receives
 * a filename string path as input and uses the Stanford CoreNLP library to parse
 * the string into tokens.
 * 
 * Class uses: STOPWORDS.java to remove all the stop words before the string is fed
 * to the NLP processor
 */
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class CoreNLPHandler {
	private ArrayList<String> outputList; // contains list of words
	private ArrayList<CoreLabel> outputtokenList;
	//public static ArrayList<String> testList;
	
	public CoreNLPHandler(String filename) {
		String text = readFiles(filename);
		text = text.replaceAll("[^a-zA-Z ]", "");	// remove punctuations from string
		_doStanfordNLP(text);
		combineSameNER();
	}

	// method to read files and return string of the .txt file
	private String readFiles(String filename) {
		Scanner input;
		String text = "";
		try {
			input = new Scanner(new FileReader(filename));
			while(input.hasNext()) {
				text = text.concat(input.next() + " ");
			}
		}
		catch(IOException e){
			System.out.println("File does not exist!!");
			System.exit(0);
		}
		return text;
	}
	
	private void _doStanfordNLP(String text) {
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
        // NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos,lemma,ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // create an empty Annotation just with the given text
        //Annotation document = new Annotation(text);	// this is from the tutorial. However, to get NER to work use CoreDocument
        CoreDocument document = new CoreDocument(text);
        // run all Annotators on this text
        pipeline.annotate(document);
        
        // these are all the sentences in this document      document.annotation().get(key) == document.get(key) if using Annotation
        //List<CoreMap> sentences = document.annotation().get(SentencesAnnotation.class);

        //List<String> words = new ArrayList<String>();
        //List<String> nerTags = new ArrayList<String>();
        List<CoreLabel> tokenList = new ArrayList<CoreLabel>();
        //List<String> posTags = new ArrayList<String>();
        
        STOPWORDS STAWP = new STOPWORDS("./STOPWORDS.txt");	// stop word handler

        for (CoreSentence sentence : document.sentences()) {
            // traversing the words in the current sentence
            for (CoreLabel token : sentence.tokens()) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                // remove tokens with words that belong in the STOPWORD HashSet
                if (STAWP.checkWord(word) == false) {
                	//words.add(word);
                	tokenList.add(token);
                }
                else {	// if it's a stop word, skip to next loop iteration
                	continue;
                }
                
                /*
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);
                if (!ne.equals("O")) {
                	//System.out.printf("%s ",ne);
                }
                nerTags.add(ne);
                */
            }
		
        }
        
        //outputList = (ArrayList<String>) words;
        outputtokenList = (ArrayList<CoreLabel>) tokenList; // set final output token list
        //System.out.println(nerTags.toString());
		
	}
	
	// method that combines tokens of the same NER
	private void combineSameNER() {
		ArrayList<String> newList = new ArrayList<String>();
		String prevNER = "";	// previous NER tag
		String ss = "";		// accumulator string for combination
		
		// create empty token and add to list so that the for-loop can look one iteration ahead
		CoreLabel empty = new CoreLabel();
		empty.set(TextAnnotation.class, " ");
		empty.set(NamedEntityTagAnnotation.class, " ");
		outputtokenList.add(empty);
		
		for (int i=0; i<outputtokenList.size(); i++) {
			String s = outputtokenList.get(i).get(TextAnnotation.class);
			String ner = outputtokenList.get(i).get(NamedEntityTagAnnotation.class);
			// if ner is O (i.e. regular word), add it to list
			if (ner.equals("O")) {
				newList.add(s);
				prevNER = "O";
			}
			// for entity words, concatenate same entity words to form new word. Then add it to newList
			else {
				if (ner.equals(prevNER)){
					ss = ss.concat("_" + s);
					// if next words has a different ner, end ss accumulation and add to list
					if (!ner.equals(outputtokenList.get(i+1).get(NamedEntityTagAnnotation.class))) {
						newList.add(ss);
					}
				}
				// begin new entity word if new ner is encountered
				else {
					ss = s;
				}
				prevNER = ner;	// advance ner tag pointer
			}
		}
		outputList = newList; // set the output list
	}
	
	
	// Getters and Setters
	public ArrayList<String> getOutputList() {
		return outputList;
	}

	public void setOutputList(ArrayList<String> outputList) {
		this.outputList = outputList;
	}
	
	public String toString() {
		return outputList.toString();
	}
}
