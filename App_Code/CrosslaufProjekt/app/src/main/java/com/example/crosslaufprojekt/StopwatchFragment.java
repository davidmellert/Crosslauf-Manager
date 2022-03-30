package com.example.crosslaufprojekt;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
//Visualisiert das fragment_stopwatch layout und ist verantwortlich für die enthaltenen Buttons
public class StopwatchFragment extends Fragment{

    //Für die Logik der Stoppuhr
    private Chronometer chronometer;
    private long pauseOffset;

    //UI-Elemente
    private Button startStopButton;
    private Button resetButton;
    private ImageView addPersonButton;

    MainActivity mainActivity;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stopwatch, container, false);
    }

    //Eine Art Konstruktor, der nachdem das View erstellt wurde aufgerufen wird und den Buttons einen OnClickListener gibt, damit bestimmmte
    //Methoden aufgerufen werden nachdem ein Knopf gedrückt wurde
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mainActivity = (MainActivity)getActivity();

        mainActivity.setTitle("Stoppuhr");
        chronometer = view.findViewById(R.id.chronometer);

        mainActivity.isStopwatchFragmentActive = true;



        startStopButton = view.findViewById(R.id.startstopButton);
        addPersonButton = view.findViewById(R.id.addPersonButton);
        resetButton = view.findViewById(R.id.resetButton);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startstopChronometer();
            }
        });

        addPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPerson();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAttentionDialog("resetChronometer");
            }
        });


        if(mainActivity.stoppuhrIsWaiting){
            aufRennEndeWarten();
        }else{
            System.out.println("IsRunning: "+ mainActivity.isRunning + " RennenLäuft: "+ mainActivity.rennenLäuft);
            if(mainActivity.isRunning && mainActivity.rennenLäuft) {
                setChronometerOnTime(mainActivity.currentTime);
            }
        }

    }

    //Die Stoppuhr wird gestartet, bzw. gestoppt
    public void startstopChronometer(){
        if(!mainActivity.stoppuhrIsWaiting) {
            mainActivity.sendMessage("G");
            if(mainActivity.rennenLäuft){
                if (!mainActivity.isRunning) {
                    mainActivity.sendMessage("I");
                    startStopButton.setText("Rennen beenden");
                    chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                    mainActivity.base = SystemClock.elapsedRealtime() - pauseOffset;
                    chronometer.start();
                    mainActivity.isRunning = true;
                    chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                        @Override
                        public void onChronometerTick(Chronometer chronometer) {
                            mainActivity.currentTime =  SystemClock.elapsedRealtime() - chronometer.getBase();
                        }
                    });
                } else {
                    openAttentionDialog("rennenBeenden");
                }
            }else{
                Toast.makeText(mainActivity.getApplicationContext(), "Rennen läuft noch nicht!", Toast.LENGTH_SHORT).show();
            }

        }else{
            mainActivity.sendMessage("GU");
        }
    }

    //Die Stoppuhr wird zurückgesetzt (Diese Methode wird von der MainActivity ausgeführt, nachdem der AttentionDialog geschlossen wurde,
    // denn der Listener funktioniert nicht im Fragment)
    public void resetChronometer(){
        startStopButton.setText("Rennen starten");
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
        mainActivity.isRunning = false;
        mainActivity.currentTime = 0;
        mainActivity.base = 0;
    }

    //Es wird eine Nachricht mit der Zeit an den Server gesendet, wenn die Stoppuhr gerade läuft
    public void addPerson() {

        String time = String.valueOf(SystemClock.elapsedRealtime() - chronometer.getBase());
        if(mainActivity.isRunning) {
            mainActivity.sendMessage("C" + time);
            //Toast.makeText(mainActivity.getApplicationContext(), time, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mainActivity.getApplicationContext(), "Stoppuhr pausiert!", Toast.LENGTH_SHORT ).show();
        }

    }

    //Wenn der Reset Button gedrückt wird, dann wird erst ein Attention Dialog geöffnet
    public void openAttentionDialog(String usage){
        AttentionDialog dialog = new AttentionDialog(usage);
        dialog.show(mainActivity.getSupportFragmentManager(), "attention dialog reset");
    }

    //Es wird alles  bis auf den Überprüfungsbutton ausgeblendet
    public void aufRennEndeWarten(){

        startStopButton.setText("Überprüfen, ob Rennen beendet");

        resetButton.setClickable(false);
        resetButton.setVisibility(View.INVISIBLE);

        addPersonButton.setVisibility(View.INVISIBLE);
        addPersonButton.setClickable(false);

        chronometer.setVisibility(View.INVISIBLE);

        mainActivity.stoppuhrIsWaiting = true;
    }

    //Es wird alles  bis auf den Überprüfungsbutton eingeblendet
    public void rennenBeendet(){
        startStopButton.setText("Rennen starten");

        resetButton.setClickable(true);
        resetButton.setVisibility(View.VISIBLE);

        addPersonButton.setVisibility(View.VISIBLE);
        addPersonButton.setClickable(true);

        chronometer.setVisibility(View.VISIBLE);

        mainActivity.stoppuhrIsWaiting = false;
    }

    public void setChronometerOnTime(long time){
        startStopButton.setText("Rennen beenden");
        chronometer.setBase(SystemClock.elapsedRealtime() - mainActivity.currentTime);
        chronometer.start();
        mainActivity.isRunning = true;
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                mainActivity.currentTime =  SystemClock.elapsedRealtime() - chronometer.getBase();

            }
        });
    }


}
