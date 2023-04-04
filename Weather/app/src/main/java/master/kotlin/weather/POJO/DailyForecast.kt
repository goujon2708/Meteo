package master.kotlin.weather.POJO

import com.google.gson.annotations.SerializedName

class DailyForecast (
    @SerializedName("dt") val dt: Long ,
    @SerializedName("temp") val temp: Temp ,
    @SerializedName("weather") val weather: List<Weather> = listOf(),
)