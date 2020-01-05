package ie.gmit.sw;

import java.util.*;
import java.util.concurrent.*;

// Implementation of Singleton design pattern
public class RealDatabase implements Database {
	// Maps languages to their k-mers and frequency of occurrence
	private Map<Language, Map<Integer, LanguageEntry>> db = new ConcurrentHashMap<>();
	private volatile static RealDatabase database;

	private RealDatabase() {
		
	}
	
	public static RealDatabase getInstance() {
		if (database == null) {
			synchronized (RealDatabase.class) {
				if (database == null) {
					return database = new RealDatabase();
				}
			}
		}
		
		return database;
	}
	
	@Override
	public void add(CharSequence s, Language lang) {
		int kmer = s.hashCode();
		Map<Integer, LanguageEntry> langDb = getLanguageEntries(lang);

		int frequency = 1;

		if (langDb.containsKey(kmer)) {
			frequency += langDb.get(kmer).getFrequency();
		}

		langDb.put(kmer, new LanguageEntry(kmer, frequency));
	}

	private Map<Integer, LanguageEntry> getLanguageEntries(Language lang) {
		Map<Integer, LanguageEntry> langDb = null;

		if (db.containsKey(lang)) {
			langDb = db.get(lang);
		} else {
			langDb = new TreeMap<Integer, LanguageEntry>();

			db.put(lang, langDb);
		}

		return langDb;
	}

	@Override
	public void resize(int max) {
		Set<Language> keys = db.keySet();

		for (Language lang : keys) {
			Map<Integer, LanguageEntry> top = getTop(max, lang);
			db.put(lang, top);
		}
	}
	
	@Override
	public Map<Integer, LanguageEntry> getTop(int max, Language lang) {
		Map<Integer, LanguageEntry> temp = new TreeMap<>();
		List<LanguageEntry> les = new ArrayList<>(db.get(lang).values());
		Collections.sort(les);

		int rank = 1;

		for (LanguageEntry le : les) {
			le.setRank(rank);
			temp.put(le.getKmer(), le);

			if (rank == max)
				break;
			rank++;
		}

		return temp;
	}

	@Override
	public Language getLanguage(Map<Integer, LanguageEntry> query) {
		TreeSet<OutOfPlaceMetric> oopm = new TreeSet<>();

		Set<Language> langs = db.keySet();

		for (Language lang : langs) {
			oopm.add(new OutOfPlaceMetric(lang, getOutOfPlaceDistance(query, db.get(lang))));
		}
		
		return oopm.first().getLanguage();
	}
	
	private int getOutOfPlaceDistance(Map<Integer, LanguageEntry> query, Map<Integer, LanguageEntry> subject) {
		int distance = 0;

		Set<LanguageEntry> les = new TreeSet<>(query.values());

		for (LanguageEntry q : les) {
			LanguageEntry s = subject.get(q.getKmer());

			if (s == null) {
				distance += subject.size() + 1;
			} else {
				distance += s.getRank() - q.getRank();
			}
		}

		return distance;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		int langCount = 0;
		int kmerCount = 0;

		Set<Language> keys = db.keySet();

		for (Language lang : keys) {
			langCount++;
			stringBuilder.append(lang.name() + "->\n");

			Collection<LanguageEntry> m = new TreeSet<>(db.get(lang).values());
			kmerCount += m.size();

			for (LanguageEntry languageEntry : m) {
				stringBuilder.append("\t" + languageEntry + "\n");
			}
		}

		stringBuilder.append(kmerCount + " total k-mers in " + langCount + " languages");

		return stringBuilder.toString();
	}
}