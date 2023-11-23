package org.landroo.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HttpServer extends NanoHTTPD
{
	private static final String TAG = "HttpServer";
	
	private Handler handler;
	
	public boolean httpLog = true;
	public boolean amobaLog = true;
	
	public HttpServer(int port, Handler h) throws IOException
	{
		super(port);
		
		handler = h;
	}

	public Response serve(String uri, String method, Properties header, Properties parms, Properties files)
	{
		if(parms.getProperty("game") != null)
		{
			String msg = "";
			try
			{
				String game = parms.getProperty("game");
				String name = URLDecoder.decode(parms.getProperty("name"), "ISO-8859-1");
				String command = URLDecoder.decode(parms.getProperty("command"), "ISO-8859-1");
				if(game.equals("amoba")) msg = processCommand(name, command);
			}
			catch (UnsupportedEncodingException e)
			{
				Log.i(TAG, e.getMessage());
			}
			
			return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, msg);
		}
		
		return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "");
	}
	
    private void sendMessage(String message)
    {
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("update", message);
        msg.setData(b);
        handler.sendMessage(msg);
    }
    
	public String processCommand(String name, String command)
	{
		String sRes = "";
		String[] sComm = command.split(";", -1);
		
		// step:
		// in:
		// http://192.168.0.122:8080/?game=amoba&name=dani&command=5;laci;38;26
		// out: error | 5
		if (sComm[0].equals("5")) sRes += name + ";" + sComm[2] + ";" + sComm[3]; 
		sRes += ":";
		
		// message: 6
		// http://192.168.0.122:8080/?game=amoba&name=feri&command=6;Dani;Hali;Miki;Hali
		// out: error | 6
		if (sComm[0].equals("6")) sRes += name + ";" + sComm[2] + ";" + sComm[3]; 
		sRes += ":";
		
		// invite:
		// in:
		// http://192.168.0.122:8080/?game=amoba&name=dani&command=4;laci;{...};
		// out: error | 4
		if (sComm[0].equals("4")) sRes += name + ";" + sComm[2];
		sRes += ":";
		
/*		
		// invite: 4
		// in: http://192.168.0.122:8080/?game=simnote&name=dani&command=4;laci;{...};
		// out: error | 4
		if (sComm[0].equals("4")) sRes = procInvite(name, sComm, post);

		// items: 5
		// in: http://192.168.0.122:8080/?game=simnote&name=dani&command=5;laci;{...};
		// out: error | 5
		if (sComm[0].equals("5")) sRes = procResponse(name, sComm, post);

		// message: 6
		// in http://192.168.0.122:8080/?game=simnote&name=feri&command=6;Dani;Hali;Miki;Hali
		// out: error | 6
		if (sComm[0].equals("6")) sRes = procMessage(name, sComm[1], post);

		// logout: 7
		// in http://192.168.0.122:8080/?game=simnote&name=feri&command=7
		// out: error | 7
		if (sComm[0].equals("7")) sRes = deleteUser(name, post);

		// items: 8
		// in: http://192.168.0.122:8080/?game=simnote&name=dani&command=5;laci;{...};
		// out: error | 8
		if (sComm[0].equals("8")) sRes = procUpdate(name, sComm, post);
*/
		
		sendMessage(sRes);

		return "ok";
	}
	
}
