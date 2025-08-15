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

public class SkipPart2DialogFragment extends DialogFragment {

    private WelcomeFragment.Listener mListener;

    public static SkipPart2DialogFragment newInstance(String seed) {
        SkipPart2DialogFragment newDialog = new SkipPart2DialogFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_SEED, seed);
        newDialog.setArguments(args);
        return newDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (WelcomeFragment.Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement WelcomeFragment.OnFragmentInteractionListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String seed = getArguments().getString(Constants.ARG_SEED);
        String dialogMessage = getResources().getString(R.string.restore_skip_part_two_info) + "\n\n" +
                seed + "\n\n" + getResources().getString(R.string.restore_skip_info2);
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.restore_skip_title)
                .setMessage(dialogMessage)
                .setPositiveButton(R.string.button_skip, (dialog, which) -> {
                    if (mListener != null) mListener.onSeedVerified(getArguments());
                })
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> dismiss());

        return builder.create();
    }
}
