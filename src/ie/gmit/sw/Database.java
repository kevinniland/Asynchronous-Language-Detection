package ie.gmit.sw;

import java.util.Map;

// Database interface
public interface Database {
	public void add(CharSequence s, Language lang);
	public void resize(int max);
	public Map<Integer, LanguageEntry> getTop(int max, Language lang);
	public Language getLanguage(Map<Integer, LanguageEntry> query);
}
