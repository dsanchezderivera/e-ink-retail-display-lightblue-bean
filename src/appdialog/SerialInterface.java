package appdialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SerialInterface implements SerialPortEventListener {
	
	public boolean connect = false;
	
	private SerialPort serialPort;
	private ComPort comport;
	private String portname;
	private Boolean lastByteUsed = true;
	private int estado=0; // 0=waiting 1=handshake 2=parameters 3=data 4=endwait
	private int count=0;
	int opcion = 0;
	
	private BufferedReader input;
	/** The output stream to the port */
	private OutputStream output;

	private int selectedDisplay = 1;
	private Image image;
	
	
	
	public SerialInterface(SerialPort serialport){
		this.serialPort = serialport;
		conectar();
	}
	
	public SerialInterface(SerialPort serialport, int display, Image sendimage){
		this.serialPort = serialport;
		this.selectedDisplay = display;
		this.image = sendimage;
	}
	public SerialInterface(String portname,ComPort comport,SerialPort serialport, int display, Image sendimage){
		this.comport = comport;
		this.serialPort = serialport;
		this.selectedDisplay = display;
		this.image = sendimage;
		this.portname = portname;
	}
	
	
	
	public void run(){
		System.out.println("Starting Thread!");
		if(conectar())
			enviar();
		while(estado !=0)
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
			
	}
	
	public void setParameters(int display, Image sendimage){
		this.selectedDisplay = display;
		this.image = sendimage;
	}
	
	public boolean conectar(){
		try{
			
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			
			connect = true;
			System.out.println("Puerto COM conectado!");
			return true;
			}
			catch(IOException e){
				System.err.println("IO exception creating Scon: "+ e.getMessage());
				return false;
			}catch(TooManyListenersException e){
				System.err.println("Too Many Listeners exception creating Scon: "+ e.getMessage());
				return false;
			}
	}
	
	public void enviar(){
		try{
		estado=1;
		System.out.println(serialPort);
		output.write(51);
		System.out.println("Enviado primer byte");
		
		}catch(Exception e){
			System.out.print("Error enviando ");
			System.err.println(e.getLocalizedMessage());
		}
	}
	
	
	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				while(input.ready()  && lastByteUsed){
					lastByteUsed = true;
					interpreta(input.readLine());
				}
			} catch (Exception e) {
				System.out.print("Error ");
				System.err.println(e.toString());
			}
		}
		
	}
	
	public void interpreta(String lineReceived){
		try{
			System.out.println("Respuesta: "+lineReceived);
			if(lineReceived == "data"){
				switch(estado){
					case 0:	//waiting
						System.out.println("BYTE IN Estado 0:");
						break;
					case 1: //handshaking
						System.out.println("Handshake!!! enviamos parametros...");
						estado = 2;
						output.write(selectedDisplay);
						break;
					case 2:	//parameters
						System.out.println("Parametros Enviados... comienza envio datos.");
						estado = 3;
						envio_datos();
						break;
					case 3:	//data
						envio_datos();
						break;
					case 4:	//endwait
						input.close();
						output.close();
						serialPort.removeEventListener();
						estado=0;
						
						
						System.out.println("FIN TRANSMISION");
						Thread.currentThread().interrupt();
						throw new InterruptedException();
				}
			}else{
				//System.out.println("Byte distinto recibido: " + brec);
			}
		}catch(IOException e){
				System.err.println("Error interpreta - "+ e.toString());
		}catch(Exception e){
			System.out.println("Cerrando Thread");
			return;
		}
		
		lastByteUsed = true;
	}
	
	
	public void envio_datos(){
		if(count<176){
			int mult = count*33;
			try {
					for(int i=0;i<33;i++){
						output.write(image.getByte(mult+i));
						//output.write(0x00);
						//System.out.println("dato:"+ (mult+i));
					}
			} catch (IOException e) {
				System.out.println("error");
				e.printStackTrace();
			}
			count++;
		}else{
			System.out.println("Completado::::: Esperando....");
			estado= 4;
			count=0;
		}
	}

	public void send(String string) {
		System.out.println("Enviando: "+string);
		if(connect){
			try {
				output.write(string.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	

}
