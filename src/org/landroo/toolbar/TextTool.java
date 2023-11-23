package org.landroo.toolbar;

import org.landroo.simnote.NoteItem;
import org.landroo.simnote.R;
import org.landroo.simnote.ToolBarClass;
import org.landroo.tools.TextFormat;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;

public class TextTool
{
	private static final String TAG = "TextTool";
	
	private ViewGroup view;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private int displayWidth;
	private int displayHeight;
	private PopupWindow popupWindow;
	private Context context;
	private EditText editText;
	private ImageButton leftBtn;
	private ImageButton rightBtn;
	private ImageButton centBtn;
	private ImageButton fillBtn;
	
	public String resText = "";
	public Bitmap resBitmap = null;
	
	public int frameType = 0;
	public int align = 4;
	public int textColor = NoteItem.COLOR_BLACK;
	public int backColor = 0x00FFFFFF;// transparent
	public String textFont = "sans\t18\tfalse\tfalse\tfalse\tfalse";
	
	public TextTool(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.view = v;
		this.layoutInflater = inflater;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;
	}	

	public void showTextPoup(String sText)
	{
		View popupView = layoutInflater.inflate(R.layout.text_popup, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight - displayHeight / 10;
		popupWindow = new PopupWindow(popupView, w, h);
		popupWindow.setFocusable(true);

		Bitmap bitmap = ToolBarClass.drawBack(w, h, 0, 0, false, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);
		
		editText = (EditText) popupView.findViewById(R.id.textEdit);
		editText.setText(sText);
		editText.requestFocus();
		editText.setOnLongClickListener(new OnLongClickListener() 
		{
		    @Override
		    public boolean onLongClick(View view) 
		    {
		        ClipboardManager cm = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
		        String paste = (String) cm.getText();
		        if(paste != null && !paste.equals("")) editText.setText(paste);
		        
		        return true;
		    }
		});
/*		editText.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				// TODO Auto-generated method stub
				return true;
			}
		});*/
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

		// textclose button
		ImageButton imgBtn = (ImageButton) popupView.findViewById(R.id.textclose);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				resText = editText.getText().toString();
				popupWindow.dismiss();
			}
		});

		// text ready button
		imgBtn = (ImageButton) popupView.findViewById(R.id.textready);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				resText = editText.getText().toString();
				TextFormat tf = new TextFormat(context, displayWidth / 4 * 3, displayHeight / 4 * 3, align);
				tf.setPaint(textFont, textColor, backColor);
				tf.frameType = frameType;
				resBitmap = tf.formatText(resText, false);
				if(resBitmap != null) handler.sendEmptyMessage(21);

				popupWindow.dismiss();
			}
		});

		// text font button
		imgBtn = (ImageButton) popupView.findViewById(R.id.textfont);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				resText = editText.getText().toString();
				popupWindow.dismiss();
				// show font popup
				handler.sendEmptyMessage(61);
			}
		});
		
		// text color button
		imgBtn = (ImageButton) popupView.findViewById(R.id.textcolor);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				resText = editText.getText().toString();
				popupWindow.dismiss();
				handler.sendEmptyMessage(63);
			}
		});

		// text left radio
		leftBtn = (ImageButton) popupView.findViewById(R.id.textleft);
		leftBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				align = 1;
				setAlight(align);
			}
		});

		// text right radio
		rightBtn = (ImageButton) popupView.findViewById(R.id.textright);
		rightBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				align = 2;
				setAlight(align);
			}
		});
		
		// text center radio
		centBtn = (ImageButton) popupView.findViewById(R.id.textcent);
		centBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				align = 3;
				setAlight(align);
			}
		});

		// text fill radio
		fillBtn = (ImageButton) popupView.findViewById(R.id.textfill);
		fillBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				align = 4;
				setAlight(align);
			}
		});
		
		setAlight(align);

		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);

		return;
	}
	
	// set radio states
	private void setAlight(int mode)
	{
		switch(mode)
		{
		case 1:
			leftBtn.setBackgroundResource(R.drawable.border_sel);
			rightBtn.setBackgroundColor(0);
			centBtn.setBackgroundColor(0);
			fillBtn.setBackgroundColor(0);
			break;
		case 2:
			rightBtn.setBackgroundResource(R.drawable.border_sel);
			leftBtn.setBackgroundColor(0);
			centBtn.setBackgroundColor(0);
			fillBtn.setBackgroundColor(0);
			break;
		case 3:
			centBtn.setBackgroundResource(R.drawable.border_sel);
			rightBtn.setBackgroundColor(0);
			leftBtn.setBackgroundColor(0);
			fillBtn.setBackgroundColor(0);
			break;
		case 4:
			fillBtn.setBackgroundResource(R.drawable.border_sel);
			rightBtn.setBackgroundColor(0);
			centBtn.setBackgroundColor(0);
			leftBtn.setBackgroundColor(0);
			break;
		}
	}
}
