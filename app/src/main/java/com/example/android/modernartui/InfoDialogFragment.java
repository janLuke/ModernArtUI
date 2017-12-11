package com.example.android.modernartui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class InfoDialogFragment extends DialogFragment {


    public InfoDialogFragment() {
        // Required empty public constructor
    }


    public static InfoDialogFragment newInstance() {
        return new InfoDialogFragment();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.fragment_info_dialog, null))
                .setTitle(R.string.more_info_dialog_title)
                .setPositiveButton(R.string.visit_MOMA_button_text, (dialogInterface, i) -> {
                    Intent visitMomaIntent = new Intent(Intent.ACTION_VIEW);
                    Uri momaUri = Uri.parse(getString(R.string.moma_link));
                    visitMomaIntent.setData(momaUri);
                    startActivity(visitMomaIntent);
                })
                .setNegativeButton(R.string.not_now_button_text, (dialogInterface, i) -> {
                    dialogInterface.cancel();
                });

        return builder.create();
    }

}
