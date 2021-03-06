package com.example.alohomora

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_liberacao.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL



class LiberacaoActivity : AppCompatActivity() {

    operator fun JSONArray.iterator(): Iterator<JSONObject> =
        (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liberacao)

        setSupportActionBar(toolbar2)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Liberar"
        toolbar2.setTitleTextColor(Color.WHITE)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar2)
        progressBar.visibility = View.INVISIBLE

        val buttonPost = findViewById<Button>(R.id.botaoUnlock)
        buttonPost.setOnClickListener {

            val date = inputDate2.text
            val userid = inputUser2.text
            val hour = inputHour2.text
            val padlock = inputPadlock2.text

            val json = JSONObject("{request: {\n    \"" +
                    "date\": \"$date\",\n    \"" +
                    "user_id\": \"$userid\",\n    \"" +
                    "hour\": \"$hour\",\n    \"" +
                    "padlock\": \"$padlock\"\n  }\n}")

            progressBar.visibility = View.VISIBLE

            HttpTask {
                progressBar.visibility = View.INVISIBLE
                if (it == null) {
                    println("ERRO DE CONEXAO MIZERA!")
                    return@HttpTask
                }
                println(it)
            }.execute("POST", "https://alohomorabeta1.mybluemix.net/app/request/relese", json.toString())
        }
    }

    class HttpTask(var callback: (String?) -> Unit) : AsyncTask<String, Unit, String>()  {

        override fun doInBackground(vararg params: String): String? {
            val url = URL(params[1])
            val httpClient = url.openConnection() as HttpURLConnection
            httpClient.readTimeout = 10*1000
            httpClient.connectTimeout = 10*1000
            httpClient.requestMethod = params[0]

            if (params[0] == "POST") {
                httpClient.instanceFollowRedirects = false
                httpClient.doOutput = true
                httpClient.doInput = true
                httpClient.useCaches = false
                httpClient.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            try {
                if (params[0] == "POST") {
                    httpClient.connect()
                    val os = httpClient.outputStream
                    val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
                    writer.write(params[2])
                    writer.flush()
                    writer.close()
                    os.close()
                }
                if (httpClient.responseCode == HttpURLConnection.HTTP_OK) {
                    val stream = BufferedInputStream(httpClient.inputStream)
                    return readStream(inputStream = stream)
                } else {
                    println("ERROR ${httpClient.responseCode}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                httpClient.disconnect()
            }

            return null
        }

        private fun readStream(inputStream: BufferedInputStream): String {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            bufferedReader.forEachLine { stringBuilder.append(it) }
            return stringBuilder.toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            callback(result)
        }
    }
}