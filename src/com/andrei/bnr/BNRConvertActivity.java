package com.andrei.bnr;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BNRConvertActivity extends Activity implements OnClickListener {
	private static final String baseURL = "http://webservices.dragonflame.org/bnr/index.php";
	private static final String TAG = "MyAct";
	private static EditText textAmount;
	private static Button myButton;
	private static TextView textResult;
	private static TextView textDate;
	private static Spinner sp1;
	private static Spinner sp2;
	private ArrayAdapter<String> adapter;
	
    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.v(TAG, "This is test 1");
		textAmount = (EditText) findViewById(R.id.my_edit);
		myButton = (Button) findViewById(R.id.my_button);
		textResult = (TextView) findViewById(R.id.textResult);
		textDate = (TextView) findViewById(R.id.textDate);
		sp1 = (Spinner)findViewById(R.id.spinner1);
		sp2 = (Spinner)findViewById(R.id.spinner2);
		myButton.setOnClickListener(this);
		myButton.setClickable(false);
		
		adapter = 
                new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item);      
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		String fullURL = baseURL + "?method=list_currencies";
		new AsyncREST().execute(fullURL,"get_list");
		
		String fullURL2 = baseURL + "?method=fetch_latest_date";
		new AsyncREST().execute(fullURL2,"get_date");
	}

	public void onClick(View arg0) {
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		Context context = getApplicationContext();
		if (this.isOnline() && textAmount.getText().length() > 0 && Float.parseFloat(textAmount.getText().toString()) > 0) {
			String fullURL = baseURL + "?method=math_convert&sum="
					+ textAmount.getText() + "&from="+adapter.getItem(sp1.getSelectedItemPosition())+"&to="+adapter.getItem(sp2.getSelectedItemPosition());
			System.out.println("Test: " + fullURL);
			myButton.setClickable(false);
			new AsyncREST().execute(fullURL,"convert");
			imm.hideSoftInputFromWindow(textAmount.getWindowToken(), 0);
		} else if(!this.isOnline()) {
			CharSequence text = "To use this application you need an internet connection";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			imm.hideSoftInputFromWindow(textAmount.getWindowToken(), 0);
		} else if(textAmount.getText().length() <= 0) {
			CharSequence text = "You need to enter an amount to convert";
			new BNRErrorHandler(context, text);
		} else if(Float.parseFloat(textAmount.getText().toString()) <= 0) {
			CharSequence text = "You need to enter a positive amount to convert";
			textAmount.setText("");textAmount.setFocusable(true);
			new BNRErrorHandler(context, text);
		}
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}

	private class AsyncREST extends AsyncTask<String, Void, String> {
		private String currentOp;
		
		protected String getASCIIContentFromEntity(HttpEntity entity)
				throws IllegalStateException, IOException {
			InputStream in = entity.getContent();
			StringBuffer out = new StringBuffer();
			int n = 1;
			while (n > 0) {
				byte[] b = new byte[4096];
				n = in.read(b);
				if (n > 0)
					out.append(new String(b, 0, n));
			}
			return out.toString();
		}

		@Override
		protected String doInBackground(String... params) {
			String strURL;
			if (params.length == 2) {
				strURL = params[0];
				currentOp = params[1];
			} else {
				strURL = "";
				return strURL;
			}
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet(strURL);
			String text = null;
			try {
				HttpResponse response = httpClient.execute(httpGet,
						localContext);
				HttpEntity entity = response.getEntity();
				text = getASCIIContentFromEntity(entity);
			} catch (Exception e) {
				return e.getLocalizedMessage();
			}
			return text;
		}

		protected void onPostExecute(String results) {
			myButton.setClickable(true);
			int duration = Toast.LENGTH_SHORT;
			Context context = getApplicationContext();
			
			if (results != null) {
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = null;
				try {
					builder = builderFactory.newDocumentBuilder();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}

				try {
					InputSource is = new InputSource();
					is.setCharacterStream(new StringReader(results));
					Document document = builder.parse(is);

					JSONObject job = new JSONObject(this.getValue(document,
							"response"));
				
					if(this.currentOp == "convert") {
						String strResult = job.optString("sum");
						if(strResult == null || Float.parseFloat(strResult) < 0 ) {
							CharSequence text = "A server side error has occured!!";
							Toast toast = Toast.makeText(context, text, duration);
							toast.show();
						} else {
							textResult.setText(textAmount.getText() + " " + adapter.getItem(sp1.getSelectedItemPosition()) + " = " + strResult + " " + adapter.getItem(sp2.getSelectedItemPosition()) );
						}
						myButton.setClickable(true);
					} else if(this.currentOp == "get_list") {
						JSONArray ja = job.optJSONArray("list");
						for(int i=0;i<ja.length();i++) {
							adapter.add(ja.optString(i));
						}
				        sp1.setAdapter(adapter);
				        sp2.setAdapter(adapter);
					} else if(this.currentOp == "get_date") {
						Resources res = getResources();
						String strResult = job.optString("date");
						textDate.setText(res.getString(R.string.exchange_date) + " " + strResult);
					}

				} catch (SAXException e) {
					CharSequence text = "A SAX parser error has occured!!";
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
					e.printStackTrace();
				} catch (IOException e) {
					CharSequence text = "An IOException error has occured!!";
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
					e.printStackTrace();
				} catch (Exception e) {
					CharSequence text = "A generic error has occured!!";
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
					e.printStackTrace();
				}
			}
		}

		public String getValue(Document item, String str) {
			NodeList n = item.getElementsByTagName(str);
			return this.getElementValue(n.item(0));
		}

		public String getElementValue(Node elem) {
			Node child;
			if (elem != null) {
				if (elem.hasChildNodes()) {
					for (child = elem.getFirstChild(); child != null; child = child
							.getNextSibling()) {
						if (child.getNodeType() == Node.TEXT_NODE) {
							return child.getNodeValue();
						}
					}
				}
			}
			return "";
		}
	}
 
}