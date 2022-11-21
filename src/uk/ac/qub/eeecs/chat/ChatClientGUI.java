package uk.ac.qub.eeecs.chat;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import javax.swing.*;

public class ChatClientGUI extends Thread {
	
	private String server = "127.0.0.1";
	private int port = 8080;
	private Socket socket;
	//private BufferedReader br;
	//private BufferedWriter bw;
	private ObjectOutputStream oouts;
	private ObjectInputStream oins;
	
	private boolean quit = false;
	
	private JFrame frame = new JFrame();
	
	private JTextField input = new JTextField();
	private JTextArea output = new JTextArea();
	private JButton button = new JButton("Send");
	
	public void setPort(int p) { port = p; }
	public int getPort() { return port; }
	public void setServer(String s) { server = s; }
	public String getServer() { return server; }
	
	public void Start()
	{
		output.setEditable(false);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Send();
			}
		});
		input.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {
				
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					Send();
			}
		});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(input, BorderLayout.NORTH);
		frame.getContentPane().add(output, BorderLayout.CENTER);
		frame.getContentPane().add(button, BorderLayout.SOUTH);
		
		frame.setSize(600, 400);
		frame.setVisible(true);
		
		Write("Starting connection to "+server+":"+port);
		try
		{
			System.out.println("Making socket connection");
			socket = new Socket(server, port);
			System.out.println("Opening ObjectInputStream");
			oins = new ObjectInputStream(socket.getInputStream());
			System.out.println("Opening ObjectOutputStream");
			oouts = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("Connected");
			this.start(); // start listening thread
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void Send() // send action
	{
		String msg = input.getText();
		input.setText("");
		try
		{
			System.out.println("Sending: "+msg);
			ChatMessage m = new ChatMessage();
			m.key="message";
			m.value=msg;
			// deal with /quit command
			if (msg.equalsIgnoreCase("/quit"))
			{
				m.key="quit";
			}
			oouts.writeObject(m);
			oouts.flush();
			Write(">> "+msg);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			quit=true;
		}
	}
	
	public void Write(String msg)
	{
		output.append(msg + "\n");
	}
	
	public void run()
	{
		while(!quit)
		{
			try
			{
				ChatMessage m = (ChatMessage) oins.readObject();
				if (m == null)
					throw new IOException("Broken pipe null from server");
				if (m.key.equalsIgnoreCase("quit")) // quit message from the server
				{
					System.out.println("Quit received from server - shutting down");
					quit=true;
					System.exit(0);
				}
				else
					Write(m.value);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				quit = true;
			}
		}
	}

}
