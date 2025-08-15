package com.coinomi.wallet.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.coinomi.nfc.NFCModule;
import com.coinomi.wallet.R;


public final class NFCScannerActivity extends BaseWalletActivity {

    public static final String INTENT_EXTRA_RESULT = "result";

    private static final int REQUEST_CODE_QR_SCAN = 1000;


    private ImageView nfcImageView;
    private final Handler handler = new Handler();
    private NFCModule nfcModule;

    public static Intent createIntent(Context context) {
        return new Intent(context, NFCScannerActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_scanner);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        nfcModule = new NFCModule(this);

        nfcImageView = findViewById(R.id.nfcScanImageView);
        findViewById(R.id.scaQRButton).setOnClickListener(v -> {
            startActivityForResult(new Intent(NFCScannerActivity.this, ScanActivity.class), REQUEST_CODE_QR_SCAN);
        });
        startBlinkingAnimation();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (nfcModule != null) {
            nfcModule.readTag(intent, s -> {
                setScannedResult(s);
                return null;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcModule != null) {
            nfcModule.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcModule != null) {
            nfcModule.onPause();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_QR_SCAN) {
            if (resultCode == Activity.RESULT_OK) {
                setScannedResult(data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT));
            }
        }
    }

    private void setScannedResult(String scannedResult) {
        final Intent result = new Intent();
        result.putExtra(INTENT_EXTRA_RESULT, scannedResult);
        setResult(RESULT_OK, result);
        new Handler().post(this::finish);
    }

    private void startBlinkingAnimation() {
        AlphaAnimation animation = new AlphaAnimation(1f, 0f);
        animation.setDuration(1000);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                handler.postDelayed(() -> {
                    if (nfcImageView.getAlpha() == 0f) {
                        nfcImageView.setAlpha(1f);
                    } else {
                        nfcImageView.setAlpha(0f);
                    }
                }, 0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        nfcImageView.startAnimation(animation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
