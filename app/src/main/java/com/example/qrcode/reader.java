package com.example.qrcode;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.w3c.dom.Text;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class reader extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private Button btn;
    private TextView txtResult, txtAtividade;
    private String atividade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        // Captura string enviada pela MAINACTIVITY ao subir esta activity
        atividade = getIntent().getStringExtra("atividade");

        // init widgets
        scannerView = (ZXingScannerView)findViewById(R.id.zxscan);
        txtResult = (TextView)findViewById(R.id.txt_result);
        txtAtividade = (TextView)findViewById(R.id.txt_atividade);

        txtAtividade.setText(atividade);

        // request permission CAMERA
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        scannerView.setResultHandler(reader.this);
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(reader.this,"Voce deve aceitar permissao para app funcionar", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();
    }

    @Override
    protected void onDestroy() {
        scannerView.stopCamera();
        super.onDestroy();
    }

    @Override
    public void handleResult(Result rawResult) {
        txtResult.setText("Result=" + rawResult.getText()
                + "\n"
                + "Validar=https://eventosryder.dsicari.com.br/functions/validar_usuario.php?atividade="
                + atividade.substring(1, atividade.indexOf("]"))
                + "&usuario=" + rawResult.getText());
        //Put up the Yes/No message box
        AlertDialog.Builder builder = new AlertDialog.Builder(reader.this);
        builder
                .setTitle("Validar usuario?")
                .setMessage("codigo=" + rawResult.getText() + "\n" + "atividade=" + atividade)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Sim clicado
                        Toast.makeText(reader.this, "Validacao enviada",
                                Toast.LENGTH_SHORT).show();
                        scannerView.startCamera();
                        txtResult.setText("");
                    }
                })
                .setNegativeButton("Nao", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Nao clicado
                        Toast.makeText(reader.this, "Validacao nao enviada",
                                Toast.LENGTH_SHORT).show();
                        scannerView.startCamera();
                        txtResult.setText("");
                    }
                })
                .show();

    }



}
