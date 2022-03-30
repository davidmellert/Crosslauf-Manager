package com.example.crosslaufprojekt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

//Visualisiert das fragment_scanner layout
public class ScannerFragment extends Fragment {

    //UI-Elemente
    private Button scanStartenButton;
    private TextView barcode;
    private EditText barcodeet;
    private TextView typ;
    private EditText typet;
    private Button allesGescanntButton;

    private MainActivity mainActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity)getActivity();
        mainActivity.setTitle("Scanner");

        scanStartenButton = view.findViewById(R.id.button_scanStarten);
        barcode = view.findViewById(R.id.textView_Barcode);
        barcodeet = view.findViewById(R.id.etBarcode);
        typ = view.findViewById(R.id.textView_typ);
        typet = view.findViewById(R.id.etTyp);
        allesGescanntButton = view.findViewById(R.id.button_allesGescannt);

        mainActivity.isStopwatchFragmentActive = false;

        //Wenn schon auf "alles Gescannt gedrückt wurde, dann wird alles bis auf den Überprüfungsbutton ausgeblendet
        if(mainActivity.scannerIsWaiting)
            aufRennEndeWarten();

    }

    //Es wird alles  bis auf den Überprüfungsbutton ausgeblendet
    public void aufRennEndeWarten(){
        scanStartenButton.setText("Überprüfen, ob Rennen beendet");
        barcode.setVisibility(View.INVISIBLE);
        barcodeet.setVisibility(View.INVISIBLE);
        barcodeet.setClickable(false);
        typ.setVisibility(View.INVISIBLE);
        typet.setVisibility(View.INVISIBLE);
        typet.setClickable(false);
        allesGescanntButton.setVisibility(View.INVISIBLE);
        allesGescanntButton.setClickable(false);
        mainActivity.scannerIsWaiting = true;
        mainActivity.scannerIsReallyWaiting = true;
    }
    //Es wird alles  bis auf den Überprüfungsbutton eingeblendet
    public void rennenBeendet() {
        scanStartenButton.setText("Scan starten");
        barcode.setVisibility(View.VISIBLE);
        barcodeet.setVisibility(View.VISIBLE);
        barcodeet.setClickable(true);
        typ.setVisibility(View.VISIBLE);
        typet.setVisibility(View.VISIBLE);
        typet.setClickable(true);
        allesGescanntButton.setVisibility(View.VISIBLE);
        allesGescanntButton.setClickable(true);
        mainActivity.scannerIsWaiting = false;
        mainActivity.scannerIsReallyWaiting = false;
    }

}
