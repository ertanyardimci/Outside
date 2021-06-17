package com.nextsoftware.outside

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.emrealtunbilek.havadurumuapp.MySingleton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        checkPermissions()
       
    }


    private fun checkPermissions() {
        if(ContextCompat.checkSelfPermission(applicationContext,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),1)
        }else{
            getLocationFromLatLng()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationFromLatLng() {
        try{
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if(it!=null){
                    val latitude = it.latitude
                    val longitude = it.longitude
                    takePointFromLocation(latitude.toString(),longitude.toString())
                }else{
                    Toast.makeText(applicationContext,"No location!",Toast.LENGTH_LONG).show()
                }
            }
        }catch(e : Exception){
            Toast.makeText(applicationContext,e.localizedMessage.toString(),Toast.LENGTH_LONG).show()

        }

    }

    fun refresh(view : View){
        getLocationFromLatLng()
        Toast.makeText(applicationContext,"Refreshed",Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==1){
            if(grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext,"success to getting location",Toast.LENGTH_LONG).show()
                getLocationFromLatLng()
            }else{
                Toast.makeText(applicationContext,"Failure to getting location",Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun about(view : View){
        val intent = Intent(this,AboutActivity::class.java)
        startActivity(intent)
    }
    fun takePointFromLocation(lat: String, long: String) {
        try{
            val url = "https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+long+"&appid=d4a38a5187dbdc6f4618b606775584b6&units=metric"
            val weatherObject = JsonObjectRequest(
                Request.Method.GET, url, null, object : Response.Listener<JSONObject> {
                    override fun onResponse(response: JSONObject?) {

                        val city = response?.getString("name")
                        if(city!=null){
                            tvAddress.text = city
                        }else{
                            tvAddress.text = "Unknown"
                        }
                        tvDate.text = printDate()
                        val weather = response?.getJSONArray("weather")
                        val des = weather?.getJSONObject(0)?.getString("description")

                        val main = response?.getJSONObject("main")
                        val temp = main?.getString("temp")
                        val tempMin = "Min Temp: " + main?.getString("temp_min")+"°C"
                        val tempMax = "Max Temp: " + main?.getString("temp_max")+"°C"

                        val tempSafe = main?.getString("temp_max")?.toDouble()
                        tvMinTemp.text = tempMin
                        tvMaxTemp.text = tempMax
                        if(des=="rain" && des=="thunderstorm" && des=="shower rain"){
                            tvAphorism.setText(R.string.rainy_day_aphorism)
                            imgRainy.visibility = View.VISIBLE

                        }else if(des=="snow"){
                            tvAphorism.setText(R.string.snow_day_aphorism)
                        }else if(des=="sunny"){
                            if(tempSafe!! >= 25.0){
                                imgWarnSunny.visibility = View.VISIBLE
                            }
                        }

                        else{
                            tvAphorism.setText(R.string.randomly)
                        }
                        tvStatus.text = des

                        tvTemp.text = temp

                        val sys = response?.getJSONObject("sys")
                        val sunrise = sys?.getLong("sunrise")
                        val sunset = sys?.getLong("sunset")

                        tvSunrise.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise!! * 1000))
                        tvSunset.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset!! * 1000))



                        val wind = response?.getJSONObject("wind")
                        val windSpeed = wind?.getString("speed")

                        tvWind.text = windSpeed

                        var icon = weather?.getJSONObject(0)?.getString("icon")

                        var pic = resources.getIdentifier("icon_" + icon?.remLastChar(), "drawable", packageName) //R.drawable.icon_50n
                        imgWeather.setImageResource(pic)




                    }

                },
                object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError?) {

                    }

                })

            MySingleton.getInstance(applicationContext)?.addToRequestQueue(weatherObject)
        }catch (e : Exception){
            warning.visibility = View.VISIBLE
            container.visibility = View.INVISIBLE
            Toast.makeText(this,e.localizedMessage.toString(),Toast.LENGTH_LONG).show()
        }

    }


}

private fun String?.remLastChar(): Any? {
    return this?.substring(0,this?.length-1)
}

fun printDate(): String {

    var calendar = Calendar.getInstance().time
    var formatter = SimpleDateFormat("EEE, MMM yyyy", Locale("tr"))
    var date = formatter.format(calendar)

    return date


}