package master.kotlin.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import master.kotlin.weather.databinding.FragmentWindBinding


class WindFragment : Fragment() {
    private var _binding: FragmentWindBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWindBinding.inflate(inflater, container, false)
        val view = binding.root

        val windSpeed = arguments?.getDouble("windSpeed")

        binding.tvWindSpeedValue.text = "$windSpeed hPa"

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun isViewAvailable(): Boolean {
        return _binding != null
    }

    fun updateWindSpeed(windSpeed: Double) {
        if (isViewAvailable()) {
            binding.tvWindSpeedValue.text = "$windSpeed m/s"
        }
    }


}
