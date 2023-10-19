package com.coinomi.nfc

import android.app.Activity
import android.content.Intent

class NFCModule(activity: Activity) {

    private var nfcService: NFCService? = null

    init {
        nfcService = NFCService(activity)
    }

    fun onResume() {
        nfcService?.onResume()
    }

    fun onPause() {
        nfcService?.onPause()
    }

    fun readTag(intent: Intent?, readCallBackInfo: ((String?) -> Unit)?) {
        if (intent != null) {
            nfcService?.readTag(intent, readCallBackInfo)
        }
    }
}
