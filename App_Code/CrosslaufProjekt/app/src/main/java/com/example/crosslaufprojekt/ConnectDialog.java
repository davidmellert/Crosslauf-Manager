package com.example.crosslaufprojekt;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

//Erstellt ein Dialogfenster mit zwei Eingabefeldern. Im Prinzip genauso, wie die Klasse AttentionDialog,
//nur das hier der Methode die beiden eingegebenen Werte übergeben werden und eine xml Datei als Layout verwendet wird
public class ConnectDialog extends AppCompatDialogFragment {
    private EditText editTextIp;
    private EditText editTextPort;
    private ConnectDialogListener listener;



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        builder.setView(view)
                .setTitle("Verbindung aufbauen")
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Verbinden", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ip = editTextIp.getText().toString();

                        int port = 0;
                        try {
                            port = Integer.parseInt(editTextPort.getText().toString());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            port = 8001;
                        }
                        listener.applyTexts(ip, port);
                    }
                });
        editTextIp = view.findViewById(R.id.edit_ip);
        editTextPort = view.findViewById(R.id.edit_port);

        //Die Eingabefelder werden befüllt mit den gespeicherten Werten für Port und Id
        MainActivity mainActivity = (MainActivity)getActivity();
        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences("SPFile", 0);
        editTextIp.setText(sharedPreferences.getString("IP", "192.168.178.61"));
        editTextPort.setText(""+sharedPreferences.getInt("Port", 8000));
        return builder.create();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ConnectDialogListener) context;
        } catch (ClassCastException e) {
           throw new ClassCastException(context.toString()+" must implement ConnectDialogListener");
        }
    }

    public interface ConnectDialogListener{
        void applyTexts(String ip, int port);
    }
}
