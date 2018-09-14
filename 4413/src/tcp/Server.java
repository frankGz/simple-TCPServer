package tcp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server 
{
	Set<InetAddress> whitelist;
	Set<Worker> workers = new HashSet<Worker>();
	Map<String,String> table = new HashMap<String,String>();
	
	public Server(int port, PrintStream log, File file) throws Exception
	{
		ServerSocket server = new ServerSocket(port);
		System.out.println("Listening on: " + server.getLocalPort());
		log.println((new Date()).toString() + "|" + "Server start" + "|server's IP:" + server.getLocalPort());
		
		
		/*
		 * fire wall setting
		 */
		whitelist = new HashSet<InetAddress>();
		whitelist.add(server.getInetAddress());
		whitelist.add(InetAddress.getByName("localhost"));
		whitelist.add(InetAddress.getByName("127.0.0.1"));
		//for testing purpose
		whitelist.add(InetAddress.getByName("0:0:0:0:0:0:0:1"));
		
		
		/*
		 * auth table setting
		 */
		table.put("U1", "u1");
		table.put("U2", "U2");
		table.put("U3", "u3");
		
		System.out.println("Server address is: "+ server.getInetAddress().toString());
		//whitelist.add(other);
		
		/*
		 * staring main loop
		 */
		while (file.exists())
		{	
			
			System.out.println("Listening...");
			
			Socket client = server.accept();
			//System.out.println("Running.txt exists? " + file.exists());
			// check the client's IP and filter it
			System.out.println("accept address: "+ client.getInetAddress().toString());
			
			
			if (whitelist.contains(client.getInetAddress())) {
				log.println((new Date()).toString() + "|" + "Connection" + "|" + client.getInetAddress());
				//Worker worker = new Worker(client)).handle();
				
				/*
				Worker worker = new Worker(client, log, whitelist);
				worker.handle();
				*/
				Worker worker = new Worker(client, log, whitelist, table);
				workers.add(worker);
				worker.start();
				
				System.out.println("new thread started");
				
				
				log.println((new Date()).toString() + "|" + "Disconnected" + "|" + client.getInetAddress());
			}else {
				log.println((new Date()).toString() + "|" + "A firewall violation" + "|" + client.getInetAddress());
			}
		}
		System.out.println("shutting down");
		//terminate all the thread in the collection
		for(Worker worker : workers) {
			
			if(worker.isAlive()) {
				worker.setFlag(false);
			}
			
		}
		log.println((new Date()).toString()+"|"+"Server shutdown");
		log.close();
		server.close();
	}

	public static void main(String[] args) throws Exception 
	{
		File log = new File("log.txt");
		File running = new File("running.txt");
		running.createNewFile();
		PrintStream printLog = null;
		
		try {
			printLog = new PrintStream(log);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("failed to get writing stream on log.txt");
			e.printStackTrace();
			System.exit(1);
		}
		
		/*
		FileInputStream fstream = new FileInputStream("textfile.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		while ((strLine = br.readLine()) != null)   {
		  // Print the content on the console
		  System.out.println (strLine);
		}

		br.close();
		*/

		
		
		//System.out.println("Running.txt exists? " + running.exists());
		Server s = new Server(0,printLog,running.getAbsoluteFile());
		
		

	}

}

