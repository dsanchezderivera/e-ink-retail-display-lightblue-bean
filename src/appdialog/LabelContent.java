package appdialog;

public class LabelContent{
	
	Product product;
	int port;
	boolean oferta;
	boolean happyhour;
	
	public LabelContent(int port, Product product, boolean oferta, boolean happyhour){
		this.product = product;
		this.port = port;
		this.oferta = oferta;
		this.happyhour = happyhour;
	}
	
	public Product getProduct(){
		return product;
	}
	
	public int getDisplay(){
		return port;
	}
	
	public boolean isOferta(){
		return oferta;
	}
	
	public boolean isHappyhour(){
		return happyhour;
	}
	
	public void setHappyhour(boolean happyhour){
		this.happyhour = happyhour;
	}
	public void setoffer(boolean offer){
		this.oferta = offer;
	}

}
