package com.crosslauf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.Scanner;


//Der Network Server ist die Verbindung zwischen dem Android Client und dem PC, 
//auf welchem dieses Programm l�uft
public class NetworkServer {
	
	//Start-Methode
	public static void main(String[] args) {
		NetworkServer server = new NetworkServer(8000);
		server.startListening();
	}
	
	private int port;
	
	//Solange dieser Boolean auf true ist, l�uft der Server
	private boolean isRunning = true;
	
	//Diese beiden Variablen sind daf�r da, um erst dann die Daten in die DB zu
	//�bertragen, wenn die Zeit gestoppt und alle Codes gescannt wurden.
	private boolean hatGescannt = false;
	private boolean hatZeitGestoppt = false;
	
	//Der Network Server gibt die Daten des Clients weiter an die dbv
	private DatenbankVerwaltung dbv;
	
	//Um auch die Zeit zu haben, wann ein Code gescannt wurde, wird diese Base ben�tigt
	private long base;
	
	public NetworkServer(int port) {
		this.port = port;
		dbv = new DatenbankVerwaltung();
	}
	
	
	//�ffnet eine Server Socket auf dem in der Startmethode gew�hlten Port, zu der sich die Android Clients verbinden k�nnen
	public void startListening() {
		
		System.out.println("[Server] Server starten...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				while(isRunning) {
					try {
						
						ServerSocket serverSocket = new ServerSocket(port);
						//System.out.println("[Server] Warten auf Verbindung...");
						Socket remoteClientSocket = serverSocket.accept();
						//System.out.println("[Server] Client verbunden.");
						
						Scanner s = new Scanner(new BufferedReader(new InputStreamReader(remoteClientSocket.getInputStream())));
						PrintWriter pw = new PrintWriter(new PrintWriter(new OutputStreamWriter(remoteClientSocket.getOutputStream())));
						if(s.hasNextLine()) {
							//System.out.println("[Server]Nachricht erhalten");
							String message = s.nextLine();
							char[] messageArray = message.toCharArray();
							
							
							
							//Die Verbindung soll getrennt werden
							if(messageArray[0] == 'A') {
								isRunning = false;
								System.out.println("[Server] Verbindung trennen.");
								
								pw.println("AVerbindung getrennt.");
								
								
							}
							//Ein QR-Code wurde gescannt
							else if(messageArray[0] == 'B') {
								
								//Den ersten Buchstaben entfernen, da dieser nur der Erkennungsbuchstabe ist
								String qrcode = erstenBuchstabenEntfernen(messageArray);
								if(qrcode != null) {
									
									//Zeit in Millisekunden seit dem Start der Stoppuhr berechnen
									long aktuelleZeit = Instant.now().toEpochMilli();
									long serverZeit = aktuelleZeit - base;
									System.out.println("Gescannter Barcode: "+ qrcode + " nach "+ serverZeit +" ms.");
									
									//Code und Serverzeit an Datenbank weiterleiten
									dbv.codesInQueueEinf�gen(qrcode);
									dbv.serverZeitenInQueueEinf�gen(serverZeit);
									
									pw.println("BErfolgreich gescannt");
									
								}else {
									pw.println("BNull bekommmen");
								}
								
								
								
								
							//Eine gestoppte Zeit wurde �bertragen
							}else if(messageArray[0] == 'C') {
								//Den ersten Buchstaben entfernen, da dieser nur der Erkennungsbuchstabe ist
								long time = Long.parseLong(erstenBuchstabenEntfernen(messageArray));
								
								System.out.println("Gestoppte Zeit in ms: "+time);
								
								
								
								//Zeit an Datenbank weiterleiten
								dbv.zeitenInQueueEinf�gen(time);
								
								pw.println("CZeit erhalten");
								
							//Der Jahrgang und das Geschlecht wurden beim Start des Rennens in der Form: Dm2002 �bertragen 
							}else if(messageArray[0] == 'D') {
								//�berpr�fen, ob das Rennen schon von einem anderen Ger�t gestartet wurde
								if(!dbv.getRennenL�uft()) {
									if(messageArray[1]=='m') {
										dbv.setGeschlecht("m�nnlich");
									}else if(messageArray[1]=='w') {
										dbv.setGeschlecht("weiblich");
									}else {
										System.out.println("Fehler im String!!!: Kein Geschlecht erkannt:"+ messageArray[1]);
									}
									String temp = erstenBuchstabenEntfernen(messageArray);
									messageArray = temp.toCharArray();
									
									//�berpr�fen, ob der Array die richtige L�nge hat, also vor dem Entfernen des Geschlechts noch 5 Buchstaben
									if(messageArray.length == 5) {
										String jahrgang = erstenBuchstabenEntfernen(messageArray);
										dbv.setJahrgang(jahrgang);
										dbv.setRennenL�uft(true);
										pw.println("DErfolgreich Jahrgang und Geschlecht �bertragen");
										System.out.println("Rennen gestartet: Jahrgang: "+jahrgang+" Geschlecht: "+dbv.getGeschlecht());
									}else {
										System.out.println("Fehler im String, hat nicht die richtige L�nge.");
										pw.println("DFehler im String");
									}
									
								}else {
										pw.println("DRennenL�uftSchon");
								}
								
							//Das Rennen wurde beendet und die Queues werden in die Datenbank �bertragen
							}else if(messageArray[0] == 'E'){
								//Es wurde fertig gescannt
								if(messageArray[1] == 'S') {
									if(!hatGescannt) {
										if(!hatZeitGestoppt) {
											hatGescannt = true;
											pw.println("EHat fertig gescannt");
										}else {
											dbv.queues�bertragen();
											hatGescannt = false;
											hatZeitGestoppt = false;
											dbv.setRennenL�uft(false);
											pw.println("EErfolgreich Daten �bertragen und Rennen beendet");
										}
										
										
									}else {
										pw.println("ESchon fertig gescannt");
									}
									//Es wurde fertig die Zeit gestoppt
								}else if(messageArray[1] == 'U') {
									if(!hatZeitGestoppt) {
										if(!hatGescannt){
											hatZeitGestoppt = true;
											pw.println("EFertig die Zeit gestoppt");
											System.out.println("Fertig die Zeit gestoppt");
										}else {
											dbv.queues�bertragen();
											hatGescannt = false;
											hatZeitGestoppt = false;
											dbv.setRennenL�uft(false);
											pw.println("EErfolgreich Daten �bertragen und Rennen beendet");
										}
									}else {
										pw.println("EZeit wurde schon gestoppt");
									}
								}else {
									pw.println("!Fehler im String!");
								}
								
								
							//Verbindung wird getestet
							}else if(messageArray[0] == 'F') {
								pw.println("FVerbindung erfolgreich");
								
							//Anfrage, ob das Rennen l�uft
							}else if(messageArray[0] == 'G') {
								if(dbv.getRennenL�uft()) {
									if(messageArray.length > 1) {
										if(messageArray[1] == 'R')
											pw.println("GJR");
										else pw.println("GJa, l�uft");
										
									}
										pw.println("GJa, l�uft");
								}else {
									if(messageArray.length > 1)
										pw.println("GN"+messageArray[1]+"ein, l�uft nicht");
									else
										pw.println("GNein, l�uft nicht");
								}
							//Rennen wird neugestartet
							}else if(messageArray[0] == 'H') {
								System.out.println("Rennen wird neugestartet!");
								dbv.rennenNeustarten();
								hatZeitGestoppt = false;
								hatGescannt = false;
								pw.println("H");
								
								//Stoppuhr wird gestartet und die Base auf die aktuelle Zeit gesetzt
							}else if(messageArray[0] == 'I') {
								System.out.println("Stoppuhr wird gestartet!");
								Instant instant = Instant.now();
								base = instant.toEpochMilli();	
								//System.out.println(""+base);
								pw.println("I");
							}else {
							
								System.out.println("Fehler im String!!!"+message);
								pw.println("!Fehler in der �bertragung!");
							}
							pw.flush();
						}
						
						
						//Verbindung schlie�en
						s.close();
						pw.close();
						remoteClientSocket.close();
						serverSocket.close();
						
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}).start();
		}
	
	//Der erste Buchstabe eines CharArrays wird entfernt und gibt den verbleibenden String zur�ck
	private String erstenBuchstabenEntfernen(char[] messageArray) {
		for(int i=0; i<messageArray.length-1; i++) {
			messageArray[i]= messageArray[i+1];
		}
		String tempString = "";
		for(int i=0; i<messageArray.length-1; i++) {
			tempString += messageArray[i];
			
		}
		return tempString;
	}
}