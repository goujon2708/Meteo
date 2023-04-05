import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import master.kotlin.weather.POJO.ForecastModel
import master.kotlin.weather.databinding.FragmentForecastBinding
import java.text.SimpleDateFormat
import java.util.*
import com.bumptech.glide.Glide



class ForecastFragment : Fragment() {

    private lateinit var binding: FragmentForecastBinding
    private lateinit var viewModel: WeatherViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForecastBinding.inflate(inflater, container, false)
        val view = binding.root

        val forecast = arguments?.getParcelable<ForecastModel>("forecast")
        updateForecastData(forecast)

        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(WeatherViewModel::class.java)

        viewModel.forecast.observe(viewLifecycleOwner, { forecast ->
            updateForecastData(forecast)
        })
    }

    fun updateForecastData(forecast: ForecastModel?) {
        if (forecast != null) {
            Log.d("UPDATE_FORECAST", "Updating forecast data")

            binding.day1Date.text = convertTimestampToDate(forecast.daily[1].dt)
            binding.day1TempMax.text = String.format("%.0f°C", forecast.daily[1].temp.max)
            binding.day1TempMin.text = String.format("%.0f°C", forecast.daily[1].temp.min)
            //binding.day1Description.text = forecast.daily[1].weather[0].description
            loadImage(binding.day1Icon, forecast.daily[1].weather[0].icon)
            Log.d("UPDATE_FORECAST", "Day 1: ${binding.day1Date.text}, Max: ${binding.day1TempMax.text}, Min: ${binding.day1TempMin.text}, Icon: ${forecast.daily[0].weather[0].icon}")

            binding.day2Date.text = convertTimestampToDate(forecast.daily[2].dt)
            binding.day2TempMax.text = String.format("%.0f°C", forecast.daily[2].temp.max)
            binding.day2TempMin.text = String.format("%.0f°C", forecast.daily[2].temp.min)
            //binding.day2Description.text = forecast.daily[2].weather[0].description
            loadImage(binding.day2Icon, forecast.daily[2].weather[0].icon)
            Log.d("UPDATE_FORECAST", "Day 2: ${binding.day2Date.text}, Max: ${binding.day2TempMax.text}, Min: ${binding.day2TempMin.text}, Icon: ${forecast.daily[1].weather[0].icon}")

            binding.day3Date.text = convertTimestampToDate(forecast.daily[3].dt)
            binding.day3TempMax.text = String.format("%.0f°C", forecast.daily[3].temp.max)
            binding.day3TempMin.text = String.format("%.0f°C", forecast.daily[3].temp.min)
            //binding.day3Description.text = forecast.daily[3].weather[0].description
            loadImage(binding.day3Icon, forecast.daily[3].weather[0].icon)
            Log.d("UPDATE_FORECAST", "Day 3: ${binding.day3Date.text}, Max: ${binding.day3TempMax.text}, Min: ${binding.day3TempMin.text}, Icon: ${forecast.daily[2].weather[0].icon}")

        } else {
            Log.e("UPDATE_FORECAST", "Forecast data is null")
        }
    }


    private fun loadImage(imageView: ImageView, iconCode: String) {
        val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
        Glide.with(requireContext())
            .load(iconUrl)
            .into(imageView)
    }

    private fun convertTimestampToDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        return dateFormat.format(Date(timestamp * 1000L))
    }

    fun isViewAvailable(): Boolean {
        return binding != null
    }

    fun updateForecast(forecast: ForecastModel?) {
        if (forecast != null) {
            Log.d("UPDATE_FORECAST", "Updating forecast data")

            binding.day1Date.text = convertTimestampToDate(forecast.daily[1].dt)
            binding.day1TempMax.text = String.format("%.0f°C", forecast.daily[1].temp.max)
            binding.day1TempMin.text = String.format("%.0f°C", forecast.daily[1].temp.min)
            loadImage(binding.day1Icon, forecast.daily[1].weather[0].icon)
            Log.d("UPDATE_FORECAST", "Day 1: ${binding.day1Date.text}, Max: ${binding.day1TempMax.text}, Min: ${binding.day1TempMin.text}, Icon: ${forecast.daily[0].weather[0].icon}")

            binding.day2Date.text = convertTimestampToDate(forecast.daily[2].dt)
            binding.day2TempMax.text = String.format("%.0f°C", forecast.daily[2].temp.max)
            binding.day2TempMin.text = String.format("%.0f°C", forecast.daily[2].temp.min)
            loadImage(binding.day2Icon, forecast.daily[2].weather[0].icon)
            Log.d("UPDATE_FORECAST", "Day 2: ${binding.day2Date.text}, Max: ${binding.day2TempMax.text}, Min: ${binding.day2TempMin.text}, Icon: ${forecast.daily[1].weather[0].icon}")

            binding.day3Date.text = convertTimestampToDate(forecast.daily[3].dt)
            binding.day3TempMax.text = String.format("%.0f°C", forecast.daily[3].temp.max)
            binding.day3TempMin.text = String.format("%.0f°C", forecast.daily[3].temp.min)
            loadImage(binding.day3Icon, forecast.daily[3].weather[0].icon)
            Log.d("UPDATE_FORECAST", "Day 3: ${binding.day3Date.text}, Max: ${binding.day3TempMax.text}, Min: ${binding.day3TempMin.text}, Icon: ${forecast.daily[2].weather[0].icon}")

        } else {
            Log.e("UPDATE_FORECAST", "Forecast data is null")
        }
    }

}
