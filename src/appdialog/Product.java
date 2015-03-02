package appdialog;

public class Product {
	
	private long id;
	private String name;
	private String mfact;
	private double price;
	private String pricexplain;
	private String units;
	private String url;

	
	public Product(long id, String name, String mfact, double price, String pricexplain, String units, String url){
		this.id = id;
		this.name = name;
		this.mfact = mfact;
		this.price = price;
		this.pricexplain = pricexplain;
		this.units = units;
		this.url = url;
	}
	
	public long getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getMfact(){
		return mfact;
	}
	
	public double getPrice(){
		return price;
	}
	
	public String getPriceexplain(){
		return pricexplain;
	}
	
	public String getUnits(){
		return units;
	}
	
	public String getUrl(){
		return url;
	}
}
