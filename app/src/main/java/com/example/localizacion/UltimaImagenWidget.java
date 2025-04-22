package com.example.localizacion;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.localizacion.utils.Constantes;

public class UltimaImagenWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("Widget", "🟡 onUpdate");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                Constantes.URL_IMAGEN_RANDOM,
                null,
                response -> {
                    try {
                        String nombre = response.getString("imagen_nombre");
                        String url = Constantes.URL_IMAGENES + nombre;

                        ImageRequest imageRequest = new ImageRequest(
                                url,
                                bitmap -> {
                                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
                                    for (int appWidgetId : appWidgetIds) {
                                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_imagen);
                                        views.setImageViewBitmap(R.id.widgetImage, scaled);
                                        appWidgetManager.updateAppWidget(appWidgetId, views);
                                    }
                                },
                                150, 150,
                                ImageView.ScaleType.CENTER_CROP,
                                Bitmap.Config.RGB_565,
                                error -> Log.e("Widget", "Error cargar imagen", error)
                        );

                        Volley.newRequestQueue(context).add(imageRequest);
                    } catch (Exception e) {
                        Log.e("Widget", "Error  JSON", e);
                    }
                },
                error -> Log.e("Widget", " Error  obtener imagen aleatoria", error)
        );

        Volley.newRequestQueue(context).add(request);

    }

    @Override
    public void onEnabled(Context context) {
        Log.d("Widget", "✅ onEnabled llamado");
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName widget = new ComponentName(context, UltimaImagenWidget.class);
        int[] ids = manager.getAppWidgetIds(widget);
        onUpdate(context, manager, ids);
    }
}
