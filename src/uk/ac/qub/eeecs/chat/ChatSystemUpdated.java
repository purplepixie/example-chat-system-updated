package uk.ac.qub.eeecs.chat;

public class ChatSystemUpdated {

	public static void main(String[] args) {
		
		// Some default credentials
		String server = "127.0.0.1";
		int port = 8080;
		
		
		if (args.length > 1)
			port = Integer.parseInt(args[1]);
		if (args.length > 2)
			server = args[2];

		if (args.length < 1)
		{
			error();
			System.exit(1);
		}
		
		if (args[0].equalsIgnoreCase("server"))
		{
			ChatServer cs = new ChatServer();
			cs.setPort(port);
			cs.Start();
		}
		else if(args[0].equalsIgnoreCase("client"))
		{
			ChatClientGUI cc = new ChatClientGUI();
			cc.setPort(port);
			cc.setServer(server);
			cc.Start();
		}
		else
		{
			error();
		}
		
	}
	
	public static void error()
	{
		System.out.println("Usage: command client|server [port] [server]");
	}

}
