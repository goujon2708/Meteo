package master.kotlin.weather

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import master.kotlin.weather.POJO.ModelClass
import master.kotlin.weather.Utilities.ApiUtilities
import master.kotlin.weather.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt



class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 123

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    private var modelClassData: ModelClass? = null // Déclaration de la variable de classe
    private lateinit var weatherFragmentAdapter: WeatherFragmentAdapter

    // Liste des favoris
    private lateinit var villesFavorites: ArrayList<String>

    // Spinner du menu déroulant des favoris
    private lateinit var spinner : Spinner

    // Adapter pour la liste des favoris
    private lateinit var arrayAdapter: ArrayAdapter<String>

    // Sert pour récupérer la valeur courant dans la barre de recherche
    private lateinit var editText: EditText

    // icones des favoris (une étoile pleine jaune et une étoile vide)
    private lateinit var notFavorisIV: ImageView
    private lateinit var favorisIV: ImageView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        supportActionBar?.hide()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        activityMainBinding.rlMainLayout.visibility = View.GONE

        // Initialisation de la liste
        this.villesFavorites = arrayListOf("Favoris", "Dubai", "Paris", "Madrid")

        // Récupération du spinner
        this.spinner = findViewById(R.id.sp_favoris)

        // Construction du menu déroulant
        this.arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, villesFavorites)

        // Récupération de la valeur rentrée dans la barre de recherche
        this.editText = findViewById(R.id.et_get_city_name)

        // Récupration des icones "favoris"
        this.notFavorisIV = findViewById(R.id.iv_notFavoris)
        this.favorisIV = findViewById(R.id.iv_favoris)

        // Au lancement de l'application, si la ville où est localisé l'utilisateur est déjà ajoutée aux favoris
        // alors affichage de l'étoile pleine
        // sinon on afficge l'étoile vide
        if(this.villesFavorites.contains(this.editText.text.toString())) {

            notFavorisIV.visibility = View.GONE
            favorisIV.visibility = View.VISIBLE
        } else {

            favorisIV.visibility = View.GONE
            notFavorisIV.visibility = View.VISIBLE
        }


        // Affichage du spinner
        spinner.adapter = arrayAdapter

        // Affichage du toast à la sélection d'une ville
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long) {

                if(parent?.getItemAtPosition(position).toString() != "Favoris") {

                    getCityWeather(parent?.getItemAtPosition(position).toString())
                    notFavorisIV.visibility = View.GONE
                    favorisIV.visibility = View.VISIBLE
                }

                Toast.makeText(applicationContext, "ville sélectionnée : " + villesFavorites[position], Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }




        // Gestion de la maj du menu favoris
        this.notFavorisIV.setOnClickListener {

            // l'utilisateur choisit d'ajouter la ville aux favoris
            notFavorisIV.visibility = View.GONE
            favorisIV.visibility = View.VISIBLE
            majFavoris(this.editText.text.toString())
            // Requête API et maj de l'interface
            getCityWeather(this.editText.text.toString())
        }

        this.favorisIV.setOnClickListener {

            // L'utilisateur choisit de supprimer la liste des favoris
            favorisIV.visibility = View.GONE
            notFavorisIV.visibility = View.VISIBLE
            majFavoris(this.editText.text.toString())
            // Requête API et maj de l'interface
            getCityWeather(this.editText.text.toString())
        }


        // Set up the ViewPager with a PagerAdapter
        viewPager = activityMainBinding.viewPager
        viewPager.visibility = View.VISIBLE // Set ViewPager2 visibility to VISIBLE

        weatherFragmentAdapter = WeatherFragmentAdapter(this)
        viewPager.adapter = weatherFragmentAdapter // Set the adapter after initializing ViewPager2

        // Vérifier si les permissions ont déjà été accordées
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Les permissions ont déjà été accordées
            initUI()
        } else {
            // Les permissions n'ont pas encore été accordées, les demander à l'utilisateur
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
        }

        activityMainBinding.etGetCityName.setOnEditorActionListener { v, actionId, KeyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                getCityWeather(activityMainBinding.etGetCityName.text.toString())

                if(this.villesFavorites.contains(this.editText.text.toString())) {

                    notFavorisIV.visibility = View.GONE
                    favorisIV.visibility = View.VISIBLE
                } else {

                    favorisIV.visibility = View.GONE
                    notFavorisIV.visibility = View.VISIBLE
                }

                val view = this.currentFocus

                if (view != null) {

                    val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    activityMainBinding.etGetCityName.clearFocus()
                }
                true
            } else false
        }
    }


    /**
     * Met à jour la liste des favoris lorsque l'utilisateur interagit avec le bouton "favoris"
     * Met également à jour le menu déroulant
     */
    private fun majFavoris(nomVille: String) {

        // suppression de la ville courante de la liste des favoris
        if(this.villesFavorites.contains(nomVille)) {

            this.villesFavorites.remove(nomVille)

            // Maj de l'IHM
            this.arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, this.villesFavorites)
            this.spinner.adapter = this.arrayAdapter

        // ajout de la ville courante dans la liste des favoris
        } else {

            this.villesFavorites.add(nomVille)

            // Maj de l'IHM
            this.arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, this.villesFavorites)
            this.spinner.adapter = this.arrayAdapter
        }
    }


    private fun initUI() {
        // Initialiser l'interface utilisateur ici
        activityMainBinding.rlMainLayout.visibility = View.VISIBLE
        getCurrentLocation()
        // ...
    }

