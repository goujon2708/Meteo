package master.kotlin.weather.POJO

import com.google.gson.annotations.SerializedName

data class ForecastModel(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("daily") val daily: List<DailyForecast>
)


