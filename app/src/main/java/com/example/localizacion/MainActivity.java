package com.example.localizacion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.localizacion.utils.Constantes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private ImageView imageView;
    private Button btnFoto;
    private Uri imagenUri;
    private String email;

    private HashMap<Marker, String> marcadorImagenMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        btnFoto = findViewById(R.id.btnFoto);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        email = getIntent().getStringExtra("email");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentMapa);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnFoto.setOnClickListener(v -> sacarFoto());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }


        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long intervalo = 180000;
        long primerLanzamiento = System.currentTimeMillis() + 10000;



        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                primerLanzamiento,
                intervalo,
                pendingIntent
        );

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        obtenerUbicacion();
        mostrarMarcadores();

        mMap.setOnMarkerClickListener(marker -> {
            if (marcadorImagenMap.containsKey(marker)) {
                mostrarDialogoImagen(marcadorImagenMap.get(marker));
            }
            return false;
        });
    }

    private void obtenerUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
                            mMap.addMarker(new MarkerOptions().position(pos).title("Estás aquí"));
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }


    private void sacarFoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
            return;
        }

        File foto = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "foto.jpg");
        imagenUri = FileProvider.getUriForFile(this,
                getPackageName() + ".provider", foto);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageView.setImageURI(imagenUri);
            subirImagen(imagenUri);
        }
    }

    private void subirImagen(Uri uri) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    String encoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                    String nombreImagen = System.currentTimeMillis() + ".jpg";

                    JSONObject json = new JSONObject();
                    json.put("email", email);
                    json.put("imagen", encoded);
                    json.put("latitud", lat);
                    json.put("longitud", lon);
                    json.put("nombre_imagen", nombreImagen);

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                            Constantes.URL_UPLOAD_IMAGE, json,
                            response -> {
                                Toast.makeText(this, "Imagen subida", Toast.LENGTH_SHORT).show();
                                mostrarMarcadores(); // Refrescar mapa

                                getSharedPreferences("prefs", MODE_PRIVATE)
                                        .edit()
                                        .putString("ultima_imagen", nombreImagen)
                                        .apply();
                            },
                            error -> Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                    );

                    Volley.newRequestQueue(this).add(request);

                } catch (IOException | org.json.JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error procesando imagen", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void mostrarMarcadores() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                Constantes.URL_GET_MARCADORES,
                null,
                response -> {
                    mMap.clear(); // Limpiar mapa
                    marcadorImagenMap.clear(); // Reset

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            double lat = obj.getDouble("latitud");
                            double lon = obj.getDouble("longitud");
                            String email = obj.getString("email");
                            String imagen = obj.getString("imagen_nombre");

                            LatLng punto = new LatLng(lat, lon);
                            Marker marker = mMap.addMarker(new MarkerOptions().position(punto).title(email));
                            marcadorImagenMap.put(marker, imagen);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> Toast.makeText(this, "Error cargando marcadores", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }

    private void mostrarDialogoImagen(String nombreImagen) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View vista = LayoutInflater.from(this).inflate(R.layout.dialog_imagen, null);
        ImageView img = vista.findViewById(R.id.dialogImageView);
        builder.setView(vista);
        builder.setPositiveButton("Cerrar", null);
        builder.setTitle("Imagen de marcador");

        String url = Constantes.URL_IMAGENES + nombreImagen;

        ImageRequest imageRequest = new ImageRequest(url,
                response -> img.setImageBitmap(response),
                0, 0, ImageView.ScaleType.CENTER_INSIDE,
                Bitmap.Config.RGB_565,
                error -> Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(imageRequest);

        builder.create().show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sacarFoto();
        }
    }
}
