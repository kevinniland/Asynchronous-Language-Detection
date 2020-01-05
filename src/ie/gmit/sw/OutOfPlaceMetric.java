package ie.gmit.sw;

public class OutOfPlaceMetric implements Comparable<OutOfPlaceMetric> {
	private Language lang;
	private int distance;

	public OutOfPlaceMetric(Language lang, int distance) {
		super();
		
		this.lang = lang;
		this.distance = distance;
	}

	public Language getLanguage() {
		return lang;
	}

	public int getAbsoluteDistance() {
		return Math.abs(distance);
	}

	@Override
	public int compareTo(OutOfPlaceMetric outOfPlaceMetric) {
		return Integer.compare(this.getAbsoluteDistance(), outOfPlaceMetric.getAbsoluteDistance());
	}

	@Override
	public String toString() {
		return "[lang=" + lang + ", distance=" + getAbsoluteDistance() + "]";
	}
}
