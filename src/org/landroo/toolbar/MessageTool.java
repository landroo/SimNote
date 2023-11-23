package org.landroo.toolbar;

import java.util.Timer;
import java.util.TimerTask;

import org.landroo.simnote.R;
import org.landroo.simnote.ToolBarClass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MessageTool
{
	private static final int TIMEOUT = 10000;
	
	private ViewGroup view;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private int displayWidth;
	private int displayHeight;
	private PopupWindow popupWindow;
	private Context context;
	private int callBack1 = 0;
	private int callBack2 = 0;
	private TextView textView;
	private Timer closeTimer;
	
	public int resMode = 0;
	public String resText = "";
	
	private Handler toolHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if(msg.what == 1) popupWindow.dismiss();
		}
	};
	
	public MessageTool(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.view = v;
		this.layoutInflater = inflater;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;
	}
	
	// text popup window
	public void showMessagePoup(String sText, int call1, int call2, int mode)
	{
		View popupView = layoutInflater.inflate(R.layout.message_box, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight / 4;
		popupWindow = new PopupWindow(popupView, w, h);
		popupWindow.setFocusable(true);

		Bitmap bitmap = ToolBarClass.drawBack(w, h, 0, 0, false, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);
		
		textView = (TextView) popupView.findViewById(R.id.message_text);
		textView.setText(sText);
		
		callBack1 = call1;
		callBack2 = call2;
		resText = sText;
		ImageButton imgbtn1 = (ImageButton) popupView.findViewById(R.id.message_dismiss);
		imgbtn1.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popupWindow.dismiss();
				if(closeTimer != null) closeTimer.cancel();
				if(callBack2 != 0) handler.sendEmptyMessage(callBack1);
				callBack2 = 0;
				resMode = 0;
			}
		});
		
		ImageButton imgbtn2 = (ImageButton) popupView.findViewById(R.id.message_ok);
		imgbtn2.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popupWindow.dismiss();
				if(closeTimer != null) closeTimer.cancel();
				if(callBack1 != 0) handler.sendEmptyMessage(callBack1);
				callBack1 = 0;
				resMode = 1;
			}
		});
		
		ImageButton imgbtn3 = (ImageButton) popupView.findViewById(R.id.add_message);
		imgbtn3.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popupWindow.dismiss();
				if(closeTimer != null) closeTimer.cancel();
				if(callBack1 != 0) handler.sendEmptyMessage(callBack1);
				callBack1 = 0;
				resMode = 2;
			}
		});
		
		LinearLayout ll = (LinearLayout) popupView.findViewById(R.id.message_buttons);
		
		switch(mode)
		{
		case 1:
			imgbtn1.setVisibility(View.VISIBLE);
			imgbtn2.setVisibility(View.VISIBLE);
			imgbtn3.setVisibility(View.GONE);
			break;
		case 2:
			imgbtn1.setVisibility(View.GONE);
			imgbtn2.setVisibility(View.VISIBLE);
			imgbtn3.setVisibility(View.GONE);
			break;
		case 3:
			imgbtn1.setVisibility(View.GONE);
			imgbtn2.setVisibility(View.VISIBLE);
			imgbtn3.setVisibility(View.GONE);
			closeTimer = new Timer();
			closeTimer.scheduleAtFixedRate(new CloseTask(), TIMEOUT, 1000);
			break;
		case 4:
			imgbtn1.setVisibility(View.GONE);
			imgbtn2.setVisibility(View.GONE);
			imgbtn3.setVisibility(View.VISIBLE);
			ll.setVisibility(View.GONE);
			closeTimer = new Timer();
			closeTimer.scheduleAtFixedRate(new CloseTask(), TIMEOUT, 1000);
			break;
		}
		
		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);

		return;
	}
	
	class CloseTask extends TimerTask
	{
		public void run()
		{
			toolHandler.sendEmptyMessage(1);
			if(callBack2 != 0) handler.sendEmptyMessage(callBack2);
			closeTimer.cancel();
		}
	}

}