// The rest of your code remains the same.


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // Vérifier si l'utilisateur a accordé les permissions
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Les permissions ont été accordées, initialiser l'interface utilisateur
                initUI()
            } else {
                // Les permissions ont été refusées, afficher un message ou quitter l'application
                finish()
            }
        }
    }

    private fun getCityWeather(cityName : String){
        activityMainBinding.pbLoading.visibility=View.VISIBLE
        ApiUtilities.getApiInterface()?.getCityWeatherData(cityName, API_KEY)?.enqueue(object : Callback<ModelClass>{

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call:Call<ModelClass>, response: Response<ModelClass>){

                setDataOnView(response.body(), viewPager.currentItem)

            }

            override fun onFailure(call:Call<ModelClass>,t : Throwable) {
                Toast.makeText(applicationContext, "Not a Valid city Name", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION=100
        const val API_KEY = "768ad9b8668e5007738ef583962d1679"
    }
    private fun getCurrentLocation(){

        if (checkPermissions()){
            if(isLocationEnabled()){
                if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION )
                    != PackageManager.PERMISSION_GRANTED){
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){
                        task -> val location : Location? = task.result
                    if(location==null){
                        Toast.makeText(this,"Null received", Toast.LENGTH_SHORT).show()
                    }else{
                        fetchCurrentLocationWeather(
                            location.latitude.toString(),
                            location.longitude.toString()
                        )
                    }
                }
            }else{
                Toast.makeText(this, "Turn on location",Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

        }else{
            requestPermission()
        }

    }
    private fun requestPermission() {

        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    private fun fetchCurrentLocationWeather(latitude : String, longitude: String){
        activityMainBinding.pbLoading.visibility=View.VISIBLE
        ApiUtilities.getApiInterface()?.getCurrentWeatherData(latitude,longitude,API_KEY)?.enqueue(object :Callback<ModelClass>{

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call:Call<ModelClass>, response: Response<ModelClass>){
                if(response.isSuccessful){
                    setDataOnView(response.body(),viewPager.currentItem)
                }
            }

            override fun onFailure(call:Call<ModelClass>,t : Throwable) {
                Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
            }



        })

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataOnView(body: ModelClass?, position: Int) {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentDate = sdf.format(Date())
        activityMainBinding.tvDateAndTime.text = currentDate

        // Mise à jour des données du fragment et notification à l'adaptateur
        val humidity = body?.main?.humidity?.toString() ?: "N/A"
        val pressure = body?.main?.pressure ?: 0

        // Mettre à jour les données du fragment Humidity
        val humidityBundle = Bundle()
        humidityBundle.putString("humidity", humidity)
        weatherFragmentAdapter.setFragmentData(0, humidityBundle)

        // Mettre à jour les données du fragment Pressure
        val pressureBundle = Bundle()
        pressureBundle.putInt("pressure", pressure)
        weatherFragmentAdapter.setFragmentData(1, pressureBundle)

        // common data to be displayed in all views/fragments
        activityMainBinding.tvDayMaxTemp.text = "Jour " + kelvinToCelsius(body!!.main.temp_max).roundToInt() + "°C"
        activityMainBinding.tvDayMinTemp.text = "Nuit " + kelvinToCelsius(body!!.main.temp_min).roundToInt() + "°C"
        activityMainBinding.tvFeelsLke.text = "Ressenti " + kelvinToCelsius(body!!.main.feels_like).roundToInt() + "°C"
        activityMainBinding.tvWeatherType.text = body.weather[0].main
        activityMainBinding.tvSunrise.text = timeStampToLocalDate(body.sys.sunrise.toLong())
        activityMainBinding.tvWindSpeed.text = body?.wind?.speed.toString() + " m/s"
        activityMainBinding.tvTempF.text = "" + ((kelvinToCelsius(body?.main?.temp ?: 0.0)).times(1.8).plus(32).roundToInt())
        activityMainBinding.etGetCityName.setText(body?.name)
        activityMainBinding.tvTemp.text = "" + kelvinToCelsius(body?.main?.temp ?: 0.0) + "°C"

        updateUI(body?.weather?.get(0)?.id ?: 0)
        weatherFragmentAdapter.notifyDataSetChanged()
    }


    private fun onPageSelected(position: Int) {
        setDataOnView(modelClassData, position)
    }

    private fun updateUI(id : Int) {
        if(id in 200..232 ){
            //Thunderstorm
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.thunderstorm)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.thunderstorm))
            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.thunderstorm_bg
            )
            activityMainBinding.llMainBgBelow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.thunderstorm_bg
            )
            activityMainBinding.llMainBgAbove.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.thunderstorm_bg
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.thunderstorm_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.thunderstorm)
        } else if(id in 300..321){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.drizzle)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.drizzle))
            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.drizzle_bg
            )
            activityMainBinding.llMainBgBelow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.drizzle_bg
            )
            activityMainBinding.llMainBgAbove.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.drizzle_bg
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.drizzle_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.drizzle)
        }else if(id in 500..531){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.rain)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.rain))
            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.rainy_bg
            )
            activityMainBinding.llMainBgBelow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.rainy_bg
            )
            activityMainBinding.llMainBgAbove.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.rainy_bg
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.rainy_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.rain)
        }else if(id in 600..620){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.snow)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.snow))
            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.snow_bg_img
            )
            activityMainBinding.llMainBgBelow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.snow_bg_img
            )
            activityMainBinding.llMainBgAbove.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.snow_bg_img
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.snow_bg_img)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.snow)
        }else if(id in 701..781){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.atmosphere)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.atmosphere))
            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.mist_bg
            )
            activityMainBinding.llMainBgBelow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.mist_bg
            )
            activityMainBinding.llMainBgAbove.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.mist_bg
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.mist_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.mist)
        } else if(id == 800){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.clear)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.clear))
            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.clear_bg
            )
            activityMainBinding.llMainBgBelow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.clear_bg
            )
            activityMainBinding.llMainBgAbove.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.clear_bg
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.clear_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.clear)
        }else{
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.clouds)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.clouds))
            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.cloud_bg
            )
            activityMainBinding.llMainBgBelow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.cloud_bg
            )
            activityMainBinding.llMainBgAbove.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.cloud_bg
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.cloud_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.clouds)
        }

        activityMainBinding.pbLoading.visibility = View.GONE
        activityMainBinding.rlMainLayout.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeStampToLocalDate(timeStamp: Long): String{
        val localTime= timeStamp.let{
            Instant.ofEpochSecond(it).
                    atZone(ZoneId.systemDefault()).toLocalTime()
        }

        return localTime.toString()
    }
    private fun isLocationEnabled() : Boolean{
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions() : Boolean{
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }
        return false
    }
    private fun kelvinToCelsius(temp:Double):Double {
        var intTemp = temp
        intTemp = intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }


}