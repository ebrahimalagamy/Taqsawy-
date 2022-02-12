package com.hema.taqsawy.ui.favorite.favoriteDetails

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hema.taqsawy.adapter.MainAdapter
import com.hema.taqsawy.adapter.NextDayAdapter
import com.hema.taqsawy.data.network.response.DailyItem
import com.hema.taqsawy.data.network.response.HourlyItem
import com.hema.taqsawy.databinding.ActivityFavoriteDetailsBinding
import com.hema.taqsawy.internal.UnitSystem
import com.hema.taqsawy.providers.MyContextWrapper
import com.hema.taqsawy.providers.SharedPreferencesProvider
import java.io.IOException
import java.util.*

class FavoriteDetailsActivity : AppCompatActivity() {

    private lateinit var nextDayAdapter: NextDayAdapter
    private lateinit var mainAdapter: MainAdapter
    private lateinit var binding: ActivityFavoriteDetailsBinding
    private lateinit var favoriteDetailsViewModel: FavoriteDetailsViewModel
    private lateinit var sharedPref: SharedPreferencesProvider
    private lateinit var lang: String
    private var address: String = ""

    override fun attachBaseContext(newBase: Context?) {
        sharedPref = SharedPreferencesProvider(newBase!!)
        lang = sharedPref.getLanguage!!
        super.attachBaseContext(MyContextWrapper.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val lat = intent.getStringExtra("lat")
        val lng = intent.getStringExtra("lng")

        favoriteDetailsViewModel = ViewModelProvider(this)[FavoriteDetailsViewModel::class.java]

        //fetch favorite data from DB
        favoriteDetailsViewModel.getFavoriteWeatherData(lat, lng).observe(this) {
            if (it != null) {
                val daily: List<DailyItem?>? = it.daily
                nextDayAdapter = NextDayAdapter(applicationContext, daily)
                binding.rvDailyWeather.adapter = nextDayAdapter
                binding.rvDailyWeather.layoutManager =
                    LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
                binding.rvDailyWeather.setHasFixedSize(true)
                nextDayAdapter?.notifyDataSetChanged()
            }
            if (it != null) {
                val hourly: List<HourlyItem?>? = it.hourly
                mainAdapter = MainAdapter(this, hourly)

                binding.rvListWeatherHome.adapter = mainAdapter
                binding.rvListWeatherHome.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.rvListWeatherHome.setHasFixedSize(true)
                mainAdapter?.notifyDataSetChanged()

                val description = it.current?.weather?.get(0)?.description

                binding.tvTempeatur.text =
                    String.format(
                        Locale.getDefault(),
                        "%.0f°${UnitSystem.tempUnit}",
                        it.current?.temp
                    )

                val geocoderAddres = Geocoder(this, Locale(sharedPref.getLanguage.toString()))

                try {
                    if (sharedPref.getLanguage.toString() == "ar") {
                        address = geocoderAddres.getFromLocation(it.lat, it.lon, 1)[0].countryName
                            ?: it.timezone.toString()
                    } else {
                        address = geocoderAddres.getFromLocation(it.lat, it.lon, 1)[0].adminArea
                            ?: it.timezone.toString()
                        address += ",${
                            geocoderAddres.getFromLocation(
                                it.lat,
                                it.lon,
                                1
                            )[0].countryName ?: it.timezone.toString()
                        }"
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                binding.location.text = address
                binding.tvWeather.text = description.toString()
                binding.windSpeedTxt.text =
                    it.current?.windSpeed.toString() + " ${UnitSystem.WindSpeedUnit}"
                binding.humidityTxt.text = it.current?.humidity.toString() + " %"
                binding.pressure.text = it.current?.pressure.toString() + " hpa"
                binding.clouds.text = it.current?.clouds.toString() + " %"


            }
        }
    }

}