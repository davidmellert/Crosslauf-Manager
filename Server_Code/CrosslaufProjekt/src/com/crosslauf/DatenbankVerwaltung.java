package com.crosslauf;

//Diese Klasse erhält die Renndaten vom Network Server und überträgt diese nach Abschluss des Rennens in
//die Access Datenbank
public class DatenbankVerwaltung {
	
	//Verantwortlich für die Verbindung zwischen dem JavaScript und der Datenbank
	private DatabaseConnector dc;
	
	//Variable um die Position während der Übertragung in die Datenbank zu behalten
	private int nächstePosition;
	
	//Queues in denen die Namen und Zeiten während eines Rennens zwischengespeichert werden
	private Queue<String> namen;
	private Queue<String> zeiten;
	private Queue<String> serverZeiten;
	
	//Strings in denen der Jahrgang und das Geschlecht des aktuellen Rennens gespeichert wird
	private String jahrgang;
	private String geschlecht;
	
	//Ist true sobald ein Client ein neues Rennen startet und wird wieder false, wenn alles gescannt und 
	//fertig gestoppt wurde
	private boolean rennenLäuft;
	
	public DatenbankVerwaltung() {
		dc = new DatabaseConnector("", 0, "Crosslauf.accdb","", "");
		namen = new Queue<String>();
		zeiten = new Queue<String>();
		serverZeiten = new Queue<String>();
	
		rennenLäuft = false;
		
		jahrgang = "2020";
		geschlecht = "männlich";	
				
	}
	
	
	//Diese Methode wird aufgerufen, wenn das Rennen neugestartet werden soll. Dafür werden die Queues geleert
	public void rennenNeustarten() {
		namen = new Queue<String>();
		zeiten= new Queue<String>();
		serverZeiten= new Queue<String>();
	}
	
	//Die gescannten QrCodes werden in einer Schlange gesammelt,
	//wo sie dann nachher, wenn das Rennen beendet wurde in die Datenbank übertragen werden können.
	//Aber nur wenn der QrCode auch im richtigen Bereich liegt
	public void codesInQueueEinfügen(String qrcode) {
		try {
			int temp = Integer.parseInt(qrcode);
			if(temp>0 && temp<1000)
				namen.enqueue(qrcode);
        } catch (NumberFormatException e) {
           // e.printStackTrace();
        }
	}
	
	//Die Zeiten werden der Queue angehängt
	public void zeitenInQueueEinfügen(long zeit) {
		
		zeiten.enqueue(zeitUmformen(zeit));
	}
	
	
	//Die Server Zeiten werden der Queue angehängt
	public void serverZeitenInQueueEinfügen(long serverZeit) {
		serverZeiten.enqueue(zeitUmformen(serverZeit));
	}
	
	//Die Queues werden in die Datenbank übertragen, sodass anschließend beide Queues wieder
	//leer sind und die Position, Zeit in Verbindung mit der SchülerID in der Datenbank gespeichert ist
	public void queuesÜbertragen() {
		//int nächsterEintrag = findNextFreeEntry();
		int nächsterEintrag = 1;
		nächstePosition = 1;
		
		while(!namen.isEmpty()&& !zeiten.isEmpty()) {
		
		
		
			String nächsteSchülerID = namen.front();
			
			String nächsteZeit = zeiten.front();
			
			String nächsteServerZeit = serverZeiten.front();
			
			dc.executeStatement("INSERT INTO Position (ID, Position, SchuelerID, Zeit, Jahrgang, Geschlecht, Zeit_beim_Scannen) VALUES ("+nächsterEintrag+", "+nächstePosition+", '"+nächsteSchülerID+"', '"+nächsteZeit+"', '"+jahrgang+"', '"+geschlecht+"', '"+nächsteServerZeit+"')");
			System.out.println(/*"ID: "+nächsterEintrag+", "*/"Position: "+nächstePosition+", SchülerID "+nächsteSchülerID+", Zeit: "+nächsteZeit+", Jahrgang: "+jahrgang+", Geschlecht: "+geschlecht+", Zeit beim Scannen: "+nächsteServerZeit);
			nächsterEintrag++;
			nächstePosition++;
			namen.dequeue();
			zeiten.dequeue();
			serverZeiten.dequeue();
		
		
		
		}
		if(!namen.isEmpty()) {
			System.out.println("Mehr Namen als Zeiten eingescannt. Folgende SchülerIDs waren noch übrig: ");
			while(!namen.isEmpty()) {
				System.out.println("Name: "+namen.front()+", Zeit beim Scannen: "+serverZeiten.front());
				namen.dequeue();
				serverZeiten.dequeue();
			}
		}else if(!zeiten.isEmpty()) {
			System.out.println("Öfter die Zeit gestoppt, als Namen eingescannt. Folgende Zeiten waren noch vorhanden: ");
			while(!zeiten.isEmpty()) {
				System.out.println(zeiten.front()+", ");
				zeiten.dequeue();
			}
		}else {
			System.out.println("Dieselbe Anzahl an Namen, wie Zeiten.");
		}
	}
	
	//Setter für den Jahrgang und das Geschlecht des aktuellen Rennens
	public void setJahrgang(String jahrgang) {
		this.jahrgang = jahrgang;
		
	}
	public void setGeschlecht(String geschlecht) {
		this.geschlecht = geschlecht;
	}
	
	
	//Getter des Geschlechts
	public String getGeschlecht() {
		return geschlecht;
	}
	
	//Setter, ob das Rennen läuft
	public void setRennenLäuft(boolean temp) {
		this.rennenLäuft = temp;
	}
	
	//Getter, ob das Rennen läuft
	public boolean getRennenLäuft() {
		return this.rennenLäuft;
	}
	
	
	//Die Zeit in ms wird in die Form 00:00 umgeformt, falls jemand länger als eine Stunde braucht (was nicht der Fall sein wird) 
	//sieht dieser String zwar komisch aus, ist aber noch lesbar
	public String zeitUmformen(long timeLong){
		int timeMinutenInt = (int)timeLong/60000;
		timeLong = timeLong%60000;
		int timeSekundenInt = (int)timeLong/1000;
		if(timeSekundenInt < 10) {
			return timeMinutenInt+":0"+timeSekundenInt;
		}else {
			return timeMinutenInt+":"+timeSekundenInt;
		}
	}
}
