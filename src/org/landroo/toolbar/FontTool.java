package org.landroo.toolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.landroo.simnote.NoteItem;
import org.landroo.simnote.R;
import org.landroo.simnote.ToolBarClass;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class FontTool
{
	private static final String TAG = "FontTool";
	
	private ViewGroup view;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private int displayWidth;
	private int displayHeight;
	private PopupWindow popupWindow;
	private ImageView imageView;
	private Context context;
	private ListView fontListView;
	private List<String> fontNames = new ArrayList<String>();
	private List<String> fontFolders = new ArrayList<String>();
	private int callBack = 0;
	private SeekBar seekBar;
	
	public int fontColor = NoteItem.COLOR_BLACK;
	public int fontSize = 18;
	public int fontID = 0;
	
	public boolean bold = false;
	public boolean italic = false;
	public boolean filled = false;
	public boolean shadow = false;
	
	public FontTool(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
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
		View popupView = layoutInflater.inflate(R.layout.font_popup, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight - displayHeight / 10;
		popupWindow = new PopupWindow(popupView, w, h);
		popupWindow.setFocusable(true);

		Bitmap bitmap = ToolBarClass.drawBack(w, h, 0, 0, false, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);
		
		imageView = (ImageView) popupView.findViewById(R.id.fontimage);

		fontListView = (ListView) popupView.findViewById(R.id.font_list);
		fontListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				fontID = arg2;
				imageView.setImageBitmap(showFont(fontID));
			}
		});

		// font close
		ImageButton imgBtn = (ImageButton) popupView.findViewById(R.id.font_close);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(callBack != 0) handler.sendEmptyMessage(callBack);
				popupWindow.dismiss();
			}
		});
		
		// font ready
		imgBtn = (ImageButton) popupView.findViewById(R.id.font_ok);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(callBack != 0) handler.sendEmptyMessage(callBack);
				popupWindow.dismiss();
			}
		});
		
		// font color
		imgBtn = (ImageButton) popupView.findViewById(R.id.font_color);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// call color popup
				if(callBack != 0) handler.sendEmptyMessage(64);
				popupWindow.dismiss();
			}
		});
		
		// font bold
		imgBtn = (ImageButton) popupView.findViewById(R.id.font_bold);
		if(bold) imgBtn.setBackgroundResource(R.drawable.border);
		else imgBtn.setBackgroundColor(0);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				bold = !bold;
				if(bold) v.setBackgroundResource(R.drawable.border);
				else v.setBackgroundColor(0);
				imageView.setImageBitmap(showFont(fontID));
			}
		});
		
		// font italic
		imgBtn = (ImageButton) popupView.findViewById(R.id.font_italic);
		if(italic) imgBtn.setBackgroundResource(R.drawable.border);
		else imgBtn.setBackgroundColor(0);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				italic = !italic;
				if(italic) v.setBackgroundResource(R.drawable.border);
				else v.setBackgroundColor(0);
				imageView.setImageBitmap(showFont(fontID));
			}
		});		
		
		// font fill
		imgBtn = (ImageButton) popupView.findViewById(R.id.font_fill);
		if(filled) imgBtn.setBackgroundResource(R.drawable.border);
		else imgBtn.setBackgroundColor(0);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				filled = !filled;
				if(filled) v.setBackgroundResource(R.drawable.border);
				else v.setBackgroundColor(0);
				imageView.setImageBitmap(showFont(fontID));
			}
		});
		
		// font shadow
		imgBtn = (ImageButton) popupView.findViewById(R.id.font_shadow);
		if(shadow) imgBtn.setBackgroundResource(R.drawable.border);
		else imgBtn.setBackgroundColor(0);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				shadow = !shadow;
				if(shadow) v.setBackgroundResource(R.drawable.border);
				else v.setBackgroundColor(0);
				imageView.setImageBitmap(showFont(fontID));
			}
		});		
		
		// font size seek bar
		seekBar = (SeekBar) popupView.findViewById(R.id.font_size_seek_bar);
		seekBar.setProgress(fontSize);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				fontSize = progress + 10;
				imageView.setImageBitmap(showFont(fontID));
			}
		});
		
		if(fontNames.size() == 0) fillFonts();
		
		FontListAdapter adapter = new FontListAdapter(context, fontNames);
		fontListView.setAdapter(adapter);
		
		imageView.setImageBitmap(showFont(fontID));

		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);

		callBack = call;
		
		return;
	}
	
	// fill font list
	private void fillFonts()
	{
		TypedArray arr = context.getResources().obtainTypedArray(R.array.font_names);
		for(int i = 0; i < arr.length(); i++)
		{
			fontNames.add(arr.getString(i));
			fontFolders.add(arr.getString(i));
		}
		arr.recycle();
		
		String projectDir = context.getString(R.string.project_dir) + "/fonts";
		File folder = new File(Environment.getExternalStorageDirectory(), projectDir);
		File[] files = folder.listFiles();
		for(int i = 0; i < files.length; i++)
		{
			fontNames.add(files[i].getName().substring(0, files[i].getName().lastIndexOf(".")));
			fontFolders.add(files[i].getAbsolutePath());
		}

		return;
	}

	// font list adapter
	private class FontListAdapter extends BaseAdapter
	{
		List<String> list;
		private LayoutInflater mInflater;

		public FontListAdapter(Context context, List<String> list)
		{
			this.list = list;
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount()
		{
			return list.size();
		}

		@Override
		public Object getItem(int position)
		{
			return list.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null) convertView = mInflater.inflate(R.layout.font_row, null);

			String line = list.get(position);
			TextView title = (TextView) convertView.findViewById(R.id.fontlabel);
			title.setText(line);

			return convertView;
		}
	}
	
	// monospace, sans, serif
	private Bitmap showFont(int id)
	{
		String font = fontFolders.get(id);
		Typeface tf;
		if(font.indexOf("/") == -1) tf = Typeface.create(font, Typeface.NORMAL);
		else tf = Typeface.createFromFile(font);
		if(tf != null)
		{
			if(bold) tf = Typeface.create(tf, Typeface.BOLD);
			if(italic) tf = Typeface.create(tf, Typeface.ITALIC);
		}
		else
		{
			Log.i(TAG, font);
		}
			
		Paint paint = new Paint();
		paint.setTextSize(fontSize);
		paint.setTypeface(tf);
		paint.setColor(0xFFFFFFFF);
		paint.setAntiAlias(true);
		paint.setFakeBoldText(true);
		
		if(shadow) paint.setShadowLayer(3, 3, 3, Color.BLACK);
		if(filled) paint.setStyle(Style.STROKE);
		else paint.setStyle(Style.FILL);
		
		int w = displayWidth - displayWidth / 20;
		int h = displayHeight / 6;
		
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		
		String text = "Simple notes";
		float txtWidth = paint.measureText(text);
		
		canvas.drawText(text, (w - txtWidth) / 2, fontSize, paint);
		
		return bitmap;
	}
	
	// get font string
	public String getFont()
	{
		StringBuilder sb = new StringBuilder();
		if(fontFolders.size() != 0) sb.append(fontFolders.get(fontID));
		else sb.append("sans");
		sb.append("\t");
		sb.append(fontSize);
		sb.append("\t");
		sb.append(bold);
		sb.append("\t");
		sb.append(italic);
		sb.append("\t");
		sb.append(filled);
		sb.append("\t");
		sb.append(shadow);

		return sb.toString();
	}
	
	// set font
	public void setFont(String fontList)
	{
		if(fontNames.size() == 0) fillFonts();
		String[] fontArr = fontList.split("\t");
		String font = fontArr[0];

		for(int i = 0; i < fontFolders.size(); i++)
		{
			if(fontFolders.get(i).equals(font))
			{
				fontID = i;
				break;
			}
		}
		
		if(fontArr.length > 1)
		{
			fontSize = Integer.parseInt(fontArr[1]);
			bold = fontArr[2].equals("true") ? true: false;
			italic = fontArr[3].equals("true") ? true: false;
			filled = fontArr[4].equals("true") ? true: false;
			shadow = fontArr[5].equals("true") ? true: false;
		}
		
		 showFont(fontID);
		 
		 return;
	}
}
