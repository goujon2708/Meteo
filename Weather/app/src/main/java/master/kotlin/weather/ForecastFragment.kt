import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import master.kotlin.weather.POJO.ForecastModel
import master.kotlin.weather.databinding.FragmentForecastBinding


class ForecastFragment : Fragment() {

    private lateinit var binding: FragmentForecastBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun updateForecastData(forecast: ForecastModel?) {
        if (forecast != null) {
            binding.day1Date.text = forecast.daily[0].dt.toString()
            binding.day1TempMax.text = forecast.daily[0].temp.max.toString()
            binding.day1TempMin.text = forecast.daily[0].temp.min.toString()
            binding.day1Description.text = forecast.daily[0].weather[0].description

            binding.day2Date.text = forecast.daily[1].dt.toString()
            binding.day2TempMax.text = forecast.daily[1].temp.max.toString()
            binding.day2TempMin.text = forecast.daily[1].temp.min.toString()
            binding.day2Description.text = forecast.daily[1].weather[0].description

            binding.day3Date.text = forecast.daily[2].dt.toString()
            binding.day3TempMax.text = forecast.daily[2].temp.max.toString()
            binding.day3TempMin.text = forecast.daily[2].temp.min.toString()
            binding.day3Description.text = forecast.daily[2].weather[0].description
        }
    }

}
