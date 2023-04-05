import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import master.kotlin.weather.POJO.ForecastModel

class WeatherViewModel : ViewModel() {

    private val _forecast = MutableLiveData<ForecastModel?>()
    val forecast: LiveData<ForecastModel?>
        get() = _forecast

    fun updateForecast(forecastModel: ForecastModel?) {
        _forecast.value = forecastModel
    }
}
