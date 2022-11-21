package uk.ac.qub.eeecs.chat;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;



public class ChatServer {
	
	public class ClientConnection extends Thread
	{
		private Socket socket;
		private ChatServer chatServer;
		//private BufferedReader br = null;
		//private BufferedWriter bw = null;
		private ObjectOutputStream oouts;
		private ObjectInputStream oins;
		private int id;
		
		public ClientConnection(int myid)
		{
			id = myid;
		}
		
		public int getID() { return id; }
		
		public void Init(Socket s, ChatServer cs)
		{
			socket = s;
			chatServer = cs;
			try
			{
				oouts = new ObjectOutputStream(socket.getOutputStream());
				oins = new ObjectInputStream(socket.getInputStream());
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			System.out.println("Client Initialised");
		}
		
		public void run()
		{
			System.out.println("Client Running");
			try 
			{
				while(true)
				{
					ChatMessage m = (ChatMessage) oins.readObject();
					if (m == null)
						throw new IOException("Null from client, broken pipe");
					System.out.println("Client Sent: "+m.key+" / "+m.value);
					this.chatServer.Send(m,id);
				}
			} 
			catch (IOException | ClassNotFoundException e) 
			{
				e.printStackTrace();
				chatServer.ClientError(this);
			}
		}
		
		public void Send(ChatMessage m)
		{
			try {
				oouts.writeObject(m);
				oouts.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	private ServerSocket serverSocket;
	private boolean quit = false;
	private int port = 8080;
	private List<ClientConnection> clients;
	private int nextclientid = 1;
	
	
	public void Start()
	{
		System.out.println("Starting Chat Server...");
		clients = new ArrayList<ClientConnection>();
		try
		{
			serverSocket = new ServerSocket(port);
			System.out.println("Listening on port "+port);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			quit = true;
			System.exit(1);
		}
		
		while(!quit)
		{
			try 
			{
				System.out.println("ServerSocket waiting for connection");
				Socket s = serverSocket.accept();
				System.out.println("Connection from client - will be ID "+nextclientid);
				ClientConnection client = new ClientConnection(nextclientid++);
				client.Init(s,this);
				clients.add(client);
				client.start();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
		}
	}
	
	public void Send(ChatMessage m, int originid)
	{
		if (m.key.equalsIgnoreCase("quit"))
		{
			System.out.println("System is quitting");
			quit=true;
		}
		else
			System.out.print("Sending \""+m.value+"\": ");
		for(ClientConnection c: clients)
		{
			if (c.getID() != originid || m.key.equalsIgnoreCase("quit")) // send text to all but origin, quit to all
			{
				try
				{
					System.out.print("-");
					c.Send(m);
					System.out.print("+ ");
				}
				catch(Exception e)
				{
					ClientError(c);
					System.out.print("X ");
				}
			}
		}
		System.out.println(" DONE");
		if (quit) System.exit(0);
	}
	
	public void ClientError(ClientConnection con)
	{
		clients.remove(con);
	}
	
	public void setPort(int p) { port = p; }
	public int getPort() { return port; }

}
