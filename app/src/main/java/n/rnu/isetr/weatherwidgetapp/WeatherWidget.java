package n.rnu.isetr.weatherwidgetapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.RemoteViews;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherWidget extends AppWidgetProvider {


    public static Bitmap BuildUpdate(String txttemp,int size,Context context){
        Paint paint=new Paint();
        paint.setTextSize(size);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setSubpixelText(true);
        paint.setAntiAlias(true);
        float baseline=-paint.ascent();
        int width=(int) (paint.measureText(txttemp)+0.5f);
        int height=(int) (baseline+paint.descent()+0.5f);
        Bitmap image=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_4444);
        Canvas canvas=new Canvas(image);
        canvas.drawText(txttemp,0,baseline,paint);
        return image;


    }


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);

        String url= "https://api.openweathermap.org/data/2.5/weather?lat="+MainActivity.getLat()+"&lon="+MainActivity.getLon()+"&appid=f16906242ab83a34571e68f79182b08e&units=metric";

        JsonObjectRequest jsObjRequest= new JsonObjectRequest
                (Request.Method.GET, url,null, new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject mainJSONObject=response.getJSONObject("main");
                            JSONObject sysJSONObject=response.getJSONObject("sys");
                            JSONObject windJSONObject=response.getJSONObject("wind");

                            String temp=Integer.toString((int) Math.round(mainJSONObject.getDouble("temp")));
                            String hum=Integer.toString((int) Math.round(mainJSONObject.getDouble("humidity")));
                            String city=response.getString("name");
                            String country=sysJSONObject.getString("country");
                            String loc=city+","+country;
                            String vent=Integer.toString((int) Math.round(windJSONObject.getDouble("speed")));


                            views.setImageViewBitmap(R.id.imgtemp, BuildUpdate(temp+"°C",200,context));
                            views.setImageViewBitmap(R.id.imgloc, BuildUpdate(loc,50,context));
                           views.setImageViewBitmap(R.id.imghum, BuildUpdate("Humidity "+hum+"%",50,context));
                           views.setImageViewBitmap(R.id.imgvent, BuildUpdate("Wind "+vent+" M/S",50,context));



                            // Instruct the widget manager to update the widget
                            appWidgetManager.updateAppWidget(appWidgetId, views);

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        //todo
                    }
                });
        //Access the RequestQueue through your singleton class.
        RequestQueue queue= Volley.newRequestQueue(context);
        queue.add(jsObjRequest);





    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

}