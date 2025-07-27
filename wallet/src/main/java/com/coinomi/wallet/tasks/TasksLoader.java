package com.coinomi.wallet.tasks;

import java.util.List;

public enum TasksLoader {
    INSTANCE;

    public void loadPartnersData(HttpRequestsFactory.Response<List<PartnerData>> responseListener, String uri) {
        new GetPartnersDataTask(responseListener, uri).execute(0);
    }

    public void getCryptoWaterHash(HttpRequestsFactory.Response<CheckCryptoWaterTask.CryptoWaterResponse> responseListener, String address) {
        new CheckCryptoWaterTask(responseListener, address).execute(0);
    }
}
