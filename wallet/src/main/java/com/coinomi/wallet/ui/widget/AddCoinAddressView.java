package com.coinomi.wallet.ui.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.coinomi.wallet.R;

public class AddCoinAddressView extends FrameLayout {

    private AddCoinListener addCoinListener;
    private Button addCoin;
    private EditText coinHostName;
    private EditText coinPort;
    private TextWatcher editTextWatcher;

    public AddCoinAddressView(@NonNull Context context) {
        this(context, null);
    }

    public AddCoinAddressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddCoinAddressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout();
    }

    private void initLayout() {
        inflate(getContext(), R.layout.view_add_coin_address, this);

        coinHostName = findViewById(R.id.coinHostname);
        coinHostName.addTextChangedListener(getTextWatcher());

        coinPort = findViewById(R.id.coinPortAddress);
        coinPort.addTextChangedListener(getTextWatcher());

        addCoin = findViewById(R.id.coinAddButton);
    }

    @Override
    protected void onDetachedFromWindow() {
        coinHostName.removeTextChangedListener(getTextWatcher());
        coinPort.removeTextChangedListener(getTextWatcher());
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        if (coinHostName != null) {
            coinHostName.addTextChangedListener(getTextWatcher());
        }
        if (coinPort != null) {
            coinPort.addTextChangedListener(getTextWatcher());
        }
        super.onAttachedToWindow();
    }

    private void enableAddCoinButton() {
        boolean enableButton = !TextUtils.isEmpty(coinHostName.getText().toString())
                && !TextUtils.isEmpty(coinPort.getText().toString());
        addCoin.setEnabled(enableButton);

        if (enableButton) {
            addCoin.setOnClickListener(v -> {
                if (addCoinListener != null) {
                    addCoinListener.onCoinAdded(coinHostName.getText().toString(), Integer.parseInt(coinPort.getText().toString()));
                }
                coinHostName.setText("");
                coinPort.setText("");
                addCoin.setEnabled(false);
            });
        } else {
            addCoin.setOnClickListener(null);
        }
    }

    public void setAddCoinListener(AddCoinListener coinListener) {
        this.addCoinListener = coinListener;
    }

    private TextWatcher getTextWatcher() {
        if (editTextWatcher == null) {
            editTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    enableAddCoinButton();
                }
            };
        }
        return editTextWatcher;
    }

    public interface AddCoinListener {
        void onCoinAdded(String hostName, int port);
    }
}
