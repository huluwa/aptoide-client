package cm.aptoide.pt;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.content.SharedPreferences;

public class NetworkApis {
	
	public static final int TIME_OUT = 12000;
	
	public static HttpResponse getHttpResponse(String url, String srv, Context mctx){
		try{
			DbHandler db = new DbHandler(mctx);
			
			SharedPreferences sPref = mctx.getSharedPreferences("aptoide_prefs", Context.MODE_PRIVATE);
			String myid = sPref.getString("myId", "NoInfo");
			String myscr = sPref.getInt("scW", 0)+"x"+sPref.getInt("scH", 0);
						
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, TIME_OUT);
			HttpConnectionParams.setSoTimeout(httpParameters, TIME_OUT);
			
			DefaultHttpClient mHttpClient = new DefaultHttpClient(httpParameters);
			mHttpClient.setRedirectHandler(new RedirectHandler() {

				public boolean isRedirectRequested(HttpResponse response,
						HttpContext context) {
					return false;
				}
				
				public URI getLocationURI(HttpResponse response, HttpContext context)
				throws ProtocolException {
					return null;
				}
			});
			
			
			HttpGet mHttpGet = new HttpGet(url);
			mHttpGet.setHeader("User-Agent", "aptoide-" + mctx.getString(R.string.ver_str)+";"+ Configs.TERMINAL_INFO+";"+myscr+";id:"+myid);
			mHttpGet.setHeader("Accept-Encoding", "gzip");
						
			String[] logins = null; 
			logins = db.getLogin(srv);
			if(logins != null){
				URL mUrl = new URL(url);
				mHttpClient.getCredentialsProvider().setCredentials(
						new AuthScope(mUrl.getHost(), mUrl.getPort()),
						new UsernamePasswordCredentials(logins[0], logins[1]));
			}

			
			HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
			
			
			// Redirect used... 
			Header[] azz = mHttpResponse.getHeaders("Location");
			if(azz.length > 0){
				String newurl = azz[0].getValue();
				mHttpGet = null;
				mHttpGet = new HttpGet(newurl);
				mHttpGet.setHeader("User-Agent", "aptoide-" + mctx.getString(R.string.ver_str)+";"+ Configs.TERMINAL_INFO+";"+myscr+";id:"+myid);
				mHttpGet.setHeader("Accept-Encoding", "gzip");
				
				if(logins != null){
	    			URL mUrl = new URL(newurl);
	    			mHttpClient.getCredentialsProvider().setCredentials(
	                        new AuthScope(mUrl.getHost(), mUrl.getPort()),
	                        new UsernamePasswordCredentials(logins[0], logins[1]));
	    		}
				
				mHttpResponse = null;
				mHttpResponse = mHttpClient.execute(mHttpGet);
				
				
			}
			return mHttpResponse;
		}catch(Exception e){
			System.out.println("=============================================");
			e.printStackTrace();
			System.out.println("=============================================");
			return null;
		}
		
