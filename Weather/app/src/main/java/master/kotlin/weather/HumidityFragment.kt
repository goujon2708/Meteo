package master.kotlin.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import master.kotlin.weather.databinding.FragmentHumidityBinding

class HumidityFragment : Fragment() {
    private var _binding: FragmentHumidityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHumidityBinding.inflate(inflater, container, false)
        val view = binding.root

        // Récupérer les données de l'API via les arguments
        val humidity = arguments?.getString("humidity")

        // Mettre à jour la vue avec les données d'humidité
        binding.tvHumidityValue.text = humidity ?: "N/A"

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateHumidity(humidity: String) {
        binding.tvHumidityValue?.text = humidity
    }

    fun isViewAvailable(): Boolean {
        return _binding != null
    }
}
