package master.kotlin.weather

import ForecastFragment
import PressureFragment
import android.annotation.SuppressLint
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
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import master.kotlin.weather.POJO.ForecastModel
import master.kotlin.weather.POJO.ModelClass
import master.kotlin.weather.Utilities.ApiUtilities
import master.kotlin.weather.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

    private lateinit var tabLayout: TabLayout


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
        this.arrayAdapter = ArrayAdapter(this, R.layout.color_spinner_layout, villesFavorites)
        this.arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)

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
                } else {
                    getCurrentLocation()
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
            updateViewPager() // Ajoutez cette ligne
        }

        this.favorisIV.setOnClickListener {

            // L'utilisateur choisit de supprimer la liste des favoris
            favorisIV.visibility = View.GONE
            notFavorisIV.visibility = View.VISIBLE
            majFavoris(this.editText.text.toString())
            // Requête API et maj de l'interface
            getCityWeather(this.editText.text.toString())
            updateViewPager() // Ajoutez cette ligne
        }


        // Set up the ViewPager with a PagerAdapter
        viewPager = activityMainBinding.viewPager
        viewPager.visibility = View.VISIBLE // Set ViewPager2 visibility to VISIBLE

        weatherFragmentAdapter = WeatherFragmentAdapter(this)
        viewPager.adapter = weatherFragmentAdapter // Set the adapter after initializing ViewPager2

        viewPager.adapter = weatherFragmentAdapter // Définir l'adaptateur après l'initialisation de ViewPager2


        tabLayout = activityMainBinding.tabLayout

        // Vérifier si les permissions ont déjà été accordées
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Les permissions ont déjà été accordées
            initUI()
        } else {

            // Les permissions n'ont pas encore été accordées, les demander à l'utilisateur
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
        }

        // gestion de la saisie au clavier
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
                updateViewPager()
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
            this.arrayAdapter = ArrayAdapter(this, R.layout.color_spinner_layout, this.villesFavorites)
            this.arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
            this.spinner.adapter = this.arrayAdapter

        // ajout de la ville courante dans la liste des favoris
        } else {

            this.villesFavorites.add(nomVille)

            // Maj de l'IHM
            this.arrayAdapter = ArrayAdapter(this, R.layout.color_spinner_layout, this.villesFavorites)
            this.arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
            this.spinner.adapter = this.arrayAdapter
        }
    }


    /**
     * Initialisation de l'application
     */
    private fun initUI() {

        // Initialiser l'interface utilisateur ici
        activityMainBinding.rlMainLayout.visibility = View.VISIBLE
        getCurrentLocation()

        val tabLayout = activityMainBinding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Hum"
                1 -> tab.text = "Press"
                2 -> tab.text = "Wind"
                3 -> tab.text = "Farh"
                4 -> tab.text = "Sun"
                5 -> tab.text = "Prev"
            }
        }.attach()
    }

    private fun updateViewPager() {
        weatherFragmentAdapter = WeatherFragmentAdapter(this)
        viewPager.adapter = weatherFragmentAdapter
        viewPager.adapter?.notifyDataSetChanged()
    }

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

    /**
     * Appel de l'API pour récupérer les données en fonction de la ville
     */
    private fun getCityWeather(cityName : String) {

        activityMainBinding.pbLoading.visibility=View.VISIBLE

        // appel API
        ApiUtilities.getApiInterface()?.getCityWeatherData(cityName, API_KEY)?.enqueue(object : Callback<ModelClass> {

            override fun onResponse(call:Call<ModelClass>, response: Response<ModelClass>) {

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

    /**
     * Renvoi la location courante de l'utilisateur
     */
    private fun getCurrentLocation() {

        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Toast.makeText(this, "Null received", Toast.LENGTH_SHORT).show()
                    } else {
                        val latitude = location.latitude.toString()
                        val longitude = location.longitude.toString()

                        fetchCurrentLocationWeather(latitude, longitude)
                        fetchWeatherForecast(latitude, longitude)
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

        } else {
            requestPermission()
        }

    }

    private fun requestPermission() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION)
    }


    /**
     * Récupération des données météo en fonction de la localisation exacte de l'utilisateur
     */
    private fun fetchCurrentLocationWeather(latitude : String, longitude: String) {

        activityMainBinding.pbLoading.visibility=View.VISIBLE

        ApiUtilities.getApiInterface()?.getCurrentWeatherData(latitude,longitude,API_KEY)?.enqueue(object :Callback<ModelClass> {

            override fun onResponse(call:Call<ModelClass>, response: Response<ModelClass>) {

                if(response.isSuccessful) {

                    setDataOnView(response.body(),viewPager.currentItem)
                    Log.d("API CALL", "Forecast retrieved: $response")
                }
            }

            override fun onFailure(call:Call<ModelClass>,t : Throwable) {

                Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
            }
        })

    }

    /**
     * Récupération de la prédiction de la météo sur plusieurs jours
     */
    private fun fetchWeatherForecast(latitude: String, longitude: String) {
        activityMainBinding.pbLoading.visibility = View.VISIBLE
        ApiUtilities.getApiInterface()?.getWeatherForecast(
            latitude,
            longitude,
            "current,minutely,hourly,alerts",
            "metric",
            API_KEY
        )?.enqueue(object : Callback<ForecastModel> {

            override fun onResponse(call: Call<ForecastModel>, response: Response<ForecastModel>) {
                if (response.isSuccessful) {
                    val forecast = response.body()
                    // Convertir l'objet ForecastModel en chaîne JSON
                    val gson = Gson()
                    val jsonForecast = gson.toJson(forecast)
                    Log.d("API CALL", "Forecast retrieved: $jsonForecast")

                    // Passer les données de prévision au ForecastFragment
                    val forecastData = Bundle().apply {
                        putParcelable("forecast", forecast)
                    }
                    val adapter = viewPager.adapter as WeatherFragmentAdapter
                    adapter.setFragmentData(5, forecastData) // 5 étant l'index de ForecastFragment dans l'adaptateur
                } else {
                    Log.e("API CALL", "Failed to retrieve forecast: ${response.code()}")
                }
                activityMainBinding.pbLoading.visibility = View.GONE
            }


            override fun onFailure(call: Call<ForecastModel>, t: Throwable) {
                Log.e("API CALL", "Error fetching forecast: ${t.message}")
                activityMainBinding.pbLoading.visibility = View.GONE
            }
        })
    }


    /**
     * Mise à jour des données sur l'IHM
     */
    @SuppressLint("SetTextI18n")
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

        val temperatureBundle = Bundle()
        temperatureBundle.putInt("tempF", kelvinToCelsius(body?.main?.temp ?: 0.0).times(1.8).plus(32).roundToInt())

        val windBundle = Bundle()
        windBundle.putDouble("windSpeed", body?.wind?.speed ?: 0.0)

        val sunrise = timeStampToLocalDate(body?.sys?.sunrise?.toLong() ?: 0)
        val sunset = timeStampToLocalDate(body?.sys?.sunset?.toLong() ?: 0)
        val sunInfoBundle = Bundle()
        sunInfoBundle.putString("sunrise", sunrise)
        sunInfoBundle.putString("sunset", sunset)

        weatherFragmentAdapter.setFragmentData(0, humidityBundle)
        weatherFragmentAdapter.setFragmentData(1, pressureBundle)
        weatherFragmentAdapter.setFragmentData(2, windBundle)
        weatherFragmentAdapter.setFragmentData(3, temperatureBundle)
        weatherFragmentAdapter.setFragmentData(4, sunInfoBundle) // Utilisez l'index 4 pour le fragment SunInfo

        weatherFragmentAdapter.notifyDataSetChanged()

        // Mettre à jour les données du fragment Humidity
        val humidityFragment = weatherFragmentAdapter.getFragment(0) as? HumidityFragment
        humidityFragment?.let {
            if (it.isViewAvailable()) {
                it.updateHumidity(humidity)
            }
        }

        val sunInfoFragment = weatherFragmentAdapter.getFragment(4) as? SunInfoFragment
        sunInfoFragment?.let {
            if (it.isViewAvailable()) {
                it.updateSunInfo(sunrise, sunset)
            }
        }

        // Mettre à jour les données du fragment Pressure
        val pressureFragment = weatherFragmentAdapter.getFragment(1) as? PressureFragment
        pressureFragment?.let {
            if (it.isViewAvailable()) {
                it.updatePressure(pressure)
            }
        }
        
        val windFragment = weatherFragmentAdapter.getFragment(2) as? WindFragment
        windFragment?.let {
            if (it.isViewAvailable()) {
                it.updateWindSpeed(body?.wind?.speed ?: 0.0)
            }
        }

        val temperatureFragment = weatherFragmentAdapter.getFragment(3) as? TemperatureFragment
        temperatureFragment?.let {
            if (it.isViewAvailable()) {
                it.updateTempF(kelvinToCelsius(body?.main?.temp ?: 0.0).times(1.8).plus(32).roundToInt())
            }
        }


        // common data to be displayed in all views/fragments
        activityMainBinding.tvDayMaxTemp.text = "Day : " + kelvinToCelsius(body!!.main.temp_max).roundToInt() + "°C"
        activityMainBinding.tvDayMinTemp.text = "Night : " + kelvinToCelsius(body.main.temp_min).roundToInt() + "°C"
        activityMainBinding.tvFeelsLke.text = "Feels alike : " + kelvinToCelsius(body.main.feels_like).roundToInt() + "°C"
        activityMainBinding.tvWeatherType.text = body.weather[0].main
        activityMainBinding.etGetCityName.setText(body.name)
        activityMainBinding.tvTemp.text = "" + kelvinToCelsius(body.main.temp).roundToInt() + "°C"
        activityMainBinding.etGetCityName.setText(body.name)
        activityMainBinding.tvTemp.text = "" + kelvinToCelsius(body.main.temp ?: 0.0).roundToInt() + "°C"

        updateUI(body.weather?.get(0)?.id ?: 0)

    }


    /**
     * Mise à jour de l'interface utilisateur
     */
    private fun updateUI(id : Int) {

        if(id in 200..232 ) {

            //Thunderstorm
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.thunderstorm)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.thunderstorm))
            activityMainBinding.tabLayout.setBackgroundColor(resources.getColor(R.color.thunderstorm))
            activityMainBinding.tabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.white))

            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.thunderstorm_bg
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.thunderstorm_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.thunderstorm)
        } else if(id in 300..321) {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.drizzle)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.drizzle))
            activityMainBinding.tabLayout.setBackgroundColor(resources.getColor(R.color.drizzle))
            activityMainBinding.tabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.white))

            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.drizzle_bg
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.drizzle_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.drizzle)

        } else if(id in 500..531) {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.rain)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.rain))
            activityMainBinding.tabLayout.setBackgroundColor(resources.getColor(R.color.rain))
            activityMainBinding.tabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.white))

            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.rainy_bg
            )


            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.rainy_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.rain)

        } else if(id in 600..620) {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.snow)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.snow))
            activityMainBinding.tabLayout.setBackgroundColor(resources.getColor(R.color.snow))
            activityMainBinding.tabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.white))

            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.snow_bg_img
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.snow_bg_img)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.snow)
        } else if(id in 701..781) {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.atmosphere)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.atmosphere))
            activityMainBinding.tabLayout.setBackgroundColor(resources.getColor(R.color.atmosphere))
            activityMainBinding.tabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.white))

            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.mist_bg
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.mist_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.mist)
        } else if(id == 800) {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.clear)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.clear))
            activityMainBinding.tabLayout.setBackgroundColor(resources.getColor(R.color.clear))
            activityMainBinding.tabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.white))

            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.clear_bg
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.clear_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.sun)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.clouds)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.clouds))
            activityMainBinding.tabLayout.setBackgroundColor(resources.getColor(R.color.clouds))
            activityMainBinding.tabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.white))

            activityMainBinding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.cloud_bg
            )

            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.cloud_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.clouds)
        }

        activityMainBinding.pbLoading.visibility = View.GONE
        activityMainBinding.rlMainLayout.visibility = View.VISIBLE
    }


    /**
     * Méthode de conversion
     */
    private fun timeStampToLocalDate(timeStamp: Long): String {
        val localDateTime = timeStamp.let {
            Instant.ofEpochSecond(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }

        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        return localDateTime.format(formatter)
    }


    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Méthode qui renvoie vrai si l'utilisateur accepte de partager sa localisation
     */
    private fun checkPermissions(): Boolean {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {

            return true
        }
        return false
    }

    /**
     * Méthode de conversion
     */
    private fun kelvinToCelsius(temp:Double): Double {

        var intTemp = temp
        intTemp = intTemp.minus(273)

        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }
}