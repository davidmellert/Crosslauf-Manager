package com.example.crosslaufprojekt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

//Es wird ein Fragment erstellt mit dem Layout fragment_help
public class HelpFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Wenn der Button gedrückt wird, dann wird geändert, ob das Gerät bei einer neuen Zeit vibriert
        final Button vibrationEinstellungen = view.findViewById(R.id.button_vibration);
        vibrationEinstellungen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrationEinstellungen();
            }
        });

        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.isStopwatchFragmentActive = false;
        mainActivity.setTitle("Hilfe");
    }

    public void vibrationEinstellungen(){
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.setIsVibrating();
    }
}
