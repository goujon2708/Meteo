package master.kotlin.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import master.kotlin.weather.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var activityMainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding=DataBindingUtil.setContentView(this,R.layout.activity_main)
        supportActionBar?.hide()
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
        activityMainBinding.rlMainLayout.visibility = View.GONE

    }
}