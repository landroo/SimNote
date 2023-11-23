package org.landroo.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.landroo.simnote.JsonClass;
import org.landroo.simnote.SimNoteActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class WebClass
{
	private static final String TAG = "WebClass";
	private static final String error = "error";
	
	public List<Partner> userList = Collections.synchronizedList(new ArrayList<Partner>());// all user logged in
	public List<String> activeUsers = new ArrayList<String>();// users connected
	public String selUser;// selected users from list
	public List<String> messages = Collections.synchronizedList(new ArrayList<String>());
	public List<String> invites = Collections.synchronizedList(new ArrayList<String>());
	public List<String> responses = Collections.synchronizedList(new ArrayList<String>());
	public List<String> updates = Collections.synchronizedList(new ArrayList<String>());
	
	public String mainip = "";			// 
	public String game = "";			// game type
	public String myname = "";			// my player name
	public String myip = ""; 			// my ip
	public int httpport = 8484;			// direct port
	
	private Handler handler;
	public int polling = 5000;			// default polling interval
	
	private Timer timer = null;
	
	public boolean loggedIn = false;
	
	// partner class for direct communication
	public class Partner
	{
		public String name;
		public String ip;
		public List<String> messages = new ArrayList<String>();
		
		public Partner(String n, String addr)
		{
			name = n;
			ip = addr;
		}
	}
	
	// Constructor
	public WebClass(String sUrl, String sGame, String sName, Handler cHandler, int port)
	{
		this.mainip = sUrl;
		this.game = sGame;
		this.myname = sName;
		this.httpport = port;
		
		this.handler = cHandler;
		
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(new netTimer(), 0, polling);
	}
	
	// command 1 login
	// in: http://192.168.0.105:8080/?game=amoba&name=test&command=1;129.168.0.191
	// out: miki;feri;laci;
	public void login()
	{
		if(!loggedIn)
		{
			myip = getLocalIpAddress();
			new webTask().execute(mainip, "1", myip, "");
		}
	}
	
	// command 2 ping
	// in:  http://192.168.0.122:8080/?game=amoba&name=dani&command=2
	// out: roli;5;10;miki;18;33;:feri;Hali;:laci;1280;720;40;
	public void poll()
	{
		if(loggedIn) new webTask().execute(mainip, "2", "", "");
	}
	
	// command 3 get user list
	public void users()
	{
		// TODO upgrade full user list
	}
	
	// command 4 invite: 4;laci;{...};
	public void invite(String note)
	{
		if(loggedIn)
		{
			String ip = mainip;
			for(WebClass.Partner partner: userList)
			{
				// check selected name in active players too
				if(selUser.equals(partner.name) && !activeUsers.contains(partner.name))
				{
//					if(checkServer(partner.ip)) ip = partner.ip + ":" + httpport;
					new webTask().execute(ip, "4", partner.name, note);

			    	break;
				}
			}
		}
	}
	
	//command 5 response invite: 5;laci;{...};
	public void responseInvite(String user, String note)
	{
		if(loggedIn && !note.equals(""))
		{
			String ip = mainip;
			for(WebClass.Partner partner: userList)
			{
				// check selected name in active players too
				if(user.equals(partner.name))
				{
//					if(checkServer(partner.ip)) ip = partner.ip + ":" + httpport;
					new webTask().execute(ip, "5", partner.name, note);

			    	break;
				}
			}
		}
	}
	
	// command 6 message
	// http://192.168.0.105:8080/?game=amoba&name=dani&command=6;landroo;Hali
	public void message(String sMessages)
	{
		if(loggedIn)
		{
			StringBuffer sb = new StringBuffer();
			String ip = mainip;
			for(Partner partner: userList)
			{
				if(selUser.equals(partner.name))
				{
			    	sb.append(partner.name);
			    	sb.append("\t");
			    	sb.append(sMessages);
			    	sb.append("\t");
		    	
//			    	if(checkServer(ip)) ip = partner.ip + ":" + httpport;
					new webTask().execute(ip, "6", sb.toString(), "");

			    	break;
				}
			}
		}
	}
	
	// command 7 logout: 7
	public void logout()
	{
		if(loggedIn)
		{
			this.loggedIn = false;
			new webTask().execute(mainip, "7", "", "");
		}
		userList.clear();
		activeUsers.clear();
	}
	
	// command 8 response invite: 5;laci;{...};
	public void Update(String note)
	{
		if(loggedIn && !note.equals(""))
		{
			String ip = mainip;
			for(WebClass.Partner partner: userList)
			{
				if(activeUsers.contains(partner.name))
				{
//					if(checkServer(partner.ip)) ip = partner.ip + ":" + httpport;
					new webTask().execute(ip, "8", partner.name, note);
				}
			}
		}
	}
	
	
	// new web task
	private class webTask extends AsyncTask<String, Integer, Long>
	{
		protected Long doInBackground(String... sParams)
		{
			String ip = sParams[0];
			int command = Integer.parseInt(sParams[1]);
			String getParams = "";
			if(sParams.length > 2) getParams = sParams[2];
			String postParams = "";
			if(sParams.length > 3) postParams = sParams[3];
			request(ip, command, getParams, postParams);
			
			return (long)0;
		}
	}

	// send request and process response
	public void request(String ip, int command, String getParams, String postParams) 
	{
		String response = sendRequest(ip, command, getParams, postParams);

		if(response.equals("&"))
		{
			Log.i(TAG, "ERROR! " + ip + " command :" + command + " params: " + getParams);
			handler.sendEmptyMessage(124);	// cannot access server
		}
		else if(response.equals(error))
		{
			handler.sendEmptyMessage(122);	// username occupied
		}
		else
		{
			boolean proc = true;
			switch(command)
			{
			case 1:// 1 login (http://192.168.0.122:8080/?game=amoba&name=dani&command=1)
				this.loggedIn = true;
				if(!response.equals(""))
				{
					this.userList.clear();
					String[] sArr = response.split("\t");
					for(int i = 0; i < sArr.length; i += 2)
						this.userList.add(new Partner(sArr[i], sArr[i + 1]));
				}
				handler.sendEmptyMessage(123);	// show login success
				break;
			case 2:// 2 ping (http://192.168.0.122:8080/?game=simnote&name=dani&command=2)
				break;
			case 3:// 3 get user list
				if(!response.equals(""))
				{
					this.userList.clear();
					String[] sArr = response.split("\t");
					String[] sPair;
					for(int i = 0; i < sArr.length; i++)
					{
						sPair = sArr[i].split("\t", -1);
						this.userList.add(new Partner(sPair[0], sPair[1]));
					}
				}
				break;
			case 4:// 4 invite (4;Nexus)
				break;
			case 5:// 5 response invite (5;fer;)
				break;
			case 6:// 6 message (6;peti;Hali)
				break;
			case 7:// 7 logout
				proc = false;
				break;
			}

			if(proc) processResult(response);
		}
	}

	private String sendRequest(String ip, int iCommand, String getParams, String postParams)
	{
		String result = "&";
		HttpURLConnection conn = null;
		StringBuffer answer = new StringBuffer();
		
		// http://192.168.0.105:8080/?game=amoba&name=zte&command=1;
		String urlAddress = "";
		try
		{
			urlAddress = "http://" + ip + "/?game=" + game + "&name=" + URLEncoder.encode(myname, "ISO-8859-1") + "&command=" + iCommand + ";" + URLEncoder.encode(getParams, "ISO-8859-1");
			//Log.i(TAG, urlAddress);
		}
		catch (UnsupportedEncodingException e)
		{
			Log.i(TAG, e.getMessage());
		} 
		
		synchronized(this) 
		{
			try 
			{
				// Check if task has been interrupted
				if(Thread.interrupted()) throw new InterruptedException();
	
				URL url = new URL(urlAddress);
				conn = (HttpURLConnection) url.openConnection();
				conn.setUseCaches(false);
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setDoInput(true);
	
		         // Start the query
				conn.connect();
				
				// send post params
				if(!postParams.equals(""))
				{
					DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
					wr.write(postParams.getBytes("UTF-8"));
					wr.flush();
					wr.close();
				}

	            // Get the response
	            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	            String line;
	            while ((line = reader.readLine()) != null) answer.append(line);
				reader.close();
				
				result = answer.toString();
			} 
			catch(EOFException eo)
			{
			}
			catch(Exception ex) 
			{
				// if direct communication failed try through the main server
				if(!ip.equals(mainip)) sendRequest(mainip, iCommand, getParams, postParams); 
				else handler.sendEmptyMessage(120);
				//Log.i(TAG, "sendRequest: " + urlAddress + " params" + params, ex);
			} 
			finally 
			{
				if(conn != null) conn.disconnect();
			}
		}
		// All done
		//Log.i(TAG, "sendRequest: " + urlAddress + "\nreturned: " + result);
	      
		return result;
	}
	
	// post file
	public String sendFile(String surl, String sPost, String fName, String sFooter)
	{
		StringBuffer answer = new StringBuffer();

		try
		{
			HttpURLConnection conn = (HttpURLConnection) new URL(surl).openConnection();
			// conn.setConnectTimeout(30 * 1000);
			// conn.setReadTimeout(30 * 1000);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("Cookie", "userid=XXXX; password=XXXX");
			conn.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; U; Android 4.1.2; hu-hu; GT-N7100 Build/JZO54K) AppleWebKit/534.30 (KHTML, like Kecko) Version/4.0 Mobile Safari/534.30");
			conn.connect();

			// Send request
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.write(sPost.getBytes("UTF-8"));
			if (fName != null)
			{
				BufferedInputStream fileInputStream;
				int totalBytes;

				try
				{
					fileInputStream = new BufferedInputStream(new FileInputStream(fName));
					totalBytes = fileInputStream.available();

					for (int j = 0; j < totalBytes; j++)
					{
						// Write the data to the output stream
						wr.write(fileInputStream.read());
					}

					fileInputStream.close();

					//Log.i(TAG, sFooter);

					wr.writeBytes(sFooter);
				}
				catch (Exception ex)
				{
				}
			}
			wr.flush();
			wr.close();

			// Get the response
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null)
			{
				answer.append(line);
			}
			reader.close();

			Log.i(TAG, answer.toString());

			// Output the response
			return answer.toString();
		}
		catch (Exception ex)
		{
			Log.e(TAG, "sendRequest", ex);
		}

		return answer.toString();
	}
	
    class netTimer extends TimerTask 
    {
        public void run() 
        {
        	poll();
        }
    }
    
    public String getLocalIpAddress() 
	{
		String res = "";
	    try 
	    {
	        for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
	        {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) 
	            {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) 
	                {
	                    res = inetAddress.getHostAddress();
	                }
	            }
	        }
	    } 
	    catch (SocketException ex) 
	    {
	        Log.e(TAG, ex.toString());
	    }
	    
	    //Log.i(TAG, res);
	    
	    return res;
	}
    
	private boolean checkServer(String ip)
	{
		if(ip.equals("")) return false;
		
		String[] sa = ip.split("[.]");
        byte[] addr = new byte[4];
        addr[0] = (byte) Integer.parseInt(sa[0]);
        addr[1] = (byte) Integer.parseInt(sa[1]);
        addr[2] = (byte) Integer.parseInt(sa[2]);
        addr[3] = (byte) Integer.parseInt(sa[3]);
		
		InetAddress in;
		
		try 
		{
			in = InetAddress.getByAddress(addr);
			if(in.isReachable(1000)) return true;
		}
		catch(Exception ex) 
		{
			ex.printStackTrace();
		}
		
		return false;
	}
	
	// return full user list
	public ArrayList<String> getUserList()
	{
		ArrayList<String> list = new ArrayList<String>();
		for(Partner partner: userList)
			list.add(partner.name);
		
		return list;
	}
	
	//
	private void processAddUser(String param)
	{
		String[] params = param.split("\t");
		try
		{
			for (int i = 0; i < params.length; i += 2)
			{
				boolean bIs = false;
				for (WebClass.Partner partner : userList)
				{
					if (!partner.name.equals(params[i]))
					{
						bIs = true;
						break;
					}
				}
				if (bIs == false)
				{
					WebClass.Partner newpartner = new Partner(params[i], params[i +1]);
					userList.add(newpartner);
				}
			}
		}
		catch (Exception ex)
		{
			Log.i(TAG, "procAddUser error: " + param);
		}
	}
	
	// process recived messages
	private void processResult(String response)
	{
		String[] arr = response.split("\b", -1);		
		try
		{
			synchronized (this)
			{
				if (!arr[0].equals("")) Log.i(TAG, response);
				if (!arr[1].equals("")) processMessage(arr[1]);// messages: feri;Hali;laci;szia
				if (!arr[2].equals("")) processInvite(arr[2]);// invites: laci;{...};
				if (!arr[3].equals("")) processAddUser(arr[3]);// added: miki;
				if (!arr[4].equals("")) processRemoveUser(arr[4]);// removed: tibi;
				if (!arr[5].equals("")) processResponse(arr[5]);// responses: laci;{...};
				if (!arr[6].equals("")) processUpdate(arr[6]);// updates: laci;{...};
			}
		}
		catch (Exception ex)
		{
			Log.i(TAG, "processResult error: " + response);
		}
		
		return;
	}
	
	// add the invitation to the list
	private void processInvite(String param)
	{
		if(!param.equals(""))
		{
			this.invites.add(param);
			handler.sendEmptyMessage(128);
		}
	}

	// remove from user list  
	private void processRemoveUser(String param)
	{
		String[] params = param.split("\t");
		try
		{
			for (int i = 0; i < params.length; i += 2)
			{
				for (Partner partner : userList)
				{
					if (partner.name.equals(params[i]))
					{
						userList.remove(partner);
						activeUsers.remove(params[i]);
						break;
					}
				}
			}
		}
		catch (Exception ex)
		{
			Log.i(TAG, "procRemoveUser: " + param);
		}
	}
	
	// send message object
    private void sendMessage(int id, String text)
    {
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("id", id);
        b.putString("text", text);
        msg.setData(b);
        handler.sendMessage(msg);
    }
    
    // process message
    private void processMessage(String param)
    {
		String[] params = param.split("\t");
		String msg;
		try
		{
			for (int i = 0; i < params.length; i += 2)
			{
				if (!params[i].equals(""))
				{
					msg = params[i] + "\t" + params[i + 1];
					
					this.messages.add(msg);
					handler.sendEmptyMessage(121);
					
					for(Partner partner: userList)
					{
						if(partner.name.equals(params[i]))
						{
							partner.messages.add(params[i + 1]);
							break;
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			Log.i(TAG, "processMessage error: " + param);
		}
    }
    
    private void processResponse(String param)
    {
		if(!param.equals(""))
		{
			this.responses.add(param);
			handler.sendEmptyMessage(193);
		}
    }
    
    private void processUpdate(String param)
    {
		if(!param.equals(""))
		{
			this.updates.add(param);
			handler.sendEmptyMessage(194);
		}
    }
    
	// check network state
	public static boolean isConnected(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		boolean bOK = false;

		if (connectivityManager != null)
		{
			try
			{
				networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (networkInfo != null && networkInfo.isConnectedOrConnecting()) bOK = true;

				networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if (networkInfo != null && networkInfo.isConnectedOrConnecting()) bOK = true;
			}
			catch (Exception ex)
			{
				Log.e(TAG, "getNetworkInfo");
			}
		}

		return bOK;
	}

    
}
