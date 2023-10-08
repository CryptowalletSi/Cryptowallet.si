package com.coinomi.wallet.ui.crypto_water;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.coinomi.core.coins.AquariuscoinMain;
import com.coinomi.core.coins.LanacoinMain;
import com.coinomi.core.coins.TajcoinMain;
import com.coinomi.wallet.R;
import com.coinomi.wallet.tasks.CheckCryptoWaterTask;
import com.coinomi.wallet.tasks.TasksLoader;
import com.coinomi.wallet.ui.BaseWalletActivity;
import com.coinomi.wallet.util.TimeUtils;

public class CryptoWaterActivity extends BaseWalletActivity {

   private static final String EXTRA_ADDRESS = "extraAddress";

   public static Intent createIntent(Context context, String address) {
      Intent intent = new Intent(context, CryptoWaterActivity.class);
      intent.putExtra(EXTRA_ADDRESS, address);
      return intent;
   }

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_crypto_wallet);

      TasksLoader.INSTANCE.getCryptoWaterHash(data -> {
         findViewById(R.id.loadingContainer).setVisibility(View.GONE);
         if (data == null) {
            Toast.makeText(this, "CryptoWater hash not found", Toast.LENGTH_LONG).show();
            finish();
         } else {
            showData(data);
         }
      }, getExtraAddress());
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(false);
   }

   private void showData(CheckCryptoWaterTask.CryptoWaterResponse data) {
      String ticker = CheckCryptoWaterTask.getTickerFromAddress(data.address);
      int iconResource = R.drawable.lanacoin;
      String title = LanacoinMain.get().getSymbol();
      switch (ticker) {
         case CheckCryptoWaterTask.TICKER_LANA:
            iconResource = R.drawable.lanacoin;
            break;
         case CheckCryptoWaterTask.TICKER_ARCO:
            iconResource = R.drawable.aquariuscoin;
            title = AquariuscoinMain.get().getSymbol();
            break;
         case CheckCryptoWaterTask.TICKER_TAJ:
            iconResource = R.drawable.tajcoin;
            title = TajcoinMain.get().getSymbol();
            break;
      }

      TextView titletext = findViewById(R.id.coinTitle);
      titletext.setText(title);

      ImageView coinIcon = findViewById(R.id.coinIcon);
      coinIcon.setImageResource(iconResource);

      TextView coinAddress = findViewById(R.id.coinAddress);
      coinAddress.setText(data.address);

      TextView coinBalance = findViewById(R.id.coinBalance);
      coinBalance.setText(data.balance);

      TextView hashTime = findViewById(R.id.hashDate);
      hashTime.setText(TimeUtils.toTimeString(this, Long.parseLong(data.firstBlockTimestamp)));
   }

   private String getExtraAddress() {
      return getIntent().getStringExtra(EXTRA_ADDRESS);
   }
}
