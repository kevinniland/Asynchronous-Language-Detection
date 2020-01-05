package ie.gmit.sw;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import javax.servlet.*;
import javax.servlet.http.*;

/*
 * To compile this servlet, open a command prompt in the web application directory and execute the following commands:
 *
 * Linux/Mac																	Windows
 * ---------																	---------
 * cd WEB-INF/classes/															cd WEB-INF\classes\
 * javac -cp .:$TOMCAT_HOME/lib/servlet-api.jar ie/gmit/sw/*.java				javac -cp .:%TOMCAT_HOME%/lib/servlet-api.jar ie/gmit/sw/*.java
 * cd ../../																	cd ..\..\
 * jar -cf ngrams.war *															jar -cf ngrams.war *
 *
 * Drag and drop the file ngrams.war into the webapps directory of Tomcat to deploy the application. It will then be
 * accessible from http://localhost:8080.
 *
 * NOTE: the text file containing the 253 different languages needs to be placed in /data/wili-2018-Edited.txt. This means
 * that you must have a "data" directory in the root of your file system that contains a file called "wili-2018-Edited.txt".
 * Do NOT submit the wili-2018 text file with your assignment!
 *
*/
public class ServiceHandler extends HttpServlet {
	private RealDatabase realDatabase = RealDatabase.getInstance();
	private ExecutorService executorService = Executors.newFixedThreadPool(50);
	private File file;
	private String languageDataSet = null; // This variable is shared by all HTTP requests for the servlet
	private long jobNumber = 0; // The number of the task in the async queue
	private static final long serialVersionUID = 1L;
	private int kmer = 4; // Set initial kmer size - can then be changed depending on the option the user sets
	
	static BlockingQueue<Job> blockingQueue = new ArrayBlockingQueue<Job>(20);
	static Map<CharSequence, CharSequence> outQueue = new ConcurrentHashMap<CharSequence, CharSequence>(); // Concurrent hash map - Checks for finished job
	
