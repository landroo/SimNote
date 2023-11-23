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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SelectTool
{
	private static final String TAG = "HelpTool";
	
	private ViewGroup view;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private int displayWidth;
	private int displayHeight;
	private PopupWindow popupWindow;
	private Context context;
	private ListView selectListView;
	private List<String> selectNames = new ArrayList<String>();
	private int callBack = 0;
	
	public String fileName = "";
	public String itemName = "";
	
	public SelectTool(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.view = v;
		this.layoutInflater = inflater;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;

	}
	
	// list selector popup window
	public void showSelectPoup(int resID, int call)
	{
		View popupView = layoutInflater.inflate(R.layout.list_popup, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight / 2;
		popupWindow = new PopupWindow(popupView, w, h);
		popupWindow.setFocusable(true);

		Bitmap bitmap = ToolBarClass.drawBack(w, h, 0, 0, false, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);
		
		callBack = call;
		selectListView = (ListView) popupView.findViewById(R.id.selectionList);
		selectListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				//String listSelected = selectionNames.get(arg2);
				popupWindow.dismiss();
				if(callBack != 0)
				{
					// list max ten!
					handler.sendEmptyMessage(callBack + arg2);
				}
				callBack = 0;
			}
		});
		
		TypedArray arr = context.getResources().obtainTypedArray(resID);
		selectNames.clear();
		for(int i = 0; i < arr.length(); i++)
			selectNames.add(arr.getString(i));
		arr.recycle();
		
		SelectListAdapter adapter = new SelectListAdapter(context, selectNames);
		selectListView.setAdapter(adapter);
		
		ImageButton btnTextCancel = (ImageButton) popupView.findViewById(R.id.selectionDismiss);
		btnTextCancel.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popupWindow.dismiss();
			}
		});

		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);

		return;
	}

	// selection list adapter
	private class SelectListAdapter extends BaseAdapter
	{

		List<String> list;
		private LayoutInflater mInflater;

		public SelectListAdapter(Context context, List<String> list)
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
			if (convertView == null) convertView = mInflater.inflate(R.layout.list_row, null);

			String line = list.get(position);
			TextView title = (TextView) convertView.findViewById(R.id.listLabel);
			title.setText(line);

			return convertView;
		}
	}
	
}
