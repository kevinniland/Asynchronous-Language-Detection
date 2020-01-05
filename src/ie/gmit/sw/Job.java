package ie.gmit.sw;

// Class that allows for the adding and removing of jobs to the queue
public class Job {
	private String task;
	private String queryText;
	
	public Job(String task, String queryText) {
		super();
		
		this.task = task;
		this.queryText = queryText;
	}

	// Get task
	public String getTask() {
		return task;
	}

	// Get query text
	public String getQueryText() {
		return queryText;
	}
}
