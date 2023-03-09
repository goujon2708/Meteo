package master.kotlin.weather.POJO

import com.google.gson.annotations.SerializedName

data class Wind(

    @SerializedName("speed") val speed: Double,
    @SerializedName("def") val deg: Int
)
