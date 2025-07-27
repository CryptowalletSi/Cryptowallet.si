package com.coinomi.wallet.tasks;

import com.google.gson.reflect.TypeToken;

import java.util.List;

public class GetPartnersDataTask extends GenericTask<List<PartnerData>> {
    private String uri;

    public GetPartnersDataTask(HttpRequestsFactory.Response<List<PartnerData>> response, String uri) {
        super(response);
        this.uri = uri;
    }

    @Override
    protected List<PartnerData> processData() {
        return HttpRequestsFactory.createGetRequest(uri, new TypeToken<List<PartnerData>>(){}.getType());
    }
}
