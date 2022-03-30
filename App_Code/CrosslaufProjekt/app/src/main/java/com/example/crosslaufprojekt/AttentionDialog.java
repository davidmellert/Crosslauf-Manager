package com.example.crosslaufprojekt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

//Ist ein Kind der Klasse DialogFragment, kann so ein Dialogfenster öffnen.
//Wird erstellt, wenn nochmal nachgefragt wird, ob man dies oder jenes wirklich machen möchte
public class AttentionDialog extends DialogFragment {

    //Hier wird der Nutzen des Dialogfenster gespeichert, welcher im Konstruktor übergeben werden muss.
    //Je nach Nutzen sieht das Fenster anders aus und ruft andere Methoden auf.
    private String usage;

    //Das Interface, damit wenn auf ok/weiter etc. gedrückt wurde die entsprechende Methode in der Klasse
    //MainActivity ausgeführt wird.
    private AttentionDialogListener listener;

    public AttentionDialog(String usage){
        this.usage = usage;
    }

    //Ein Dialogfenster wird erstellt
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Wenn das Fragment, welches für die Stoppuhr verantwotlich ist ein Dialogfenster öffnen möchte, um zu fragen, ob
        //man wirklich die Zeit zurücksetzen möchte wird folgendes Fenster erstellt und wenn auf den "Positiven Button" gedrückt wird,
        //dann wird die Methode onResetClicked() des Interface aufgerufen.
        if(usage == "resetChronometer"){
            builder.setTitle("Achtung!")
                    .setMessage("Bist du dir sicher, dass du die das Rennen neustarten willlst? Alle Zeiten und gescannten Qr-Codes dieses Rennens werden gelöscht!")
                    .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setPositiveButton("Rennen neustarten", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onNeustartClicked();
                }
            });

        //Hier passiert dasselbe, nur sieht das Fenster anders aus und die aufgerufene Methode ist eine andere
        }else if(usage == "closeConnection") {
            builder.setTitle("Achtung!")
                    .setMessage("Bist du dir sicher, dass du die Verbindung trennen willst? " +
                            "Die App funktioniert erst wieder, wenn das Programm auf dem PC gestartet wird.")
                    .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setPositiveButton("Weiter", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onWeiterClicked();
                }
            });
            //Hier passiert dasselbe, nur sieht das Fenster anders aus und die aufgerufene Methode ist eine andere
        }else if(usage == "rennenBeenden") {
            builder.setTitle("Achtung!")
                    .setMessage("Bist du dir sicher, dass du das Rennen beenden willst? Es können keine weiteren Zeiten übergeben werden! ")
                    .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setPositiveButton("Rennen beenden", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onRennenBeendenClicked();
                }
            });
        //Hier passiert dasselbe, nur sieht das Fenster anders aus und die aufgerufene Methode ist eine andere
        }else if(usage == "allesGescannt") {
            builder.setTitle("Achtung!")
                    .setMessage("Bist du dir sicher, dass du alles gescannt hast? Du kannst, nachdem du auf 'Weiter' gedrückt hast, nichts mehr einscannen!")
                    .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setPositiveButton("Weiter", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onNichtMehrScannenClicked();
                }
            });
        }
        return builder.create();
    }

    //Diese Methode sorgt dafür, dass die Klasse MainActivity die Methoden auch ausführt.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (AttentionDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+" must implement AttentionDialogListener");
        }
    }

    //Das Interface, welches von der MainActivity implementiert werden muss und die Methoden für die verschiedenen Dialogfenster enthält.
    public interface AttentionDialogListener{
        void onWeiterClicked();
        void onNeustartClicked();
        void onRennenBeendenClicked();
        void onNichtMehrScannenClicked();
    }
}
