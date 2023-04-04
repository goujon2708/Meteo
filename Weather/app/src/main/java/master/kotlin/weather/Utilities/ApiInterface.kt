package master.kotlin.weather.Utilities

import master.kotlin.weather.POJO.ForecastModel
import master.kotlin.weather.POJO.ModelClass
import retrofit2.Call
import retrofit2.http.Query
import retrofit2.http.GET

interface ApiInterface {

    @GET("weather")
    fun getCurrentWeatherData(
        @Query("lat") latitude:String,
        @Query("lon") longitude:String,
        @Query("APPID") api_key:String
    ):Call<ModelClass>

    @GET("weather")
    fun getCityWeatherData(
        @Query("q") CityName:String,
        @Query("APPID") api_key:String
    ):Call<ModelClass>

    @GET("onecall")
    fun getWeatherForecast(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("exclude") exclude: String,
        @Query("units") units: String,
        @Query("appid") api_key: String
    ): Call<ForecastModel>

}

