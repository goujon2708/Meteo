import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import master.kotlin.weather.databinding.FragmentPressureBinding

class PressureFragment : Fragment() {
    private var _binding: FragmentPressureBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPressureBinding.inflate(inflater, container, false)
        val view = binding.root

        // Récupérer les données de l'API via les arguments
        val pressure = arguments?.getInt("pressure")

        // Mettre à jour la vue avec les données de pression
        binding.tvPressureValue.text = "$pressure hPa"
        binding.tvPressureLabel.text = "Pressure"

        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updatePressure(pressure: Int) {
        binding.tvPressureValue?.text = pressure.toString()
    }

    fun isViewAvailable(): Boolean {
        return _binding != null
    }
}
