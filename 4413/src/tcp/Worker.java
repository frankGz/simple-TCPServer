package tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.rmi.CORBA.Util;
import javax.swing.RowSorter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.omg.CORBA.UNKNOWN;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import projA.Course;

public class Worker extends Thread{
	
	public Socket client;
	public PrintStream log;
	public Set<InetAddress> whiteList;
	private boolean flag = true;
	private Map<String,String> table;
	
	BufferedReader in = null;
	BufferedWriter out = null;
	
	public void handle() {
		
		try {
			 in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			 out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(in != null) {
			
				
				while (flag && !client.isClosed()) {
					//String[] commandline = in.readLine().split(" ");
					try {
					String command = in.readLine();
					
					//handles nullpointerexception
					if (command!=null) {
						String[] commandline = command.split(" ");
				
						switch (commandline[0].toUpperCase()) {
						case "GETTIME":
							getTime();
							break;
						case "BYE":
							bye();
							break;
	
						case "PUNCH":
							punch(commandline[1]);
							break;
	
						case "PLUG":
							plug(commandline[1]);
	
							break;
							
						case "PRIME":
							prime(commandline[1]);
							break;
							
						case "AUTH":
							auth(commandline[1],commandline[2]);
							break;
							
						case "ROSTER":
							roster(commandline[1],commandline[2]);
							break;
	
						default:
							unknownMethod(commandline[0]);
							break;
						}

					}
					}catch (ArrayIndexOutOfBoundsException e) {
						e.printStackTrace();
						log.println((new Date()).toString() + "|" + "An exception" + "|ArrayIndexOutOfBoundsException:" + e.getMessage());
					}catch (IOException e) {
						e.printStackTrace();
						log.println((new Date()).toString() + "|" + "An exception" + "|" + e.getMessage());
						break;
					}catch (Exception e) {
						e.printStackTrace();
						log.println((new Date()).toString() + "|" + "An exception" + "|" + e.getMessage());
					}
				}
				
				//while end
				
				try {
					in.close();
					out.close();
					client.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					log.println((new Date()).toString() + "|" + "An exception" + "|" + e.getMessage());
				}
				
			
			
		}
	}
	//log.println((new Date()).toString() + "|" + "An exception" + "|" + e.getMessage());
	private void roster(String string, String string2) throws IOException, JAXBException {
		Course course = projA.Util.getCourse(string);
		
		if(string2.toUpperCase().equals("XML")) {
			
			//System.out.println(course);
			JAXBContext context = JAXBContext.newInstance(course.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(course, out);
			out.flush();
			
			
		}else if (string2.toUpperCase().equals("JSON")) {
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			out.write(gson.toJson(course));
			out.write("\r\n");
			out.flush();
			
			
			
		}else {
			out.write("Don't understand <"+ string + "> <" +string2 +">");
			out.write("\r\n");
			out.flush();
		}
		

	}

	private void auth(String key, String value) throws IOException {
		// TODO Auto-generated method stub
		if(table.containsKey(key)&&table.get(key).equals(value)) {
			out.write("You are in!");
			out.write("\r\n");
			out.flush();	
		}else {
			out.write("Auth Failure");
			out.write("\r\n");
			out.flush();	
		}
		
	}

	private void prime(String string) throws IOException {
		int bitLength = Integer.parseInt(string);
		BigInteger bigInteger = BigInteger.probablePrime(bitLength * 3, new Random());
		out.write(bigInteger.toString());
		out.write("\r\n");
		out.flush();
	}

	private void unknownMethod(String string) throws IOException {
		out.write("Don't understand <"+ string + ">");
		out.write("\r\n");
		out.flush();		
	}

	private void plug(String string) {
		// None. The given ip is removed from the firewall's whitelist
		for(InetAddress ip : whiteList) {
			if(ip.toString().contains(string)) {
				whiteList.remove(ip);
				System.out.println(ip.toString()+" is removed from whitelist");
			}
		}
		
	}

	private void punch(String string) throws UnknownHostException {
		// None. The given ip is added to the firewall's whitelist
		InetAddress address = InetAddress.getByName(string);
		System.out.println("adding address: " + address.toString()); 
	}

	private void bye() throws IOException {
		setFlag(false);
		
	}

	private void getTime() throws IOException {
		out.write(((new Date()).toString()));
		out.write("\r\n");
		out.flush();
	}
	
	
	
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	

	public Worker(Socket client,PrintStream log, Set<InetAddress> whiteList, Map<String,String> table) {
		this.client = client;
		this.log = log;
		this.whiteList = whiteList;
		this.table = table;
		
	}

	@Override
	public void run() {
		
		this.handle();
	}

}
