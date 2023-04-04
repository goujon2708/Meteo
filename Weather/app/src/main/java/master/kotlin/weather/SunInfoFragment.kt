package master.kotlin.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import master.kotlin.weather.databinding.FragmentSunInfoBinding

class SunInfoFragment : Fragment() {
    private var _binding: FragmentSunInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSunInfoBinding.inflate(inflater, container, false)
        val view = binding.root

        val sunrise = arguments?.getString("sunrise") ?: "N/A"
        val sunset = arguments?.getString("sunset") ?: "N/A"

        binding.tvSunriseValue.text = sunrise
        binding.tvSunsetValue.text = sunset

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateSunInfo(sunrise: String, sunset: String) {
        binding.tvSunriseValue.text = sunrise
        binding.tvSunsetValue.text = sunset
    }
}
