package com.faa1192.kazanweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import com.faa1192.kazanweather.ui.theme.KazanWeatherTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


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

enum class State {
    CURRENT, FORECAST_5_DAYS
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val currentWeatherResponse = remember {
        mutableStateOf("")
    }
    val forecastWeatherResponse = remember {
        mutableStateOf("")
    }
    val lastUpdateCurrent = remember {
        mutableLongStateOf(0)
    }
    val lastUpdateForecast = remember {
        mutableLongStateOf(0)
    }

    val state: MutableState<State> = remember {
        mutableStateOf(
            State.CURRENT
        )
    }

    val ln = stringResource(R.string.ln)

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        currentWeather(currentWeatherResponse, stringResource(R.string.ln), lastUpdateCurrent)
        forecastWeather(forecastWeatherResponse, stringResource(R.string.ln), lastUpdateForecast)
        /*   Text(
               text = stringResource(R.string.kazan),
               modifier = modifier
           )*/
        Text(text = "")
        Text(text = "")

        Column(modifier = Modifier.fillMaxWidth()) {
            TabRow(selectedTabIndex = state.value.ordinal) {
                State.entries.forEachIndexed { index, value ->
                    Tab(text = {
                        Text(
                            value.name.replace("_", " ").lowercase().capitalize(Locale.current)
                        )
                    },
                        selected = state.value.ordinal == index,
                        onClick = { state.component2().invoke(State.entries[index]) }
                    )
                }
            }
            when (state.value) {
                State.CURRENT -> Current(currentWeatherResponse, ln, lastUpdateCurrent)
                State.FORECAST_5_DAYS -> Forecast(forecastWeatherResponse, ln, lastUpdateForecast)
                else -> println("")
            }
        }


        /*    Row {
                Button(onClick = { state.value = State.CURRENT }) {
                    Text(text = "Current tab")
                }
                Button(onClick = { state.value = State.FORECAST_5_DAYS }) {
                    Text(text = "Forecast tab")
                }
            }*/
        Text(text = "")
        Text(text = "")
        Text(text = "")

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KazanWeatherTheme {
        Greeting("Android")
    }
}


