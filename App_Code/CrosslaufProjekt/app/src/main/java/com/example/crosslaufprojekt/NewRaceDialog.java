package com.example.crosslaufprojekt;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;


public class NewRaceDialog extends AppCompatDialogFragment {
    private EditText editTextJahrgang;

    private NewRaceDialogListener listener;

    private boolean isMännlich = true;


    //Die Auswahl zwischen entweder männlich oder weiblich
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private RadioButton radioButtonMännlich;
    private RadioButton radioButtonWeiblich;




    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_neuesrennen, null);

        builder.setView(view)
                .setTitle("Neues Rennen")
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Rennen starten", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        char geschlecht;
                        if(isMännlich)
                            geschlecht = 'm';
                        else
                            geschlecht = 'w';


                        int jahrgang = 0;
                        try {
                            jahrgang = Integer.parseInt(editTextJahrgang.getText().toString());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            //Sollte immer funktionieren, da man nur Zahlen eingeben kann
                        }
                        listener.applyJahrgangGeschlecht(jahrgang, geschlecht);
                    }
                });
        editTextJahrgang = view.findViewById(R.id.edit_jahrgang);
        radioGroup = view.findViewById(R.id.radioGroup);

        radioButtonMännlich = view.findViewById(R.id.radio_männlich);
        radioButtonWeiblich = view.findViewById(R.id.radio_weiblich);

        radioButtonMännlich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkButton(v);
            }
        });
        radioButtonWeiblich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkButton(v);
            }
        });






        return builder.create();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (NewRaceDialogListener) context;
        } catch (ClassCastException e) {
           throw new ClassCastException(context.toString()+" must implement NewRaceDialogListener");
        }
    }

    //Interface, damit die MainActivity auf das Ergebnis des Dialogfensters zugreifen kann
    public interface NewRaceDialogListener{
        void applyJahrgangGeschlecht(int jahrgang, char geschlecht);
    }


    //Wenn ein button gedrückt wird, dann wird der Boolean isMännlich entsprechend verändert
    private void checkButton(View view){
        int radioId = radioGroup.getCheckedRadioButtonId();
        radioButton = view.findViewById(radioId);
        if(radioButton.getText() == "männlich")
            isMännlich = true;
        else
            isMännlich = false;
    }
}
