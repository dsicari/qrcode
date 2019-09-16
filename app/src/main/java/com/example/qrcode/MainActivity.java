package com.example.qrcode;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.StringRequest;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private RequestQueue requestQueue;
    private String jsonGetEventos;
    private List<String> spinnerStrEventos;
    private List<String> spinnerStrEventosAtividades;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logStatus("app iniciado", false);

        // Add listener para spinners
        Spinner spinnerEvento = (Spinner) findViewById(R.id.spinnerEvento);
        spinnerEvento.setOnItemSelectedListener(MainActivity.this);
        Spinner spinnerEventoAtividade = (Spinner) findViewById(R.id.spinnerEventoAtividade);
        spinnerEvento.setOnItemSelectedListener(MainActivity.this);

        // request permission INTERNET
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.INTERNET)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        boolean rslt = getEventos();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this,"Voce deve aceitar permissao para app funcionar", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();

        // request permission CHECK CONNECTION
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_NETWORK_STATE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(isConnected()) logStatus("app conectado", true);
                        else logStatus("app nao conectado", true);

                        getEventos();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this,"Voce deve aceitar permissao para app funcionar", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();
    }

    public void OnClickBtnLerQRCode(View view) {
        Intent intent = new Intent(this, reader.class);
        Spinner sp = (Spinner)findViewById(R.id.spinnerEventoAtividade);
        String idEvento = sp.getSelectedItem().toString().substring(1, sp.getSelectedItem().toString().indexOf("]"));
        intent.putExtra("atividade", sp.getSelectedItem().toString());
        startActivity(intent);
    }

    public boolean getEventos(){
        boolean rslt=false;

        requestQueue = Volley.newRequestQueue(MainActivity.this);
        //String url = "https://api.myjson.com/bins/xbspb,"; // retorna json como exemplo
        String url = "https://eventosryder.dsicari.com.br/functions/get_eventos.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        jsonGetEventos = response;
                        logStatus("getEventos()="+ response.substring(0,100), false);
                        spinnerStrEventos =  new ArrayList<String>();
                        //HashMap<Integer,String> spinnerMapEventos = new HashMap<Integer, String>();
                        try {
                            JSONObject jGetEventos = new JSONObject(jsonGetEventos);
                            JSONArray jArray = jGetEventos.getJSONArray("eventos");
                            for (int i=0; i < jArray.length(); i++)
                            {
                                try {
                                    JSONObject j = jArray.getJSONObject(i);
                                    //spinnerMapEventos.put(Integer.parseInt(j.getString("id_evento")), j.getString("nome_evento"));
                                    /*
                                        // SE UTILIZAR HASHMAP
                                        // String name = spinner.getSelectedItem().toString();
                                        // String id = spinnerMap.get(spinner.getSelectedItemPosition());
                                     */
                                    spinnerStrEventos.add("[" + j.getString("id_evento") + "] " + j.getString("nome_evento"));
                                } catch (JSONException e) {
                                    logStatus("Spinner Eventos falhou, " + e.getMessage(), false);
                                }
                            }
                            if(spinnerStrEventos.size() > 0)
                                setSpinnerValues((Spinner)findViewById(R.id.spinnerEvento), spinnerStrEventos);

                        } catch (JSONException e) {
                            logStatus("Spinner Eventos falhou, " + e.getMessage(), false);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logStatus("getEventos() fails" + error.getMessage(), false);
            }
        });

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);

        return rslt;
    }

    public void setSpinnerValues(Spinner sp, List<String> spinnerStr){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, spinnerStr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }

    public void logStatus(String str, boolean append){
        TextView t=(TextView)findViewById(R.id.txtviewStatus);
        if(!append) t.setText(str);
        else t.append(";" + str);
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    // Listeners dos spinners
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.spinnerEvento)
        {
            Log.d("DBG", jsonGetEventos);
            Spinner sp = (Spinner)findViewById(parent.getId());
            int idEvento = Integer.parseInt(sp.getSelectedItem().toString().substring(1, sp.getSelectedItem().toString().indexOf("]"))); // retorna [id]
            spinnerStrEventosAtividades =  new ArrayList<String>();
            spinnerStrEventosAtividades.add("[0] Entrada"); // adiciona a atividade entrada para poder validar a entrada dos participantes no !! EVENTO !!
            try {
                JSONObject jGetEventos = new JSONObject(jsonGetEventos);
                JSONArray jArray = jGetEventos.getJSONArray("eventosAtividades");
                for (int i=0; i < jArray.length(); i++)
                {
                    try {
                        JSONObject j = jArray.getJSONObject(i);
                        //spinnerMapEventos.put(Integer.parseInt(j.getString("id_evento")), j.getString("nome_evento"));
                        //  SE UTILIZAR HASHMAP
                        //  String name = spinner.getSelectedItem().toString();
                        //  String id = spinnerMap.get(spinner.getSelectedItemPosition());
                        if(Integer.parseInt(j.getString("id_evento")) == idEvento) {
                            spinnerStrEventosAtividades.add("[" + j.getString("id_atividade") + "] " + j.getString("nome_atividade"));
                        }
                    } catch (JSONException e) {
                        logStatus("Spinner EventosAtividades falhou, " + e.getMessage(), false);
                    }
                }
                if(spinnerStrEventosAtividades.size() > 0)
                    setSpinnerValues((Spinner)findViewById(R.id.spinnerEventoAtividade), spinnerStrEventosAtividades);

            } catch (JSONException e) {
                logStatus("Spinner EventosAtividades falhou, " + e.getMessage(), false);
            }
        }
        else if(parent.getId() == R.id.spinnerEventoAtividade)
        {
            // ...
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    // FIM Listeners dos spinners
}
