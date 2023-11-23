package org.landroo.toolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.landroo.simnote.R;
import org.landroo.simnote.ToolBarClass;
import org.landroo.tools.VectorItem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class VectorTool
{
	private static final String TAG = "VectorTool";
	
	private ViewGroup view;
	private vectorListAdapter vectorAdapter;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private int displayWidth;
	private int displayHeight;
	private PopupWindow popupWindow;
	private Context context;
	private ListView fileListView;
	private List<String> filePath = null;
	private String projectDir;
	private int callBack = 0;
	private String userFolder;
	private TextView textView;
	
	public String fileName = "";
	public String itemName = "";
	public int density = 24;
	
	public VectorTool(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.view = v;
		this.layoutInflater = inflater;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;

		projectDir = context.getResources().getString(R.string.project_dir);
	}
	
	// show file popup window
	public void showVectorPopup(String folder, int call, int dens)
	{
		View popupView = layoutInflater.inflate(R.layout.vector_popup, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight - displayHeight / 10;
		popupWindow = new PopupWindow(popupView, w, h);
		popupWindow.setFocusable(true);

		Bitmap bitmap = ToolBarClass.drawBack(w, h, 0, 0, false, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);

		callBack = call;
		userFolder = folder;
		fileListView = (ListView) popupView.findViewById(R.id.vectorlist);
		fileListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				fileName = filePath.get(arg2);
				popupWindow.dismiss();
				if(callBack != 0) handler.sendEmptyMessage(callBack);
			}
		});

		// close button
		ImageButton imgBtn = (ImageButton) popupView.findViewById(R.id.vector_close);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popupWindow.dismiss();
			}
		});
		
		// folder button
		imgBtn = (ImageButton) popupView.findViewById(R.id.vector_folder);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setFolder(userFolder);
			}
		});
		
		// circle button
		imgBtn = (ImageButton) popupView.findViewById(R.id.vector_circle);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				fileName = "circle";
				popupWindow.dismiss();
				if(callBack != 0) handler.sendEmptyMessage(callBack);
			}
		});

		// square button
		imgBtn = (ImageButton) popupView.findViewById(R.id.vector_square);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				fileName = "square";
				popupWindow.dismiss();
				if(callBack != 0) handler.sendEmptyMessage(callBack);
			}
		});
		
		// capsule button
		imgBtn = (ImageButton) popupView.findViewById(R.id.vector_capsule);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				fileName = "capsule";
				popupWindow.dismiss();
				if(callBack != 0) handler.sendEmptyMessage(callBack);
			}
		});
		
		// arror button
		imgBtn = (ImageButton) popupView.findViewById(R.id.vector_arrow);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				fileName = "arrow";
				popupWindow.dismiss();
				if(callBack != 0) handler.sendEmptyMessage(callBack);
			}
		});
		
		String sText = context.getResources().getString(R.string.density);
		textView = (TextView) popupView.findViewById(R.id.vector_density_text);
		textView.setText(sText + ": " + density);
		
		// density seek bar
		SeekBar seekBar = (SeekBar) popupView.findViewById(R.id.density_seek_bar);
		seekBar.setProgress(dens);
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
				density = progress + 3;
				String sText = context.getResources().getString(R.string.density);
				textView.setText(sText + ": " + density);
			}
		});		

		String path = "/" + projectDir + "/vector";
		setFolder(path);

		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);

		return;
	}
	
	private void setFolder(String path)
	{
		filePath = new ArrayList<String>();
		try
		{
			File[] imageFiles = new File(Environment.getExternalStorageDirectory() + File.separator + path).listFiles();
			for (int i = 0; i < imageFiles.length; i++)
				filePath.add(imageFiles[i].getAbsolutePath());
		}
		catch (Exception ex)
		{
			Log.i(TAG, "" + ex);
		}
		
		vectorAdapter = new vectorListAdapter(context, (ArrayList<String>) filePath);
		fileListView.setAdapter(vectorAdapter);

		return;
	}
	
	// vector list adapter
	private class vectorListAdapter extends ArrayAdapter<String>
	{
		private ArrayList<String> values;
		private TextView textView;
		private View rowView;
		private LayoutInflater inflater;
		private ImageView imageView;
		private VectorItem vectorItem;

		public vectorListAdapter(Context context, ArrayList<String> values)
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
				if (rowView == null) rowView = inflater.inflate(R.layout.vector_row, parent, false);

				textView = (TextView) rowView.findViewById(R.id.label);
				imageView = (ImageView) rowView.findViewById(R.id.icon);

				String sText = values.get(position);
				vectorItem = new VectorItem(sText, displayWidth / 6);
				
				imageView.setImageResource(R.drawable.filefolder);
				imageView.setImageBitmap(vectorItem.bitmap);
				textView.setText(vectorItem.name);

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
