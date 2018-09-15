/*
 *                       PacketEye (Packet Sniffer) application
 *                      Developed as mini project in RGUKT Nuzvid
 *            Developed by Ayyappa swamy, Rama Krishna, Vara Lakshmi and Mani
 *                               Completed in 07/04/2017
 */
 
 
 // This file contains code for the GUI design, actions to the buttons and some methods to capture data, save data and retrieve(view) the data



import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import javax.swing.*;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterfaceAddress;

public class GUI2 {

	//GUI components
	private JFrame MainWindow=new JFrame("PacketEye 1.0");	                                //Main Window for the applications
	private JFrame helpWindow,aboutWindow;	                                                // Windows to display help and about content
	private JButton start,pause,resume,stop,help,about,exit,save,load,search,interfaces;	//Buttons
	private JTextField searchField;                                                         //Text area (search)
	private static JTextArea numberOfPackets = new JTextArea();                             //Text area that displays number of captured packets
	private static int interfaceNumber;
	private static CaptureThread threadObject;	                                            //Object for the class CaptureThread
	private static JpcapCaptor captor;
	private static String inputText="";
	private static boolean captureState = false;                                            //Flag that is helpful for capturing and stopping the capturing
	private static boolean pauseState = false;                                              //Flag used to pause and resume capturing
	
	private static jpcap.NetworkInterface[] devices = JpcapCaptor.getDeviceList();          //variable used to store interfaces details
	
	public static int count;                                                                //count variable that stores number of captured packets
	public static JTextArea displayResult = new JTextArea();                                //Text area to display the captured data
	
	
	//Main method
	public static void main(String[] args) throws NullPointerException{
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try{
					GUI2 window = new GUI2();	            //Creating object for GUI2 class
					window.MainWindow.setVisible(true);		//Making window visible
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	//Constructor for GUI2 class
	public GUI2(){
		createGUIComponents();	//Calling method to create GUI components
	}
	
	
	//Method to create and set GUI components to the main window
	public void createGUIComponents(){
		
		//-----------------------------------Main window Frame----------------------------------------------------------------//
		
		MainWindow.setSize(1200, 730);		
		MainWindow.setLocationRelativeTo(null);		                //To align the window at the center of the screen
		MainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	//To kill the background running process
		MainWindow.setLayout(null);                                 //To set the layout to null
		MainWindow.setResizable(false);		                        //To restrict the window from maximizing
		
		//-----------------------------------Interfaces-----------------------------------------------------------------------//
		
		interfaces = new JButton("Interfaces");                     //To create button
		interfaces.setBounds(25,10,200,20);                         //To set the size and bounds for the button
		interfaces.setEnabled(true);                                //To enable the button
		MainWindow.getContentPane().add(interfaces);                //To add button to the main window
		
		//Adding action to the Interfaces button
		interfaces.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				save.setEnabled(false);
				interfaceNumber=Integer.parseInt(JOptionPane.showInputDialog(getInterfacesDetails(), "Enter interface number"));
				while(true){
					if(interfaceNumber>=0 && interfaceNumber<devices.length)	break;
					interfaceNumber=Integer.parseInt(JOptionPane.showInputDialog(getInterfacesDetails(), "Enter correct interface number"));
				}
				count=0;
				numberOfPackets.setText("");
				start.setVisible(true);
				start.setEnabled(true);
				interfaces.setEnabled(false);
			}
		});
		
		
		//--------------------Start Button---------------------------------------------------------------------------//
		
		start = new JButton("START");
		start.setBounds(250,10,110,20);
		MainWindow.getContentPane().add(start);
		start.setEnabled(false);
		
