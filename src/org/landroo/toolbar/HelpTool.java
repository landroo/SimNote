package org.landroo.toolbar;

import java.util.ArrayList;
import java.util.List;

import org.landroo.simnote.R;
import org.landroo.simnote.ToolBarClass;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
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
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class HelpTool
{
	private static final String TAG = "HelpTool";
	
	private ViewGroup view;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private int displayWidth;
	private int displayHeight;
	private PopupWindow popupWindow;
	private Context context;
	private ListView helpListView;
	private boolean mainHelp = true;
	
	public HelpTool(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.view = v;
		this.layoutInflater = inflater;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;
	}
	
	// help popup window
	public void showHelpPoup()
	{
		View popupView = layoutInflater.inflate(R.layout.help_popup, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight - displayHeight / 10;
		popupWindow = new PopupWindow(popupView, w, h);
		popupWindow.setFocusable(true);

		Bitmap bitmap = ToolBarClass.drawBack(w, h, 0, 0, false, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);
		
		helpListView = (ListView) popupView.findViewById(R.id.help_list);
		helpListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				//String listSelected = selectionNames.get(arg2);
				if(mainHelp)
				{
					switch(arg2)
					{
					case 6:// picture tools
					case 7:// text tools
						setlist(R.array.maintool_help, R.array.maintool_helptext, R.array.maintool_helpid);
						mainHelp = false;
						break;
					case 8:// polygon tools
						setlist(R.array.polytool_help, R.array.polytool_helptext, R.array.polytool_helpid);
						mainHelp = false;
						break;
					case 9:// hand draw tools
						setlist(R.array.handtool_help, R.array.handtool_helptext, R.array.handtool_helpid);
						mainHelp = false;
						break;
					}
				}
			}
		});
		
		ImageButton btnTextCancel = (ImageButton) popupView.findViewById(R.id.help_dismiss);
		btnTextCancel.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(mainHelp == false)
				{
					mainHelp = true;
					setlist(R.array.toolbar_help, R.array.toolbar_helptext, R.array.toolbar_helpid);
				}
				else popupWindow.dismiss();
			}
		});
		
		setlist(R.array.toolbar_help, R.array.toolbar_helptext, R.array.toolbar_helpid);

		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);

		return;
	}
	
	private void setlist(int nameID, int textID, int iconID)
	{
		List<String> helpName = new ArrayList<String>();
		List<String> helpText = new ArrayList<String>();
		List<String> helpID = new ArrayList<String>();
	
		TypedArray arr = context.getResources().obtainTypedArray(nameID);
		helpName.clear();
		for(int i = 0; i < arr.length(); i++) helpName.add(arr.getString(i));
		arr.recycle();
		
		arr = context.getResources().obtainTypedArray(textID);
		helpText.clear();
		for(int i = 0; i < arr.length(); i++) helpText.add(arr.getString(i));
		arr.recycle();
		
		arr = context.getResources().obtainTypedArray(iconID);
		helpID.clear();
		for(int i = 0; i < arr.length(); i++) helpID.add(arr.getString(i));
		arr.recycle();

		HelpListAdapter adapter = new HelpListAdapter(context, helpName, helpText, helpID);
		helpListView.setAdapter(adapter);
	}

	// selection list adapter
	private class HelpListAdapter extends BaseAdapter
	{

		List<String> name;
		List<String> text;
		List<String> id;
		private LayoutInflater mInflater;

		public HelpListAdapter(Context context, List<String> name, List<String> text, List<String> id)
		{
			this.name = name;
			this.text = text;
			this.id = id;
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount()
		{
			return name.size();
		}

		@Override
		public Object getItem(int position)
		{
			return name.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null) convertView = mInflater.inflate(R.layout.help_row, null);

			String line = name.get(position);
			TextView title = (TextView) convertView.findViewById(R.id.help_label);
			title.setText(line);
			
			line = text.get(position);
			title = (TextView) convertView.findViewById(R.id.help_text);
			title.setText(line);
			
			line = id.get(position);
			int drawableResourceId = context.getResources().getIdentifier(line, "drawable", context.getPackageName());
			ImageView imageView = (ImageView) convertView.findViewById(R.id.help_icon);
			imageView.setImageResource(drawableResourceId);

			return convertView;
		}
	}

}
