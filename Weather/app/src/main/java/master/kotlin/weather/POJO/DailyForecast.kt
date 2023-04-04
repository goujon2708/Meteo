package master.kotlin.weather.POJO

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DailyForecast (
    @SerializedName("dt") val dt: Long,
    @SerializedName("temp") val temp: Temp,
    @SerializedName("weather") val weather: List<Weather> = listOf(),
) : Parcelable {
    override fun toString(): String {
        return "DailyForecast(dt=$dt, temp=$temp, weather=$weather)"
    }
}

