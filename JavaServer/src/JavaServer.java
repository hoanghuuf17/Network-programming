import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;

public class JavaServer extends Config implements Runnable{
	
	private JFrame frame;
	private JTextField txtPip = null;
	private JTextField txtPort = null;
	private JButton btnAction = null;
	private JButton btnStart;
	private JTextArea textArea = null;
	
	//client conection via socket class
	private Socket conn;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private int phpLength;
	private volatile boolean run = true;
	public JavaServer(Socket c) {
		conn = c;
	}
	
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JavaServer window = new JavaServer();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public void run() {
				BufferedReader in = null;
				PrintWriter out = null;
				BufferedOutputStream dataOut = null;
				String fileRequested = null;
				try {
					in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String input = in.readLine();
					StringTokenizer parse = new StringTokenizer(input);
					String method = parse.nextToken().toUpperCase();
					fileRequested = parse.nextToken().toLowerCase();
					// get character to client
					out = new PrintWriter(conn.getOutputStream());
					// get binary outputstream to client
					dataOut = new BufferedOutputStream(conn.getOutputStream());
					// get first line of the request from the client
						
					// only get or head 
					//check method
					if(!method.equals("GET") && !method.equals("HEAD")) {
						//return the not supported file to the client
						File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
						int fileLeng = (int)file.length();
						String contentMimeType = "text/html";
						//read file
						byte[] fileData = readFileData(file, fileLeng); 	
//						 send http headers with data to client
						out.println("HTTP/1.1.501 Not Implemented");
						out.println("Server : Java HTTP Server 1.0");
						out.println("Content-type: " + contentMimeType);
						out.println("Content-length: " + file.length());
						out.println();
						out.flush(); // flush character output stream
						
						//file
						dataOut.write(fileData, 0, fileLeng);
						dataOut.flush();
						}else {	
							//GET or HEAD method 
							if(fileRequested.endsWith("/")) {
								fileRequested += DEFAULT_FILE;
							}
							if(fileRequested.endsWith(".php")) {
								String f = fileRequested.substring(1);
								File file = new File(WEB_ROOT, f);
								System.out.println(file);
								int fileLength  = (int)file.length();
								
								
								int filePhpLength = 0;
//								System.out.println(fileLength);
								String content = getContentType(fileRequested);
								byte[] fileData = readphp(file, fileLength);
								filePhpLength = getLengthPhp();
								out.println("HTTP/1.1.200");
								out.println("Server : Java HTTP Server 1.0");
								out.println("Content-type: " + content);
								out.println("Content-length: " + filePhpLength);
								out.println();
								out.flush(); // flush character output stream
								
								dataOut.write(fileData, 0, filePhpLength);
								dataOut.flush();
							}
							else {
							File file = new File(WEB_ROOT, fileRequested);
							System.out.println(file);
							int fileLength  = (int)file.length();
							System.out.println(fileLength);
							String content = getContentType(fileRequested);
							if(method.equals("GET")) {
								//get method
								byte[] fileData = readFileData(file, fileLength);
								out.println("HTTP/1.1.200");
								out.println("Server : Java HTTP Server 1.0");
								out.println("Content-type: " + content);
								out.println("Content-length: " + file.length());
								out.println();
								out.flush(); // flush character output stream
								
								dataOut.write(fileData, 0, fileLength);
								dataOut.flush();
							}
							
							
							if(verbose) {
								System.out.println("File" + fileRequested + "of type" + content + "returned");
							}
							
							
							}
						}
					
					} catch(FileNotFoundException fnfe) {
						try {
							fileNotFound(out, dataOut, fileRequested);
						}catch (IOException ioe) {
							System.err.println("Error with file not found exception" + ioe.getMessage());
						}
					}catch (IOException e1) {
					System.err.println("Server error:" + e1);
				} finally {
					try {
						in.close();
						out.close();
						dataOut.close();
						conn.close();
					} catch (Exception e) {
						System.err.println("Error classing stream:" + e.getMessage());
					} // close character input stream
					 
				}
				
	}
	
	 
	public JavaServer() {
		initialize();
	}
	
//	class StartSwingWorker extends SwingWorker<T, V>
	
	
private class Start implements Runnable {
			@Override
			public void run() {
				while(run) {
				try {
					ServerSocket serverConnect = new ServerSocket(PORT);
					String p = String.valueOf(PORT);
					long pid = ProcessHandle.current().pid();
					String pi = String.valueOf(pid);
					txtPip.setText(pi);
					txtPort.setText(p);
					textArea.setText("Status change detected: running \n");
					//listen
					while (true) {
						JavaServer myServer = new JavaServer(serverConnect.accept());
						if(verbose) {
							textArea.append("Connection opened. (" + new Date() + ")\n");
						}
						Thread thread = new  Thread(myServer);
						thread.start();
					}
				} catch (IOException e) {
					textArea.append("Server connection error: " + e.getMessage() + "\n");
				}
			}
				textArea.append("Server stoped");
			}
		}
public void stop() {
    run = false;
}
	
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if(fileIn != null)
				fileIn.close();
		}
