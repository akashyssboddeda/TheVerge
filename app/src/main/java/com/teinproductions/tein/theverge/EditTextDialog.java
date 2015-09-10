package com.teinproductions.tein.theverge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.EditText;

import java.io.Serializable;


public class EditTextDialog extends DialogFragment {
    private static final String LISTENER = "LISTENER";
    private static final String DIALOG_TITLE = "DIALOG_TITLE";

    private EditText editText;
    private OnSearchClickListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        editText = new EditText(getActivity());
        editText.setPadding(20, 10, 20, 10);

        String title = getArguments().getString(DIALOG_TITLE);
        listener = (OnSearchClickListener) getArguments().getSerializable(LISTENER);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(editText)
                .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) listener.onClickSearch(editText.getText().toString());
                    }
                })
                .setNeutralButton(android.R.string.cancel, null)
                .create();
    }

    interface OnSearchClickListener extends Serializable {
        void onClickSearch(String query);
    }

    public static void show(FragmentManager fm, String title, OnSearchClickListener listener) {
        EditTextDialog fragment = new EditTextDialog();
        Bundle args = new Bundle();
        args.putSerializable(LISTENER, listener);
        args.putString(DIALOG_TITLE, title);
        fragment.setArguments(args);

        fragment.show(fm, "editTextDialog_" + title);
    }
}