		//Adding action to the start button
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exit.setEnabled(false);
				start.setEnabled(false);
				pause.setEnabled(true);
				stop.setEnabled(true);
				search.setEnabled(true);
				displayResult.setText("");
				captureState = true;
				displayCapturedData();		//Method used to capture the packets
			}
		});
		
		
		//-------------------Pause Button--------------------------------------------------------------------//
		
		pause = new JButton("PAUSE");
		pause.setBounds(380, 10, 110, 20);
		pause.setEnabled(false);
		MainWindow.getContentPane().add(pause);
		
		//Adding action to the pause button
		pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pauseState=true;
				pause.setVisible(false);
				resume.setVisible(true);
			}
		});
		
		//-------------------Resume Button---------------------------------------------------------------------//
		
		resume = new JButton("RESUME");
		resume.setBounds(380, 10, 110, 20);
		resume.setVisible(false);
		MainWindow.getContentPane().add(resume);
		
		//Adding action to the resume button
		resume.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pauseState=false;
				resume.setVisible(false);
				pause.setVisible(true);
			}
		});
		
		//-------------------Stop Button------------------------------------------------------------------------//
		
		stop = new JButton("STOP");
		stop.setBounds(510, 10, 110, 20);
		stop.setEnabled(false);
		MainWindow.getContentPane().add(stop);
		
		//Adding action to the stop button
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exit.setEnabled(true);
				save.setEnabled(true);
				pause.setVisible(true);
				pause.setEnabled(false);
				start.setEnabled(false);
				pauseState=false;
				captureState = false;
				threadObject.finished();      //Terminating the thread
				interfaceNumber=-1;
				interfaces.setEnabled(true);
				stop.setEnabled(false);
				inputText  = "";
			}
		});

		//--------------------Help Button----------------------------------------------------------------------//
		
		help = new JButton("HELP");
		help.setBounds(640, 10, 110, 20);
		MainWindow.getContentPane().add(help);
		
		//Adding action to the help button
		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showHelpContent();     //calling showHelpContent method to display help content
			}
		});
		
		//------------------------About Button-------------------------------------------------------------------//
		
		about = new JButton("ABOUT");
		about.setBounds(640,40,110,20);
		MainWindow.getContentPane().add(about);
		
		//Adding action to the about button
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showAboutContent();    //calling showAboutContent method to display About content
			}
		});
					
				
		//-------------------Exit Button---------------------------------------------------------------------------//

		exit = new JButton("EXIT");
		exit.setBounds(770, 10, 110, 20);
		MainWindow.getContentPane().add(exit);
		
		//Adding action to the exit button
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			MainWindow.setVisible(false);
				MainWindow.dispose();
			}
		});
				
		//--------------------SearchField (Search box)-----------------------------------------------------------//
		
		searchField = new JTextField();
		searchField.setBounds(10,40,240,20);
		searchField.setColumns(15);
		searchField.setToolTipText("Enter protocol(tcp,udp,icmp)/ port 80,443:");
		MainWindow.getContentPane().add(searchField);
		
		
		//---------------------Search Button---------------------------------------------------------------------//
		
		search = new JButton("SEARCH");
		search.setBounds(250,40,110,20);
		search.setEnabled(false);
		MainWindow.getContentPane().add(search);
		
		//Adding action to the search button
		search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				inputText = searchField.getText().toString().trim();
			}
		});
		
		//-----------------------Save Button---------------------------------------------------------------------//
		
		save = new JButton("SAVE");
		save.setBounds(380,40,110,20);
		save.setEnabled(false);
		MainWindow.getContentPane().add(save);
		
		//Adding action to the save button
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveCapturedData();             //calling saveCapturedData method to save the data
			}
		});
		
		//-----------------------Load Button---------------------------------------------------------------------//
		
		load = new JButton("LOAD");
		load.setBounds(510,40,110,20);
		MainWindow.getContentPane().add(load);
		
		//Adding action to the load button
		load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				search.setEnabled(false);
				count=0;
				numberOfPackets.setText("");
				openSavedFile();              //Calling openSavedFile method to display saved file data
			}
		});
		
		//----------------------numberOfPackets JTextArea------------------------------------------------------------------//
		
		JLabel label= new JLabel();
		label.setText("No of packets captured:");
		label.setBounds(10,680,140,20);
		MainWindow.getContentPane().add(label);
		
		numberOfPackets.setEditable(false);
		numberOfPackets.setBounds(150, 680, 60, 20);
		numberOfPackets.setFocusable(false);
		MainWindow.getContentPane().add(numberOfPackets);
		
		//--------------------displayResult JTextArea----------------------------------------------------------------------//
		
		//Table to display the headers
		String[] column = {"NO", "TIME", "PROTOCOL", "SRC IP", "DEST IP", "SRC MAC", "DEST MAC", "SRC PORT", "DEST PORT", "LENGTH"};
		String[][] data  = {};
		
		JTable table = new JTable(data,column);
		JScrollPane jsp = new JScrollPane(table);  //Adding table to the Scroll pane
		jsp.setVisible(true);
		jsp.setBounds(10,70,1180,30);
	
		
		//Restricting size for every column of the header
		table.getColumnModel().getColumn(0).setMaxWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(40);
		table.getColumnModel().getColumn(2).setPreferredWidth(40);
		table.getColumnModel().getColumn(3).setPreferredWidth(90);
		table.getColumnModel().getColumn(4).setPreferredWidth(120);
		table.getColumnModel().getColumn(5).setPreferredWidth(120);
		table.getColumnModel().getColumn(6).setPreferredWidth(120);
		table.getColumnModel().getColumn(7).setPreferredWidth(60);
		table.getColumnModel().getColumn(8).setPreferredWidth(60);
		table.getColumnModel().getColumn(9).setPreferredWidth(50);


		table.setVisible(true);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		MainWindow.getContentPane().add(jsp);
		
		displayResult.setEditable(false);
		displayResult.setFont(new Font("monospaced", Font.BOLD, 12));   //Setting font to the displaying text
		
		//To print data in next line after exceeding the JTextField
		displayResult.setLineWrap(true);
		displayResult.setWrapStyleWord(true);
		
		//-------------------sp JSrollPane--------------------------------------------------------------------------//
		
		JScrollPane sp = new JScrollPane(displayResult);
		
		sp.setBounds(10,90,1180,590);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		MainWindow.getContentPane().add(sp);
		
		
		
		//------------------------------------------------------------------------------------------------------//
		
	}
	
	//---------------------------------------Method to display help content--------------------------------------------------------//
	
	protected void showHelpContent() {
		helpWindow =new JFrame("HELP");
		JTextArea text1 = new JTextArea();
		helpWindow.setSize(500,380);
		helpWindow.setLocationRelativeTo(null);
		helpWindow.setFocusable(false);
		helpWindow.setVisible(true);
		helpWindow.setResizable(false);
		helpWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		helpWindow.add(text1);
		text1.setEditable(false);
		text1.setHighlighter(null);
		text1.setFont(new Font("Serif",Font.ITALIC,16));
		text1.append("\tPacketEye is a Java packet Capturing application.\n"
					+"\n\t\tBUTTIONS\n"
					+"INTERFACES :: Lists the available interfaces on host\n\t   and takes the interface number to capture packets."+
					"\nSTART \t:: Starts capturing of packets on selected interface."+
					"\nSEARCH \t :: Filters packets on a specific protocol/port (eg: icmp, tcp, \n\t  port 80, port 443 etc )"+
					"\nPAUSE\t :: Pauses capturing packets until RESUME button is clicked."+
					"\nRESUME \t:: Resumes capturing of packets."+
					"\nSTOP \t:: Stops capturing packets."+
					"\nSAVE\t:: Saves the captured data into a text file."+
					"\nLOAD\t:: Loads already saved data to the application."+
					"\nABOUT\t:: Displays the ABOUT content of the application."+
					"\nHELP  \t:: Displays the HELP window."+
					"\nEXIT  \t :: Stops capturing and exit the application.");
		
	}

	//---------------------------------------Method to display about content-------------------------------------------------------------------//

	protected void showAboutContent() {
		aboutWindow=new JFrame("About PacketEye");
		JTextArea text = new JTextArea();
		aboutWindow.setSize(270,100);
		aboutWindow.setVisible(true);
		aboutWindow.setLocationRelativeTo(null);
		aboutWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		aboutWindow.setResizable(false);
		
		text.setEditable(false);
		text.setHighlighter(null);
		text.setFont(new Font("Serif",Font.ITALIC,16));
		text.append("\tPacketEye 1.0\nPacketEye application is developed by \nRGUKT 2012 batch students");
		
		aboutWindow.getContentPane().add(text);
		
	}

	//---------------------------------------Method to get the details of available interfaces---------------------------------------------------------//
	
	protected StringBuffer getInterfacesDetails() {
		StringBuffer information=new StringBuffer("");
		for(int i=0;i<devices.length;i++){
			information.append(i+": "+((NetworkInterfaceAddress)devices[i].addresses[1]).address+"  ("+devices[i].description+")\n");
		}
		return information;
	}
	
	
	//--------------------------------------Method to save the captured data into a text file---------------------------------------------------------//
	
	protected void saveCapturedData() {
		try{
			String title = JOptionPane.showInputDialog("Enter file name:");
			if(title==null)
				return;
			String capData = new String (displayResult.getText().toString());
			File datafile = new File(title+".txt");
			FileOutputStream dataStream = new FileOutputStream(datafile);
			PrintStream printStream = new PrintStream(dataStream);
			printStream.print(capData);
			
			printStream.close();
			JOptionPane.showMessageDialog(null, "Captured data saved successfully.");
		}catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error occured: Could not save data");
		}
	}


	//------------------------------------------Method to display the captured Data-------------------------------------------------------------------//
	
	public static void displayCapturedData() {
		count = 0;
		threadObject = new CaptureThread() {
			
			@Override
			public Object construct() {
				try{
					count = 0;
					captor = JpcapCaptor.openDevice(devices[interfaceNumber], 65535, true,1000);
					while(captureState){
						while(pauseState) {
							Thread.currentThread();
							Thread.sleep(1000);
						}
						if(!pauseState){
							captor.setFilter(inputText, true);
							captor.processPacket(-1, new PacketPrinter());
							numberOfPackets.setText(""+count);
						}
					}
					captor.close();
					} catch(Exception e){
					e.printStackTrace();
					}
				return 0;
			}
		};
		
		threadObject.start();
	}
	 
	//----------------------------------------Method to load saved file-------------------------------------------------------------------------------//
	
	protected void openSavedFile() {
		String CapData = "";
		displayResult.setText("");
		
		JFileChooser fc = new JFileChooser();
		int i = fc.showOpenDialog(new Frame());
		if(i == JFileChooser.APPROVE_OPTION){
			File f = fc.getSelectedFile();
			String filepath = f.getAbsolutePath();
			
			if(filepath != null){
				try{
					File Data = new File(filepath);
					FileInputStream fis = new FileInputStream(Data);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					
					
					while((CapData = br.readLine())!=null){
						CapData += "\n";
						CapData = CapData + br.readLine() + "\n";
						displayResult.append(CapData);
					}
					
					br.close();
					fis.close();
					
					displayResult.setFont(new Font("monospaced", Font.BOLD, 12));
					JOptionPane.showMessageDialog(null, "Data successfully loaded.");
				}catch (Exception e) {
					JOptionPane.showMessageDialog(null, "File access error : could not load data.");
				}
			}
			
		}
	}

}
