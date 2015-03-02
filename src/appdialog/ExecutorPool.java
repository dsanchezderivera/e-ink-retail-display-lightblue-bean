package appdialog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorPool {
	
	private ExecutorService es = Executors.newSingleThreadExecutor();
	
	public void execute(Runnable r){
		es.execute(r);
		System.out.println("A�adido Thread al pool");
	}
}
