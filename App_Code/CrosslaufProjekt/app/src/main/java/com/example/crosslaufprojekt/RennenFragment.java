package com.example.crosslaufprojekt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

//Visualisiert das fragment_rennen layout
public class RennenFragment extends Fragment {

    private MainActivity mainActivity;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rennen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity)getActivity();
        mainActivity.isStopwatchFragmentActive = false;
        mainActivity.setTitle("Crosslauf Manager");

        Button neuesRennen = view.findViewById(R.id.button_neues_rennen);
        final Button rennenLaden = view.findViewById(R.id.button_rennen_laden);

        neuesRennen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                neuesRennen();
            }
        });

        rennenLaden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rennenLaden();
            }
        });

    }

    //Der Dialog zum Rennen starten wird geöffnet
    public void neuesRennen(){
        mainActivity.openNewRaceDialog();
    }

    //Die Idee dieses Buttons war, dass man eine Anfrage an den Server schickt, um eine Liste der verschiedenen Rennen zu bekommen
    //und dann könnte man eins auswählen bei dem dann die Platzierungen angezeigt werden. (Nicht notwendig und nicht Datenschutzrechtlich)
    public void rennenLaden(){
        Toast.makeText(mainActivity.getApplicationContext(), "Noch nicht verfügbar!", Toast.LENGTH_SHORT).show();
    }


}
