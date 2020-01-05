package ie.gmit.sw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

// Taken from the 'first half' of the Parser class in this video: https://web.microsoftstream.com/video/c12997b2-2e7f-4047-8763-b73d0ac0712c?referrer=https:%2F%2Flearnonline.gmit.ie%2Fcourse%2Fview.php%3Fid%3D945
public class DatabaseBuilder implements Runnable {
	private RealDatabase database;
	private File file;
	private int k;

	public DatabaseBuilder(RealDatabase database, File file, int k) {
		super();

		this.database = database;
		this.file = file;
		this.k = k;
	}

	@Override
	public void run() {
		System.out.println("Reading WiLI language dataset...");

		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileStr)));
			String line = null;

			System.out.println("Done");
			System.out.println("Parsing file...");

			while ((line = bufferedReader.readLine()) != null) {
				String[] fileRecord = line.trim().split("@");

				if (fileRecord.length != 2)
					continue;
				parse(fileRecord[0], fileRecord[1]);
			}

			System.out.println("Done");
			bufferedReader.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void parse(String text, String lang, int... ks) {
		Language language = Language.valueOf(lang);

		for (int i = 0; i <= text.length() - k; i++) {
			CharSequence kmer = text.substring(i, i + k);
			database.add(kmer, language);
		}
	}
}
