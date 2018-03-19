package com.example.joaom.cup;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TelaPrincipal extends AppCompatActivity {

    private static final int SOLICITA_ATIVACAO = 1;
    private static final int SOLICITA_CONEXAO = 2;

    private ThingSpeakService mService;

    ConnectedThread threadDeConexao;

    Handler meuHandler;
    StringBuilder dadosBluetooth = new StringBuilder();
    final int handlerState = 0;

    BluetoothAdapter meuBluetooth;
    BluetoothDevice meuDispositivo;
    BluetoothSocket meuSuporte;
    Button btnConexao;
    Button btnAcender;
    Button btnApagar;
    Button btnLigar;
    Button btnDesligar;
    Button btnAtualizar;
    TextView txtDado;
    TextView txtEstado;
    TextView txtVazao;
    TextView txtTemperatura;
    TextView txtNivel;

    String testeee;

    boolean conexao = false;

    private static String MAC;

    UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_principal);

        getSupportActionBar().hide();

        btnConexao = (Button) findViewById(R.id.btn_conectar);
        btnAcender = (Button) findViewById(R.id.btn_acender);
        btnApagar = (Button) findViewById(R.id.btn_apagar);
        btnLigar = (Button) findViewById(R.id.btn_ligar);
        btnDesligar = (Button) findViewById(R.id.btn_desligar);
        btnAtualizar = (Button) findViewById(R.id.btn_atualizar);
        txtEstado = (TextView) findViewById(R.id.txt_estado);
        txtVazao = (TextView) findViewById(R.id.txt_vazao);
        txtTemperatura = (TextView) findViewById(R.id.txt_temperatura);
        txtDado = (TextView) findViewById(R.id.txt_dadoatualizado);
        txtNivel = (TextView) findViewById(R.id.txt_nivel);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit mRetrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.thingspeak.com")
                .client(client)
                .build();
        mService = mRetrofit.create(ThingSpeakService.class);

        meuBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (meuBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Seu dispositivo não possui conexão Bluetooth. O aplicativo será encerrado", Toast.LENGTH_LONG).show();
        }

        if (!meuBluetooth.isEnabled()) {
            Intent ativaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaBluetooth, SOLICITA_ATIVACAO);

        }


        btnConexao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (conexao) {

                    try {
                        meuSuporte.close();
                        btnConexao.setText("Conectar Bluetooth");
                        conexao = false;
                        Toast.makeText(getApplicationContext(), "Bluetooth foi desconectado", Toast.LENGTH_LONG).show();


                    } catch (IOException erro) {
                        Toast.makeText(getApplicationContext(), "Ocorreu um erro", Toast.LENGTH_LONG).show();
                    }

                } else {

                    Intent abreLista = new Intent(TelaPrincipal.this, ListaDeDispositivos.class);
                    startActivityForResult(abreLista, SOLICITA_CONEXAO);

                }

            }

        });

        btnLigar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(conexao){
                    threadDeConexao.enviar("l");

                }else{
                    Toast.makeText(getApplicationContext(), "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
                }

            }
        });

        btnDesligar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(conexao){
                    threadDeConexao.enviar("d");

                }else{
                    Toast.makeText(getApplicationContext(), "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
                }

            }
        });

        btnAcender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(conexao){
                    threadDeConexao.enviar("a");

                }else{
                    Toast.makeText(getApplicationContext(), "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
                }

            }
        });

        btnApagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(conexao){
                    threadDeConexao.enviar("b");

                }else{
                    Toast.makeText(getApplicationContext(), "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
                }

            }
        });


        btnAtualizar.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                Call<Resposta> call = mService.getThingSpeak();

                call.enqueue(new Callback<Resposta>() {
                    @Override
                    public void onResponse(Call<Resposta> call, Response<Resposta> response) {

                        int size = response.body().getFeeds().size();

                        float nivel = Float.parseFloat(response.body().getFeeds().get(size-1).getField3());

                        float vazao = Float.parseFloat(response.body().getFeeds().get(size-1).getField2());

                        String ultimaData = response.body().getChannel().getUpdatedAt();

                        String dia = ultimaData.substring(8,10);
                        String mes = ultimaData.substring(5,7);
                        String ano = ultimaData.substring(0,4);

                        String datafinal = dia + "/" + mes + "/" + ano;

                        int horaFusoErrado = Integer.parseInt(ultimaData.substring(11,13));
                        String horaRestante = ultimaData.substring(13,19);

                        String horaFuso = String.valueOf(horaFusoErrado - 2);

                        String horaFinal = horaFuso + horaRestante;

                        if (vazao > 0){

                            txtEstado.setText("Bomba ligada");

                        }else{

                            txtEstado.setText("Bomba desligada");

                        }


                        txtDado.setText("Última atualização \n" + "Data: " + datafinal + "   Hora: " + horaFinal);
                        txtTemperatura.setText(response.body().getFeeds().get(size-1).getField1() + " ºC");
                        txtVazao.setText(vazao + " L/min");
                        txtNivel.setText(nivel + "%");

                    }

                    @Override
                    public void onFailure(Call<Resposta> call, Throwable t) {

                        txtDado.setText("Favor habilitar sua internet");

                    }
                });


            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case SOLICITA_ATIVACAO:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Bluetooth foi conectado", Toast.LENGTH_LONG).show();

                }

                if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "Bluetooth não foi conectado. O aplicativo foi encerrado", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case SOLICITA_CONEXAO:
                if (resultCode == Activity.RESULT_OK) {
                    MAC = data.getExtras().getString(ListaDeDispositivos.ENDERECO_MAC);

                    meuDispositivo = meuBluetooth.getRemoteDevice(MAC);

                    try {
                        meuSuporte = meuDispositivo.createRfcommSocketToServiceRecord(MEU_UUID);
                        meuSuporte.connect();

                        conexao = true;

                        threadDeConexao = new ConnectedThread(meuSuporte);
                        threadDeConexao.start();

                        btnConexao.setText("Desconectar Bluetooth");

                        Toast.makeText(getApplicationContext(), "Você foi conectado com: " + MAC, Toast.LENGTH_LONG).show();

                    } catch (IOException erro) {

                        conexao = false;

                        Toast.makeText(getApplicationContext(), "Ocorreu um erro. O dispositivo que você deseja parear está indisponível", Toast.LENGTH_LONG).show();
                    }

                }

                if (resultCode == Activity.RESULT_CANCELED) {

                }
        }

    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;


            while (true) {
                try {

                    bytes = mmInStream.read(buffer);

                    String dadosBt = new String(buffer, 0, bytes);

                    meuHandler.obtainMessage(handlerState, bytes, -1, dadosBt).sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }


        public void enviar(String dadosEnviar) {
            byte[] msgBuffer = dadosEnviar.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) { }
        }

    }

}