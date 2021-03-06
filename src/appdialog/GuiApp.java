package appdialog;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;

import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent; 

import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;

import javax.swing.JSeparator;
import javax.swing.JCheckBox;

import java.awt.Font;

import javax.swing.JTextField;


public class GuiApp {

	private JFrame frame;
	private JComboBox<String> comboBox1;
	private JComboBox<Integer> dispList;
	private JComboBox<String> comboImage;
	private JComboBox<String> comboProductsBox;
	private JLabel lblConectado;
	private Integer[] dispInts = { 1, 2};
	DrawImage  drawimage = new DrawImage();
	ProductImg prodImage = new ProductImg();
	ArrayList<Product> productos;
	private ComPort comport;
	private SerialPort serialport;
	private String portname;
	private ExecutorPool exPool= new ExecutorPool();
	
	private JComboBox<String> comboBoxetiqueta1config;
	private JComboBox<String> comboBoxetiqueta2config;
	private JCheckBox checkBox;
	private JCheckBox chckbxNewCheckBox;
	private JLabel lblNewLabel_1;
	
	private Thread daemon = null;
	
	private SerialInterface serialInterface;
	private JTextField restURL;
	private JTextField textFieldProductName;
	private JTextField textFieldManufacturer;
	private JTextField textFieldPrice;
	private JTextField textFieldPriceDetails;
	private JTextField textFieldUnits;

	
	
	//private SerialController scon;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiApp window = new GuiApp();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	
	/**
	 * Create the application.
	 */
	public GuiApp() {
		//scon = new SerialController();
		//System.setProperty("gnu.io.rxtx.SerialPorts", "/tmp/tty.LightBlue-Bean");
		//System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/tty.LightBlue-Bean");
		comport = new ComPort();
		initialize();
		rellenarBoxCom(comport.listPort());
		productos = createProducts();
		rellenaProductsBox(productos);
		
		
	}
	
	public ArrayList<Product> createProducts(){
		ArrayList<Product> products = new ArrayList<Product>();
		products.add(new Product(1, "Tomate Frito", "Orlando", 1.50, "15,75�/Litro", "150ml", ""));
		products.add(new Product(2, "Manzana Golden", "Hacendado", 0.75, "4,5�/Kg", "250gr", ""));
		products.add(new Product(3, "Leche Entera", "Pascual", 0.89, "0,89�/Litro", "1L", ""));
		products.add(new Product(4, "Patatas Bolsa", "Lays", 0.65, "17�/Kg", "250gr", ""));
		return products;
	}
	
