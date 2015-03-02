package appdialog;

import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class ComPort {
	
	private SerialPort serialPort;
	
	
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 5000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 57600;
	
	
	
	
	@SuppressWarnings("rawtypes")
	public Enumeration listPort() {
		return CommPortIdentifier.getPortIdentifiers();
	  }
	
	public boolean connecttoPort(String portName){
		@SuppressWarnings("rawtypes")
		Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier portId = null;  // will be set if port found
		while (portIdentifiers.hasMoreElements())
		{
		    CommPortIdentifier pid = (CommPortIdentifier) portIdentifiers.nextElement();
		    if(pid.getPortType() == CommPortIdentifier.PORT_SERIAL &&
		       pid.getName().equals(portName)) 
		    {
		        portId = pid;
		        break;
		    }
		}
		if(portId == null)
		{
		    System.err.println("Could not find serial port " + portName);
		}
				
		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			
			//input = serialPort.getInputStream();
			//output = serialPort.getOutputStream();

			// add event listeners
			
			//serialPort.addEventListener(this);
			//serialPort.notifyOnDataAvailable(true);
			return true;
		} 
		catch (Exception e) {
			System.err.println(e.toString());
			return false;
		}
	}
	
	public SerialPort getSerialPort(){
		return serialPort;
	}
	
	public void disconnect(){
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}


}
