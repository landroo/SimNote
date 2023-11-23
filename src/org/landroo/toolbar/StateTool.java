package org.landroo.toolbar;

import org.landroo.simnote.R;
import org.landroo.simnote.ToolBarClass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;

public class StateTool
{
	private ViewGroup view;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private int displayWidth;
	private int displayHeight;
	private Context context;
	private PopupWindow popupWindow;
	private int callBack = 0;
	
	public int[] states = {1, 1, 1, 1, 1, 1};
	
	public StateTool(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.view = v;
		this.layoutInflater = inflater;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;
	}
	
	// show font popup window
	public void showFontPopup(int call)
	{
		View popupView = layoutInflater.inflate(R.layout.state_popup, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight / 3;
		popupWindow = new PopupWindow(popupView, w, h);
		popupWindow.setFocusable(true);

		Bitmap bitmap = ToolBarClass.drawBack(w, h, 0, 0, false, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);

		// state close
		ImageButton imgBtn = (ImageButton) popupView.findViewById(R.id.stateclose);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(callBack != 0) handler.sendEmptyMessage(callBack);
				popupWindow.dismiss();
			}
		});
		
		// state move
		imgBtn = (ImageButton) popupView.findViewById(R.id.statemove);
		if(states[2] == 1) imgBtn.setBackgroundResource(R.drawable.border);
		else imgBtn.setBackgroundColor(0);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(states[2] == 1) states[2] = 0;
				else states[2] = 1;
				if(states[2] == 1) v.setBackgroundResource(R.drawable.border);
				else v.setBackgroundColor(0);
			}
		});
		
		// state rotate
		imgBtn = (ImageButton) popupView.findViewById(R.id.staterotate);
		if(states[3] == 1) imgBtn.setBackgroundResource(R.drawable.border);
		else imgBtn.setBackgroundColor(0);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(states[3] == 1) states[3] = 0;
				else states[3] = 1;
				if(states[3] == 1) v.setBackgroundResource(R.drawable.border);
				else v.setBackgroundColor(0);
			}
		});
		
		// state zoom
		imgBtn = (ImageButton) popupView.findViewById(R.id.statezoom);
		if(states[4] == 1) imgBtn.setBackgroundResource(R.drawable.border);
		else imgBtn.setBackgroundColor(0);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(states[4] == 1) states[4] = 0;
				else states[4] = 1;
				if(states[4] == 1) v.setBackgroundResource(R.drawable.border);
				else v.setBackgroundColor(0);
			}
		});
		
		// state resize
		imgBtn = (ImageButton) popupView.findViewById(R.id.stateresize);
		if(states[4] == 1) imgBtn.setBackgroundResource(R.drawable.border);
		else imgBtn.setBackgroundColor(0);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(states[5] == 1) states[5] = 0;
				else states[5] = 1;
				if(states[5] == 1) v.setBackgroundResource(R.drawable.border);
				else v.setBackgroundColor(0);
			}
		});

		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);

		callBack = call;
	}

}
