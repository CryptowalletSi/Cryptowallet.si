package com.coinomi.wallet.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.coinomi.stratumj.ServerAddress;
import com.coinomi.wallet.R;

public class CoinAddressView extends LinearLayout {

    private ImageView deleteAddress;
    private CheckBox coinAddressCheck;

    public ServerAddress address;
    private CoinAddressInterface coinAddressInterface;

    public CoinAddressView(Context context) {
        this(context, null);

    }

    public CoinAddressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoinAddressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout();
    }

    private void initLayout() {
        inflate(getContext(), R.layout.view_coin_address, this);
        deleteAddress = findViewById(R.id.coinAddressDelete);
        coinAddressCheck = findViewById(R.id.coinAddressCheck);

        deleteAddress.setOnClickListener(v -> {
            if (coinAddressInterface != null) {
                coinAddressInterface.onCoinAddressDeleted(address);
            }
        });

        coinAddressCheck.setOnCheckedChangeListener(getCheckedChangeListener());
    }

    public void setCoinAddressInterface(CoinAddressInterface coinAddressInterface) {
        this.coinAddressInterface = coinAddressInterface;
    }

    public void setCoinAddressData(ServerAddress address) {
        this.address = address;
        coinAddressCheck.setOnCheckedChangeListener(null);
        coinAddressCheck.setChecked(address.isEnabled());
        coinAddressCheck.setOnCheckedChangeListener(getCheckedChangeListener());
        coinAddressCheck.setEnabled(!address.isDefaultAddress());
        coinAddressCheck.setText(address.getHost() + ":" + address.getPort());
        deleteAddress.setVisibility(!address.isDefaultAddress() ? View.VISIBLE : View.GONE);
    }

    public interface CoinAddressInterface {

        void onCoinAddressEnabled(boolean enabled, ServerAddress address);
        void onCoinAddressDeleted(ServerAddress address);
    }

    private CompoundButton.OnCheckedChangeListener getCheckedChangeListener() {
        return (buttonView, isChecked) -> {
            if (coinAddressInterface != null && coinAddressCheck.isEnabled()) {
                coinAddressInterface.onCoinAddressEnabled(isChecked, address);
            }
        };
    }
}
