package com.frkn.ygahack;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  implements TextToSpeech.OnInitListener {

    private TextView txtSpeechInput;
    private ImageButton onayButton;
    private ImageView btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextToSpeech tts;
    private BluetoothAdapter BTAdapter;
    private String product;
    private List productList = new ArrayList<String>();
    private String[] reyons = {"MELIH-PC", "SugaBook"};
    private int i = 0;
    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("INFO", "cihaz bulundu");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                if(device.getName()!=null){

                    if(device.getName().equals(reyons[i])) {

                        txtSpeechInput.setText("Gidilecek reyon: " + device.getName() + " " + rssi);

                        //reyon bulundu unregister..
                        if(rssi > -50) {
                            unregisterReceiver(bReciever);
                            speakOut(product + " ürünlerini ikinci ve üçüncü raflarda bulabilirsiniz", TextToSpeech.QUEUE_ADD);
                            BTAdapter.cancelDiscovery();
                            //TODO startActivity(camera)
                            Intent i = new Intent(MainActivity.this, CameraActivity.class);
                            startActivity(i);
                        }else {
                            BTAdapter.cancelDiscovery();
                        }
                    }
                }

                Log.d("BLUETOOTH--", "Device: " + device.getName() + " " + rssi);
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                //sürekli devam
                BTAdapter.startDiscovery();
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] list = {"süt","peynir","yoğurt","yumurta"};
        Collections.addAll(productList, list);
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        tts = new TextToSpeech(this, this);
        btnSpeak = (ImageView) findViewById(R.id.btnSpeak);
        onayButton = (ImageButton) findViewById(R.id.onayButton);

        if(!BTAdapter.isEnabled())
            BTAdapter.enable();

        if (BTAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        onayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                speakOut(product + " için 2 metre ilerleyiniz.",TextToSpeech.QUEUE_FLUSH);
                if(productList.contains(product)) {
                    i = 1;

                }
                if(BTAdapter.isDiscovering())
                    BTAdapter.cancelDiscovery();

                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                registerReceiver(bReciever, filter);
                Log.d("INFO", "başladı ");
                BTAdapter.startDiscovery();
            }
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    product = result.get(0);
                    txtSpeechInput.setText(product);
                    Log.d("TEXT", "Text Edited");
                    txtSpeechInput.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                    txtSpeechInput.announceForAccessibility(txtSpeechInput.getText() + " onaylamak için çift dokunun.");
                    speakOut(txtSpeechInput.getText().toString(), TextToSpeech.QUEUE_FLUSH);

                    onayButton.setVisibility(View.VISIBLE);

                }
                break;
            }

        }
    }

    private void speakOut(String text, int mode) {
        tts.speak(text, mode, null);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                //btnSpeak.setEnabled(true);
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }



    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if(BTAdapter.isDiscovering())
            BTAdapter.cancelDiscovery();

        unregisterReceiver(bReciever);
        super.onDestroy();
    }
}
