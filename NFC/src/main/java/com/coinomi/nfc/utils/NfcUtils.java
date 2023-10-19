package com.coinomi.nfc.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

public class NfcUtils {

    public static void showEnableNFCDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enable NFC");
        builder.setMessage("Do you want to enable NFC for scanning NFT tag?");
        builder.setPositiveButton("OK", (dialog, which) -> {
            // User clicked OK, open NFC settings
            openNfcSettings(context);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // User clicked Cancel, do nothing or handle as needed
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static void openNfcSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
        try {
            context.startActivity(intent);
        } catch (Throwable throwable) {
            // Handle the case where the device doesn't have an activity to handle the NFC settings intent
            Toast.makeText(context, "NFC settings not available on this device", Toast.LENGTH_SHORT).show();
        }
    }
}
