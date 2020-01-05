package ie.gmit.sw;

public class LanguageEntry implements Comparable<LanguageEntry> {
	private int kmer; // Contiguous substring of text of size n
	private int frequency; // Frequency of the occurrence of a k-mer in a language
	private int rank; // Ranking of the k-mer in terms of its frequency

	public LanguageEntry(int kmer, int frequency) {
		super();

		this.kmer = kmer;
		this.frequency = frequency;
	}

	public int getKmer() {
		return kmer;
	}

	public void setKmer(int kmer) {
		this.kmer = kmer;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	// Compare one language entry (k-mer) to another by their frequency in descending order
	@Override
	public int compareTo(LanguageEntry next) {
		return -Integer.compare(frequency, next.getFrequency());
	}

	@Override
	public String toString() {
		return "[" + kmer + "/" + frequency + "/" + rank + "]";
	}
}