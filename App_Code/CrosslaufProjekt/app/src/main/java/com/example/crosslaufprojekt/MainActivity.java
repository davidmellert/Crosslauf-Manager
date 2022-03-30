package com.example.crosslaufprojekt;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;


//Klasse der Hauptaktivität: Ist für die Verwaltung der Fragments und UI-Element verantwortlich und wird beim Start der Applikation ausgeführt.
//Sie implementiert verschiedene Listener, damit die Methoden dieser Interface ausgeführt werden können. (Wenn in einem Dialogfenster auf weiter/ok gedrückt wird oder in der Navigationleiste links etwas ausgewählt wird. )
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ConnectDialog.ConnectDialogListener, AttentionDialog.AttentionDialogListener, NewRaceDialog.NewRaceDialogListener {

    //Für die logische Schaltung des "Navigation Drawers"
    private DrawerLayout drawer;

    //Port und Ip zur Herstellung einer Verbindung zwischen Server und Client
    private String ip;
    private int port;

    //Hier wird die zu übermittelnde Nachricht gespeichert
    private String message;

    //Wahrheitswert, ob zurzeit ein Rennen läuft (sollte so sein, wie auf dem PC)
    public boolean rennenLäuft;

    //Wahrheitswerte, ob schon entweder alles gescannt oder die zeit fertig gestoppt wurde
    public boolean stoppuhrIsWaiting;
    public boolean scannerIsWaiting;

    //Diese Variable wird nur umgestellt wenn die UI geändert wird, also wenn z.B das Rennen neugestartet wird (Vom Stopwatch Fragment und einem anderen Gerät), dann ändert sich
    //zwar scannerIsWaiting, diese Variable aber nicht. So kann überprüft werden, ob das Interface schon wieder resettet wurde oder eben nicht.
    public boolean scannerIsReallyWaiting;


    //Variablen, damit die Stoppuhr auch weiterläuft, wenn man das Gerät kippt oder das Fragment wechselt
    //IsRunning ist true, wenn die Stoppuhr gestartet wurde und isStopwatchFragmentActive ist true wenn man gerade auf dem Stopwatch Fragment ist
    public boolean isRunning;
    public boolean isStopwatchFragmentActive;
    public long currentTime;
    public long base;

    //Im Hilfe-Menü kann es ein- und ausgestellt werden, ob die App beim Zeit übergeben vibriert oder nicht
    public boolean isVibrating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Befehle, damit der Drawer (das was man von links aufziehen kann) richtig funktioniert.
        Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        //Die Variablen für die IP und den Port werden aus den SharedPreferences geholt (Darin können Variablen auch nach Schließung der App gespeichert bleiben).
        SharedPreferences sharedPreferences = getSharedPreferences("SPFile", Context.MODE_PRIVATE);

        ip = sharedPreferences.getString("IP", "192.168.178.61");
        port = sharedPreferences.getInt("Port", 8001);
        isVibrating = sharedPreferences.getBoolean("isVibrating", true);

        rennenLäuft = false;

        stoppuhrIsWaiting = false;
        scannerIsWaiting = false;
        isRunning = false;
        isStopwatchFragmentActive = false;

        //Nur wenn savedInstanceState == null ist, also wenn die App neugestartet wurde, dann soll das Angezeigte Item das ScannerFragment sein
        //und auch links im Drawer ausgewählt sein.
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RennenFragment()).commit();
            sendMessage("GR");

        } else{
           base = savedInstanceState.getLong("base");
           rennenLäuft = savedInstanceState.getBoolean("rennenLäuft");
           isRunning = savedInstanceState.getBoolean("isRunning");
           stoppuhrIsWaiting = savedInstanceState.getBoolean("stoppuhrIsWaiting");
           isStopwatchFragmentActive = savedInstanceState.getBoolean("isStopwatchFragmentActive");
           scannerIsWaiting = savedInstanceState.getBoolean("scannerIsWaiting");

            System.out.println("Not null "+ isStopwatchFragmentActive);
            currentTime = savedInstanceState.getLong("time");
            System.out.println("currentTime: "+currentTime);


            System.out.println("Base jetzt auf: "+base);

            if(!isStopwatchFragmentActive && isRunning && rennenLäuft){
                base = SystemClock.elapsedRealtime() - currentTime;
            }
        }
    }

    //Wenn ein anderes Item in der Navigationsleiste ausgewählt wurde, dann soll das entsprechende Fragment/ Diaglogfenster geöffnet werden
    //und die Navigationsleiste wieder eingezogen werden.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_rennen:
                sendMessage("G");
                if(rennenLäuft) {
                    Toast.makeText(this, "Rennen läuft schon!", Toast.LENGTH_SHORT).show();
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    navigationView.setCheckedItem(R.id.nav_stopwatch);
                    System.out.println(base);
                    currentTime = SystemClock.elapsedRealtime() - base;
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StopwatchFragment()).commit();

                }else{
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RennenFragment()).commit();
                }
                break;
            case R.id.nav_stopwatch:
                System.out.println(base);
                currentTime = SystemClock.elapsedRealtime() - base;
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StopwatchFragment()).commit();
                break;
            case R.id.nav_scanner:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ScannerFragment()).commit();
                break;
            case R.id.nav_help:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HelpFragment()).commit();
                break;
            case R.id.nav_connect:
                openConnectDialog();
                break;
            case R.id.nav_close_connection:
                openAttentionDialog();
                break;



        }
        if(item.getItemId() != R.id.nav_stopwatch && !(item.getItemId() == R.id.nav_rennen && rennenLäuft)){
            if(isRunning && rennenLäuft) {
                base = SystemClock.elapsedRealtime() - currentTime;
            }
        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    //Wenn zurück gedrückt wird und die Navigationsleiste offen ist, wird diese geschlossen.
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Wenn der Button "Scan starten" gedrückt wurde wird diese Methode ausgeführt, die mit der API des ZXING Scanners einen Code einscannt.
    public void scanStarten(View v){
        if(!scannerIsWaiting && !scannerIsReallyWaiting) {
            sendMessage("G");
            if (rennenLäuft) {
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.initiateScan();
            } else {
                Toast.makeText(this, "Rennen läuft noch nicht!", Toast.LENGTH_SHORT).show();
            }
        }else if(!scannerIsWaiting && scannerIsReallyWaiting){
            ScannerFragment scannerFragment = (ScannerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            scannerFragment.rennenBeendet();
        }else if(scannerIsWaiting){
            sendMessage("GS");
        }



    }

    //Wenn ein Code eingescannt wurde, wird diese Methode aufgerufen, die die beiden Textfelder auf den gescannten Code und Typ setzen.
    //Anschließend wird der gescannte Code an den Server weitergeleitet. Am Anfang mit dem Buchstaben 'B' als Erkennungsbuchstaben.
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        EditText etBarcode = (EditText) findViewById(R.id.etBarcode);
        EditText etTyp = (EditText) findViewById(R.id.etTyp);
        if(resultCode == RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {

                String barcode = scanResult.getContents();
                String typ = scanResult.getFormatName();

                etBarcode.setText(barcode);
                sendMessage("B" + barcode);
                etTyp.setText(typ);
            }
        }else if(resultCode == RESULT_CANCELED){
            etBarcode.setText("Scan abgebrochen");
            etTyp.setText("/");
        }
    }
    //Es wird ein neuer Dialog geöffnet
    public void openConnectDialog(){
        ConnectDialog connectDialog = new ConnectDialog();
        connectDialog.show(getSupportFragmentManager(), "connect dialog");
    }

    //Die eingegebenen Werte im ConnectDialog werden in den lokalen Variablen und den SharedPreferences gespeichert, anschließend
    //wird eine Nachricht an den Server gesendet, um zu überprüfen, ob die Verbindung funktioniert, falls dem so ist erscheint ein Toast
    @Override
    public void applyTexts(String tempIp, int tempPort) {
        this.ip = tempIp;
        this.port = tempPort;

        SharedPreferences sharedPreferences = getSharedPreferences("SPFile", 0);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("IP", ip);
        editor.putInt("Port", port);

        editor.apply();
        sendMessage("F");
        System.out.println("IP: "+ sharedPreferences.getString("IP", "192.168.178.61")+" Port: "+ sharedPreferences.getInt("Port", 8001));

    }
    //Diese Methode wird von der Methode onNavigationItemSelected aufgerufen, wenn das gewählte Item CloseConnection ist.
    //Ein Dialog wird geöffnet, ob die Verbindung wirklich getrennt werden soll.
    public void openAttentionDialog(){
        AttentionDialog dialog = new AttentionDialog("closeConnection");
        dialog.show(getSupportFragmentManager(), "attentionDialogStopConnection");

    }

    //Wenn man auf den allesGescannt Button im Scanner Menü drückt wird dieses Menü aufgerufen
    public void openAttentionFertigDialog(View v){
        AttentionDialog dialog = new AttentionDialog("allesGescannt");
        dialog.show(getSupportFragmentManager(), "attention dialog scannenBeenden");
    }
    //Wenn die Verbindung wirklich getrennt werden soll, dann wird diese Methode aufgerufen und die Verbingung getrennt, indem eine
    //bestimmte Nachricht an den Server gesendet wird, der daraufhin sein Programm beendet und somit keine Verbindung mehr hergestellt werden kann.
    @Override
    public void onWeiterClicked() {

        sendMessage("A");
    }

    //Wenn aus dem stopWatch Fragment der Button reset gedrückt wird und dann das Dialogfenster fragt, ob man das wirklich tun will und dann auf Reset klickt,
    //wird diese Methode aufgerufen, die das Fragment findet und die Methode: resetChronometer aufruft. Außerdem wird dem Server gesagt, dass er das Rennen neustarten
    //soll, also den Inhalt der Queues löscht
    @Override
    public void onNeustartClicked(){
        sendMessage("H");

    }

    @Override
    public void onRennenBeendenClicked() {
        this.isRunning = false;
        sendMessage("EU");
    }

    @Override
    public void onNichtMehrScannenClicked() {
        sendMessage("ES");
    }

    //Ein neues Dialogfenster zum Auswählen des Jahrgangs und des Geschlecht wird geöffnet
    public void openNewRaceDialog(){
        NewRaceDialog dialog = new NewRaceDialog();
        dialog.show(getSupportFragmentManager(), "newRaceDialog");
    }

    //Diese Methode wird vom HelpFragment aufgerufen wenn der Button zu den Vibrationseinstellungen gedrückt wurde
    public void setIsVibrating(){
        isVibrating = !isVibrating;
        SharedPreferences sharedPreferences = getSharedPreferences("SPFile", 0);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isVibrating", isVibrating);

        editor.apply();
        if(isVibrating)
        Toast.makeText( this , "Vibration angeschaltet", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Vibration ausgeschaltet", Toast.LENGTH_SHORT).show();

    }

    //Wenn auf Rennen starten gedrückt wird, dann werden die eingegebenen Werte in einen String verpackt, der dann an den Server geschickt wird
    @Override
    public void applyJahrgangGeschlecht(int jahrgang, char geschlecht) {
        String msg = "D";
        if(jahrgangÜberprüfen(jahrgang)){
            msg += geschlecht;
            msg += jahrgang;
            sendMessage(msg);
        }else{
            Toast.makeText(this, "Fehler im Jahrgang! " + jahrgang+ " Erneut versuchen", Toast.LENGTH_SHORT).show();
        }

    }
    //Es wird überprüft, ob der Jahrgang vierstellig ist, wenn nicht, dann wird auch keine Nachricht an den Server gesendet
    private boolean jahrgangÜberprüfen(int jahrgang){
        String temp = ""+jahrgang;
        char[] tempcharArray = temp.toCharArray();
        if(tempcharArray.length == 4)
            return true;
        else
            return false;

    }

    //Dies ist eine Hilfmethode, um dem Server eine Nachricht zu schicken
    public void sendMessage(String msg){
        message = msg;
        new AndroidClient().execute();
    }


    //Diese Innere Klasse ist für die Verbindung zwischen dem Handy und dem PC zuständig und baut die Verbindung zum Server auf.
    //Sie erbt von der Klasse AsyncTask, damit dieser Thread nicht auf dem UIThread läuft, was die App zum Abstürzen bringt
    //Eine Klasse die von AyncTask erbt kann vier Methoden haben: onPreExecute, onProgressUpdate, doInBackground und onPostExecute
    private class AndroidClient extends AsyncTask<Void, Void, char[]>{
        //Zu Beginn wird die Adresse, um sich mit dem Server zu verbinden mit den Variablen ip und port erstellt und lokale Variable msg auf
        //Varible message der äußeren Klasse gesetzt, denn dadurch, dass die Methode doInBackground im Hintergrund läuft, kann sich bevor eine Serververbindung
        //hergestellt wurde, die Nachricht schon wieder geändert haben
        SharedPreferences sharedPreferences = getSharedPreferences("SPFile", 0);

        InetSocketAddress address = new InetSocketAddress(sharedPreferences.getString("IP", "192.168.178.61"), sharedPreferences.getInt("Port", 8001));
        String msg = message;



        //Diese Methode wird mit AndroidClient.execute() aufgerufen und gibt einen char Array zurück, welcher der Methode onPostExecute übergeben wird.
        //Sie baut eine Verbindung zum Server auf, verschickt die übergebene Nachricht und erhält dann eine Antwort des Servers, welche dann als charArray
        //der onPostExecute Methode übergeben wird.
        @Override
        protected char[] doInBackground(Void... voids) {
            try {
                System.out.println("[Client] Verbinde zu Server...");
                Socket socket = new Socket();
                System.out.println("[Client] Socket erstellt.");
                socket.connect(address);
                System.out.println("[Client] Client verbunden.");

                System.out.println("[Client] Sende Nachricht...");
                PrintWriter pw = new PrintWriter(new PrintWriter(new OutputStreamWriter(socket.getOutputStream())));
                pw.println(msg);
                pw.flush();
                System.out.println("[Client] Nachricht gesendet.");

                Scanner s = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream())));

                char[] messageArray;
                if(s.hasNextLine()) {
                    System.out.println("[Client] Nachricht vom Server erhalten");
                    String serverMessage = s.nextLine();
                    messageArray = serverMessage.toCharArray();


                }else{
                    messageArray = null;
                }

                //Verbindung schließen
                pw.close();
                s.close();
                socket.close();

                return messageArray;

            } catch (IOException e) {
                e.printStackTrace();
                char[] array = new char[1];
                array[0] = '1';
                return array;
            }


        }
        //Der charArray wird untersucht, welche Nachricht er enthält und die entsprechende Methode aufgerufen.
        //Damit der Nutzer auch ein visuelles Feedback in Form eines "Toasts" bekommt, müssen externe Methoden aufgerufen werden.
        @Override
        protected void onPostExecute(char[] messageArray) {
            super.onPostExecute(messageArray);
            if(messageArray[0] == 'A'){
                receivedA();
            }else if(messageArray[0] == 'B'){
                receivedB(messageArray);
            }else if(messageArray[0] == 'C'){
                receivedC();
            }else if(messageArray[0] == 'D'){
                String serverMsg = erstenBuchstabenEntfernen(messageArray);
                receivedD(serverMsg);
            }else if(messageArray[0] == 'E'){
                String serverMsg = erstenBuchstabenEntfernen(messageArray);
                receivedE(serverMsg);
            }else if(messageArray[0] == 'F'){
                receivedF();
            }else if(messageArray[0] == 'G'){
                String serverMsg = erstenBuchstabenEntfernen(messageArray);
                receivedG(serverMsg);
            }else if(messageArray[0] == 'H'){
                receivedH();
            }else if(messageArray[0] == 'I'){
                receivedI();
            }else if(messageArray[0] == '!'){
                receivedError();
            }else if(messageArray[0] == '1'){
                receivedNoConnection();
            }else{
                System.out.println("Fehler in der Nachricht des Servers!");
            }
        }
    }

    //Dieselbe Methode, wie auch in der Klasse NetzwerkServer, die auf dem PC laufen sollte.
    //Sie sorgt dafür, dass der erste Buchstabe (Erkennungsbuchstabe) der erhaltenen Nachricht vom Server gelöscht wird,
    //damit weitere Botschaften "entschlüsselt" werden können.
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

    //Erfolgreich die Verbindung zum Server getrennt
    private void receivedA(){
        Toast.makeText(this, "Verbindung getrennt", Toast.LENGTH_SHORT).show();
        System.out.println("Verbindung erfolgreich getrennt");
    }

    //Erfolgreich den QR-Code übermittelt
    private void receivedB(char[] serverMessage){
        if(serverMessage[1] == 'E'){
            Toast.makeText(this, "Erfolgreich gescannt", Toast.LENGTH_SHORT).show();
            System.out.println("Erfolgreich gescannt");
        }else if(serverMessage[1] == 'N'){
            Toast.makeText(this, "Scan abgebrochen", Toast.LENGTH_SHORT).show();
            System.out.println("Scan abgebrochen");
        }
    }
    //Erfolgreich die Zeit übermittelt
    private void receivedC(){
        if(isVibrating) {
            if (Build.VERSION.SDK_INT >= 26) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);
            }
        }
        Toast.makeText(this, "Zeit erfolgreich übertragen", Toast.LENGTH_SHORT).show();
        System.out.println("Zeit erfolgreich übertragen");
    }
    //Antwort des Servers, ob die Einstellungen für das nächste Rennen korrekt waren
    private void receivedD(String serverMsg){
        char[] gArray = serverMsg.toCharArray();
        if(gArray[0] == 'E'){
            //Einstellungen waren richtig
            rennenLäuft = true;

            //Zum anderen Fragment umleiten
            NavigationView navigationView = findViewById(R.id.nav_view);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StopwatchFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_stopwatch);

            Toast.makeText(this, "Renndaten erfolgreich übertragen", Toast.LENGTH_SHORT).show();
            System.out.println("Renneinstellungen waren korrekt");
        }else if(gArray[0] == 'F'){
            //Einstellungen waren falsch
            System.out.println("Renneinstellungen waren nicht korrekt");
        }else if(gArray[0] == 'R') {
            //Es wurde bereits ein Rennen gestartet
            Toast.makeText(this, "Es läuft bereits ein Rennen!", Toast.LENGTH_SHORT).show();

            NavigationView navigationView = findViewById(R.id.nav_view);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ScannerFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_scanner);

        }else
        {
            System.out.println("Fehler in der Nachricht des Servers, erster Buchstabe D!");
        }
    }

    //Die Zeit und oder der Scan für dieses Rennen wurde beendet
    private void receivedE(String serverMsg){
        char[] serverArray = serverMsg.toCharArray();
        //Zeit und Rennen sind fertig und die Daten der Queues wurden in die Datenbank übertragen
        if(serverArray[0] == 'E') {
            rennenLäuft = false;
            if(scannerIsWaiting) {
                scannerIsWaiting = false;
            }
            if(stoppuhrIsWaiting) {
                stoppuhrIsWaiting = false;
            }
            if(isRunning && isStopwatchFragmentActive){
                StopwatchFragment stopwatchFragment = (StopwatchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                stopwatchFragment.resetChronometer();
            }
            System.out.println("Daten erfolgreich an die Datenbank übertragen und Rennen beendet");
            Toast.makeText(this, "Daten übertragen und Rennen beendet", Toast.LENGTH_SHORT).show();
            //Machen, dass man wieder den Scanner/Stoppuhr sieht

            //QR-Codes wurden fertig eingescannt
        } else if (serverArray[0] == 'H') {
            //Machen, dass man nicht mehr QR-Codes einscannen kann
            Toast.makeText(this, "QR-Codes fertig gescannt", Toast.LENGTH_SHORT).show();
            ScannerFragment scannerFragment = (ScannerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            scannerFragment.aufRennEndeWarten();
            //Die Zeit wurde angehalten
        }else if(serverArray[0] == 'F'){
            //Machen, dass man nicht mehr die Stoppuhr bedienen kann
            Toast.makeText(this, "Zeit wurde fertig gestoppt", Toast.LENGTH_SHORT).show();
            StopwatchFragment stopwatchFragment = (StopwatchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            stopwatchFragment.resetChronometer();
            stopwatchFragment.aufRennEndeWarten();

            //Es wurde schon dem Server gemeldet, dass die Codes eingescannt wurden
        }else if(serverArray[0] == 'S'){
            Toast.makeText(this, "Die Codes wurden schon fertig eingescannt", Toast.LENGTH_SHORT).show();
            ScannerFragment scannerFragment = (ScannerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            scannerFragment.aufRennEndeWarten();
            //Es wurde schon dem Server gemeldet, dass die Zeit fertig gestoppt wurde
        }else if(serverArray[0] == 'Z'){
            Toast.makeText(this, "Die Zeit wurde schon gestoppt", Toast.LENGTH_SHORT).show();
            StopwatchFragment stopwatchFragment = (StopwatchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            stopwatchFragment.resetChronometer();
            stopwatchFragment.aufRennEndeWarten();
        }
        else{
            System.out.println("Fehler in der Nachricht des Servers, erster Buchstabe E!");
        }
    }

    //Antwort des Servers erhalten, also funktioniert die Verbindung
    private void receivedF(){
        Toast.makeText(this, "Verbindung erfolgreich hergestellt", Toast.LENGTH_SHORT).show();
        System.out.println("Verbindung erfolgreich");
    }

    //Antwort des Servers auf die Anfrage, ob das Rennen läuft
    private void receivedG(String serverMsg){
        char[] gArray = serverMsg.toCharArray();
        if(gArray[0] == 'J'){
            rennenLäuft = true;
            System.out.println("Rennen läuft");
            if(gArray[1] == 'R'){
                currentTime = SystemClock.elapsedRealtime() - base;
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StopwatchFragment()).commit();

            }            //Toast.makeText(this, "Rennen läuft noch", Toast.LENGTH_SHORT).show();
        }else if(gArray[0] == 'N'){
            rennenLäuft = false;
            System.out.println("Rennen läuft nicht");
            //Die Stoppuhr wollte es wissen und wird nun wieder zum normalen Bildschirm gesetzt
            if(gArray[1] == 'U'){
                StopwatchFragment stopwatchFragment = (StopwatchFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                stopwatchFragment.resetChronometer();
                stopwatchFragment.rennenBeendet();
                //Der Scanner wollte es wissen
            }else if(gArray[1] == 'S'){
                ScannerFragment scannerFragment = (ScannerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                scannerFragment.rennenBeendet();
            }
        }else{
            System.out.println("Fehler in der Nachricht des Servers, erster Buchstabe G!");
        }
    }

    //Rennen wurde neugestartet
    private void receivedH(){
        Toast.makeText(this, "Rennen neugestartet!", Toast.LENGTH_SHORT).show();
        System.out.println("Rennen neugestartet");
        StopwatchFragment stopwatchFragment = (StopwatchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        stopwatchFragment.resetChronometer();
        if(scannerIsWaiting)
            scannerIsWaiting = false;
    }

    //Es gibt eine Rückmeldung vom Server, da es ansonsten eine Fehlermeldung geben würde
    private void receivedI(){
        System.out.println("Dem Server wurde erfolgreich mitgeteilt, dass die Stoppuhr gestartet wurde");
    }

    //Server hat einen Fehler im Übertragenen String erkannt
    private void receivedError(){
        Toast.makeText(this, "Fehler in der Übertragung zum Server", Toast.LENGTH_SHORT).show();
        System.out.println("Der Server hat einen Fehler im Übertragenen String erkannt");
    }

    //Es konnte keine Verbindung zum Server aufgebaut werden
    private void receivedNoConnection(){
        Toast.makeText(this, "Keine Verbindung!", Toast.LENGTH_LONG).show();
        System.out.println("Keine Verbindung zum Server.");
    }


    //Hier können Variablen zwischengespeichert werden, die erhalten bleiben sollen, wenn die Orientation sich ändert,
    //da dann die App praktisch neugestartet wird
     @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        currentTime = SystemClock.elapsedRealtime() - base;
        outState.putLong("time", currentTime);
        outState.putLong("base", base);
        outState.putBoolean("isRunning", isRunning);
        outState.putBoolean("isStopwatchFragmentActive", isStopwatchFragmentActive);
        outState.putBoolean("stoppuhrIsWaiting", stoppuhrIsWaiting);
        outState.putBoolean("scannerIsWaiting", scannerIsWaiting);
        outState.putBoolean("rennenLäuft", rennenLäuft);
    }


}
