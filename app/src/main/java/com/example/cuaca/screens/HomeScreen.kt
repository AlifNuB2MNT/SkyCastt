package com.example.cuaca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val cities = listOf("Jakarta", "Bandung", "Surabaya", "Medan", "Denpasar")

    var selectedCity by remember { mutableStateOf(cities[0]) }
    var expanded by remember { mutableStateOf(false) }

    var currentWeatherText by remember { mutableStateOf("Loading cuaca saat ini...") }
    var currentWeatherIconCode by remember { mutableStateOf("01d") }

    var forecastList by remember { mutableStateOf<List<ForecastItem>>(emptyList()) }

    val apiKey = "b6283e1e1be63e5b77f29f4773f76318"

    LaunchedEffect(selectedCity) {
        val current = fetchCurrentWeatherWithIcon(selectedCity, apiKey)
        currentWeatherText = current.first
        currentWeatherIconCode = current.second

        forecastList = fetchForecast(selectedCity, apiKey)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Navigation Bar atas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(color = androidx.compose.ui.graphics.Color(0xFF74C476)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Forecast Weather",
                color = androidx.compose.ui.graphics.Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Konten utama di bawah navigation bar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Pilih Kota:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedCity,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Kota") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    cities.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city) },
                            onClick = {
                                selectedCity = city
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                WeatherIcon(iconCode = currentWeatherIconCode, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = currentWeatherText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                )
            }

            Text(
                text = "Forecast 5 hari (3 jam sekali):",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (forecastList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada data forecast")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(forecastList) { item ->
                        ForecastRow(item)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherIcon(iconCode: String, modifier: Modifier = Modifier) {
    val url = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
    AsyncImage(
        model = url,
        contentDescription = "Weather Icon",
        modifier = modifier
    )
}

@Composable
fun ForecastRow(item: ForecastItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherIcon(iconCode = item.iconCode, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = item.dateTime,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${item.temp}°C",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = item.weatherMain,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

data class ForecastItem(
    val dateTime: String,
    val temp: Double,
    val weatherMain: String,
    val iconCode: String
)

// Fetch current weather with icon code
suspend fun fetchCurrentWeatherWithIcon(city: String, apiKey: String): Pair<String, String> = withContext(Dispatchers.IO) {
    val urlString =
        "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"
    try {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000

        val responseCode = conn.responseCode
        if (responseCode != 200) {
            val errorStream = conn.errorStream?.bufferedReader()?.readText()
            return@withContext Pair("Error response: $responseCode\n$errorStream", "01d")
        }

        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(response)
        val weatherArray = json.getJSONArray("weather")
        val weatherMain = weatherArray.getJSONObject(0).getString("main")
        val weatherDesc = weatherArray.getJSONObject(0).getString("description")
        val temp = json.getJSONObject("main").getDouble("temp")
        val iconCode = weatherArray.getJSONObject(0).getString("icon")

        Pair("Cuaca di $city: $weatherMain ($weatherDesc), suhu: $temp°C", iconCode)

    } catch (e: Exception) {
        e.printStackTrace()
        Pair("Terjadi kesalahan: ${e.localizedMessage}", "01d")
    }
}

// Fetch forecast with icon code
suspend fun fetchForecast(city: String, apiKey: String): List<ForecastItem> = withContext(Dispatchers.IO) {
    val urlString =
        "https://api.openweathermap.org/data/2.5/forecast?q=$city&appid=$apiKey&units=metric"
    try {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000

        val responseCode = conn.responseCode
        if (responseCode != 200) {
            val errorStream = conn.errorStream?.bufferedReader()?.readText()
            println("Error response: $responseCode\n$errorStream")
            return@withContext emptyList()
        }

        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(response)
        val list = json.getJSONArray("list")
        val sdfInput = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val sdfOutput = SimpleDateFormat("EEE, dd MMM HH:mm", Locale.getDefault())

        val result = mutableListOf<ForecastItem>()

        for (i in 0 until list.length()) {
            val item = list.getJSONObject(i)
            val dtTxt = item.getString("dt_txt")
            val main = item.getJSONObject("main")
            val temp = main.getDouble("temp")
            val weatherObj = item.getJSONArray("weather").getJSONObject(0)
            val weatherMain = weatherObj.getString("main")
            val iconCode = weatherObj.getString("icon")

            val date = sdfInput.parse(dtTxt)
            val formattedDate = if (date != null) sdfOutput.format(date) else dtTxt

            result.add(ForecastItem(formattedDate, temp, weatherMain, iconCode))
        }

        result
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}
