package com.coinomi.wallet.ui.common;


import androidx.fragment.app.Fragment;

import butterknife.Unbinder;

public class BaseFragment extends Fragment {

    protected Unbinder unbinder;

    protected void setBinder(Unbinder binder) {
        this.unbinder = binder;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
