package com.coinomi.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Parcelable
import android.util.Log
import com.coinomi.nfc.utils.NfcUtils
import java.util.*
import kotlin.collections.ArrayList

class NFCService(private val context: Activity) {

    private var adapter: NfcAdapter? = null

    init {
        adapter = NfcAdapter.getDefaultAdapter(context)
    }

    fun onResume() {
        if (adapter != null && !adapter!!.isEnabled) {
            showEnableDialog()
        } else if (adapter != null && adapter!!.isEnabled) {
            Log.e("NFCService", "NFC enabled");
            enableNfcForegroundDispatch()
        }
    }

    private fun showEnableDialog() {
        NfcUtils.showEnableNFCDialog(context)
    }

    fun onPause() {
        disableNFCForeground()
    }

    fun readTag(intent: Intent, readCallBackInfo: ((String?) -> Unit)?) {
        val tag = intent.getParcelableExtraCompat<Tag>(NfcAdapter.EXTRA_TAG)

        Log.e("NFCService", "NFC readTag $tag");
        try {
            val ndef = Ndef.get(tag)
            ndef.connect()
            val messages = intent.getParcelableArrayExtraCompat<NdefMessage>(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (messages != null) {
                val ndefMessages = ArrayList<NdefMessage>()

                messages.forEach {
                    ndefMessages.add(it)
                }

                val record = ndefMessages.firstOrNull()?.records?.firstOrNull()
                val payload = record?.payload
                if (payload != null) {
                    val textArray = Arrays.copyOfRange(payload, payload[0] + 1, payload.size)
                    val text = String(textArray)
                    Log.e("NFCService", "Read text: $text")

                    readCallBackInfo?.invoke(text)
                }
            }
        } catch (t: Throwable) {
            Log.e("NFCService", "NFC reading FAilEd: ", t)
            readCallBackInfo?.invoke(null)
        }
    }

    private fun enableNfcForegroundDispatch() {
        try {
            val intent = Intent(context, context.javaClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val nfcPendingIntent = buildPendingIntent(intent)
            adapter?.enableForegroundDispatch(context, nfcPendingIntent, null, null)
            Log.e("NFCService", "NFC enableNfcForegroundDispatch ${context.javaClass} $adapter");
        } catch (t: Throwable) {
            Log.e("NFCService", "NFC enableNfcForegroundDispatch FAilEd: ", t)
        }
    }

    private fun disableNFCForeground() {
        try {
            adapter?.disableForegroundDispatch(context)
            Log.e("NFCService", "NFC disableNFCForeground $adapter");
        } catch (t: Throwable) {
            Log.e("NFCService", "NFC DISABLING FAilEd: ", t)
        }
    }

    private fun buildPendingIntent(intent: Intent): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        return PendingIntent.getActivity(context, 0, intent, flag)
    }


    private inline fun <reified T : Parcelable> Intent.getParcelableArrayExtraCompat(key: String): Array<T>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableArrayExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableArrayExtra(key)?.filterIsInstance<T>()?.toTypedArray()
        }
    }

    private inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(key)
        }
    }
}
