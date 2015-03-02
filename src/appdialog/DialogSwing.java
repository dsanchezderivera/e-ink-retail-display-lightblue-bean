package appdialog;

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
import javax.swing.JFormattedTextField;
import java.awt.Font;


public class DialogSwing {

	private JFrame frame;
	private JComboBox<String> comboBox1;
	private JComboBox<Integer> dispList;
	private JComboBox<String> comboImage;
	private JComboBox<String> comboProductsBox;
	private JLabel lblConectado;
	private Integer[] dispInts = { 1, 2};
	DrawImage  drawimage = new DrawImage();
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

	
	
	//private SerialController scon;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DialogSwing window = new DialogSwing();
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
	public DialogSwing() {
		//scon = new SerialController();
		comport = new ComPort();
		initialize();
		rellenarBoxCom(comport.listPort());
		productos = createProducts();
		rellenaProductsBox(productos);
		
		
	}
	
	public ArrayList<Product> createProducts(){
		ArrayList<Product> products = new ArrayList<Product>();
		products.add(new Product(1, "Product 1", "Orlando", 1.50, "15,75€/Litro", "150ml", ""));
		products.add(new Product(2, "Manzana Golden", "Hacendado", 0.75, "4,5€/Kg", "250gr", ""));
		products.add(new Product(3, "Leche Entera", "Pascual", 0.89, "0,89€/Litro", "1L", ""));
		products.add(new Product(4, "Patatas Bolsa", "Lays", 0.65, "17€/Kg", "250gr", ""));
		return products;
	}
	
	public void rellenaProductsBox(ArrayList<Product> products){
		for (Product p : products){
			comboProductsBox.addItem(p.getName());
			comboBoxetiqueta1config.addItem(p.getName());
			comboBoxetiqueta2config.addItem(p.getName());
		}
	}
	
	
	
	
	public void rellenarBoxCom(@SuppressWarnings("rawtypes") Enumeration ports) {
	    while (ports.hasMoreElements()) {
	      CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
	      String type;
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
	  }
	
	
	

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 531, 341);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("COM Port");
		lblNewLabel.setBounds(25, 11, 65, 27);
		frame.getContentPane().add(lblNewLabel);
		
		comboBox1 = new JComboBox<>();
		comboBox1.setBounds(100, 14, 89, 20);
		frame.getContentPane().add(comboBox1);
		
		JButton btnConectar = new JButton("Send Image");
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
				//findPort(comboBox1.getSelectedItem().toString());
				portname = comboBox1.getSelectedItem().toString();
				if(comport.connecttoPort(portname)){
					serialport = comport.getSerialPort();
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
		dispList.setBounds(199, 81, 89, 20);
		dispList.setSelectedIndex(0);
		frame.getContentPane().add(dispList);
		
		JLabel lblEtiqueta = new JLabel("Label:");
		lblEtiqueta.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblEtiqueta.setBounds(199, 56, 65, 14);
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
		comboProductsBox.setBounds(351, 81, 129, 20);
		frame.getContentPane().add(comboProductsBox);
		
		JLabel lblProducto = new JLabel("Product:");
		lblProducto.setBounds(351, 56, 89, 14);
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
		comboBoxetiqueta1config.setBounds(114, 187, 130, 20);
		frame.getContentPane().add(comboBoxetiqueta1config);
		
		comboBoxetiqueta2config = new JComboBox();
		comboBoxetiqueta2config.setBounds(114, 212, 130, 20);
		frame.getContentPane().add(comboBoxetiqueta2config);
		
		checkBox = new JCheckBox("");
		checkBox.setBounds(294, 186, 21, 23);
		frame.getContentPane().add(checkBox);
		
		chckbxNewCheckBox = new JCheckBox("");
		chckbxNewCheckBox.setBounds(294, 211, 21, 23);
		frame.getContentPane().add(chckbxNewCheckBox);
		
		JLabel lblHoraFeliz = new JLabel("Sys Active");
		lblHoraFeliz.setBounds(283, 165, 65, 14);
		frame.getContentPane().add(lblHoraFeliz);
		
		JButton btnA = new JButton("Accept and Send");
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
		btnOpenConfig.setBounds(53, 256, 110, 23);
		frame.getContentPane().add(btnOpenConfig);
	}
}
