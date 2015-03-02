package appdialog;

import gnu.io.SerialPort;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DaemonRunnable implements Runnable{
	
	private SerialPort serialPort;
	private ComPort comport;
	private String portname;
	
	private ExecutorPool exPool;
	private ArrayList<LabelContent> labels = new ArrayList<LabelContent>();
	
	private double offerpercent = 0.20;
	
	
	public DaemonRunnable(String portname,ComPort comport, SerialPort serialPort, ExecutorPool exPool, ArrayList<LabelContent> labels ){
		this.serialPort = serialPort;
		this.exPool = exPool;
		this.labels = labels;
		this.comport = comport;
		this.portname = portname;
	}

	@Override
	public void run() {
		System.out.println("Inicializando");
		initializate();
		
		
		while (!Thread.currentThread().isInterrupted()) {
		    try {
		    	System.out.println("Checking!");
		        if(checkhours()){
		        	System.out.println("Hora feliz detectada");
		        	checkchanges(true);
		        	
		        }else{
		        	System.out.println("Hora feliz no detectada");
		        	checkchanges(false);
		        }
		        Thread.sleep(10000);
		    } catch (InterruptedException ex) {
		        Thread.currentThread().interrupt();
		        System.out.println("Thread interrupmido");
		    }
		}
		
	}
	
	private void checkchanges(boolean happyhour) {
		for(LabelContent label: labels){
			System.out.println("Label: "+label.getProduct().getName());
			System.out.println("Estado actual de oferta: "+ label.isOferta());
			if(happyhour){
				if(label.isHappyhour()){
					if(!label.isOferta()){
						System.out.println("Label: "+label.getProduct().getName() +" ha entrado en no ofertado");
						label.setoffer(true);
						System.out.println("Label is oferta: "+label.isOferta());
						ProductImg prodimg = new ProductImg(label.getProduct());
						prodimg.setPrice(offerpercent);
						SerialController scon = new SerialController(portname, comport, serialPort, label.getDisplay(), prodimg);
						exPool.execute(scon);
					}
				}
			}else{
				if(label.isHappyhour()){
					if(label.isOferta()){
						System.out.println("Label: "+label.getProduct().getName() +" ha entrado en ofertado");
						label.setoffer(false);
						ProductImg prodimg = new ProductImg(label.getProduct());
						SerialController scon = new SerialController(portname, comport, serialPort, label.getDisplay(), prodimg);
						exPool.execute(scon);
					}
				}
			}
				
			//ProductImg prodimg = new ProductImg(label.getProduct());
			//SerialController scon = new SerialController(serialPort, label.getDisplay(), prodimg);
			//exPool.execute(scon);
		}
		
	}

	public void initializate(){
		for(LabelContent label: labels){
			ProductImg prodimg = new ProductImg(label.getProduct());
			SerialController scon = new SerialController(portname, comport,serialPort, label.getDisplay(), prodimg);
			exPool.execute(scon);
		}
	}
	
	
	public boolean checkhours(){
		int from = 1800;
	    int to = 1900;
	    Date date = new Date();
	    Calendar c = Calendar.getInstance();
	    c.setTime(date);
	    int t = c.get(Calendar.HOUR_OF_DAY) * 100 + c.get(Calendar.MINUTE);
	    return (to > from && t >= from && t <= to || to < from && (t >= from || t <= to));
		
	}

}
