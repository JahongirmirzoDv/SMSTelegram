package life.hnj.sms2telegram.smshandler

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

private const val TAG = "TelegramMessageWorker"

class TelegramMessageWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val apiUrl = inputData.getString("url")
        val msg = inputData.getString("msg")
        val chatId = "2060858865"

        val queue = Volley.newRequestQueue(applicationContext)
        val payload = JSONObject()
        payload.put("text", msg)
        payload.put("chat_id", chatId)
        payload.put("text", msg)
        payload.put("chat_id", "1376290940")


        val req = object : JsonObjectRequest(
            Method.POST,
            apiUrl,
            payload,
            Response.Listener { _ -> Log.d(TAG, "MSG send success") },
            Response.ErrorListener { _ ->
                Log.d(TAG, "MSG send error")
            }) {
        }
        queue.add(req)


//        val queue1 = Volley.newRequestQueue(applicationContext)
//        val payload1 = JSONObject()
//        payload1.put("text", msg)
//        payload1.put("chat_id", "1376290940")
//
//
//        val req1 = object : JsonObjectRequest(
//            Method.POST,
//            apiUrl,
//            payload,
//            Response.Listener { _ -> Log.d(TAG, "MSG send success") },
//            Response.ErrorListener { _ ->
//                Log.d(TAG, "MSG send error")
//            }) {
//        }
//        queue1.add(req1)
        return Result.success()
    }
}