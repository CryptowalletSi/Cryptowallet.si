package com.coinomi.wallet.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.coinomi.wallet.R;

/**
 * @author John L. Jegutanis
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
