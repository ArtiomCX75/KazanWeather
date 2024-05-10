package com.faa1192.kazanweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.faa1192.kazanweather.ui.theme.KazanWeatherTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KazanWeatherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val response = remember {
        mutableStateOf("")
    }
    Column {
        //  sendGet(response)
        Text(
            text = "Казань",
            modifier = modifier
        )
        if (response.component1().isNotBlank()) {
            val jsonObject = JSONObject(response.component1())
            val main = jsonObject.getJSONObject("main")
            val wind = jsonObject.getJSONObject("wind")
            val temp = main["temp"]
            val windSpeed = wind["speed"]
            val windDeg = when (wind["deg"].toString().toInt()) {
                in 0..22 -> "С"
                in 23..67 -> "СВ"
                in 68..112 -> "В"
                in 113..157 -> "ЮВ"
                in 158..202 -> "Ю"
                in 203..247 -> "ЮЗ"
                in 248..295 -> "З"
                in 296..340 -> "СЗ"
                in 341..360 -> "С"
                else -> ""
            }
            val windGust = wind["gust"]


            Text("Температура: $temp С")
            Text("Ветер: $windSpeed м/с; $windDeg; порывы: $windGust ")
            Text("")

        }
        Button(onClick = { sendGet(response) }) {
            Text(text = "update")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KazanWeatherTheme {
        Greeting("Android")
    }
}


fun sendGet(response: MutableState<String>) {
    val url =
        URL("https://api.openweathermap.org/data/2.5/weather?id=551487&appid=5fa682315be7b0b6b329bca80a9bbf08&lang=en&units=metric")
    CoroutineScope(Dispatchers.IO).launch {
        try {
            var resp = ""
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"  // optional default is GET

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                inputStream.bufferedReader().use {
                    it.lines().forEach { line ->
                        resp += line
                        println(line)
                    }
                }
                response.component2().invoke(resp)
            }
        } catch (e: Exception) {
            println(e)
        }
    }
}