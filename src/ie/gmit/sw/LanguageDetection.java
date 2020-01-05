package ie.gmit.sw;

import java.util.Map;
import java.util.TreeMap;

// Taken from the 'second half' of the Parser class found in this video: https://web.microsoftstream.com/video/c12997b2-2e7f-4047-8763-b73d0ac0712c?referrer=https:%2F%2Flearnonline.gmit.ie%2Fcourse%2Fview.php%3Fid%3D945
public class LanguageDetection implements Runnable {
	private RealDatabase database;
	private int k;
	private boolean keepRunning = true;
	
	public LanguageDetection(RealDatabase database, int k) {
		super();
		
		this.database = database;
		this.k = k;
	}

	@Override
	public void run() {
		while (keepRunning) {
			try {
				System.out.println("Assigning job from queue...");
				
				Job languageDetection = ServiceHandler.blockingQueue.take();
//				Job addTask = new Job(languageDetection.getTask(), analyseQuery(languageDetection.getQueryText()).name());
//				
//				ServiceHandler.outQueue.add(addTask);
				
				ServiceHandler.outQueue.put(languageDetection.getTask(), analyseQuery(languageDetection.getQueryText()).name());
				
				System.out.println("Done");
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}
	
	public Language analyseQuery(String queryText) {
		System.out.println("Analysing query text...");
		
		Map<Integer, LanguageEntry> queryMap = new TreeMap<>();
		int kmer, frequency = 1;

		for (int i = 0; i <= queryText.length() - k; i++) {
			CharSequence query = queryText.substring(i, i + k);
			kmer = query.hashCode();

			if (queryMap.containsKey(kmer)) {
				frequency += queryMap.get(kmer).getFrequency();
			}

			queryMap.put(kmer, new LanguageEntry(kmer, frequency));
		}

		System.out.println("Done");
//		System.out.println(database.getLanguage(queryMap));
		return database.getLanguage(queryMap);
	}
}
