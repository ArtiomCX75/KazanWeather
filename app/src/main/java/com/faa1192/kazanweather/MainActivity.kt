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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    val lastUpdate = remember {
        mutableLongStateOf(0)
    }
    val ln = stringResource(R.string.ln)
    Column {
        updateData(response, stringResource(R.string.ln), lastUpdate)
        Text(
            text = stringResource(R.string.kazan),
            modifier = modifier
        )
        if (response.component1().isNotBlank()) {
            val jsonObject = JSONObject(response.component1())
            val main = jsonObject.getJSONObject("main")
            val wind = jsonObject.getJSONObject("wind")
            val weather = jsonObject.getJSONArray("weather").getJSONObject(0)
            val description = weather["description"]
            val temp = main["temp"]
            val tempFeel = main["feels_like"]
            val tempMin = main["temp_min"]
            val tempMax = main["temp_max"]
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


            Text("${stringResource(R.string.description)}: $description")
            Text("${stringResource(R.string.temperature)}: $temp С, ${stringResource(R.string.temp_feels_like)} $tempFeel C")
            Text("${stringResource(R.string.temp_min)}/${stringResource(R.string.temp_max)} $tempMin/$tempMax C")
            Text("")
            Text("Ветер: $windSpeed м/с; $windDeg; порывы: $windGust м/с")
            Text("")

        }
        Button(onClick = { updateData(response, ln, lastUpdate) }) {
            Text(text = stringResource(R.string.update))
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


fun updateData(response: MutableState<String>, ln: String, lastUpdate: MutableState<Long>) {
    val last = lastUpdate.component1()
    val current = System.currentTimeMillis()
    val diff = (current - last)
    if (diff < 60 * 1000) {
        return
    } else {
        lastUpdate.component2().invoke(current)
        val url =
            URL(
                "https://api.openweathermap.org/data/2.5/weather" +
                        "?id=551487" +
                        "&appid=5fa682315be7b0b6b329bca80a9bbf08" +
                        "&lang=$ln" +
                        "&units=metric"
            )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resp: StringBuilder = StringBuilder()
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"  // optional default is GET
                    println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")
                    inputStream.bufferedReader().use {
                        it.lines().forEach { line ->
                            resp.append(line)
                        }
                    }
                    response.component2().invoke(resp.toString())
                }
            } catch (e: Exception) {
                println(e)
                throw e
            }
        }
    }
}
/*
{
  "coord": {
    "lon": 49.1221,
    "lat": 55.7887
  },
  "weather": [
    {
      "id": 804,
      "main": "Clouds",
      "description": "overcast clouds",
      "icon": "04n"
    }
  ],
  "base": "stations",
  "main": {
    "temp": -0.24,
    "feels_like": -6.09,
    "temp_min": -0.66,
    "temp_max": 0.97,
    "pressure": 1012,
    "humidity": 70,
    "sea_level": 1012,
    "grnd_level": 998
  },
  "visibility": 10000,
  "wind": {
    "speed": 6.71,
    "deg": 247,
    "gust": 12.67
  },
  "clouds": {
    "all": 94
  },
  "dt": 1715298749,
  "sys": {
    "type": 2,
    "id": 48937,
    "country": "RU",
    "sunrise": 1715301652,
    "sunset": 1715359140
  },
  "timezone": 10800,
  "id": 551487,
  "name": "Kazan’",
  "cod": 200
}
 */

/*
{
  "coord": {
    "lon": 49.1221,
    "lat": 55.7887
  },
  "weather": [
    {
      "id": 804,
      "main": "Clouds",
      "description": "пасмурно",
      "icon": "04d"
    }
  ],
  "base": "stations",
  "main": {
    "temp": 7.39,
    "feels_like": 4.28,
    "temp_min": 5.41,
    "temp_max": 7.72,
    "pressure": 1006,
    "humidity": 91,
    "sea_level": 1006,
    "grnd_level": 992
  },
  "visibility": 10000,
  "wind": {
    "speed": 5.11,
    "deg": 228,
    "gust": 10.27
  },
  "clouds": {
    "all": 100
  },
  "dt": 1715390391,
  "sys": {
    "type": 2,
    "id": 48937,
    "country": "RU",
    "sunrise": 1715387935,
    "sunset": 1715445656
  },
  "timezone": 10800,
  "id": 551487,
  "name": "Казань",
  "cod": 200
}
 */