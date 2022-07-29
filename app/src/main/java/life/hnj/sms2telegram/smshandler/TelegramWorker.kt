package life.hnj.sms2telegram.smshandler

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

private val TAG = "TelegramWorker"

class TelegramWorker (
    appContext: Context,
    workerParams: WorkerParameters,
    ) :
    Worker(appContext, workerParams) {

        override fun doWork(): ListenableWorker.Result {
            val apiUrl = inputData.getString("url")
            val msg = inputData.getString("msg")
            val chatId = "832432376"

            val queue = Volley.newRequestQueue(applicationContext)
            val payload = JSONObject()
            payload.put("text", msg)
            payload.put("chat_id", chatId)


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
            return ListenableWorker.Result.success()
        }
    }