		//catch(IOException e) {return null; }
		
		
	}
	
	/**
	 * @author rafael
	 * 
	 * @param mctx
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static InputStream getInputStream(Context mctx, String url) throws IOException{
		
		URL urlObj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection(); //Careful with UnknownHostException. Throws MalformedURLException, IOException
		
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/xml");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		conn.setConnectTimeout(TIME_OUT);
		
		SharedPreferences sPref = mctx.getSharedPreferences("aptoide_prefs", Context.MODE_PRIVATE);
		String myid = sPref.getString("myId", "NoInfo");
		String myscr = sPref.getInt("scW", 0)+"x"+sPref.getInt("scH", 0);
		
		conn.setRequestProperty("User-Agent", "aptoide-" + mctx.getString(R.string.ver_str)+";"+ Configs.TERMINAL_INFO+";"+myscr+";id:"+myid+";"+sPref.getString(Configs.LOGIN_USER_NAME, ""));
		
		return conn.getInputStream();
		
	}
	
	
	/**
	 * @author rafael
	 * 
	 * @param mctx
	 * @param url
	 * @param args
	 * @return
	 * @throws IOException
	 */
	public static HttpURLConnection send(Context mctx, String url, String... args) throws IOException {
		
		URL urlObj = new URL(String.format(url,(Object[])args));
		
		HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();//Careful with UnknownHostException 
		
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Accept", "application/xml");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		
		SharedPreferences sPref = mctx.getSharedPreferences("aptoide_prefs", Context.MODE_PRIVATE);
		String myid = sPref.getString("myId", "NoInfo");
		String myscr = sPref.getInt("scW", 0)+"x"+sPref.getInt("scH", 0);
		conn.setRequestProperty("User-Agent", "aptoide-" + mctx.getString(R.string.ver_str)+";"+ Configs.TERMINAL_INFO+";"+myscr+";id:"+myid+";"+sPref.getString(Configs.LOGIN_USER_NAME, ""));
		
		return conn;
		
	}
	
	
	public static HttpResponse getHttpResponse(String url, String usr, String pwd, Context mctx){
		try{
			//DbHandler db = new DbHandler(mctx);

			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, TIME_OUT);
			HttpConnectionParams.setSoTimeout(httpParameters, TIME_OUT);

			DefaultHttpClient mHttpClient = new DefaultHttpClient(httpParameters);
			mHttpClient.setRedirectHandler(new RedirectHandler() {

				public boolean isRedirectRequested(HttpResponse response,
						HttpContext context) {
					return false;
				}

				public URI getLocationURI(HttpResponse response, HttpContext context)
				throws ProtocolException {
					return null;
				}
			});
			
			HttpGet mHttpGet = new HttpGet(url);
			//mHttpGet.setHeader("User-Agent", "aptoide-" + mctx.getString(R.string.ver_str)+";fetch_icon");

			//String[] logins = null; 
			//logins = db.getLogin(srv);
			if(usr != null || pwd != null){
				URL mUrl = new URL(url);
				mHttpClient.getCredentialsProvider().setCredentials(
						new AuthScope(mUrl.getHost(), mUrl.getPort()),
						new UsernamePasswordCredentials(usr, pwd));
			}

			HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
			
			
			// Redirect used... 
			Header[] azz = mHttpResponse.getHeaders("Location");
			if(azz.length > 0){
				String newurl = azz[0].getValue();
				mHttpGet = null;
				mHttpGet = new HttpGet(newurl);
				
				if(usr != null || pwd != null){
	    			URL mUrl = new URL(newurl);
	    			mHttpClient.getCredentialsProvider().setCredentials(
	                        new AuthScope(mUrl.getHost(), mUrl.getPort()),
	                        new UsernamePasswordCredentials(usr, pwd));
	    		}
				
				mHttpResponse = null;
				mHttpResponse = mHttpClient.execute(mHttpGet);
				
				
			}
			return mHttpResponse;
		}catch(Exception e) {return null; }
		
	}
	
	public static DefaultHttpClient createItOpen(String url, String usr, String pwd){
		try{
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpParameters, TIME_OUT);

		DefaultHttpClient mHttpClient = new DefaultHttpClient(httpParameters);
		mHttpClient.setRedirectHandler(new RedirectHandler() {

			public boolean isRedirectRequested(HttpResponse response,
					HttpContext context) {
				return false;
			}

			public URI getLocationURI(HttpResponse response, HttpContext context)
			throws ProtocolException {
				return null;
			}
		});
		
		if(usr != null || pwd != null){
			URL mUrl = new URL(url);
			mHttpClient.getCredentialsProvider().setCredentials(
					new AuthScope(mUrl.getHost(), mUrl.getPort()),
					new UsernamePasswordCredentials(usr, pwd));
		}
		
		mHttpClient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
			
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				// TODO Auto-generated method stub
				return 0;
			}
		});
		
		mHttpClient.setReuseStrategy(new ConnectionReuseStrategy() {
			
			public boolean keepAlive(HttpResponse response, HttpContext context) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		
		return mHttpClient;
		}catch (Exception e) {return null;	}
		
	}
	
	public static HttpResponse fetch(String fetch_file, DefaultHttpClient mHttpClient){
		try{
		HttpGet mHttpGet = new HttpGet(fetch_file);
		
		HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
		
		// Redirect used... 
		Header[] azz = mHttpResponse.getHeaders("Location");
		if(azz.length > 0){
			String newurl = azz[0].getValue();
			mHttpGet = null;
			mHttpGet = new HttpGet(newurl);
			
			mHttpResponse = null;
			mHttpResponse = mHttpClient.execute(mHttpGet);
		}
		
		return mHttpResponse;
		}catch (Exception e) {return null;	}
		
	}
	
	
	
	public static HttpResponse imgWsGet(String url){
		try{
						
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, TIME_OUT);
			HttpConnectionParams.setSoTimeout(httpParameters, TIME_OUT);
			
			DefaultHttpClient mHttpClient = new DefaultHttpClient(httpParameters);
			mHttpClient.setRedirectHandler(new RedirectHandler() {

				public boolean isRedirectRequested(HttpResponse response,
						HttpContext context) {
					return false;
				}
				

				public URI getLocationURI(HttpResponse response, HttpContext context)
				throws ProtocolException {
					return null;
				}
			});
			
			
			HttpGet mHttpGet = new HttpGet(url);

			
			HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
			
			return mHttpResponse;
		}catch(Exception e){
			System.out.println("=============================================");
			e.printStackTrace();
			System.out.println("=============================================");
			return null;
		}
	
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
