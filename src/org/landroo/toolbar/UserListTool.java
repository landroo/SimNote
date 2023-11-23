package org.landroo.toolbar;

import java.util.ArrayList;
import java.util.List;

import org.landroo.simnote.R;
import org.landroo.simnote.ToolBarClass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class UserListTool
{
	private static final String TAG = "VectorTool";
	
	private ViewGroup view;
	private userListAdapter userAdapter;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private int displayWidth;
	private int displayHeight;
	private PopupWindow popupWindow;
	private Context context;
	private ListView userListView;
	private List<String> userList;
	private List<String> partnerList;
	
	public String selectedUser;
	
	public UserListTool(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.view = v;
		this.layoutInflater = inflater;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;
	}
	
	// show file popup window
	public void showUserlistPopup(List<String> users, List<String> partners)
	{
		View popupView = layoutInflater.inflate(R.layout.userlist_popup, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight - displayHeight / 10;
		popupWindow = new PopupWindow(popupView, w, h);
		popupWindow.setFocusable(true);

		Bitmap bitmap = ToolBarClass.drawBack(w, h, 0, 0, false, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);
		
		userListView = (ListView) popupView.findViewById(R.id.userlist);
		userListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
			}
		});

		ImageButton imgBtn = (ImageButton) popupView.findViewById(R.id.userlist_close);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popupWindow.dismiss();
			}
		});
		
		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);
		
		partnerList = partners;
		
		userList = users;
		userAdapter = new userListAdapter(context, (ArrayList<String>) userList);
		userListView.setAdapter(userAdapter);
	}
	
	// vector list adapter
	private class userListAdapter extends ArrayAdapter<String>
	{
		private ArrayList<String> values;
		private TextView textView;
		private View rowView;
		private LayoutInflater inflater;
		private ImageView imageView;
		private ImageButton imgBtn;

		public userListAdapter(Context context, ArrayList<String> values)
		{
			super(context, R.layout.file_row, values);
			this.values = values;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			try
			{
				rowView = convertView;
				if (rowView == null) rowView = inflater.inflate(R.layout.userlist_row, parent, false);

				textView = (TextView) rowView.findViewById(R.id.label_user);

				String sText = values.get(position);
				textView.setText(sText);
				
				imgBtn = (ImageButton) rowView.findViewById(R.id.userlist_invite);
				imgBtn.setId(position);
				imgBtn.setOnClickListener(new Button.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						// invite
						selectedUser = userList.get(v.getId());
						handler.sendEmptyMessage(126);
						popupWindow.dismiss();
					}
				});
				if(partnerList.contains(sText)) imgBtn.setVisibility(View.GONE);
				
				imgBtn = (ImageButton) rowView.findViewById(R.id.userlist_message);
				imgBtn.setId(position);
				imgBtn.setOnClickListener(new Button.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						// send message
						selectedUser = userList.get(v.getId());
						handler.sendEmptyMessage(125);
						popupWindow.dismiss();
					}
				});
			}
			catch (OutOfMemoryError e)
			{
				Log.e(TAG, "Out of memory error in vectorListAdapter!");
				System.gc();
			}

			return rowView;
		}

	}

}