fun currentWeather(response: MutableState<String>, ln: String, lastUpdate: MutableState<Long>) {
    val last = lastUpdate.component1()
    val current = System.currentTimeMillis()
    val diff = (current - last)
    if (diff < 60 * 1000 * 10) { // 10 min
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

fun forecastWeather(response: MutableState<String>, ln: String, lastUpdate: MutableState<Long>) {
    val last = lastUpdate.component1()
    val current = System.currentTimeMillis()
    val diff = (current - last)
    if (diff < 60 * 1000 * 60 * 3) { // 3 hours
        return
    } else {
        lastUpdate.component2().invoke(current)
        val url =
            URL(
                "https://api.openweathermap.org/data/2.5/forecast" +
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

@Composable
fun Current(response: MutableState<String>, ln: String, lastUpdate: MutableState<Long>) {
    val uriHandler = LocalUriHandler.current
    if (response.component1().isNotBlank()) {
        val jsonObject = JSONObject(response.component1())
        val main = jsonObject.getJSONObject("main")
        val wind = jsonObject.getJSONObject("wind")
        val sys = jsonObject.getJSONObject("sys")
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
        val sunrise = sys["sunrise"].toString().toLong() * 1000
        val sunset = sys["sunset"].toString().toLong() * 1000

        val sunriseDateTime: LocalDateTime =
            Instant.ofEpochMilli(sunrise).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val sunsetDateTime: LocalDateTime =
            Instant.ofEpochMilli(sunset).atZone(ZoneId.systemDefault()).toLocalDateTime()

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val sunriseText: String = sunriseDateTime.format(formatter)
        val sunsetText: String = sunsetDateTime.format(formatter)

        val pressure = (main["pressure"].toString().toLong() / 1.3333).roundToInt().toString()
        val humidity = main["humidity"]

        Text("${stringResource(R.string.description)}: $description")
        Text("${stringResource(R.string.temperature)}: $temp С, ${stringResource(R.string.temp_feels_like)} $tempFeel C")
        Text("${stringResource(R.string.temp_min)}/${stringResource(R.string.temp_max)} $tempMin/$tempMax C")
        Text("")
        Text("Ветер: $windSpeed м/с; $windDeg; порывы: $windGust м/с")
        Text("")
        Text("${stringResource(R.string.pressure)}: $pressure")
        Text("${stringResource(R.string.humidity)}: $humidity%")
        Text("")
        Text("${stringResource(R.string.sunrise)}: $sunriseText")
        Text("${stringResource(R.string.sunset)}: $sunsetText")


    }
    Button(
        onClick = { currentWeather(response, ln, lastUpdate) },
        modifier = Modifier.fillMaxWidth(1f)
    ) {
//        Text(text = stringResource(R.string.update))
        Text(text = "update")

    }

    Button(
        onClick = {
            uriHandler.openUri("https://github.com/ArtiomCX75/KazanWeather/blob/master/TermsConditions.md")
        },
        modifier = Modifier.fillMaxWidth(1f)
    ) {
        Text(text = stringResource(R.string.policy))

    }


}

@Composable
fun Forecast(response: MutableState<String>, ln: String, lastUpdate: MutableState<Long>) {
    if (response.component1().isNotBlank()) {
        val jsonObject = JSONObject(response.component1())
        val arr: JSONArray = jsonObject.getJSONArray("list")

        var counter = 0
        Column {
            while (counter < arr.length()) {
                val element = arr.getJSONObject(counter)
                val main = element.getJSONObject("main")
                val weather = element.getJSONArray("weather").getJSONObject(0)

                val temp = main["temp"]
                val dt = element["dt_txt"]
                val description = weather["description"]

                Text(text = "$dt\n$temp C  $description\n")

                counter++
            }
        }
    }
    Button(
        onClick = { forecastWeather(response, ln, lastUpdate) },
        modifier = Modifier.fillMaxWidth(1f)
    ) {
        Text(text = "update")
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

/*
{
  "cod": "200",
  "message": 0,
  "cnt": 40,
  "list": [
    {
      "dt": 1715904000,
      "main": {
        "temp": 9.95,
        "feels_like": 8.12,
        "temp_min": 7.96,
        "temp_max": 9.95,
        "pressure": 1015,
        "sea_level": 1015,
        "grnd_level": 1001,
        "humidity": 82,
        "temp_kf": 1.99
      },
      "weather": [
        {
          "id": 803,
          "main": "Clouds",
          "description": "облачно с прояснениями",
          "icon": "04n"
        }
      ],
      "clouds": {
        "all": 72
      },
      "wind": {
        "speed": 3.63,
        "deg": 240,
        "gust": 10.05
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-17 00:00:00"
    },
    {
      "dt": 1715914800,
      "main": {
        "temp": 9.62,
        "feels_like": 7.11,
        "temp_min": 8.95,
        "temp_max": 9.62,
        "pressure": 1014,
        "sea_level": 1014,
        "grnd_level": 999,
        "humidity": 86,
        "temp_kf": 0.67
      },
      "weather": [
        {
          "id": 500,
          "main": "Rain",
          "description": "небольшой дождь",
          "icon": "10d"
        }
      ],
      "clouds": {
        "all": 81
      },
      "wind": {
        "speed": 5.01,
        "deg": 232,
        "gust": 11.41
      },
      "visibility": 9862,
      "pop": 1,
      "rain": {
        "3h": 1.36
      },
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-17 03:00:00"
    },
    {
      "dt": 1715925600,
      "main": {
        "temp": 9.26,
        "feels_like": 6.45,
        "temp_min": 8.92,
        "temp_max": 9.26,
        "pressure": 1013,
        "sea_level": 1013,
        "grnd_level": 998,
        "humidity": 90,
        "temp_kf": 0.34
      },
      "weather": [
        {
          "id": 500,
          "main": "Rain",
          "description": "небольшой дождь",
          "icon": "10d"
        }
      ],
      "clouds": {
        "all": 91
      },
      "wind": {
        "speed": 5.58,
        "deg": 234,
        "gust": 10.66
      },
      "visibility": 10000,
      "pop": 1,
      "rain": {
        "3h": 2.34
      },
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-17 06:00:00"
    },
    {
      "dt": 1715936400,
      "main": {
        "temp": 8.83,
        "feels_like": 6.15,
        "temp_min": 8.83,
        "temp_max": 8.83,
        "pressure": 1012,
        "sea_level": 1012,
        "grnd_level": 999,
        "humidity": 96,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 500,
          "main": "Rain",
          "description": "небольшой дождь",
          "icon": "10d"
        }
      ],
      "clouds": {
        "all": 100
      },
      "wind": {
        "speed": 4.93,
        "deg": 245,
        "gust": 8.61
      },
      "visibility": 10000,
      "pop": 1,
      "rain": {
        "3h": 1.4
      },
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-17 09:00:00"
    },
    {
      "dt": 1715947200,
      "main": {
        "temp": 9.03,
        "feels_like": 6.11,
        "temp_min": 9.03,
        "temp_max": 9.03,
        "pressure": 1013,
        "sea_level": 1013,
        "grnd_level": 1000,
        "humidity": 91,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 500,
          "main": "Rain",
          "description": "небольшой дождь",
          "icon": "10d"
        }
      ],
      "clouds": {
        "all": 100
      },
      "wind": {
        "speed": 5.7,
        "deg": 1,
        "gust": 9.05
      },
      "visibility": 10000,
      "pop": 1,
      "rain": {
        "3h": 0.26
      },
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-17 12:00:00"
    },
    {
      "dt": 1715958000,
      "main": {
        "temp": 9.48,
        "feels_like": 6.94,
        "temp_min": 9.48,
        "temp_max": 9.48,
        "pressure": 1014,
        "sea_level": 1014,
        "grnd_level": 1001,
        "humidity": 86,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 804,
          "main": "Clouds",
          "description": "пасмурно",
          "icon": "04d"
        }
      ],
      "clouds": {
        "all": 100
      },
      "wind": {
        "speed": 4.99,
        "deg": 352,
        "gust": 10.03
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-17 15:00:00"
    },
    {
      "dt": 1715968800,
      "main": {
        "temp": 8.44,
        "feels_like": 5.91,
        "temp_min": 8.44,
        "temp_max": 8.44,
        "pressure": 1016,
        "sea_level": 1016,
        "grnd_level": 1002,
        "humidity": 87,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 804,
          "main": "Clouds",
          "description": "пасмурно",
          "icon": "04n"
        }
      ],
      "clouds": {
        "all": 100
      },
      "wind": {
        "speed": 4.37,
        "deg": 347,
        "gust": 8.91
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-17 18:00:00"
    },
    {
      "dt": 1715979600,
      "main": {
        "temp": 7.33,
        "feels_like": 4.53,
        "temp_min": 7.33,
        "temp_max": 7.33,
        "pressure": 1017,
        "sea_level": 1017,
        "grnd_level": 1003,
        "humidity": 86,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 804,
          "main": "Clouds",
          "description": "пасмурно",
          "icon": "04n"
        }
      ],
      "clouds": {
        "all": 98
      },
      "wind": {
        "speed": 4.39,
        "deg": 2,
        "gust": 8.87
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-17 21:00:00"
    },
    {
      "dt": 1715990400,
      "main": {
        "temp": 6.09,
        "feels_like": 3.14,
        "temp_min": 6.09,
        "temp_max": 6.09,
        "pressure": 1018,
        "sea_level": 1018,
        "grnd_level": 1005,
        "humidity": 83,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 804,
          "main": "Clouds",
          "description": "пасмурно",
          "icon": "04n"
        }
      ],
      "clouds": {
        "all": 99
      },
      "wind": {
        "speed": 4.1,
        "deg": 3,
        "gust": 8.37
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-18 00:00:00"
    },
    {
      "dt": 1716001200,
      "main": {
        "temp": 6.42,
        "feels_like": 3.23,
        "temp_min": 6.42,
        "temp_max": 6.42,
        "pressure": 1020,
        "sea_level": 1020,
        "grnd_level": 1006,
        "humidity": 76,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 804,
          "main": "Clouds",
          "description": "пасмурно",
          "icon": "04d"
        }
      ],
      "clouds": {
        "all": 100
      },
      "wind": {
        "speed": 4.74,
        "deg": 9,
        "gust": 8.45
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-18 03:00:00"
    },
    {
      "dt": 1716012000,
      "main": {
        "temp": 8.43,
        "feels_like": 5.51,
        "temp_min": 8.43,
        "temp_max": 8.43,
        "pressure": 1021,
        "sea_level": 1021,
        "grnd_level": 1007,
        "humidity": 58,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 804,
          "main": "Clouds",
          "description": "пасмурно",
          "icon": "04d"
        }
      ],
      "clouds": {
        "all": 100
      },
      "wind": {
        "speed": 5.3,
        "deg": 28,
        "gust": 5.91
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-18 06:00:00"
    },
    {
      "dt": 1716022800,
      "main": {
        "temp": 8.88,
        "feels_like": 6.15,
        "temp_min": 8.88,
        "temp_max": 8.88,
        "pressure": 1022,
        "sea_level": 1022,
        "grnd_level": 1008,
        "humidity": 56,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 804,
          "main": "Clouds",
          "description": "пасмурно",
          "icon": "04d"
        }
      ],
      "clouds": {
        "all": 100
      },
      "wind": {
        "speed": 5.11,
        "deg": 27,
        "gust": 6.1
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-18 09:00:00"
    },
    {
      "dt": 1716033600,
      "main": {
        "temp": 11.61,
        "feels_like": 10.11,
        "temp_min": 11.61,
        "temp_max": 11.61,
        "pressure": 1021,
        "sea_level": 1021,
        "grnd_level": 1008,
        "humidity": 49,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 804,
          "main": "Clouds",
          "description": "пасмурно",
          "icon": "04d"
        }
      ],
      "clouds": {
        "all": 92
      },
      "wind": {
        "speed": 4.12,
        "deg": 65,
        "gust": 6.24
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-18 12:00:00"
    },
    {
      "dt": 1716044400,
      "main": {
        "temp": 12.06,
        "feels_like": 10.76,
        "temp_min": 12.06,
        "temp_max": 12.06,
        "pressure": 1021,
        "sea_level": 1021,
        "grnd_level": 1008,
        "humidity": 55,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 801,
          "main": "Clouds",
          "description": "небольшая облачность",
          "icon": "02d"
        }
      ],
      "clouds": {
        "all": 23
      },
      "wind": {
        "speed": 3.3,
        "deg": 32,
        "gust": 5.43
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-18 15:00:00"
    },
    {
      "dt": 1716055200,
      "main": {
        "temp": 9.01,
        "feels_like": 7.66,
        "temp_min": 9.01,
        "temp_max": 9.01,
        "pressure": 1023,
        "sea_level": 1023,
        "grnd_level": 1009,
        "humidity": 66,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 802,
          "main": "Clouds",
          "description": "переменная облачность",
          "icon": "03n"
        }
      ],
      "clouds": {
        "all": 46
      },
      "wind": {
        "speed": 2.48,
        "deg": 37,
        "gust": 4.86
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-18 18:00:00"
    },
    {
      "dt": 1716066000,
      "main": {
        "temp": 5.81,
        "feels_like": 4.61,
        "temp_min": 5.81,
        "temp_max": 5.81,
        "pressure": 1023,
        "sea_level": 1023,
        "grnd_level": 1009,
        "humidity": 78,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 801,
          "main": "Clouds",
          "description": "небольшая облачность",
          "icon": "02n"
        }
      ],
      "clouds": {
        "all": 16
      },
      "wind": {
        "speed": 1.72,
        "deg": 37,
        "gust": 2.34
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-18 21:00:00"
    },
    {
      "dt": 1716076800,
      "main": {
        "temp": 4.81,
        "feels_like": 3.19,
        "temp_min": 4.81,
        "temp_max": 4.81,
        "pressure": 1024,
        "sea_level": 1024,
        "grnd_level": 1010,
        "humidity": 80,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 802,
          "main": "Clouds",
          "description": "переменная облачность",
          "icon": "03n"
        }
      ],
      "clouds": {
        "all": 30
      },
      "wind": {
        "speed": 1.96,
        "deg": 23,
        "gust": 2.46
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-19 00:00:00"
    },
    {
      "dt": 1716087600,
      "main": {
        "temp": 7.64,
        "feels_like": 5.9,
        "temp_min": 7.64,
        "temp_max": 7.64,
        "pressure": 1024,
        "sea_level": 1024,
        "grnd_level": 1010,
        "humidity": 67,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 801,
          "main": "Clouds",
          "description": "небольшая облачность",
          "icon": "02d"
        }
      ],
      "clouds": {
        "all": 11
      },
      "wind": {
        "speed": 2.67,
        "deg": 36,
        "gust": 4.5
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-19 03:00:00"
    },
    {
      "dt": 1716098400,
      "main": {
        "temp": 12.32,
        "feels_like": 10.99,
        "temp_min": 12.32,
        "temp_max": 12.32,
        "pressure": 1024,
        "sea_level": 1024,
        "grnd_level": 1011,
        "humidity": 53,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01d"
        }
      ],
      "clouds": {
        "all": 9
      },
      "wind": {
        "speed": 2.96,
        "deg": 58,
        "gust": 3.58
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-19 06:00:00"
    },
    {
      "dt": 1716109200,
      "main": {
        "temp": 14.89,
        "feels_like": 13.56,
        "temp_min": 14.89,
        "temp_max": 14.89,
        "pressure": 1024,
        "sea_level": 1024,
        "grnd_level": 1010,
        "humidity": 43,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 801,
          "main": "Clouds",
          "description": "небольшая облачность",
          "icon": "02d"
        }
      ],
      "clouds": {
        "all": 12
      },
      "wind": {
        "speed": 3.34,
        "deg": 66,
        "gust": 3.7
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-19 09:00:00"
    },
    {
      "dt": 1716120000,
      "main": {
        "temp": 15.86,
        "feels_like": 14.6,
        "temp_min": 15.86,
        "temp_max": 15.86,
        "pressure": 1023,
        "sea_level": 1023,
        "grnd_level": 1009,
        "humidity": 42,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 801,
          "main": "Clouds",
          "description": "небольшая облачность",
          "icon": "02d"
        }
      ],
      "clouds": {
        "all": 13
      },
      "wind": {
        "speed": 2.96,
        "deg": 71,
        "gust": 3.23
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-19 12:00:00"
    },
    {
      "dt": 1716130800,
      "main": {
        "temp": 14.85,
        "feels_like": 13.77,
        "temp_min": 14.85,
        "temp_max": 14.85,
        "pressure": 1022,
        "sea_level": 1022,
        "grnd_level": 1009,
        "humidity": 53,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01d"
        }
      ],
      "clouds": {
        "all": 6
      },
      "wind": {
        "speed": 2.09,
        "deg": 68,
        "gust": 2.64
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-19 15:00:00"
    },
    {
      "dt": 1716141600,
      "main": {
        "temp": 10.33,
        "feels_like": 9.09,
        "temp_min": 10.33,
        "temp_max": 10.33,
        "pressure": 1023,
        "sea_level": 1023,
        "grnd_level": 1010,
        "humidity": 64,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01n"
        }
      ],
      "clouds": {
        "all": 6
      },
      "wind": {
        "speed": 1.05,
        "deg": 130,
        "gust": 1.15
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-19 18:00:00"
    },
    {
      "dt": 1716152400,
      "main": {
        "temp": 8.69,
        "feels_like": 8.69,
        "temp_min": 8.69,
        "temp_max": 8.69,
        "pressure": 1023,
        "sea_level": 1023,
        "grnd_level": 1010,
        "humidity": 71,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01n"
        }
      ],
      "clouds": {
        "all": 2
      },
      "wind": {
        "speed": 0.36,
        "deg": 196,
        "gust": 0.54
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-19 21:00:00"
    },
    {
      "dt": 1716163200,
      "main": {
        "temp": 7.68,
        "feels_like": 7.68,
        "temp_min": 7.68,
        "temp_max": 7.68,
        "pressure": 1023,
        "sea_level": 1023,
        "grnd_level": 1010,
        "humidity": 75,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01n"
        }
      ],
      "clouds": {
        "all": 1
      },
      "wind": {
        "speed": 0.87,
        "deg": 335,
        "gust": 0.93
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-20 00:00:00"
    },
    {
      "dt": 1716174000,
      "main": {
        "temp": 10.58,
        "feels_like": 9.42,
        "temp_min": 10.58,
        "temp_max": 10.58,
        "pressure": 1024,
        "sea_level": 1024,
        "grnd_level": 1010,
        "humidity": 66,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01d"
        }
      ],
      "clouds": {
        "all": 0
      },
      "wind": {
        "speed": 0.57,
        "deg": 102,
        "gust": 0.72
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-20 03:00:00"
    },
    {
      "dt": 1716184800,
      "main": {
        "temp": 14.87,
        "feels_like": 13.74,
        "temp_min": 14.87,
        "temp_max": 14.87,
        "pressure": 1024,
        "sea_level": 1024,
        "grnd_level": 1010,
        "humidity": 51,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01d"
        }
      ],
      "clouds": {
        "all": 3
      },
      "wind": {
        "speed": 1.76,
        "deg": 148,
        "gust": 1.82
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-20 06:00:00"
    },
    {
      "dt": 1716195600,
      "main": {
        "temp": 17.27,
        "feels_like": 16.12,
        "temp_min": 17.27,
        "temp_max": 17.27,
        "pressure": 1023,
        "sea_level": 1023,
        "grnd_level": 1009,
        "humidity": 41,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01d"
        }
      ],
      "clouds": {
        "all": 10
      },
      "wind": {
        "speed": 2.05,
        "deg": 152,
        "gust": 1.8
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-20 09:00:00"
    },
    {
      "dt": 1716206400,
      "main": {
        "temp": 18.1,
        "feels_like": 17.01,
        "temp_min": 18.1,
        "temp_max": 18.1,
        "pressure": 1021,
        "sea_level": 1021,
        "grnd_level": 1008,
        "humidity": 40,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 801,
          "main": "Clouds",
          "description": "небольшая облачность",
          "icon": "02d"
        }
      ],
      "clouds": {
        "all": 12
      },
      "wind": {
        "speed": 2.55,
        "deg": 163,
        "gust": 1.77
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-20 12:00:00"
    },
    {
      "dt": 1716217200,
      "main": {
        "temp": 16.03,
        "feels_like": 15.1,
        "temp_min": 16.03,
        "temp_max": 16.03,
        "pressure": 1021,
        "sea_level": 1021,
        "grnd_level": 1008,
        "humidity": 54,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01d"
        }
      ],
      "clouds": {
        "all": 8
      },
      "wind": {
        "speed": 2.94,
        "deg": 162,
        "gust": 3.65
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-20 15:00:00"
    },
    {
      "dt": 1716228000,
      "main": {
        "temp": 11.74,
        "feels_like": 10.56,
        "temp_min": 11.74,
        "temp_max": 11.74,
        "pressure": 1021,
        "sea_level": 1021,
        "grnd_level": 1007,
        "humidity": 61,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01n"
        }
      ],
      "clouds": {
        "all": 5
      },
      "wind": {
        "speed": 2.02,
        "deg": 182,
        "gust": 2.47
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-20 18:00:00"
    },
    {
      "dt": 1716238800,
      "main": {
        "temp": 10.21,
        "feels_like": 8.98,
        "temp_min": 10.21,
        "temp_max": 10.21,
        "pressure": 1021,
        "sea_level": 1021,
        "grnd_level": 1007,
        "humidity": 65,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01n"
        }
      ],
      "clouds": {
        "all": 8
      },
      "wind": {
        "speed": 1.45,
        "deg": 199,
        "gust": 1.48
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-20 21:00:00"
    },
    {
      "dt": 1716249600,
      "main": {
        "temp": 9.4,
        "feels_like": 9.4,
        "temp_min": 9.4,
        "temp_max": 9.4,
        "pressure": 1021,
        "sea_level": 1021,
        "grnd_level": 1007,
        "humidity": 70,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01n"
        }
      ],
      "clouds": {
        "all": 9
      },
      "wind": {
        "speed": 0.98,
        "deg": 202,
        "gust": 1.03
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-21 00:00:00"
    },
    {
      "dt": 1716260400,
      "main": {
        "temp": 12.42,
        "feels_like": 11.36,
        "temp_min": 12.42,
        "temp_max": 12.42,
        "pressure": 1020,
        "sea_level": 1020,
        "grnd_level": 1006,
        "humidity": 63,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 801,
          "main": "Clouds",
          "description": "небольшая облачность",
          "icon": "02d"
        }
      ],
      "clouds": {
        "all": 17
      },
      "wind": {
        "speed": 1.67,
        "deg": 179,
        "gust": 2.51
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-21 03:00:00"
    },
    {
      "dt": 1716271200,
      "main": {
        "temp": 17.16,
        "feels_like": 16.26,
        "temp_min": 17.16,
        "temp_max": 17.16,
        "pressure": 1019,
        "sea_level": 1019,
        "grnd_level": 1006,
        "humidity": 51,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 801,
          "main": "Clouds",
          "description": "небольшая облачность",
          "icon": "02d"
        }
      ],
      "clouds": {
        "all": 17
      },
      "wind": {
        "speed": 2.99,
        "deg": 202,
        "gust": 3.81
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-21 06:00:00"
    },
    {
      "dt": 1716282000,
      "main": {
        "temp": 19.51,
        "feels_like": 18.61,
        "temp_min": 19.51,
        "temp_max": 19.51,
        "pressure": 1018,
        "sea_level": 1018,
        "grnd_level": 1004,
        "humidity": 42,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 801,
          "main": "Clouds",
          "description": "небольшая облачность",
          "icon": "02d"
        }
      ],
      "clouds": {
        "all": 22
      },
      "wind": {
        "speed": 3.63,
        "deg": 220,
        "gust": 4.28
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-21 09:00:00"
    },
    {
      "dt": 1716292800,
      "main": {
        "temp": 20.1,
        "feels_like": 19.24,
        "temp_min": 20.1,
        "temp_max": 20.1,
        "pressure": 1016,
        "sea_level": 1016,
        "grnd_level": 1003,
        "humidity": 41,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 802,
          "main": "Clouds",
          "description": "переменная облачность",
          "icon": "03d"
        }
      ],
      "clouds": {
        "all": 42
      },
      "wind": {
        "speed": 3.7,
        "deg": 230,
        "gust": 4.77
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-21 12:00:00"
    },
    {
      "dt": 1716303600,
      "main": {
        "temp": 16.78,
        "feels_like": 16.08,
        "temp_min": 16.78,
        "temp_max": 16.78,
        "pressure": 1015,
        "sea_level": 1015,
        "grnd_level": 1002,
        "humidity": 60,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 803,
          "main": "Clouds",
          "description": "облачно с прояснениями",
          "icon": "04d"
        }
      ],
      "clouds": {
        "all": 79
      },
      "wind": {
        "speed": 2.92,
        "deg": 231,
        "gust": 5.01
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "d"
      },
      "dt_txt": "2024-05-21 15:00:00"
    },
    {
      "dt": 1716314400,
      "main": {
        "temp": 13.37,
        "feels_like": 12.56,
        "temp_min": 13.37,
        "temp_max": 13.37,
        "pressure": 1014,
        "sea_level": 1014,
        "grnd_level": 1001,
        "humidity": 69,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 803,
          "main": "Clouds",
          "description": "облачно с прояснениями",
          "icon": "04n"
        }
      ],
      "clouds": {
        "all": 65
      },
      "wind": {
        "speed": 3.49,
        "deg": 199,
        "gust": 6.74
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-21 18:00:00"
    },
    {
      "dt": 1716325200,
      "main": {
        "temp": 12.13,
        "feels_like": 11.44,
        "temp_min": 12.13,
        "temp_max": 12.13,
        "pressure": 1013,
        "sea_level": 1013,
        "grnd_level": 1000,
        "humidity": 78,
        "temp_kf": 0
      },
      "weather": [
        {
          "id": 801,
          "main": "Clouds",
          "description": "небольшая облачность",
          "icon": "02n"
        }
      ],
      "clouds": {
        "all": 12
      },
      "wind": {
        "speed": 3.99,
        "deg": 213,
        "gust": 10.72
      },
      "visibility": 10000,
      "pop": 0,
      "sys": {
        "pod": "n"
      },
      "dt_txt": "2024-05-21 21:00:00"
    }
  ],
  "city": {
    "id": 551487,
    "name": "Казань",
    "coord": {
      "lat": 55.7887,
      "lon": 49.1221
    },
    "country": "RU",
    "population": 0,
    "timezone": 10800,
    "sunrise": 1715905673,
    "sunset": 1715964730
  }
}
 */