package org.landroo.toolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.landroo.simnote.R;
import org.landroo.simnote.ToolBarClass;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FileTool
{
	private static final String TAG = "FileTool";
	
	private ViewGroup view;
	private fileListAdapter fileAdapter;
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
	
	public String lastFile = "";
	public String fileExt = "";
	public boolean cancel = true;
	
	public FileTool(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.view = v;
		this.layoutInflater = inflater;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;

		projectDir = context.getResources().getString(R.string.project_dir);
		
		this.lastFile = Environment.getExternalStorageDirectory().getPath() + "/" + projectDir;
	}	
	// show file popup window
	public void showFilePopup(int call, String ext)
	{
		View popupView = layoutInflater.inflate(R.layout.file_popup, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight - displayHeight / 10;
		popupWindow = new PopupWindow(popupView, w, h);
		popupWindow.setFocusable(true);

		Bitmap bitmap = ToolBarClass.drawBack(w, h, 0, 0, false, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);

		fileListView = (ListView) popupView.findViewById(R.id.filelist);
		fileListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				lastFile = filePath.get(arg2);
				boolean bDir = changeDir(lastFile);
				if(callBack != 0 && bDir == false)
				{
					cancel = false;
					popupWindow.dismiss();
					handler.sendEmptyMessage(callBack);
				}
				callBack = 0;
			}
		});

		ImageButton imgBtn = (ImageButton) popupView.findViewById(R.id.file_dismiss);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(callBack != 0) handler.sendEmptyMessage(callBack);
				popupWindow.dismiss();
				cancel = true;
			}
		});

		imgBtn = (ImageButton) popupView.findViewById(R.id.file_ok);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(callBack != 0) handler.sendEmptyMessage(callBack);
				popupWindow.dismiss();
				cancel = false;
			}
		});
		
		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);

		callBack = call;
		fileExt = ext;
		getDir(lastFile);
	}

	// enter a folder
	private boolean changeDir(String sText)
	{
		String[] sPath = sText.split("\t");
		sText = sPath[2];

		File file = new File(sText);

		if (file.isDirectory())
		{
			if (file.canRead()) getDir(sText);
			else
			{
				new AlertDialog.Builder(context).setIcon(R.drawable.filefolder)
						.setTitle("[" + file.getName() + "] folder can't be read!")
						.setPositiveButton("OK", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
							}
						}).show();
			}
			
			return true;
		}
		else
		{
			lastFile = file.getPath();
		}
		
		return false;
	}

	// get folder file list
	private void getDir(String dirPath)
	{
		filePath = new ArrayList<String>();

		File f = new File(dirPath);
		File[] files = f.listFiles();
		if (files == null)
		{
			dirPath = "/";
			f = new File(dirPath);
			files = f.listFiles();
		}

		if (!dirPath.equals("/"))
		{
			filePath.add("  /\t  /\t/");
			filePath.add("  ../\t  ../\t" + f.getParent());
		}

		File file;
		String sText, ext = "";
		for (int i = 0; i < files.length; i++)
		{
			file = files[i];
			sText = file.getName().toLowerCase();
			if (sText.lastIndexOf(".") != -1) ext = sText.substring(sText.lastIndexOf("."));
			if (file.isDirectory())
				filePath.add(" " + sText + "\t " + file.getName() + "\t" + file.getPath());
			else if(fileExt.equals("*") || fileExt.indexOf(ext) != -1)
				filePath.add(sText + "\t" + file.getName() + "\t" + file.getPath());
		}
		Collections.sort(filePath);

		fileAdapter = new fileListAdapter(context, (ArrayList<String>) filePath);
		fileListView.setAdapter(fileAdapter);
	}

	// file list adapter
	private class fileListAdapter extends ArrayAdapter<String>
	{
		private ArrayList<String> values;
		private TextView textView;
		private View rowView;
		private LayoutInflater inflater;
		private ImageView imageView;

		public fileListAdapter(Context context, ArrayList<String> values)
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
				if (rowView == null) rowView = inflater.inflate(R.layout.file_row, parent, false);

				textView = (TextView) rowView.findViewById(R.id.label);
				imageView = (ImageView) rowView.findViewById(R.id.icon);

				String sExt = "";
				String sText = values.get(position);
				String[] sPath = sText.split("\t");
				sText = sPath[1];
				if (sText.indexOf(" ") == 0)
				{
					imageView.setImageResource(R.drawable.filefolder);
					sText = sText.substring(1);
				}
				else
				{
					if (sText.lastIndexOf(".") != -1) sExt = sText.substring(sText.lastIndexOf("."));
					sExt = sExt.toLowerCase();
					if (sExt.equals(".png") || sExt.equals(".jpg")) imageView.setImageResource(R.drawable.picture);
					else if (sExt.equals(".note"))
					{
						try
						{
							String name = sPath[2].substring(0, sPath[2].length() - 5) + ".jpg";
							Bitmap bitmap = BitmapFactory.decodeFile(name);
							imageView.setImageBitmap(bitmap);
						}
						catch(Exception ex)
						{
							Log.i(TAG, "" + ex);
						}
					}
					else imageView.setImageResource(R.drawable.help);
				}
				textView.setText(sText);

			}
			catch (OutOfMemoryError e)
			{
				Log.e(TAG, "Out of memory error in fileListAdapter!");
				System.gc();
			}

			return rowView;
		}

	}

}
