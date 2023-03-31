package master.kotlin.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import master.kotlin.weather.databinding.FragmentTemperatureBinding

class TemperatureFragment : Fragment() {
    private var _binding: FragmentTemperatureBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemperatureBinding.inflate(inflater, container, false)
        val view = binding.root

        val tempF = arguments?.getInt("tempF")

        binding.tvTempFValue.text = tempF?.toString() ?: "N/A"

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateTempF(tempF: Int) {
        binding.tvTempFValue.text = "$tempF°F"
    }

}
