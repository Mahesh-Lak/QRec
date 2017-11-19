import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

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
		Map<String, String> questionTwo = new HashMap<>();
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
					questionTwo.put(randomId.get(i), record.get("question2"));
				}
			}
				
		}
				
	}
	
}
