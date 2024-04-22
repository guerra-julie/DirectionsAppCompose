package com.example.cartographie

import android.os.Bundle
import android.text.Layout.Directions
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartographie.ui.theme.CartographieTheme
import com.google.gson.annotations.SerializedName
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import okhttp3.OkHttpClient
// import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CartographieTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}

// interface for API call
interface DirectionsAPI {
    @GET("{coordinates}")
    fun searchDirections(@Path("coordinates") countryName: String): Call<List<DirectionsDTO>>
}

// data class following data structure given on https://docs.mapbox.com/api/navigation/directions/#response-retrieve-directions
// the goal was to format the response from the API
data class DirectionsDTO(
    @SerializedName("code")
    val code: String,
    @SerializedName("routes")
    val route: Array<Route>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DirectionsDTO

        if (code != other.code) return false
        if (!route.contentEquals(other.route)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + route.contentHashCode()
        return result
    }
}

// data class describing the Route object as described here : https://docs.mapbox.com/api/navigation/directions/#route-object
data class Route(
    @SerializedName("duration")
    val duration: Double,
    @SerializedName("distance")
    val distance: Double,
    @SerializedName("weight_name")
    val weight_name: String, // should always be "pedestrian" in our case
    @SerializedName("weight")
    val weight: Int,
    @SerializedName("geometry")
    val geometry: String, // a polyline
    @SerializedName("legs")
    val legs: Array<Any>,
    @SerializedName("voiceLocale")
    val voiceLocale: String,
    @SerializedName("waypoints")
    val waypoints: Array<Any>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Route

        if (duration != other.duration) return false
        if (distance != other.distance) return false
        if (weight_name != other.weight_name) return false
        if (weight != other.weight) return false
        if (geometry != other.geometry) return false
        if (!legs.contentEquals(other.legs)) return false
        if (voiceLocale != other.voiceLocale) return false
        if (!waypoints.contentEquals(other.waypoints)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = duration.hashCode()
        result = 31 * result + distance.hashCode()
        result = 31 * result + weight_name.hashCode()
        result = 31 * result + weight
        result = 31 * result + geometry.hashCode()
        result = 31 * result + legs.contentHashCode()
        result = 31 * result + voiceLocale.hashCode()
        result = 31 * result + waypoints.contentHashCode()
        return result
    }
}
// still for API call
interface DirectionsRepository {
    suspend fun searchDirections(query: String): List<Directions>
}

// also for API call but the "logging" lib from okhttp doesn't seem to work

/*class DirectionsRepositoryImpl: DirectionsRepository {
    private val BASE_URL = "https://api.mapbox.com/directions/v5/mapbox/walking/"
    private val directionsAPI: DirectionsAPI

    init {
        val mHttpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        val mOkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(mHttpLoggingInterceptor)
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(mOkHttpClient)
            .build()

        directionsAPI = retrofit.create(DirectionsAPI::class.java)
    }

    override suspend fun searchDirections(query: String): List<Directions> {
        return try {
            val response = directionsAPI.searchDirections(query).awaitResponse()
            if (response.isSuccessful) {
                val directions = response.body() ?: emptyList()
                directions.map { it.toModel() }
            } else {
                emptyList()
            }
        } catch (exception: Exception) {
            val e = exception
            emptyList()
        }
    }
}*/

// UI
@Composable
fun App(modifier: Modifier = Modifier) {
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }

    Column (
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Column (
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = stringResource(R.string.welcome_text),
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
            )
            Row {
                Text(text = stringResource(R.string.start), modifier = Modifier
                    .padding(4.dp)
                    .align(alignment = Alignment.CenterVertically))
                TextField(value = start, onValueChange = { start = it }, modifier = Modifier.padding(4.dp))
            }
            Row {
                Text(text = stringResource(R.string.end), modifier = Modifier
                    .padding(4.dp)
                    .align(alignment = Alignment.CenterVertically))
                TextField(value = end, onValueChange = { end = it }, modifier = Modifier.padding(4.dp))
            }
            Button(onClick = { callAPI(start, end) }, modifier = Modifier.align(alignment = Alignment.CenterHorizontally)) {
                Text(text = stringResource(R.string.calculate))
            }
        }
        MapboxMap(
            mapViewportState = MapViewportState().apply {
                setCameraOptions() {
                    zoom(4.0)
                    center(Point.fromLngLat(2.76, 47.0)) // center of France
                    pitch(0.0)
                    bearing(0.0)
                }
            },
        )

    }
}

fun callAPI(start: String, end: String) {
    // was supposed to be the function called for API calls
}

@Preview(showBackground = true)
@Composable
fun CartographiePreview() {
    CartographieTheme {
        App(modifier = Modifier)
    }
}