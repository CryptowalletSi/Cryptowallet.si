package com.coinomi.wallet.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

import com.coinomi.wallet.Constants;
import com.coinomi.wallet.R;
import com.coinomi.wallet.ui.WelcomeFragment;

public class SkipDialogFragment extends DialogFragment {

        public static SkipDialogFragment newInstance(String seed) {
            SkipDialogFragment newDialog = new SkipDialogFragment();
            Bundle args = new Bundle();
            args.putString(Constants.ARG_SEED, seed);
            newDialog.setArguments(args);
            return newDialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String seed = getArguments().getString(Constants.ARG_SEED);
            String dialogMessage = getResources().getString(R.string.restore_skip_info1) + "\n\n" +
                    seed + "\n\n" + getResources().getString(R.string.restore_skip_info2);
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.restore_skip_title)
                   .setMessage(dialogMessage)
                   .setPositiveButton(R.string.button_skip, (dialog, which) -> {
                       dismiss();
                       SkipPart2DialogFragment skipPart2DialogFragment = SkipPart2DialogFragment.newInstance(seed);
                       skipPart2DialogFragment.show(getFragmentManager(), null);
                   })
                   .setNegativeButton(R.string.button_cancel, (dialog, which) -> dismiss());

            return builder.create();
        }
    }
