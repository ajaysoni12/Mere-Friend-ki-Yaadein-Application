package com.example.merefriendskiyaden;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

public class ProgessDialog {

    private AlertDialog loadingDialog;
    private Context context;

    public ProgessDialog(Context context) {
        this.context = context;
    }

    public AlertDialog getLoadingDialog() {
        return loadingDialog;
    }

    public void setLoadingDialog(AlertDialog loadingDialog) {
        this.loadingDialog = loadingDialog;
    }

    public void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_loading, null);

        builder.setView(dialogView);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    public void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}
