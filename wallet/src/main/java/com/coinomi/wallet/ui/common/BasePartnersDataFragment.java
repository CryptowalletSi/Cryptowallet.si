package com.coinomi.wallet.ui.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.coinomi.core.coins.nxt.Constants;
import com.coinomi.wallet.R;
import com.coinomi.wallet.tasks.GetPartnersDataTask;
import com.coinomi.wallet.tasks.HttpRequestsFactory;
import com.coinomi.wallet.tasks.TasksLoader;

import java.util.List;

import butterknife.BindView;

public class BasePartnersDataFragment extends BaseFragment {

    @Nullable
    @BindView(R.id.iv_first_partner)
    ImageView firstPartnerIv;
    @Nullable
    @BindView(R.id.iv_second_partner)
    ImageView secondPartnerIv;

    @Nullable
    @BindView(R.id.iv_third_partner)
    ImageView thirdPartnerIv;

    private HttpRequestsFactory.Response<List<GetPartnersDataTask.PartnerData>> partnerResponse;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (enableLoadingPartnersData()) {
            Log.e("PARTNER", "load partners data ->" + this.getClass().getSimpleName());
            loadPartnersData();
        }
    }

    protected void loadPartnersData() {
        partnerResponse = data -> {
            if (data != null && !data.isEmpty()) {
                try {
                    GetPartnersDataTask.PartnerData data1 = data.get(0);
                    GetPartnersDataTask.PartnerData data2 = null;
                    GetPartnersDataTask.PartnerData data3 = null;

                    if (data.size() > 1) {
                        data2 = data.get(1);
                    }

                    if (data.size() > 2) {
                        data3 = data.get(2);
                    }
                    loadPartnersImages(data1, data2, data3);
                } catch (Throwable ignored) {
                    Log.e("PARTNER", "Error loading images: ", ignored);
                }
            }
        };
        TasksLoader.INSTANCE.loadPartnersData(partnerResponse, getPartnersDataUri());
    }

    private void loadPartnersImages(
            GetPartnersDataTask.PartnerData data1,
            GetPartnersDataTask.PartnerData data2,
            GetPartnersDataTask.PartnerData data3) {
        boolean showBothImages = showBothImages();
        if (data1 == null) {
            firstPartnerIv.setVisibility(View.GONE);
        } else {
            firstPartnerIv.setVisibility(View.VISIBLE);
            setPartnerData(data1, firstPartnerIv);
        }

        Log.e("PARTNER", "Show data images ->" + this.getClass().getSimpleName());

        if (data2 == null || !showBothImages) {
            secondPartnerIv.setVisibility(View.GONE);
        } else {
            secondPartnerIv.setVisibility(View.VISIBLE);
            setPartnerData(data2, secondPartnerIv);
        }

        if (data3 == null || !showBothImages) {
            thirdPartnerIv.setVisibility(View.GONE);
        } else {
            thirdPartnerIv.setVisibility(View.VISIBLE);
            setPartnerData(data3, thirdPartnerIv);
        }
    }

    private void setPartnerData(GetPartnersDataTask.PartnerData data, ImageView imageView) {
        if (data != null) {
            Glide.with(this)
                    .load(data.imageUrl)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
            imageView.setOnClickListener(v -> openPartnerLink(data.link));
        }
    }

    private void openPartnerLink(String link) {
        Uri uri = Uri.parse(link); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        partnerResponse = null;
    }

    protected boolean enableLoadingPartnersData() {
        return false;
    }

    protected boolean showBothImages() {
        return true;
    }

    protected String getPartnersDataUri() {
        return Constants.PARTNERS_URI_OVERVIEW;
    }
}
