package com.coinomi.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coinomi.core.coins.CoinType;
import com.coinomi.core.network.CoinAddress;
import com.coinomi.core.wallet.WalletAccount;
import com.coinomi.stratumj.ServerAddress;
import com.coinomi.wallet.Constants;
import com.coinomi.wallet.R;
import com.coinomi.wallet.WalletApplication;
import com.coinomi.wallet.service.CoinService;
import com.coinomi.wallet.service.CoinServiceImpl;
import com.coinomi.wallet.ui.widget.AddCoinAddressView;
import com.coinomi.wallet.ui.widget.CoinAddressView;

import java.util.List;

import butterknife.ButterKnife;

public class CoinConnectionsActivity extends BaseWalletActivity {

    private AddCoinAddressView addCoinAddressView;
    private LinearLayout addressPlaceholder;
    private CoinAddressView.CoinAddressInterface coinAddressInterface;
    private CoinType coinType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_connections);
        ButterKnife.bind(this);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(false);
        }

        WalletApplication walletApplication = (WalletApplication) getApplication();
        String accountId = getIntent().getStringExtra(Constants.ARG_ACCOUNT_ID);

        WalletAccount account = walletApplication.getAccount(accountId);
        coinType = account != null ? account.getCoinType() : null;
        if (coinType != null) {
            showCoinConnections(coinType);
        } else {
            Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCoinConnections(CoinType coinType) {
        TextView tv = findViewById(R.id.coinConnectionTitle);
        tv.setText(getString(R.string.connections_used_addresses, coinType.getName()));

        addressPlaceholder = findViewById(R.id.coinAddressPlaceholder);

        for (CoinAddress address : Constants.getCoinsServerAddresses()) {
            if (address.getType() == coinType) {
                addAddresses(address.getAddresses());
                break;
            }
        }

        addCoinAddressView = new AddCoinAddressView(this);
        addCoinAddressView.setAddCoinListener((hostName, port) -> {
            addAddress(new ServerAddress(hostName, port, true, false));
        });
        addressPlaceholder.addView(addCoinAddressView);
    }

    private void addAddress(ServerAddress address) {
        addressPlaceholder.removeView(addCoinAddressView);

        CoinAddressView coinAddressView = new CoinAddressView(this);
        coinAddressView.setCoinAddressData(address);
        coinAddressView.setCoinAddressInterface(getCoinAddressInterface());
        addressPlaceholder.addView(coinAddressView);
        addressPlaceholder.addView(addCoinAddressView);

        getConfiguration().addCoinAddress(address, coinType);
    }

    private CoinAddressView.CoinAddressInterface getCoinAddressInterface() {
        if (coinAddressInterface == null) {
            coinAddressInterface = new CoinAddressView.CoinAddressInterface() {
                @Override
                public void onCoinAddressEnabled(boolean enabled, ServerAddress address) {
                    enableAddress(enabled, address);
                }

                @Override
                public void onCoinAddressDeleted(ServerAddress address) {
                    deleteAddress(address);
                }
            };
        }
        return coinAddressInterface;
    }

    private void addAddresses(List<ServerAddress> addresses) {
        for (ServerAddress serverAddress : addresses) {
            CoinAddressView coinAddressView = new CoinAddressView(this);
            coinAddressView.setCoinAddressData(serverAddress);
            coinAddressView.setCoinAddressInterface(getCoinAddressInterface());
            addressPlaceholder.addView(coinAddressView);
        }
    }

    private void enableAddress(boolean enabled, ServerAddress address) {
        address.setEnabled(enabled);
        getConfiguration().enableCoinAddress(address, coinType);
    }

    private void deleteAddress(ServerAddress address) {
        for (int i = 0; i < addressPlaceholder.getChildCount(); i++) {
            if (addressPlaceholder.getChildAt(i) instanceof CoinAddressView) {
                CoinAddressView addressView = (CoinAddressView) addressPlaceholder.getChildAt(i);
                if (addressView.address == address) {
                    addressPlaceholder.removeView(addressView);
                    getConfiguration().removeCoinAddress(address, coinType);
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        // refresh connections
        WalletApplication walletApplication = getWalletApplication();
        Intent intent = new Intent(CoinService.ACTION_CLEAR_CONNECTIONS, null,
                walletApplication, CoinServiceImpl.class);
        walletApplication.startService(intent);

        intent = new Intent(CoinService.ACTION_CONNECT_ALL_COIN, null,
                walletApplication, CoinServiceImpl.class);
        walletApplication.startService(intent);

        super.onBackPressed();
    }
}
