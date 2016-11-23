package com.devmel.apps.serialterminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import com.devmel.communication.IUart;
import com.devmel.communication.linkbus.Usart;
import com.devmel.communication.android.UartBluetooth;
import com.devmel.communication.android.UartUsbOTG;
import com.devmel.storage.Node;
import com.devmel.storage.SimpleIPConfig;
import com.devmel.storage.android.UserPrefs;

import android.app.Activity;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity {
    public final static String sharedPreferencesName = "com.devmel.apps.serialterminal";


    private UserPrefs userPrefs;
    private int rxBytes;
    private int txBytes;
    private int err;
    private int numb;

    private Button connectBt;
    private TextView statusText;
    private EditText receiveText;
    private EditText sendText;
    private EditText transmitText;
    private CheckBox checkCR;
    private CheckBox checkLF;

    private Thread thread;
    private IUart device;
    private String portName = null;
    private Class<?> portClass = null;
    private boolean init = false;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBt = (Button) findViewById(R.id.connectBt);
        statusText = (TextView) findViewById(R.id.statusText);
        receiveText = (EditText) findViewById(R.id.receiveText);
        receiveText.setMovementMethod(new ScrollingMovementMethod());
        sendText = (EditText) findViewById(R.id.sendText);
        transmitText = (EditText) findViewById(R.id.transmitText);
        transmitText.setMovementMethod(new ScrollingMovementMethod());
        checkCR = (CheckBox) findViewById(R.id.checkCR);
        checkLF = (CheckBox) findViewById(R.id.checkLF);

        //Listener
        connectBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectClick();
            }
        });
        final Button optBt = (Button) findViewById(R.id.optBt);
        optBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                serialOptions();
            }
        });
        final Button clearBt = (Button) findViewById(R.id.clearBt);
        clearBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearClick();
            }
        });
        final Button sendBt = (Button) findViewById(R.id.sendBt);
        sendBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendClick();
            }
        });

        final Button initBt = (Button) findViewById(R.id.initBt);
        initBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initAT();
            }
        });

        checkCR = (CheckBox) findViewById(R.id.checkCR);
        checkCR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                crClick();
            }
        });
        checkLF = (CheckBox) findViewById(R.id.checkLF);
        checkLF.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                lfClick();
            }
        });
        OBD_filter filterOBD = new OBD_filter();
        OBD_filter.PIDsArray();
        numb = 0;


    }

    @Override
    protected void onResume() {
        super.onResume();
        initPreferences();

        //Select Port
        String type = userPrefs.getString("selectedType");
        String name = userPrefs.getString("selectedName");

        boolean selected = false;
        if (type != null && name != null) {
            if (type.equals("LB")) {
                if (name.contains(" - ")) {
                    String[] names = name.split(" - ");
                    if (names != null && names.length > 0) {
                        Node devices = new Node(this.userPrefs, "Linkbus");
                        String[] ipDeviceList = devices.getChildNames();
                        if (ipDeviceList != null) {
                            for (String devStr : ipDeviceList) {
                                if (devStr.equals(names[0])) {
                                    portName = devStr;
                                    portClass = Usart.class;
                                    selected = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (type.equals("USB")) {
                String[] usbDeviceList = UartUsbOTG.list(getBaseContext());
                for (String devStr : usbDeviceList) {
                    if (devStr.equals(name)) {
                        portName = devStr;
                        portClass = UartUsbOTG.class;
                        selected = true;
                        break;
                    }
                }
            } else if (type.equals("BT")) {
                String[] btDeviceList = UartBluetooth.list();
                for (String devStr : btDeviceList) {
                    if (devStr.equals(name)) {
                        portName = devStr;
                        portClass = UartBluetooth.class;
                        selected = true;
                        break;
                    }
                }
            }
        }

        if (selected == false) {
            portName = "";
            portClass = null;
            statusText.setText(R.string.port_selected_none);
        } else {
            statusText.setText(getString(R.string.port_show) + portName);
        }

        if (userPrefs.getInt("configBaudrate") <= 0) {
            serialOptions();
        }

        if (userPrefs.getInt("CR") == 1) {
            checkCR.setChecked(true);
        } else {
            checkCR.setChecked(false);
        }

        if (userPrefs.getInt("LF") == 1) {
            checkLF.setChecked(true);
        } else {
            checkLF.setChecked(false);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            portSelect();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initPreferences() {
        if (userPrefs == null) {
            userPrefs = new UserPrefs(getSharedPreferences(MainActivity.sharedPreferencesName, Context.MODE_PRIVATE));
        }
    }


    private void lfClick() {
        if (checkLF.isChecked()) {
            userPrefs.saveInt("LF", 1);
        } else {
            userPrefs.saveInt("LF", 0);
        }
    }

    private void crClick() {
        if (checkCR.isChecked()) {
            userPrefs.saveInt("CR", 1);
        } else {
            userPrefs.saveInt("CR", 0);
        }

    }

    private void connectClick() {
        if (portClass == null) {
            portSelect();
        } else {
            if (device == null) {
                connect();
            } else {
                disconnect();
            }
        }
    }

    private void clearClick() {
        rxBytes = 0;
        txBytes = 0;
        err = 0;
        receiveText.setText("");
        transmitText.setText("");
    }

    private void sendChangingPIDs() {


        if (device != null && device.isOpen() == true && init) {
            //"atcra 374\r", "atcra 346\r", "atcra 412\r", "atcra 418\r", "atcra 231\r"
            final String[] PIDs = new String[]{"atcra 374\r", "atcra 346\r", "atcra 412\r", "atcra 418\r", "atcra 231\r"};
            final String msg = "\r atcra 374\r atma\r";

            //Runnable conRun1 = new Runnable() {
                //public void run() {
                    try {
                        if (numb >= PIDs.length) {
                            numb = 0;
                        }
                        String textInput = "\r";

                        OutputStream out = device.getOutputStream();
                        out.write(textInput.getBytes());
                        out.flush();

                        textInput = PIDs[numb];

                        out = device.getOutputStream();
                        out.write(textInput.getBytes());
                        out.flush();

                        textInput = "atma\r";

                        out = device.getOutputStream();
                        out.write(textInput.getBytes());
                        out.flush();


                        numb++;
                        transmitText.setText(textInput);

                    } catch (Exception e) {
                        disconnect();
//						e.printStackTrace();
                    }
                //}
                //};
            //new Thread(conRun1).start();
        }
    }

    private void initAT() {
        if (device != null && device.isOpen() == true) {
            final String[] initArr = new String[]{"atsp6\r", "ate0\r", "ath1\r", "atcaf0\r", "atcra 231\r", "atS0\r", "atma\r"};
            init = true;

            Runnable conRun = new Runnable() {
                public void run() {
                    try {
                        for (int i = 0; i < initArr.length; i++) {
                            String textInput = initArr[i];

                            OutputStream out = device.getOutputStream();
                            out.write(textInput.getBytes());
                            out.flush();
                            textSent(textInput);
                        }
                        handler.postDelayed(runnable, 100);
                    } catch (Exception e) {
                        disconnect();
//						e.printStackTrace();
                    }
                }
            };
            new Thread(conRun).start();
        }
    }

    private void sendClick() {
        if (device != null && device.isOpen() == true) {
            Runnable conRun = new Runnable() {
                public void run() {
                    try {
                        String textInput = sendText.getText().toString();
                        if (userPrefs.getInt("CR") == 1) {
                            textInput += "\r";
                        }
                        if (userPrefs.getInt("LF") == 1) {
                            textInput += "\n";
                        }
                        OutputStream out = device.getOutputStream();
                        out.write(textInput.getBytes());
                        out.flush();
                        textSent(textInput);
                    } catch (Exception e) {
                        disconnect();
//						e.printStackTrace();
                    }
                }
            };
            new Thread(conRun).start();
        }

    }

    private void textSent(final String textInput) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String textOriginal = transmitText.getText().toString();
                if (textOriginal.length() > 0) {
                    transmitText.setText(textOriginal + textInput);
                } else {
                    transmitText.setText(textInput);
                }
                txBytes += textInput.length();
                updateTrafficStatus();
            }
        });
    }

    private void textReceived(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //receiveText.setText(receiveText.getText().toString()+text);
                OBD_filter.packetReceived(text);
                //rxBytes += text.length();
                receiveText.setText("EVP: " + Double.toString(OBD_filter.getEVP()) + "W\n" + "SOC: " +
                        Double.toString(OBD_filter.getSOC()) + "%\n" + "Velocity:" + OBD_filter.getVelocity() +
                        "km/h\n" + "Odometer: " + OBD_filter.getOdo() + "km\n" + "Shift status: " + OBD_filter.getShiftStatus() + "\n"
                        + "Brake lamp: " + OBD_filter.getBrakeOnOff() + "\n" /*+ text + "\n" + text.length()*/);
                /* + "Pos. Lights: " + OBD_filter.positionLightsStatus + "\n"
                + "TailLights: " + OBD_filter.tailLightsStatus + "\n" + "High beam: " + OBD_filter.highBeamStatus + "\n"
                        + "Low beam: " + OBD_filter.lowBeamStatus + "\n"*/
            }
        });
    }

    private void updateTrafficStatus() {
        /*String text = "RX : "+this.rx Bytes+"  ;  TX : "+this.txBytes;
        if(err > 0){
            text += " ; ERR : "+err;
        }*/

        //statusText.setText(Double.toString(OBD_filter.EVP));
    }

    private void connectError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(message);
                connectBt.setText(R.string.connect);
            }
        });
        deviceUnselect();
    }

    private void notFound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(R.string.device_not_found);
                connectBt.setText(R.string.connect);
            }
        });
        deviceUnselect();
    }

    private void connect() {
        deviceUnselect();
        deviceSelect();

        if (device != null) {
            Runnable conRun = new Runnable() {
                public void run() {
                    try {
                        device.setParameters(userPrefs.getInt("configBaudrate"), (byte) userPrefs.getInt("configDatabits"), (byte) userPrefs.getInt("configStopbits"), (byte) userPrefs.getInt("configParity"));
                        device.open();
                    } catch (IOException e) {
                        connectError(e.getMessage());
                    } catch (Exception e) {
                    }
                }
            };
            execute(conRun);
            while (thread != null && thread.isAlive()) {
            }
            if (device == null) {
                return;
            }
            if (device.isOpen() == true) {
                //Open inStream
                try {
                    final InputStream inStream = device.getInputStream();
                    //Start read loop
                    Runnable r = new Runnable() {
                        public void run() {
                            try {
                                while (inStream != null && device.isOpen()) {
                                    try {
                                        int available = inStream.available();
                                        if (available > 0) {
                                            byte[] buffer = new byte[4096];
                                            int toRead = inStream.read(buffer, 0, available);
                                            if (toRead > 0) {
                                                textReceived(new String(buffer, 0, toRead));
                                            }
                                        }
                                    } catch (IOException e) {
                                        err++;
                                    }
                                }
                            } catch (Exception e) {
//		    					e.printStackTrace();
                            }
                        }
                    };
                    execute(r);
                    rxBytes = 0;
                    txBytes = 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectBt.setText(R.string.disconnect);
                            updateTrafficStatus();
                        }
                    });
                    //Set reset and vtg
                    if (device != null && device instanceof Usart) {
                        int reset = userPrefs.getInt("configReset");
                        int resetPulse = userPrefs.getInt("configResetPulse");
                        int vtg = userPrefs.getInt("configVtg");

                        try {
                            Usart dev = (Usart) device;
                            dev.setVTG((vtg == 1) ? true : false);
                            dev.setReset((reset == 1) ? true : false);
                            if (resetPulse > 0) {
                                dev.toggleReset(100);
                            }
                        } catch (Exception e) {
//                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    disconnect();
//					e.printStackTrace();
                }
            }
        }
        if (device == null || device.isOpen() == false) {
            notFound();
        }
    }

    private void disconnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(R.string.disconnected);
                connectBt.setText(R.string.connect);
            }
        });
        deviceUnselect();
        init = false;
    }


    private void deviceSelect() {
        if (portClass != null) {
            if (portClass.equals(UartUsbOTG.class)) {
                try {
                    UartUsbOTG uart = new UartUsbOTG(portName, getBaseContext());
                    this.device = uart;
                } catch (Exception e) {
//					e.printStackTrace();
                }
            } else if (portClass.equals(UartBluetooth.class)) {
                try {
                    UartBluetooth uart = new UartBluetooth(portName);
                    this.device = uart;
                } catch (Exception e) {
//					e.printStackTrace();
                }
            } else if (portClass.equals(Usart.class)) {
                Node devices = new Node(this.userPrefs, "Linkbus");
                SimpleIPConfig device = SimpleIPConfig.createFromNode(devices, portName);
                if (device != null) {
                    Usart uart = new Usart(device);
                    //uart.setLock(userPrefs.getInt("lock")==1 ? true : false);
                    uart.setMode(Usart.MODE_ASYNCHRONOUS);
                    uart.setInterruptMode(true, 1000);
                    this.device = uart;
                }
            }
        }
    }

    private void deviceUnselect() {
        cancel();
        Runnable conRun = new Runnable() {
            public void run() {
                try {
                    device.close();
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        };
        execute(conRun);
        while (thread != null && thread.isAlive()) {
        }
        device = null;
    }


    private void portSelect() {
        Intent intent = new Intent(this, PortSelect.class);
        startActivity(intent);
    }

    private void serialOptions() {
        Intent intent = new Intent(this, SerialOptions.class);
        startActivity(intent);
    }

    private void execute(Runnable r) {
        cancel();
        thread = new Thread(r);
        thread.start();
    }

    private void cancel() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
      /* do what you need to do */
        sendChangingPIDs();      /* and here comes the "trick" */
            handler.postDelayed(this, 100);
        }
    };
}
