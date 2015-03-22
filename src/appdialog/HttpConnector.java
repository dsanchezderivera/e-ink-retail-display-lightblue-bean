package appdialog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpConnector {
	
	final String WHITE = "/white";
	final String BLACK = "/black";
	final String IMAGE = "/image";
	
	String url;
	
	public HttpConnector(String url) throws MalformedURLException{
		this.url = url;
	}
	
	
	public void sendWhite() throws Exception {
 
		URL obj = new URL(url+WHITE);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
 
	}
	
	public void sendBlack() throws Exception {
		 
		URL obj = new URL(url+BLACK);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
 
	}


	public void sendImage(DrawImage drawimage) throws Exception  {
		URL obj = new URL(url+IMAGE);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		System.out.println("\nSending 'POST' request to URL : " + url);
		// optional default is GET
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Length", "" +  Integer.toString(5808));
		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		
		int count = 0;
		while(count<176){
			int mult = count*33;
			try {
					for(int i=0;i<33;i++){
						out.write(drawimage.getByte(mult+i));
					}
			} catch (IOException e) {
				System.out.println("error");
				e.printStackTrace();
			}
			count++;
		}
		
		out.flush();
	    out.close();
		
		int responseCode = con.getResponseCode();
		
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
		
	}

}
