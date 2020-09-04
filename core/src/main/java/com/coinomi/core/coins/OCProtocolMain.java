package com.coinomi.core.coins;

import com.coinomi.core.PartnersInfoData;
import com.coinomi.core.coins.families.PeerFamily;
import com.coinomi.core.coins.nxt.Constants;

/**
 * @author John L. Jegutanis
 */
public class OCProtocolMain extends PeerFamily implements PartnersInfoData {
    private OCProtocolMain() {
        id = "ocprotocol.main";

        addressHeader = 0;
        p2shHeader = 5;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        spendableCoinbaseDepth = 250;
        dumpedPrivateKeyHeader = 128;

        name = "OCProtocol";
        symbol = "OCP";
        uriScheme = "ocp";
        bip44Index = 10;
        unitExponent = 8;
        feeValue = value(100); // 0.000001 OCP
        minNonDust = value(1);
        softDustLimit = value(1000000); // 0.01 OCP
        softDustPolicy = SoftDustPolicy.AT_LEAST_BASE_FEE_IF_SOFT_DUST_TXO_PRESENT;
        signedMessageHeader = toBytes("OCProtocol Signed Message:\n");
    }

    private static OCProtocolMain instance = new OCProtocolMain();
    public static synchronized CoinType get() {
        return instance;
    }

    @Override
    public String getPartnerUrl() {
        return Constants.PARTNERS_URI_OCP;
    }
}