	public void rellenaProductsBox(ArrayList<Product> products){
		comboProductsBox.addItem("Custom Product");
		for (Product p : products){
			comboProductsBox.addItem(p.getName());
			comboBoxetiqueta1config.addItem(p.getName());
			comboBoxetiqueta2config.addItem(p.getName());
		}
	}
	
	
	
	
	public void rellenarBoxCom(@SuppressWarnings("rawtypes") Enumeration ports) {
		System.out.println("Detectando puertos...");
	    while (ports.hasMoreElements()) {
	      CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
	      String type;
	      
	      System.out.println("Detectado puerto: "+ port.getName()+" -- Port type: "+ port.getPortType());
	      switch (port.getPortType()) {
	      case CommPortIdentifier.PORT_PARALLEL:
	        type = "Parallel";
	        break;
	      case CommPortIdentifier.PORT_SERIAL:
	        type = "Serial";
	        comboBox1.addItem(port.getName());
	        break;
	      default: /// Shouldn't happen
	        type = "Unknown";
	        break;
	      }
	      System.out.println(port.getName() + ": " + type);
	    }
	    System.out.println("A�adiendo puerto manual... :xD");
	    comboBox1.addItem("/tmp/tty.LightBlue-Bean");
	    comboBox1.addItem("/dev/ttyS001");
	    comboBox1.addItem("ttyS001");
	  }
	
	
	

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 531, 630);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("COM Port");
		lblNewLabel.setBounds(25, 11, 65, 27);
		frame.getContentPane().add(lblNewLabel);
		
		comboBox1 = new JComboBox<>();
		comboBox1.setBounds(100, 14, 89, 20);
		frame.getContentPane().add(comboBox1);
		
		JButton btnConectar = new JButton("Send Image");
		btnConectar.setEnabled(false);
		btnConectar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					drawimage.setImage(comboImage.getSelectedIndex());
					SerialController scon = new SerialController(portname, comport,serialport, dispList.getSelectedIndex()+1,drawimage);
					exPool.execute(scon);
					//Thread t = new Thread(scon);
			        //t.start();
			}
		});
		btnConectar.setBounds(25, 119, 117, 23);
		frame.getContentPane().add(btnConectar);
		
		JButton btnConectar_1 = new JButton("Connect");
		btnConectar_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				portname = comboBox1.getSelectedItem().toString();
				if(comport.connecttoPort(portname)){
					serialport = comport.getSerialPort();
					serialInterface = new SerialInterface(serialport);
					lblConectado.setText("Conectado!");
				}
					
			}
		});
		btnConectar_1.setBounds(199, 13, 89, 23);
		frame.getContentPane().add(btnConectar_1);
		
		JButton btnDesconectar = new JButton("Disconnect");
		btnDesconectar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				comport.disconnect();
				serialport = null;
				lblConectado.setText("Desconectado");
			}
		});
		btnDesconectar.setBounds(298, 13, 110, 23);
		frame.getContentPane().add(btnDesconectar);
		
		lblConectado = new JLabel("Disconnected");
		lblConectado.setBounds(418, 17, 104, 14);
		frame.getContentPane().add(lblConectado);
		
		

		dispList = new JComboBox(dispInts);
		dispList.setEnabled(false);
		dispList.setBounds(199, 81, 89, 20);
		dispList.setSelectedIndex(0);
		frame.getContentPane().add(dispList);
		
		JLabel lblEtiqueta = new JLabel("Label:");
		lblEtiqueta.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblEtiqueta.setBounds(209, 56, 65, 14);
		frame.getContentPane().add(lblEtiqueta);
		
		JLabel lblImagen = new JLabel("Image:");
		lblImagen.setBounds(25, 56, 46, 14);
		frame.getContentPane().add(lblImagen);
		
		comboImage = new JComboBox(drawimage.getNames());
		comboImage.setBounds(25, 81, 89, 20);
		frame.getContentPane().add(comboImage);
		
		JButton btnBorrar = new JButton("Clear");
		btnBorrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					drawimage = new DrawImage();
					SerialController scon = new SerialController(portname, comport,serialport, dispList.getSelectedIndex()+1,drawimage);
					exPool.execute(scon);
					//Thread t = new Thread(scon);
			        //t.start();
			}
		});
		btnBorrar.setBounds(199, 119, 89, 23);
		frame.getContentPane().add(btnBorrar);
		
		JButton btnEnviarEtq = new JButton("Send Label");
		btnEnviarEtq.setEnabled(false);
		btnEnviarEtq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ProductImg prodimg = new ProductImg(productos.get(comboProductsBox.getSelectedIndex()));
				SerialController scon = new SerialController(portname, comport,serialport, dispList.getSelectedIndex()+1, prodimg);
				exPool.execute(scon);
			}
		});
		btnEnviarEtq.setBounds(351, 119, 129, 23);
		frame.getContentPane().add(btnEnviarEtq);
		
		comboProductsBox = new JComboBox();
		comboProductsBox.setBounds(330, 345, 129, 20);
		frame.getContentPane().add(comboProductsBox);
		
		JLabel lblProducto = new JLabel("Product:");
		lblProducto.setBounds(255, 348, 65, 14);
		frame.getContentPane().add(lblProducto);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(0, 153, 515, 1);
		frame.getContentPane().add(separator);
		
		JLabel lblConfiguracion = new JLabel("Configuration:");
		lblConfiguracion.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblConfiguracion.setBounds(10, 165, 104, 14);
		frame.getContentPane().add(lblConfiguracion);
		
		JLabel lblEtiqueta_1 = new JLabel("Label 1:");
		lblEtiqueta_1.setBounds(20, 190, 70, 14);
		frame.getContentPane().add(lblEtiqueta_1);
		
		JLabel lblEtiqueta_2 = new JLabel("Label 2:");
		lblEtiqueta_2.setBounds(20, 215, 70, 14);
		frame.getContentPane().add(lblEtiqueta_2);
		
		comboBoxetiqueta1config = new JComboBox();
		comboBoxetiqueta1config.setEnabled(false);
		comboBoxetiqueta1config.setBounds(114, 187, 130, 20);
		frame.getContentPane().add(comboBoxetiqueta1config);
		
		comboBoxetiqueta2config = new JComboBox();
		comboBoxetiqueta2config.setEnabled(false);
		comboBoxetiqueta2config.setBounds(114, 212, 130, 20);
		frame.getContentPane().add(comboBoxetiqueta2config);
		
		checkBox = new JCheckBox("");
		checkBox.setEnabled(false);
		checkBox.setBounds(294, 186, 21, 23);
		frame.getContentPane().add(checkBox);
		
		chckbxNewCheckBox = new JCheckBox("");
		chckbxNewCheckBox.setEnabled(false);
		chckbxNewCheckBox.setBounds(294, 211, 21, 23);
		frame.getContentPane().add(chckbxNewCheckBox);
		
		JLabel lblHoraFeliz = new JLabel("Sys Active");
		lblHoraFeliz.setBounds(283, 165, 65, 14);
		frame.getContentPane().add(lblHoraFeliz);
		
		JButton btnA = new JButton("Accept and Send");
		btnA.setEnabled(false);
		btnA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<LabelContent> labels = new ArrayList<LabelContent>();
				labels.add(new LabelContent(1, productos.get(comboBoxetiqueta1config.getSelectedIndex()), false, checkBox.isSelected()));
				labels.add(new LabelContent(2, productos.get(comboBoxetiqueta2config.getSelectedIndex()), false, chckbxNewCheckBox.isSelected()));
				DaemonRunnable daemonRunnable= new DaemonRunnable(portname, comport, serialport, exPool, labels);
				
				daemon = new Thread(daemonRunnable);
				daemon.start();
				lblNewLabel_1.setText("Sistema activo");
			}
		});
		btnA.setBounds(261, 256, 147, 23);
		frame.getContentPane().add(btnA);
		
		JButton btnParar = new JButton("Stop");
		btnParar.setEnabled(false);
		btnParar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				daemon.interrupt();
				lblNewLabel_1.setText("Sistema Detenido");
			}
		});
		btnParar.setBounds(418, 256, 89, 23);
		frame.getContentPane().add(btnParar);
		
		lblNewLabel_1 = new JLabel("System stopped");
		lblNewLabel_1.setBounds(362, 285, 153, 14);
		frame.getContentPane().add(lblNewLabel_1);
		
		JLabel lblHorariohoraFeliz = new JLabel("Current Config");
		lblHorariohoraFeliz.setBounds(376, 190, 118, 14);
		frame.getContentPane().add(lblHorariohoraFeliz);
		
		JLabel lblA = new JLabel("ECA Rules");
		lblA.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblA.setBounds(392, 208, 83, 14);
		frame.getContentPane().add(lblA);
		
		JButton btnOpenConfig = new JButton("Open Config");
		btnOpenConfig.setEnabled(false);
		btnOpenConfig.setBounds(53, 256, 110, 23);
		frame.getContentPane().add(btnOpenConfig);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(0, 308, 515, 2);
		frame.getContentPane().add(separator_1);
		
		JLabel lblLightblueBean = new JLabel("LightBlue Bean");
		lblLightblueBean.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblLightblueBean.setBounds(10, 321, 104, 14);
		frame.getContentPane().add(lblLightblueBean);
		
		JButton btnNewButton = new JButton("Send White");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					HttpConnector conn = new HttpConnector(restURL.getText());
					conn.sendWhite();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		btnNewButton.setBounds(25, 375, 104, 23);
		frame.getContentPane().add(btnNewButton);
		
		JButton btnSendBlack = new JButton("Send Black");
		btnSendBlack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					HttpConnector conn = new HttpConnector(restURL.getText());
					conn.sendBlack();
				} catch (MalformedURLException ex) {
					ex.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnSendBlack.setBounds(140, 375, 104, 23);
		frame.getContentPane().add(btnSendBlack);
		
		JButton btnCheckConn = new JButton("Check Conn!");
		btnCheckConn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				serialInterface.send("hello");
			}
		});
		btnCheckConn.setBounds(25, 341, 104, 23);
		frame.getContentPane().add(btnCheckConn);
		
		JLabel lblNotFound = new JLabel("OK");
		lblNotFound.setBounds(100, 321, 65, 14);
		frame.getContentPane().add(lblNotFound);
		
		restURL = new JTextField();
		restURL.setText("http://localhost:9996");
		restURL.setBounds(223, 318, 282, 20);
		frame.getContentPane().add(restURL);
		restURL.setColumns(10);
		
		JLabel lblUrl = new JLabel("URL");
		lblUrl.setBounds(197, 321, 21, 14);
		frame.getContentPane().add(lblUrl);
		
		JLabel lblResponses = new JLabel("Custom Product");
		lblResponses.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblResponses.setBounds(10, 409, 119, 14);
		frame.getContentPane().add(lblResponses);
		
		JButton btnSendImage = new JButton("Send Image");
		btnSendImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				drawimage.setImage(comboImage.getSelectedIndex());
				try {
					HttpConnector conn = new HttpConnector(restURL.getText());
					conn.sendImage(drawimage);
				} catch (MalformedURLException ex) {
					ex.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnSendImage.setBounds(261, 375, 89, 23);
		frame.getContentPane().add(btnSendImage);
		
		JButton btnSendLabel = new JButton("Send Label");
		btnSendLabel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(comboProductsBox.getSelectedIndex() == 0){
					prodImage = new ProductImg(
							new Product(
									5,
									textFieldProductName.getText(),
									textFieldManufacturer.getText(),
									Double.parseDouble(textFieldPrice.getText()),
									textFieldPriceDetails.getText(),
									textFieldUnits.getText(),
									""
									));
				}
				else{
					prodImage = new ProductImg(productos.get(comboProductsBox.getSelectedIndex()));
				}
				try {
					HttpConnector conn = new HttpConnector(restURL.getText());
					conn.sendImage(prodImage);
				} catch (MalformedURLException ex) {
					ex.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnSendLabel.setBounds(370, 375, 89, 23);
		frame.getContentPane().add(btnSendLabel);
		
		JButton btnNewButton_1 = new JButton("");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prodImage = new ProductImg(productos.get(comboProductsBox.getSelectedIndex()));
				try {
					HttpConnector conn = new HttpConnector(restURL.getText());
					conn.sendTest(prodImage);
				} catch (MalformedURLException ex) {
					ex.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnNewButton_1.setBounds(484, 156, 21, 23);
		frame.getContentPane().add(btnNewButton_1);
		
		JLabel lblProductName = new JLabel("Product Name:");
		lblProductName.setBounds(25, 434, 89, 14);
		frame.getContentPane().add(lblProductName);
		
		JLabel lblManufacturer = new JLabel("Manufacturer:");
		lblManufacturer.setBounds(25, 459, 89, 14);
		frame.getContentPane().add(lblManufacturer);
		
		JLabel lblPrice = new JLabel("Price:");
		lblPrice.setBounds(25, 484, 46, 14);
		frame.getContentPane().add(lblPrice);
		
		JLabel lblPriceDetails = new JLabel("Price details:");
		lblPriceDetails.setBounds(25, 509, 75, 14);
		frame.getContentPane().add(lblPriceDetails);
		
		JLabel lblUnits = new JLabel("Units:");
		lblUnits.setBounds(25, 534, 46, 14);
		frame.getContentPane().add(lblUnits);
		
		textFieldProductName = new JTextField();
		textFieldProductName.setBounds(114, 434, 345, 20);
		frame.getContentPane().add(textFieldProductName);
		textFieldProductName.setColumns(10);
		
		textFieldManufacturer = new JTextField();
		textFieldManufacturer.setBounds(114, 456, 345, 20);
		frame.getContentPane().add(textFieldManufacturer);
		textFieldManufacturer.setColumns(10);
		
		textFieldPrice = new JTextField();
		textFieldPrice.setBounds(114, 481, 345, 20);
		frame.getContentPane().add(textFieldPrice);
		textFieldPrice.setColumns(10);
		
		textFieldPriceDetails = new JTextField();
		textFieldPriceDetails.setBounds(114, 506, 345, 20);
		frame.getContentPane().add(textFieldPriceDetails);
		textFieldPriceDetails.setColumns(10);
		
		textFieldUnits = new JTextField();
		textFieldUnits.setBounds(114, 531, 345, 20);
		frame.getContentPane().add(textFieldUnits);
		textFieldUnits.setColumns(10);
		
	}
}
