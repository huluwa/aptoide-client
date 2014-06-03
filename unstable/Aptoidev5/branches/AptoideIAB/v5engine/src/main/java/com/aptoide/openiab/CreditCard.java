package com.aptoide.openiab;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;


import cm.aptoide.ptdev.R;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class CreditCard extends ActionBarActivity {

	private boolean wait=false;
	Activity ctx = this;
	String paykey;

//	int server = PayPal.ENV_SANDBOX;

	String userMail;
	String token;
//	String url = ("http://webservices.aptoide.com/webservices/hasPurchaseAuthorization");
	String urlPay = "http://webservices.aptoide.com/" + /*Aptoide.getConfiguration().getWebServicesUri() +*/ "webservices/payProduct";

	//http://dev.aptoide.com/webservices/payApk/e8b1d6a4dd8b5351c823cd1af95243ed70e9ad3f4f5f2f9c0e89b/rui.mateus@caixamagica.pt/diogo/com.smedio.mediaplayer/1.05.7/completed_payment/json
	String urlRedirect="http://www.paypal.com/webscr?cmd=_ap-payment&paykey=";
//	String urlRedirect="https://www.paypal.com/webapps/adaptivepayment/flow/pay?expType=mini&paykey=";
	TextView tv;
	boolean canceled=false;
	WebView web;
	int operation=1;
	boolean failed=false;

	// 1 - Get pre-approval key
	// 2 - Validation status
	// 3 - Pay
	private String repo;
	private SharedPreferences sPref;
	private String versionName;
	private String apkid;
    private int paymentTypeId;
    private int aptoideProductId;
    private int apiVersion;
    private int orderId;

    @Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

        setContentView(R.layout.paypal_account);
        Bundle b = getIntent().getExtras();

        token = b.getString("token");
        paymentTypeId = b.getInt("paymentTypeId");
        apiVersion = b.getInt("apiVersion");
        aptoideProductId = b.getInt("aptoideProductId");

        web = (WebView)findViewById(R.id.webView1);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //TODO verificar a informação de retorno
                Log.d("preapproval", "onPageStarted " + url);
                if(url.contains("return") && !wait){
                    wait=true;
                    Log.i("preapproval", "payment confirmed!");

                    Intent intent = new Intent();
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("token", token);
                    setResult(RESULT_OK,intent);
                    //comentar duas linhas
//					String params = token+"/"+repo+"/"+apkid+"/"+versionName+"/completed_payment/json";
//					send(urlPay, params);
//                    String params =token+"/"+repo+"/"+apkid+"/"+versionName+"/try_pay/json";
//                    send(urlPay, params);
                    finish();
                }
                if(url.contains("cancel") && !wait){
                    wait=true;
                    failed=true;
                    Log.i("preapproval", "payment failed!");
//					String params = token+"/"+repo+"/"+apkid+"/"+versionName+"/canceled_payment/json";
//					send(urlPay, params);
                    finish();
                }
            }
        });

        String params =apiVersion+"/"+token+"/" + aptoideProductId +"/try_pay/"+paymentTypeId+"/json";
        pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.show();
        send(urlPay, params);

    }
    public ProgressDialog pd;


	public void send(final String url, final String params){

		Thread t = new Thread(){
			public void run() {

				String temp=null;

				HttpClient client = new DefaultHttpClient();
				HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
				HttpResponse response=null;
				HttpGet request = new HttpGet();

				try {
					request.setURI(new URI(url+"/"+params));
					System.out.println(request.getURI());
				} catch (URISyntaxException e) {
					Log.e("preapproval", "URISyntaxException");
					e.printStackTrace();
				}

				try {
					response = client.execute(request);
				} catch (ClientProtocolException e) {
					Log.e("preapproval", "ClientProtocolException");
					e.printStackTrace();
				} catch (IOException e) {
					Log.e("preapproval", "IOException on response");
					e.printStackTrace();
				}

				if(response!=null){
					try {
						temp = EntityUtils.toString(response.getEntity());
					} catch (ParseException e) {
						Log.e("preapproval", "ParseException");
						e.printStackTrace();
					} catch (IOException e) {
						Log.e("preapproval", "IOException on parse");
						e.printStackTrace();
					}
					Log.i("preapproval", temp);
				}
				else{
					Log.e("preapproval", "the response is null");
				}

				Bundle data=new Bundle();
				data.putString("response", temp);
				Message msg = new Message();
				msg.setData(data);

				handler2.sendMessage(msg);
			}
		};
		t.start();
	}


	private Handler handler2 = new Handler() {

        @Override
		public void handleMessage(Message msg) {

			Bundle data=msg.getData();
			String response=data.getString("response");

			Log.i("preapproval", "handler: "+response);

			JSONObject respJSON;

			try {
				respJSON = new JSONObject(response);

				if(respJSON.getString("status").equals("OK")){
                    if(pd.isShowing())pd.dismiss();

					if(respJSON.has("pay_key")){
						paykey=respJSON.getString("pay_key");
                        orderId = respJSON.getInt("orderId");
						web.loadUrl(urlRedirect+paykey);

//                        String url = urlRedirect+paykey;
//                        Intent i = new Intent(Intent.ACTION_VIEW);
//                        i.setData(Uri.parse(url));
//                        startActivity(i);

					}
                    if(respJSON.has("paypalStatus")){
						if(respJSON.getString("paypalStatus").equals("completed")){
							System.out.println("Payed!");
                            Intent intent = new Intent();
                            intent.putExtra("OrderId", respJSON.getInt("orderId"));
                            intent.putExtra("token", token);
							setResult(RESULT_OK,intent);
							finish();
						}
					}
//					setContentView(R.layout.buy);
//					tv=(TextView)findViewById(R.id.logbuy);
//					tv.setText("Payment Completed!");
				}else {
                    Toast.makeText(ctx, "There was an error. Please try again", Toast.LENGTH_LONG).show();
                    if(pd.isShowing())pd.dismiss();

//					setContentView(R.layout.buy);
//					tv=(TextView)findViewById(R.id.logbuy);
//					tv.setText("Payment Failed!");
				}

			} catch (Exception e) {
                if(pd.isShowing())pd.dismiss();
				finish();
				Log.e("preapproval", "failed to create a JSON response object or get String");
			}
		}
	};

//	public void Download(){
//		Intent myIntent = new Intent(this, Download.class);
//		this.startActivity(myIntent);
//	}

}
