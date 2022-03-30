package com.crosslauf;

//Diese Klasse erh�lt die Renndaten vom Network Server und �bertr�gt diese nach Abschluss des Rennens in
//die Access Datenbank
public class DatenbankVerwaltung {
	
	//Verantwortlich f�r die Verbindung zwischen dem JavaScript und der Datenbank
	private DatabaseConnector dc;
	
	//Variable um die Position w�hrend der �bertragung in die Datenbank zu behalten
	private int n�chstePosition;
	
	//Queues in denen die Namen und Zeiten w�hrend eines Rennens zwischengespeichert werden
	private Queue<String> namen;
	private Queue<String> zeiten;
	private Queue<String> serverZeiten;
	
	//Strings in denen der Jahrgang und das Geschlecht des aktuellen Rennens gespeichert wird
	private String jahrgang;
	private String geschlecht;
	
	//Ist true sobald ein Client ein neues Rennen startet und wird wieder false, wenn alles gescannt und 
	//fertig gestoppt wurde
	private boolean rennenL�uft;
	
	public DatenbankVerwaltung() {
		dc = new DatabaseConnector("", 0, "Crosslauf.accdb","", "");
		namen = new Queue<String>();
		zeiten = new Queue<String>();
		serverZeiten = new Queue<String>();
	
		rennenL�uft = false;
		
		jahrgang = "2020";
		geschlecht = "m�nnlich";	
				
	}
	
	
	//Diese Methode wird aufgerufen, wenn das Rennen neugestartet werden soll. Daf�r werden die Queues geleert
	public void rennenNeustarten() {
		namen = new Queue<String>();
		zeiten= new Queue<String>();
		serverZeiten= new Queue<String>();
	}
	
	//Die gescannten QrCodes werden in einer Schlange gesammelt,
	//wo sie dann nachher, wenn das Rennen beendet wurde in die Datenbank �bertragen werden k�nnen.
	//Aber nur wenn der QrCode auch im richtigen Bereich liegt
	public void codesInQueueEinf�gen(String qrcode) {
		try {
			int temp = Integer.parseInt(qrcode);
			if(temp>0 && temp<1000)
				namen.enqueue(qrcode);
        } catch (NumberFormatException e) {
           // e.printStackTrace();
        }
	}
	
	//Die Zeiten werden der Queue angeh�ngt
	public void zeitenInQueueEinf�gen(long zeit) {
		
		zeiten.enqueue(zeitUmformen(zeit));
	}
	
	
	//Die Server Zeiten werden der Queue angeh�ngt
	public void serverZeitenInQueueEinf�gen(long serverZeit) {
		serverZeiten.enqueue(zeitUmformen(serverZeit));
	}
	
	//Die Queues werden in die Datenbank �bertragen, sodass anschlie�end beide Queues wieder
	//leer sind und die Position, Zeit in Verbindung mit der Sch�lerID in der Datenbank gespeichert ist
	public void queues�bertragen() {
		//int n�chsterEintrag = findNextFreeEntry();
		int n�chsterEintrag = 1;
		n�chstePosition = 1;
		
		while(!namen.isEmpty()&& !zeiten.isEmpty()) {
		
		
		
			String n�chsteSch�lerID = namen.front();
			
			String n�chsteZeit = zeiten.front();
			
			String n�chsteServerZeit = serverZeiten.front();
			
			dc.executeStatement("INSERT INTO Position (ID, Position, SchuelerID, Zeit, Jahrgang, Geschlecht, Zeit_beim_Scannen) VALUES ("+n�chsterEintrag+", "+n�chstePosition+", '"+n�chsteSch�lerID+"', '"+n�chsteZeit+"', '"+jahrgang+"', '"+geschlecht+"', '"+n�chsteServerZeit+"')");
			System.out.println(/*"ID: "+n�chsterEintrag+", "*/"Position: "+n�chstePosition+", Sch�lerID "+n�chsteSch�lerID+", Zeit: "+n�chsteZeit+", Jahrgang: "+jahrgang+", Geschlecht: "+geschlecht+", Zeit beim Scannen: "+n�chsteServerZeit);
			n�chsterEintrag++;
			n�chstePosition++;
			namen.dequeue();
			zeiten.dequeue();
			serverZeiten.dequeue();
		
		
		
		}
		if(!namen.isEmpty()) {
			System.out.println("Mehr Namen als Zeiten eingescannt. Folgende Sch�lerIDs waren noch �brig: ");
			while(!namen.isEmpty()) {
				System.out.println("Name: "+namen.front()+", Zeit beim Scannen: "+serverZeiten.front());
				namen.dequeue();
				serverZeiten.dequeue();
			}
		}else if(!zeiten.isEmpty()) {
			System.out.println("�fter die Zeit gestoppt, als Namen eingescannt. Folgende Zeiten waren noch vorhanden: ");
			while(!zeiten.isEmpty()) {
				System.out.println(zeiten.front()+", ");
				zeiten.dequeue();
			}
		}else {
			System.out.println("Dieselbe Anzahl an Namen, wie Zeiten.");
		}
	}
	
	//Setter f�r den Jahrgang und das Geschlecht des aktuellen Rennens
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
	
	//Setter, ob das Rennen l�uft
	public void setRennenL�uft(boolean temp) {
		this.rennenL�uft = temp;
	}
	
	//Getter, ob das Rennen l�uft
	public boolean getRennenL�uft() {
		return this.rennenL�uft;
	}
	
	
	//Die Zeit in ms wird in die Form 00:00 umgeformt, falls jemand l�nger als eine Stunde braucht (was nicht der Fall sein wird) 
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