//		System.out.println(fileData);
		return fileData;
	}
	
	private void setLengthPhp(int length) {
		phpLength = length;
	}
	
	private int getLengthPhp() {
		return phpLength;
	}
	private byte[] readphp(File file, int fileLength) throws IOException{
		FileInputStream fileIn = null;
		String f = String.valueOf(file);
		Runtime runtime = Runtime.getRuntime();
        String[] commands  = {"cmd", "-h"};
       
        Process process = null;
		try {
			process = Runtime.getRuntime().exec(new String[]{"php",f, "-m", "2"});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        BufferedReader lineReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        lineReader.lines().forEach(System.out::println);
        String a = lineReader.lines().collect(Collectors.joining());
        System.out.println(a);
        System.out.println("LENGTH_PHP: "+a.toString().length());
        setLengthPhp(a.length());
        byte[] fileData = new byte[a.length()];
        fileData = a.getBytes();
	     System.out.println(fileData);
	        return fileData;
	}
	
	private String getContentType(String fileRequested) {
		if(fileRequested.endsWith(".html") || fileRequested.endsWith(".htm")) {
			return "text/html";
		}
		if(fileRequested.endsWith(".php")) {
			return "text/html";
		}
		else
			return "text/plain";	
		}
	
	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested)throws IOException {
		File file = new File(WEB_ROOT, FILE_NOT_FOUND);
		int fileLength = (int)file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength); 
		
		out.println("HTTP/1J.1 404 Not Implemented");
		out.println("Server : paylak 1.0");
		out.println("Content-type: " + content);
		out.println("Content-length: " + file.length());
		out.println();
		out.flush(); // flush character output stream
		
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
		
		if(verbose) {
			System.out.println("FIle" + fileRequested + "not found");
		}
	}
	
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 661, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		
		JLabel lblModule = new JLabel("Module");
		lblModule.setBounds(33, 24, 46, 14);
		frame.getContentPane().add(lblModule);
		
		JLabel lblNewLabel = new JLabel("PID(s)");
		lblNewLabel.setBounds(125, 24, 46, 14);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblPorts = new JLabel("Port(s)");
		lblPorts.setBounds(212, 24, 46, 14); 
		frame.getContentPane().add(lblPorts);
		
		JLabel lblAction = new JLabel("Action");
		lblAction.setBounds(440, 24, 46, 14);
		frame.getContentPane().add(lblAction);
		
		JLabel lblServer = new JLabel("Server");
		lblServer.setBounds(33, 63, 46, 14);
		frame.getContentPane().add(lblServer);
		
		txtPip = new JTextField();
		txtPip.setBounds(122, 60, 80, 20);
		frame.getContentPane().add(txtPip);
		txtPip.setColumns(10);
		
		txtPort = new JTextField();
		txtPort.setBounds(212, 60, 70, 20);
		frame.getContentPane().add(txtPort);
		txtPort.setColumns(10);
		
		btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Start start = new Start();
				Thread thread  = new Thread(start,"T1");
				thread.start();
				btnStart.setEnabled(false);
			}
		});
		btnStart.setBounds(308, 59, 89, 23);
		frame.getContentPane().add(btnStart);
		
		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					//stop thread server
					stop();
					btnStart.setEnabled(true);
					textArea.append("Server closed \n");
					txtPip.setText("");
					txtPort.setText("");
				}
			
		});
		btnStop.setBounds(415, 59, 89, 23);
		frame.getContentPane().add(btnStop);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 204, 610, 131);
		frame.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);
		
		JLabel lblMysql = new JLabel("MySQL");
		lblMysql.setBounds(33, 111, 46, 14);
		frame.getContentPane().add(lblMysql);
		
		JLabel lblTomcat = new JLabel("Tomcat");
		lblTomcat.setBounds(33, 161, 46, 14);
		frame.getContentPane().add(lblTomcat);
		
		textField = new JTextField();
		textField.setBounds(122, 108, 80, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.setBounds(212, 108, 70, 20);
		frame.getContentPane().add(textField_1);
		textField_1.setColumns(10);
		
		JButton btnStart_1 = new JButton("Start");
		btnStart_1.setBounds(308, 107, 89, 23);
		frame.getContentPane().add(btnStart_1);
		
		JButton btnStop_1 = new JButton("Stop");
		btnStop_1.setBounds(415, 107, 89, 23);
		frame.getContentPane().add(btnStop_1);
		
		textField_2 = new JTextField();
		textField_2.setBounds(122, 158, 80, 20);
		frame.getContentPane().add(textField_2);
		textField_2.setColumns(10);
		
		textField_3 = new JTextField();
		textField_3.setBounds(212, 158, 70, 20);
		frame.getContentPane().add(textField_3);
		textField_3.setColumns(10);
		
		JButton btnStart_2 = new JButton("Start");
		btnStart_2.setBounds(308, 157, 89, 23);
		frame.getContentPane().add(btnStart_2);
		
		JButton btnStop_2 = new JButton("Stop");
		btnStop_2.setBounds(415, 157, 89, 23);
		frame.getContentPane().add(btnStop_2);
		
		JButton btnConf = new JButton("Config");
		btnConf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = new File("D:\\JAVA\\JavaServer\\src\\Config.java");
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.open(file);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}       
			}
		});
		btnConf.setBounds(531, 59, 89, 23);
		frame.getContentPane().add(btnConf);
		
		JButton btnConfig = new JButton("Config");
		btnConfig.setBounds(531, 107, 89, 23);
		frame.getContentPane().add(btnConfig);
		
		JButton btnConfig_1 = new JButton("Config");
		btnConfig_1.setBounds(531, 157, 89, 23);
		frame.getContentPane().add(btnConfig_1);
	}
}
