package org.landroo.toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.landroo.simnote.R;
import org.landroo.simnote.ToolBarClass;
import org.landroo.simnote.UrlImageView;

import android.content.Context;
import android.content.res.TypedArray;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PictureTool
{
	private static final String TAG = "PictiureTool";
	private static final String TEMP_NAME = "temp";
	
	private ViewGroup view;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private int displayWidth;
	private int displayHeight;
	private PopupWindow popupWindow;
	private Context context;
	private int callBack = 0;
	private GridView pictureGridView;
	private List<String> thumbNames = new ArrayList<String>();
	private boolean shrink;
	private String projectDir;
	private File resizedImage = null;
	
	public String lastFile = "";
	public String pictureName = "";
	public String pathName = "";
	public int lastMode = 1;
	
	public PictureTool(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
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
	
	// show picture chooser windows 
	public void showPicturePoup(int mode, int call)
	{
		View popupView = layoutInflater.inflate(R.layout.picture_popup, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight - displayHeight / 10;
		popupWindow = new PopupWindow(popupView, w, h);
		popupWindow.setFocusable(true);

		Bitmap bitmap = ToolBarClass.drawBack(w, h, 0, 0, false, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);

		lastMode = mode;
		callBack = call;
		
		// folder button
		ImageButton imageBtn = (ImageButton) popupView.findViewById(R.id.picture_folder);
		imageBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popupWindow.dismiss();
				handler.sendEmptyMessage(52);
			}
		});
		
		// close button
		imageBtn = (ImageButton) popupView.findViewById(R.id.picture_dismiss);
		imageBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popupWindow.dismiss();
			}
		});
		
		// palette button
		imageBtn = (ImageButton) popupView.findViewById(R.id.picture_palette);
		imageBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popupWindow.dismiss();
				handler.sendEmptyMessage(53);
			}
		});
		// only background
		if(mode != 3) imageBtn.setVisibility(View.GONE);

		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);
		
		// list view
		pictureGridView = (GridView) popupView.findViewById(R.id.picture_grid);
		pictureGridView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				pathName = thumbNames.get(arg2);
				pictureName = pathName.substring(pathName.lastIndexOf("/") + 1);
				int size = displayWidth / 2;
				if (size > displayHeight) size = displayHeight / 2;
				if (shrink) resizeImage(pathName, size, TEMP_NAME);
				handler.sendEmptyMessage(callBack);
				callBack = 0;
				popupWindow.dismiss();
			}
		});
	
		pictureListAdapter adapter;
		TypedArray arr = context.getResources().obtainTypedArray(R.array.picture_folders);
		String folder = arr.getString(mode);
		switch(mode)
		{
		case 0:// Photos
			shrink = true;
			loadPhotos(folder, true);
        	break;
        case 1:// Cliparts
			shrink = false;
			loadPhotos(projectDir + folder, true);
        	break;
        case 2:// Selected folder
			shrink = false;
			loadPhotos(lastFile, false);
        	break;
        case 3:// Textures
			shrink = false;
			loadPhotos(projectDir + folder, true);
        	break;
        case 4:// Chess
			shrink = false;
			loadPhotos(projectDir + folder, true);
        	break;
		}
		adapter = new pictureListAdapter(context, thumbNames);
		pictureGridView.setAdapter(adapter);
		adapter.notifyDataSetInvalidated();
		
		arr.recycle();

		return;
	}

	// picture thumb list adapter
	private class pictureListAdapter extends BaseAdapter
	{

		List<String> files;
		private LayoutInflater mInflater;

		public pictureListAdapter(Context context, List<String> files)
		{
			this.files = files;
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount()
		{
			return files.size();
		}

		@Override
		public Object getItem(int position)
		{
			return files.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null) convertView = mInflater.inflate(R.layout.picture_row, null);

			UrlImageView thumb = (UrlImageView) convertView.findViewById(R.id.image_thumb);

			String file = files.get(position);
			thumb.setImageDrawable(file, displayWidth / 4, displayHeight / 4);

			TextView title = (TextView) convertView.findViewById(R.id.grid_item_label);
			String label = file.substring(file.lastIndexOf("/") + 1);
			title.setText(label);

			return convertView;
		}
	}

	// load photos
	private int loadPhotos(String path, boolean root)
	{
		int iRet = 0;
		thumbNames.clear();
		try
		{
			File[] imageFiles;
			if(root)
				imageFiles = new File(Environment.getExternalStorageDirectory() + File.separator + path).listFiles();
			else
				imageFiles = new File(path).listFiles();
			iRet = imageFiles.length;

			for (int i = 0; i < imageFiles.length; i++)
				thumbNames.add(imageFiles[i].getAbsolutePath());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return iRet;
	}
	
	// resize image
	public boolean resizeImage(String inFile, int maxSize, String note)
	{
		Bitmap inBmp = null;
		Bitmap outBmp = null;
		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			if (shrink) options.inSampleSize = 4;
			inBmp = BitmapFactory.decodeFile(inFile, options);

			resizedImage = getOutputMediaFile(note, ".jpg");

			pathName = resizedImage.getPath();
			// Log.i(TAG, pathName);

			FileOutputStream out = new FileOutputStream(resizedImage);
			// Log.i(TAG, "resize to " + resizedImage.getAbsolutePath() + " " +
			// resizedImage.getName());

			int x = inBmp.getWidth();
			int y = inBmp.getHeight();
			int newX = 0, newY = 0;
			if (x > maxSize || y > maxSize)
			{
				double faktor = (double) x / (double) y;
				if (x > y)
				{
					newX = maxSize;
					newY = (int) Math.round(maxSize / faktor);
				}
				else
				{
					newY = maxSize;
					newX = (int) Math.round(maxSize * faktor);
				}
				outBmp = Bitmap.createScaledBitmap(inBmp, newX, newY, false);
			}
			else outBmp = Bitmap.createScaledBitmap(inBmp, x, y, false);

			outBmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
			out.flush();
			out.close();

			inBmp.recycle();
			inBmp = null;

			outBmp.recycle();
			outBmp = null;
		}
		catch (OutOfMemoryError e)
		{
			// String sTooBig = getResources().getString(R.string.toobig);
			Log.e(TAG, "" + e);
			return false;
		}
		catch (IOException e)
		{
			Log.e(TAG, "" + e);
			return false;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage());
			return false;
		}

		return true;
	}

	// create unique filename
	public File getOutputMediaFile(String name, String ext)
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), projectDir);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists())
		{
			// Failed to create directory
			if (!mediaStorageDir.mkdirs()) return null;
		}

		// Create a media file name
		File mediaFile;
		if (name == null) name = "";
		mediaFile = new File(mediaStorageDir.getPath() + File.separator + name + "_" + timeStamp + ext);

		return mediaFile;
	}

}
