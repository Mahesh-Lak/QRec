import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class QuestionRecommendation {
	public static void main(String args[]) throws Exception{
		FileReader fileReader = new FileReader("/Users/train.csv");
		CSVFormat csvFileFormat = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);
		Iterable<CSVRecord> csvRecords = csvFileParser.getRecords();
		String inputQuestionIndex = "1002";
		int i;
		List<String> randomId = new ArrayList<>();
		List<String> inputQuestion = new ArrayList<String>();
		Map<String, String> candidateSet = new HashMap<>();
		Map<String, Double> cosineMap = new HashMap<>();
		Map<String, Double> jaccardMap = new HashMap<>();
		randomId.add(inputQuestionIndex);
		for(i = 0; i < 299; i++) {
			Random rand = new Random();
			Integer rId = rand.nextInt(300000);
			if(!randomId.contains(rId.toString()))
				randomId.add(rId.toString());
		}
		for(CSVRecord record : csvRecords){
			/* Fetches input question*/
			if(inputQuestionIndex.equals(record.get("id"))) {
				if(record.get("is_duplicate").equals("1")) {
					String q1 = record.get("question1");
					String q2 = record.get("question2");
					String is_dup = record.get("is_duplicate");
					inputQuestion.add(q1);
					inputQuestion.add(q2);
					inputQuestion.add(is_dup);
				}else {
					System.out.println("Invalid");
				}
			}
			/* Forms the candidate set */
			for(i = 0; i < randomId.size(); i++) {
				if(randomId.get(i).equals(record.get("id"))) {
					candidateSet.put(randomId.get(i), record.get("question2"));
				}
			}
				
		}
		/* Examine Candidate Set */
		for(i = 0; i < candidateSet.size(); i++) {
			String questionOne = stemmingStop(inputQuestion.get(0));
			String questionTwo = stemmingStop(candidateSet.get(randomId.get(i)));
			//Cosine Similarity
			Double cosValue = cosineSimilarity(questionOne, questionTwo);
			cosineMap.put(randomId.get(i), cosValue);
			//Jaccard similarity
			Double jaccardValue = jaccardSimilarity(questionOne, questionTwo);
			jaccardMap.put(randomId.get(i), jaccardValue);
		}
		System.out.println(jaccardMap);
				
	}
	/**
	 * To eliminate stop words and perform stemming
	 *
	 */
	@SuppressWarnings("resource")
	public static String stemmingStop(String term) throws Exception {
	    Analyzer analyzer = new StandardAnalyzer();
	    String resultStemStop = "";
	    TokenStream result = analyzer.tokenStream(null, term);
	    result = new PorterStemFilter(result);
	    result = new StopFilter(result, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	    CharTermAttribute resultAttr = result.addAttribute(CharTermAttribute.class);
	    result.reset();
	    while (result.incrementToken()) {
	    	resultStemStop = resultStemStop + " " + resultAttr.toString();
	    }
	    return resultStemStop;
	}
	/**
	 * Form the Term Frequency Matrix
	 * @param terms
	 * @return
	 */
	public static Map<String, Integer> getTermFrequencyMap(String[] terms) {
        Map<String, Integer> termFrequencyMap = new HashMap<>();
        for (String term : terms) {
            Integer n = termFrequencyMap.get(term);
            n = (n == null) ? 1 : ++n;
            termFrequencyMap.put(term, n);
        }
        return termFrequencyMap;
	}
	 /**
	 * Compute Cosine similarity between two sentences
     * @param text1 
     * @param text2 
     * @return cosine similarity of text1 and text2
     */
    public static double cosineSimilarity(String text1, String text2) {
        //Get vectors
        Map<String, Integer> a = getTermFrequencyMap(text1.split("\\W+"));
        Map<String, Integer> b = getTermFrequencyMap(text2.split("\\W+"));

        //Get unique words from both sequences
        HashSet<String> intersection = new HashSet<>(a.keySet());
        intersection.retainAll(b.keySet());

        double dotProduct = 0, magnitudeA = 0, magnitudeB = 0;

        //Calculate dot product
        for (String item : intersection) {
            dotProduct += a.get(item) * b.get(item);
        }

        //Calculate magnitude a
        for (String k : a.keySet()) {
            magnitudeA += Math.pow(a.get(k), 2);
        }

        //Calculate magnitude b
        for (String k : b.keySet()) {
            magnitudeB += Math.pow(b.get(k), 2);
        }

        //return cosine similarity
        return dotProduct / Math.sqrt(magnitudeA * magnitudeB);
    }
    /**
     * Compute Jaccard Similarity between two sentences
     * @param s1
     * @param s2
     * @return
     */
    public static final double jaccardSimilarity(final String s1, final String s2) {

        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        }

        if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        }

        if (s1.equals(s2)) {
            return 1;
        }

        Map<String, Integer> profile1 = getProfile(s1);
        Map<String, Integer> profile2 = getProfile(s2);

        Set<String> union = new HashSet<String>();
        union.addAll(profile1.keySet());
        union.addAll(profile2.keySet());

        int inter = 0;

        for (String key : union) {
            if (profile1.containsKey(key) && profile2.containsKey(key)) {
                inter++;
            }
        }

        return 2.0 * inter / (profile1.size() + profile2.size());
    }
    
    public static final Map<String, Integer> getProfile(final String string) {
    	int k = 3;
    	final Pattern SPACE_REG = Pattern.compile("\\s+");
        HashMap<String, Integer> shingles = new HashMap<String, Integer>();

        String string_no_space = SPACE_REG.matcher(string).replaceAll(" ");
        for (int i = 0; i < (string_no_space.length() - k + 1); i++) {
            String shingle = string_no_space.substring(i, i + k);
            Integer old = shingles.get(shingle);
            if (old != null) {
                shingles.put(shingle, old + 1);
            } else {
                shingles.put(shingle, 1);
            }
        }

        return Collections.unmodifiableMap(shingles);
    }
    
	
}
