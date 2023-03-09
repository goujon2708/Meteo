package master.kotlin.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import master.kotlin.weather.POJO.ModelClass
import master.kotlin.weather.Utilities.ApiUtilities
import master.kotlin.weather.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var activityMainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding=DataBindingUtil.setContentView(this,R.layout.activity_main)
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)


        supportActionBar?.hide()
        activityMainBinding.rlMainLayout.visibility = View.GONE

        getCurrentLocation();

    }

    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION=100
        cont val API_KEY = "?"
    }
    private fun getCurrentLocation{

    }

    private fun fetchCurrentLocationWeather(latitude : String, longitude: String){
        activityMainBinding.pbLoading.visibility=View.VISIBLE
        ApiUtilities.getApiInterface()?.getCurrentWeatherData(latitude,longitude,API_KEY)?.enqueue(object :Callback<ModelClass>{
            override fun onResponse(call:Call<ModelClass>,response: Response<ModelClass>){
                if(response.isSuccessful){
                    setDataOnView(response.body())
                }
            }

            override fun onFailure(call:Call<ModelClass>,t : Throwable) {
                Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
            }



        })

    }

    private fun setDataOnView(body:ModelClass?){
        val sdf=SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentDate=sdf.format(Date())
        activityMainBinding.tvDateAndTime.text=currentDate
        activityMainBinding.tvDayMaxTemp.text="Day "+kelvinToCelcius(body.main.temp_max)+""
    }
}