	// init is called for every instance of the servlet
	public void init() throws ServletException {
		System.out.println("In init");
		ServletContext ctx = getServletContext(); // Get a handle on the application context
		languageDataSet = ctx.getInitParameter("LANGUAGE_DATA_SET"); // Reads the value from the <context-param>
																			// in web.xml
//		System.out.println(System.getProperty("user.dir"));
		
		// You can start to build the subject database at this point. The init() method
		// is only ever called once during the life cycle of a servlet
		// build the query database
		file = new File(languageDataSet);
		
		System.out.println("File found");
//		Parser parser = new Parser(file, kmer);
		DatabaseBuilder databaseBuilder = new DatabaseBuilder(realDatabase, file, kmer);
//		parser.setDatabase(database);

		System.out.println("File read");
		Thread thread = new Thread(databaseBuilder);

		// Use this thread for each browser - allows the adding and removing from the
		// queue
		thread.start();
		
		// Wait for the database to fully built first - Previously just started the thread and then resized it immediately, leading to inaccurate language predictions
		try {
			thread.join();
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		// Resize the database - 
		realDatabase.resize(300);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html"); // Output the MIME type
		
		PrintWriter out = resp.getWriter(); // Write out text. We can write out binary too and change the MIME type...

		// Initialise some request variables with the submitted form info. These are
		// local to this method and thread safe...
		String option = req.getParameter("cmbOptions"); // Change options to whatever you think adds value to your assignment...
		String queryText = req.getParameter("query");
		CharSequence language = req.getParameter("languagePrediction");
		String taskNumber = req.getParameter("frmTaskNumber");

		// Display some text while the predicted language is being determined
		language = "Predicting language...";
		
		out.print("<html><head><title>AOOP - Asynchronous Language Detection (G00342279)</title>");
		out.print("</head>");
		out.print("<body>");

		if (taskNumber == null) {
			taskNumber = new String("T" + jobNumber);
			jobNumber++;
			
			// Add job to in-queue
			if (queryText != null) {
				try {
					System.out.println("Adding job to the queue...");
					
					buildBlockingQueue(taskNumber, queryText);
					
//					// Add new job to the in queue - pass in the task number and query text
//					blockingQueue.put(new Job(taskNumber, queryText));
//					
//					executorService.execute(new LanguageDetection(realDatabase, kmer));
					
					System.out.println("Done");
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		} else {
			// Check out-queue for finished job
//			if (outQueue.isEmpty() != isEmpty) {
//				/* 
//				 * for each job in the outQueue, if the task number of the job equals the "overall" task number of the system, get the query text of the job
//				 * and display the predicted language
//				 */
//				System.out.println("Removing job from queue...");
//				
//				for (Job job : outQueue) {
//					if (job.getTask().equals(taskNumber)) {
//						language = job.getQueryText();
//						languagePredicted = true;
//						
//						System.out.println("Removed job from queue");
//						
//						// Remove the job from the out queue
//						outQueue.remove(job);
//							
//						req.getRequestDispatcher("index.jsp").forward(req, resp);
//					}
//				}
//			}
			if (outQueue.containsKey(taskNumber)) {
				language = outQueue.get(taskNumber);
				
				outQueue.remove(taskNumber);
			}
		}

		out.print("<H1>Processing request for Job#: " + taskNumber + "</H1>");
		out.print("<div id=\"r\"></div>");
		out.print("<font color=\"#993333\"><b>");
		out.print("Language Dataset is located at " + languageDataSet + " and is <b><u>" + file.length()
				+ "</u></b> bytes in size");
		out.print("<br>Option: " + option);
		out.print("<br>Query Text: " + queryText);
		out.print("<br>Language: " + language);
		out.print("</font><p/>");
		out.print(
				"<br>This servlet should only be responsible for handling client request and returning responses. Everything else should be handled by different objects. ");
		out.print(
				"Note that any variables declared inside this doGet() method are thread safe. Anything defined at a class level is shared between HTTP requests.");
		out.print("</b></font>");
		out.print("<P> Next Steps:");
		out.print("<OL>");
		out.print(
				"<LI>Generate a big random number to use a a job number, or just increment a static long variable declared at a class level, e.g. jobNumber.");
		out.print("<LI>Create some type of an object from the request variables and jobNumber.");
		out.print("<LI>Add the message request object to a LinkedList or BlockingQueue (the IN-queue)");
		out.print(
				"<LI>Return the jobNumber to the client web browser with a wait interval using <meta http-equiv=\"refresh\" content=\"10\">. The content=\"10\" will wait for 10s.");
		out.print("<LI>Have some process check the LinkedList or BlockingQueue for message requests.");
		out.print(
				"<LI>Poll a message request from the front of the queue and pass the task to the language detection service.");
		out.print("<LI>Add the jobNumber as a key in a Map (the OUT-queue) and an initial value of null.");
		out.print(
				"<LI>Return the result of the language detection system to the client next time a request for the jobNumber is received and the task has been complete (value is not null).");
		out.print("</OL>");
		out.print("<form method=\"POST\" name=\"frmRequestDetails\">");
		out.print("<input name=\"cmbOptions\" type=\"hidden\" value=\"" + option + "\">");
		out.print("<input name=\"query\" type=\"hidden\" value=\"" + queryText + "\">");
		out.print("<input name=\"frmTaskNumber\" type=\"hidden\" value=\"" + taskNumber + "\">");
		out.print("</form>");
		out.print("</body>");
		out.print("</html>");
		out.print("<script>");
		out.print("var wait=setTimeout(\"document.frmRequestDetails.submit();\", 10000);");
		out.print("</script>");
	}

	private void buildBlockingQueue(String taskNumber, String queryText) throws InterruptedException {
		// Add new job to the in queue - pass in the task number and query text
		blockingQueue.put(new Job(taskNumber, queryText));
		
		executorService.execute(new LanguageDetection(realDatabase, kmer));
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
}