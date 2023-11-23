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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;

public class LabelTool
{
	private ViewGroup view;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private int displayWidth;
	private int displayHeight;
	private PopupWindow popupWindow;
	private Context context;
	private int callBack = 0;
	private EditText editText;
	
	public String resText;
	
	public LabelTool(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.view = v;
		this.layoutInflater = inflater;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;
	}
	
	// text edit popup window
	public void showLabelPoup(String sText, int call)
	{
		View popupView = layoutInflater.inflate(R.layout.label_popup, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight / 4;
		popupWindow = new PopupWindow(popupView, w, h);
		popupWindow.setFocusable(true);

		Bitmap bitmap = ToolBarClass.drawBack(w, h, 0, 0, false, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);
		
		editText = (EditText) popupView.findViewById(R.id.editLabel);
		editText.setText(sText);
		editText.requestFocus();
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
		
		callBack = call;
		ImageButton imgbtn = (ImageButton) popupView.findViewById(R.id.labelDismiss);
		imgbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popupWindow.dismiss();
				if(callBack != 0) handler.sendEmptyMessage(callBack);
				callBack = 0;
			}
		});
		
		imgbtn = (ImageButton) popupView.findViewById(R.id.labelOK);
		imgbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				resText = editText.getText().toString();
				editText.setText("");
				popupWindow.dismiss();
				if(callBack != 0) handler.sendEmptyMessage(callBack);
				callBack = 0;
			}
		});
		
		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);

		return;
	}

}
