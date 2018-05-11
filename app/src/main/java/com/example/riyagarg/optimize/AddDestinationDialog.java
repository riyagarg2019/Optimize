package com.example.riyagarg.optimize;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.data.Destination;

/**
 * Created by myradeng on 5/10/18.
 */


public class AddDestinationDialog extends DialogFragment {

    private TextView destName;


    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final Destination newDest = (Destination) getArguments().getSerializable("DEST");
        View input = getActivity().getLayoutInflater().inflate(R.layout.new_destination, null);
        destName = input.findViewById(R.id.destName);
        destName.setText(newDest.getLocation());

        builder.setView(input);

        builder.setPositiveButton("Save destination", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((MapActivity) getContext()).addDestinationToDatabase(newDest);
            }
        });

        builder.setNegativeButton("Discard destination", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        return dialog;
    }

}