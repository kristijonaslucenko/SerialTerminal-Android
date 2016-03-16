package com.devmel.apps.serialterminal;

import com.devmel.apps.serialterminal.R;
import com.devmel.storage.android.UserPrefs;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class SerialOptions extends Activity {

	private EditText baudRateValue;
	private RadioGroup dataBitsValue;
	private RadioGroup stopBitsValue;
	private RadioGroup parityValue;
	private ToggleButton resetValue;
	private CheckBox resetPulseValue;
	private ToggleButton vtgValue;

	private final static int defaultBaudrate = 9600;
	private final static int defaultDatabits = 8;
	private final static int defaultStopbits = 1;
	private final static int defaultParity = 0;
	private final static int defaultReset = 0;
	private final static int defaultResetPulse = 0;
	private final static int defaultVtg = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_serial_options);

		baudRateValue = (EditText) findViewById(R.id.baudRateValue);
		dataBitsValue = (RadioGroup) findViewById(R.id.dataBitsValue);
		stopBitsValue = (RadioGroup) findViewById(R.id.stopBitsValue);
		parityValue = (RadioGroup) findViewById(R.id.parityValue);
		resetValue = (ToggleButton) findViewById(R.id.resetValue);
		resetPulseValue = (CheckBox) findViewById(R.id.resetPulseValue);
		vtgValue = (ToggleButton) findViewById(R.id.vtgValue);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		initData();
	}
	
	private void initData(){
		UserPrefs userPrefs = new UserPrefs(getSharedPreferences(MainActivity.sharedPreferencesName, Context.MODE_PRIVATE));

		int baudRate = userPrefs.getInt("configBaudrate");
		if(baudRate<600 || baudRate>250000){
			baudRate=defaultBaudrate;
		}
		int dataBits = userPrefs.getInt("configDatabits");
		if(dataBits<5 || dataBits>8){
			dataBits = defaultDatabits;
		}
		int stopBits = userPrefs.getInt("configStopbits");
		if(stopBits<1 || stopBits>2){
			stopBits = defaultStopbits;
		}
		int parity = userPrefs.getInt("configParity");
		if(parity<0 || parity>2){
			parity = defaultParity;
		}
		int reset = userPrefs.getInt("configReset");
		if(reset<0 || reset>1){
			reset = defaultReset;
		}
		int resetPulse = userPrefs.getInt("configResetPulse");
		if(resetPulse<0 || resetPulse>1){
			resetPulse = defaultResetPulse;
		}
		int vtg = userPrefs.getInt("configVtg");
		if(vtg<0 || vtg>1){
			vtg = defaultVtg;
		}
		
		baudRateValue.setText(""+baudRate);
		
		int id = R.id.dataBits8;
		switch (dataBits) {
		case 5:
			id=R.id.dataBits5;
			break;
		case 6:
			id=R.id.dataBits6;
			break;
		case 7:
			id=R.id.dataBits7;
			break;
		}
		dataBitsValue.check(id);

		id = R.id.stopBits1;
		switch (stopBits) {
		case 2:
			id=R.id.stopBits2;
			break;
		}
		stopBitsValue.check(id);
	
		id = R.id.parityNone;
		switch (parity) {
		case 1:
			id=R.id.parityOdd;
			break;
		case 2:
			id=R.id.parityEven;
			break;
		}
		parityValue.check(id);
		
		if(reset==1){
			resetValue.setChecked(true);
		}else{
			resetValue.setChecked(false);
		}
		if(resetPulse==1){
			resetPulseValue.setChecked(true);
		}else{
			resetPulseValue.setChecked(false);
		}
		
		if(vtg==1){
			vtgValue.setChecked(true);
		}else{
			vtgValue.setChecked(false);
		}

	}

	
	private void recordData(){
		UserPrefs userPrefs = new UserPrefs(getSharedPreferences(MainActivity.sharedPreferencesName, Context.MODE_PRIVATE));
		int baudRate = defaultBaudrate;
		int dataBits = defaultDatabits;
		int stopBits = defaultStopbits;
		int parity = defaultParity;
		int reset = defaultReset;
		int resetPulse = defaultResetPulse;
		int vtg = defaultVtg;
		
		try{baudRate = Integer.valueOf(baudRateValue.getText().toString());}catch(Exception e){}
		
		switch (dataBitsValue.getCheckedRadioButtonId()) {
		case R.id.dataBits5:
			dataBits=5;
			break;
		case R.id.dataBits6:
			dataBits=6;
			break;
		case R.id.dataBits7:
			dataBits=7;
			break;
		case R.id.dataBits8:
			dataBits=8;
			break;
		}
		switch (stopBitsValue.getCheckedRadioButtonId()) {
		case R.id.stopBits1:
			stopBits = 1;
			break;
		case R.id.stopBits2:
			stopBits = 2;
			break;
		}
		switch (parityValue.getCheckedRadioButtonId()) {
		case R.id.parityNone:
			parity=0;
			break;
		case R.id.parityOdd:
			parity=1;
			break;
		case R.id.parityEven:
			parity=2;
			break;
		}

		if(resetValue.isChecked()){
			reset=1;
		}else{
			reset=0;
		}
		if(resetPulseValue.isChecked()){
			resetPulse=1;
		}else{
			resetPulse=0;
		}
		if(vtgValue.isChecked()){
			vtg=1;
		}else{
			vtg=0;
		}

		userPrefs.saveInt("configBaudrate", baudRate);
		userPrefs.saveInt("configDatabits", dataBits);
		userPrefs.saveInt("configStopbits", stopBits);
		userPrefs.saveInt("configParity", parity);
		userPrefs.saveInt("configReset", reset);
		userPrefs.saveInt("configResetPulse", resetPulse);
		userPrefs.saveInt("configVtg", vtg);
	}

	
	// 2.0 and above
	@Override
	public void onBackPressed() {
		recordData();
		super.onBackPressed();
	 //   moveTaskToBack(true);
	}

	// Before 2.0
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
			recordData();
	    }
	    return super.onKeyDown(keyCode, event);
	}

}
