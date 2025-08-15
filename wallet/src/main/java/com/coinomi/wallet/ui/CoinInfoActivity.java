package com.coinomi.wallet.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coinomi.core.coins.CoinType;
import com.coinomi.core.coins.LanacoinMain;
import com.coinomi.core.coins.TajcoinMain;
import com.coinomi.core.wallet.WalletAccount;
import com.coinomi.wallet.Constants;
import com.coinomi.wallet.R;
import com.coinomi.wallet.WalletApplication;

import java.util.Locale;

public class CoinInfoActivity extends BaseWalletActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_info);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(false);
        }

        WalletApplication walletApplication = (WalletApplication) getApplication();
        String accountId = getIntent().getStringExtra(Constants.ARG_ACCOUNT_ID);

        WalletAccount account = walletApplication.getAccount(accountId);
        CoinType coinType = account != null ? account.getCoinType() : null;
        if (coinType != null) {
            showCoinInfo(coinType);
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

    private void showCoinInfo(CoinType coinType) {

        setupTitleAndImage(coinType);

        int stringArrayId = -1;
        if (coinType instanceof LanacoinMain) {
            stringArrayId = R.array.lana_coin;
        } else if (coinType instanceof TajcoinMain) {
            stringArrayId = R.array.taj_coin;
        }
        addLinks(stringArrayId);
    }

    private void setupTitleAndImage(CoinType coinType) {
        TextView title = findViewById(R.id.coinInfoTitle);
        title.setText(coinType.getName() + " - " + coinType.getSymbol().toUpperCase(Locale.ENGLISH));

        ImageView imageView = findViewById(R.id.coinInfoIv);
        imageView.setImageResource(Constants.COINS_ICONS.get(coinType));
    }

    private void addLinks(int stringArrayId) {
        LinearLayout placeHolder = findViewById(R.id.coinInfoLinkPlaceHolder);
        if (stringArrayId != -1) {
            String[] coinLinks = getResources().getStringArray(stringArrayId);
            for (String s : coinLinks) {
                View infoLinkView = LayoutInflater.from(this).inflate(R.layout.view_coin_info_link, null);

                s = s.replaceFirst(": ", ":\n");
                TextView coinLinkTv = infoLinkView.findViewById(R.id.coinInfoLink);
                coinLinkTv.setText(s);

                placeHolder.addView(infoLinkView);
            }
        }
    }
}
