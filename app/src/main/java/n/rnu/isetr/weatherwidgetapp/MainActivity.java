package n.rnu.isetr.weatherwidgetapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final String API_KEY = "df07195ad38e7d3cbb2f448d3aec3285";

    ImageView iconWeather;
    TextView tvTemp, tvLoc,tvDesc,tvWindspeed,tvHum;
    ListView lvDailyWeather;
    ProgressBar pb;

    private static String lat;
    private static String lon;
    android.location.LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );


        iconWeather = findViewById(R.id.iconWeather);
        tvTemp = findViewById(R.id.tvTemp);
        tvLoc = findViewById(R.id.tvLoc);
        tvDesc=findViewById(R.id.tvdesc);
        tvWindspeed = findViewById(R.id.windspeed);
        tvHum=findViewById(R.id.hum);
        lvDailyWeather = findViewById(R.id.lvDailyWeather);
        pb=findViewById(R.id.pb);


        //Runtime permissions
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            },100);
        }
        getLocation();


    }

    @SuppressLint("MissingPermission")
    private void getLocation() {

        try {
            locationManager = (android.location.LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER,5000,5,MainActivity.this);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
         try {
            lat = String.valueOf(location.getLatitude());
            lon= String.valueOf(location.getLongitude());
            TodaysWeather(lat,lon);


        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private void TodaysWeather(String lati , String longi) {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat="+lati+"&lon="+longi+"&appid="+API_KEY+"&units=metric";
        Ion.with(this)
                .load(apiUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (e != null) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        } else {
                            pb.setVisibility(View.INVISIBLE);
                            // convert json response to java
                            JsonObject main = result.get("main").getAsJsonObject();
                            JsonObject wind = result.get("wind").getAsJsonObject();
                            String temp = Integer.toString((int) Math.round(main.get("temp").getAsDouble()));
                            tvTemp.setText(temp + "°");
                            String hum=Integer.toString((int) Math.round(main.get("humidity").getAsDouble()));
                            String windspeed=Integer.toString((int) Math.round(wind.get("speed").getAsDouble()));
                            tvHum.setText(hum+"%");
                            tvWindspeed.setText(windspeed+" M/S");
                            JsonObject sys = result.get("sys").getAsJsonObject();
                            String city = result.get("name").getAsString();
                            String country = sys.get("country").getAsString();
                            tvLoc.setText(city + ", " + country);
                            tvLoc.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_location, 0);
                            JsonArray weather = result.get("weather").getAsJsonArray();
                            String description = weather.get(0).getAsJsonObject().get("description").getAsString();
                            tvDesc.setText(description);
                            String icon = weather.get(0).getAsJsonObject().get("icon").getAsString();
                            loadIcon(icon);
                            RestOfTheWeekWeather(lati, longi);
                        }
                    }
                });
    }



    private void RestOfTheWeekWeather(String lat,String lon) {
        String apiUrl = "https://api.openweathermap.org/data/2.5/onecall?lat="+lat+"&lon="+lon+"&exclude=hourly,minutely,current&units=metric&appid=" + API_KEY;
        Ion.with(this)
                .load(apiUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (e != null) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        } else {
                            List<Weather> weatherList = new ArrayList<>();
                            String timeZone = result.get("timezone").getAsString();
                            JsonArray daily = result.get("daily").getAsJsonArray();
                            for(int i=1;i<daily.size();i++) {
                                Long date = daily.get(i).getAsJsonObject().get("dt").getAsLong();
                                String temp = Integer.toString((int) Math.round(daily.get(i).getAsJsonObject().get("temp").getAsJsonObject().get("day").getAsDouble()));
                                String icon = daily.get(i).getAsJsonObject().get("weather").getAsJsonArray().get(0).getAsJsonObject().get("icon").getAsString();
                                weatherList.add(new Weather(date, timeZone, temp, icon));
                            }

                            // attach adapter to listview
                            DailyWeatherAdapter dailyWeatherAdapter = new DailyWeatherAdapter(MainActivity.this, weatherList);
                            lvDailyWeather.setAdapter(dailyWeatherAdapter);
                        }
                    }
                });
    }


    private void loadIcon(String icon) {
        Ion.with(this)
                .load("http://openweathermap.org/img/w/" + icon + ".png")
                .intoImageView(iconWeather);
    }



    //For widget
    public static String getLat() {
        return lat;
    }
    public static String getLon() {
        return lon;
    }






}