package com.coinomi.wallet.tasks;

import com.coinomi.core.util.CryptoWaterUtils;


public class CheckCryptoWaterTask extends GenericTask<CheckCryptoWaterTask.CryptoWaterResponse> {

   private String uri;

   private static final String URL = "https://chainz.cryptoid.info/%s/api.dws?q=addressinfo&a=%s";

   public static final String TICKER_LANA = "lana";
   public static final String TICKER_TAJ = "taj";
   public static final String TICKER_ARCO = "arco";

   public CheckCryptoWaterTask(HttpRequestsFactory.Response<CheckCryptoWaterTask.CryptoWaterResponse> response, String address) {
      super(response);
      this.uri = String.format(URL, getTickerFromAddress(address), address);
   }

   @Override
   protected CheckCryptoWaterTask.CryptoWaterResponse processData() {
      return HttpRequestsFactory.createGetRequest(uri, CheckCryptoWaterTask.CryptoWaterResponse.class);
   }

   public static String getTickerFromAddress(String address) {
      if (CryptoWaterUtils.isLanaCoin(address)) {
         return TICKER_LANA;
      }

      if (CryptoWaterUtils.isARCOCoin(address)) {
         return TICKER_ARCO;
      }

      if (CryptoWaterUtils.isTajCoin(address)) {
         return TICKER_TAJ;
      }
      return "";
   }

   public class CryptoWaterResponse {
      public String address;
      public String balance;
      public String firstBlockTimestamp;

   }
}
