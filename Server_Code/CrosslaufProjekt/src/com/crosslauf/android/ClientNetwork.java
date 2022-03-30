package com.crosslauf.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;


public class ClientNetwork {
	
	public static void main(String[] args) {
		ClientNetwork client = new ClientNetwork("192.168.178.61", 8000);
		client.sendMessage("HalliHallo");
	}
	
	private InetSocketAddress address;
	
	public ClientNetwork(String hostname, int port) {
		address = new InetSocketAddress(hostname, port);
	}
	
	public void sendMessage(String message) {
		
		
		try {
			System.out.println("[Client] Verbinde zu Server...");
			Socket socket = new Socket();
			socket.connect(address, 10000);
			System.out.println("[Client] Client verbunden.");
			
			System.out.println("[Client] Sende Nachricht...");
			PrintWriter pw = new PrintWriter(new PrintWriter(new OutputStreamWriter(socket.getOutputStream())));
			pw.println(message);
			pw.flush();
			System.out.println("[Client] Nachricht gesendet.");
			
			Scanner s = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream())));
			if(s.hasNextLine()) {
				System.out.println("[Client] Nachricht vom Server: "+ s.nextLine());
			}
			
			//Verbindung schlieﬂen
			pw.close();
			s.close();
			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
