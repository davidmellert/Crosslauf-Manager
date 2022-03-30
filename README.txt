Grundidee: 
Zeiten und Reihenfolge eines Rennens per App in eine Datenbank übertragen

Dafür hat jeder Teilnehmer eine feste ID und einen Zettel mit einem QR-Code dieser ID. Am Ziel werden dann die Zeiten
festgehalten und gleichzeitig die QR Codes in der richtigen Reihenfolge gescannt. Somit kann am Ende jedem Teilnehmer 
automatische eine Position und die entsprechende Zeit zu geordnet werden.

Im Ordner App und Server sind die exportierten Anwendungen zu finden, also die Apk der App, 
die auf einem Android-Gerät installiert werden kann und die .jar Datei des Servers. Diese bitte mit Hilfe der
.bat Datei oder der Verknüpfung "Crosslauf Manager" im ersten Ordner (wo auch diese READ_ME Datei liegt) ausführen.
Damit die Verbindung funktioniert müssen sie bei der App unter Verbinden die Ip Adresse ihres PCs angeben und
den Port 8000. (Das handy muss dabei im selben Netzwerk sein wie der PC)

Nach erfolgreicher Verbindung, kann in der App nun ein neues Rennen gestartet werden, wobei der Jahrgang und das Geschlecht ausgewählt werden muss.
Anschließend muss beim Start die Stoppuhr gestartet werden. Nun kann jedesmal wenn ein neuer Schüler das Ziel erreicht
in der App der mitlere Button in der Stoppuhr gedrückt werden um die zeit festzuhalten.
Währenddessen kann ein zweites Gerät die QR Codes der Schüler in der richtigen Reihenfolge einscannen.
Sobald alle Schüler im Ziel sind, und alle QR Codes gescannt wurden, wird das Rennen beendet und die Daten automatisch
in die Datenbank übertragen.

In den Ordnern ..._Code sind die Codes der Projekte zu finden. Also den Ordner "CrosslaufProjekt" im Server_Code
Ordner mit Eclipse ausführen und den "CrosslaufProjekt" Ordner im App_Code Ordner mit Android Studio öffnen.

Die Qr-Codes sind zum Testen beigelegt und wichtig ist, dass die Übertragung in die Datenbank nicht richtig funktioniert,
wenn sie gerade geöffnet ist. Also erst das Programm laufen lassen und Scannen und Stoppen und erst anschließend die
Datenbank öffnen. Ansonsten laden die neuen Datensätze irgendwie nicht und wenn nur nach ca. 30 Sekunden.

Die Verknüpfung: "Crosslauf Manager" soll das Icon im selben Ordner haben und die .bat Datei im Server Ordner
ausführen, aber wahrscheinlich wird das Icon nur weiß angezeigt und die Verknüpfung funktioniert nicht. :(

