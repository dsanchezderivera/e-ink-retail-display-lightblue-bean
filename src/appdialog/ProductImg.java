package appdialog;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class ProductImg extends Image{
	
	private static int width = 264;
	private static int height = 176;
	
	private String product = "Tomate Frito";
	private String mfact = "Orlando";
	private double price = 1.50;
	private String pricexplain = "15,75€/Litro";
	private String units = "150ml";
	private String url;
		
	
	public ProductImg(){
		image = converttoint(createImagen());
		//write("text.txt", imageproducto);
		//System.out.println("Done!");
	}
	
	public ProductImg(String product, String mfact, double price, String pricexplain, String units, String url){
		this.product = product;
		this.mfact = mfact;
		this.price = price;
		this.pricexplain = pricexplain;
		this.units = units;
		this.url = url;
		image = converttoint(createImagen());
		//write("text.txt", imageproducto);
		System.out.println("Done!");
	}
	
	public ProductImg(Product product){
		this.product = product.getName();
		this.mfact = product.getMfact();
		this.price = product.getPrice();
		this.pricexplain = product.getPriceexplain();
		this.units = product.getUnits();
		this.url = product.getUrl();
		image = converttoint(createImagen());
		//write("text.txt", imageproducto);
		System.out.println("Done!");
	}
		
	
	public  Integer[] createImagen(){
		Integer image[]= new Integer[5808];
		try{
			File f = new File("image.bmp");
			f.setWritable(true);
			
			BufferedImage bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
			Graphics2D g2 = bufferedImage.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, width, height);
			g2.setColor(Color.BLACK);
			
			
			//Manufacturer		
			g2.setFont(new Font("Arial", Font.ITALIC, 20));
			Rectangle rect = new Rectangle(0,0,206,20);
				//g2.draw(rect);
			Shape shape = getShapeFit(mfact, rect, g2);
			g2.setClip(shape);
			g2.fill(shape.getBounds());
			
			//Producto	
			g2.setFont(new Font("Arial",Font.BOLD, 25));
			rect= new Rectangle(0,20,206,23);
			shape = getShapeFit(product, rect, g2);
			g2.setClip(shape);
			g2.fill(shape.getBounds());
				//g2.draw(rect);
			
			//PRICE
			g2.setFont(new Font("Arial",Font.BOLD+Font.ITALIC, 80));
			rect= new Rectangle(10,45,200,90);
			DecimalFormat df = new DecimalFormat("#.##");
			shape = getShapeFit(df.format(price)+"€", rect, g2);
			g2.setClip(shape);
			g2.fill(shape.getBounds());
			
			//units
			g2.setFont(new Font("Arial",Font.PLAIN, 20));
			rect= new Rectangle(150,35,50,25);
			shape = getShapeFit(units, rect, g2);
			g2.setClip(shape);
			g2.fill(shape.getBounds());
			
			//price explain
			g2.setFont(new Font("Arial",Font.PLAIN, 18));
			rect= new Rectangle(0,155,206,20);
			shape = getShapeFit(pricexplain, rect, g2);
			g2.setClip(shape);
			g2.fill(shape.getBounds());
			
			RenderedImage ri = bufferedImage;
			
			List<Integer> intlist = new ArrayList<Integer>();
			int bit = 0;
			String binary="";
			int count=0;
			
			for(int i=0;i<bufferedImage.getHeight();i++){
				for(int j=0;j<bufferedImage.getWidth();j++){
					if((bufferedImage.getRGB(j, i)& 0xFF)<=128){
						bit=1;
					}else{
						bit=0;
					}
					binary += bit; 
					count++;
					if(count==8){
						count=0;
						intlist.add(Integer.parseInt(binary,2));
						binary="";
					}
				}
			}
			image = intlist.toArray(new Integer[0]); 
			ImageIO.write(ri, "bmp", f);
		
		}catch(IOException e){
			System.out.println("Error: "+ e.getMessage());
		}
		return image;
	}
	
	
	public Shape getShapeFit(String str,Rectangle rec, Graphics2D g2d){
		FontMetrics fm = g2d.getFontMetrics();
		FontRenderContext frc = g2d.getFontRenderContext();
		TextLayout tl = new TextLayout(str, g2d.getFont(), frc);
		AffineTransform transform = new AffineTransform();
		transform.setToTranslation(rec.getX(), rec.getY()+rec.getHeight());
		
		double scaleY = rec.getHeight() / (double) (tl.getOutline(null).getBounds().getMaxY() - tl.getOutline(null).getBounds().getMinY());
		double scaleX = rec.getWidth() / (double) fm.stringWidth(str);
		if(scaleX>1)
			scaleX= 1;
		if(scaleY>1)
			scaleY=1;
		transform.scale(scaleX, scaleY);
		return tl.getOutline(transform);
		
	}
	
	public int[] converttoint(Integer[] integer){
		 int imageint[] = new int[5808];
		for(int i=0; i<integer.length;i++){
			imageint[i]= (int)integer[i];
		}
		return imageint;
	}
	
	
	
	public byte reverseBits(int in) {
	    byte out = 0;
	    for (int ii = 0 ; ii < 8 ; ii++) {
	        byte bit = (byte)(in & 1);
	        out = (byte)((out << 1) | bit);
	        in = (byte)(in >> 1);
	    }
	    return out;
	}
	
	public static void write (String filename, int[]x){
		 try {  
		BufferedWriter outputWriter = null;
			outputWriter = new BufferedWriter(new FileWriter(filename));
		  for (int i = 0; i < x.length; i++) {
		    // Maybe:
		    outputWriter.write(x[i]+"");
		    // Or:
		    outputWriter.write(Integer.toString(x[i]));
		    outputWriter.newLine();
		  }
		  outputWriter.flush();  
		  outputWriter.close(); 
		  
	} catch (IOException e) {
		e.printStackTrace();
	}
		}
	
	public void setPrice(double offerpercent){
		if(offerpercent <= 1 && offerpercent >= 0)
			price = price*(1-(offerpercent));
			image = converttoint(createImagen());
			System.out.println("Nuevo Precio: " +price);
	}
	
}
