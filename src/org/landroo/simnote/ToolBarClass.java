package org.landroo.simnote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.landroo.simnote.R;

import org.landroo.toolbar.FileTool;
import org.landroo.toolbar.FontTool;
import org.landroo.toolbar.HelpTool;
import org.landroo.toolbar.LabelTool;
import org.landroo.toolbar.MainTool;
import org.landroo.toolbar.PaintTool;
import org.landroo.toolbar.PaletteTool;
import org.landroo.toolbar.PictureTool;
import org.landroo.toolbar.PolyTool;
import org.landroo.toolbar.SelectTool;
import org.landroo.toolbar.TextTool;
import org.landroo.toolbar.VectorTool;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

public class ToolBarClass
{
	private static final String TAG = "ToolBarClass";

	private Context context;
	private Handler handler;

	private int displayWidth;
	private int displayHeight;

	public SlidingDrawer topView;
	public Rect topViewRect;

	public SlidingDrawer leftView;
	public Rect leftViewRect;

	// tool gallery
	public Gallery gallery = null;
	private imageListAdapter imageAdapter;
	private ArrayList<Bitmap> mBitmaps = new ArrayList<Bitmap>();

	private String projectDir;
	
	public VectorTool vectorTool;
	public LabelTool labelTool;
	public SelectTool selectTool;
	public PaletteTool paletteTool;
	public FileTool fileTool;
	public FontTool fontTool;
	public TextTool textTool;
	public PictureTool pictureTool;
	public HelpTool helpTool;
	
	public MainTool mainTool;
	public PaintTool paintTool;
	public PolyTool polyTool;
	
	public int lastMode = 1;// picture list mode
	public String lastFile = "";
	
	public NoteItem lastSelectedItem = null;

	public ToolBarClass(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;
		this.projectDir = context.getResources().getString(R.string.project_dir);
		
		vectorTool = new VectorTool(c, v, inflater, w, h, handler);
		labelTool = new LabelTool(c, v, inflater, w, h, handler);
		selectTool = new SelectTool(c, v, inflater, w, h, handler);
		paletteTool = new PaletteTool(c, v, inflater, w, h, handler);
		fileTool = new FileTool(c, v, inflater, w, h, handler);
		fontTool = new FontTool(c, v, inflater, w, h, handler);
		textTool = new TextTool(c, v, inflater, w, h, handler);
		pictureTool = new PictureTool(c, v, inflater, w, h, handler);
		helpTool = new HelpTool(c, v, inflater, w, h, handler);
		
		topView = (SlidingDrawer)getTopView(inflater);
		leftView = (SlidingDrawer)getLeftView(inflater);
		
		mainTool = new MainTool(leftView, handler, context, labelTool);
		paintTool = new PaintTool(leftView, handler, paletteTool);
		polyTool = new PolyTool(leftView, handler, paletteTool);
		
		new assetTask().execute();
	}
	
	// save document in the background
	private class assetTask extends AsyncTask<String, Integer, Long>
	{
		protected Long doInBackground(String... sParams)
		{
			// save contents
			copyAssets("clipart");
			copyAssets("vector");
			copyAssets("chess");
			copyAssets("textures");
			copyAssets("fonts");

			return (long)0;
		}
	}

	// top drawer
	public View getTopView(LayoutInflater inflater)
	{
		int w = displayWidth * 3 / 5;
		int h = displayHeight / 5;
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.top_view, null);
		topView = (SlidingDrawer) view;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);
		topView.setLayoutParams(params);
		topView.setTranslationX(displayWidth / 5);

		Bitmap bitmap = drawBack(w, h, 0, w / 10, true, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);

		LinearLayout ll = (LinearLayout) view.getChildAt(1);
		ll.setBackgroundDrawable(drawable);

		topViewRect = new Rect(0, 0, w, h);

		return topView;
	}

	public boolean checkTopView(int x, int y)
	{
		Rect rect = new Rect(topViewRect);
		rect.left += displayWidth / 5;
		rect.right += displayWidth / 5;
		if (topView.isOpened() && rect.contains(x, y)) return true;

		return false;
	}

	// left drawer
	public View getLeftView(LayoutInflater inflater)
	{
		int w = displayWidth * 2 / 5;
		int h = displayHeight * 6 / 8;
		if(displayWidth > displayHeight) w = displayWidth / 4;  
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.left_view, null);
		leftView = (SlidingDrawer) view;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);
		leftView.setLayoutParams(params);
		leftView.setTranslationY(displayHeight / 6);

		Bitmap bitmap = drawBack(w, h, w / 10, 0, false, h / 20, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);

		LinearLayout ll = (LinearLayout) view.getChildAt(1);
		ll.setBackgroundDrawable(drawable);

		leftViewRect = new Rect(0, 0, w, h);

		return leftView;
	}

	public boolean checkLeftView(int x, int y)
	{
		Rect rect = new Rect(leftViewRect);
		rect.top += displayWidth / 4;
		rect.bottom += displayWidth / 4;
		if (leftView.isOpened() && rect.contains(x, y)) return true;

		return false;
	}

	// setup tool bar
	public void initGallery()
	{
		imageAdapter = new imageListAdapter(context);

		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.picture);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.text);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.polygon);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.paint);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.vector);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.filefolder);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.link);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.paper);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.help);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings);
		mBitmaps.add(bitmap);

		imageAdapter.notifyDataSetChanged();

		gallery = (Gallery) topView.findViewById(R.id.gallery);
		gallery.setAdapter(imageAdapter);
		gallery.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				topView.animateClose();
				switch (arg2)
				{
				case 0:// picture
					pictureTool.showPicturePoup(lastMode, 11);
					break;
				case 1:// text
					String resText = "";
					if(lastSelectedItem != null && lastSelectedItem.type == NoteItem.TYPE_TEXT)
					{
						resText = lastSelectedItem.text;
						textTool.align = lastSelectedItem.textAlign;
						textTool.frameType = lastSelectedItem.frameType;
						textTool.backColor = lastSelectedItem.backColor;						
						textTool.textColor = lastSelectedItem.foreColor;
						textTool.textFont = lastSelectedItem.font;
					}
					else resText = context.getResources().getString(R.string.test);
					textTool.showTextPoup(resText);
					break;
				case 2:// polygon
					handler.sendEmptyMessage(31);
					break;
				case 3:// draw
					handler.sendEmptyMessage(41);
					break;
				case 4:// vector
					vectorTool.showVectorPopup(fileTool.lastFile, 170, 24);
					break;
				case 5:// folder
					fileTool.lastFile = lastFile;
					fileTool.showFilePopup(0, "");
					break;
				case 6:// link
					handler.sendEmptyMessage(140);
					break;
				case 7:// background
					pictureTool.showPicturePoup(3, 51);
					break;
				case 8:// camera
					handler.sendEmptyMessage(91);
					break;
				case 9:// help / info
					helpTool.showHelpPoup();
					break;
				case 10:// show menu
					handler.sendEmptyMessage(130);
					break;
				}
			}
		});
	}

	// calculate pixel size
	public static float dipToPixels(Context context, float dipValue)
	{
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
	}

	// draw drawer background
	public static Bitmap drawBack(int w, int h, int xOff, int yOff, boolean gr, int rad, boolean border, Context context)
	{
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);

		int BLACK = 0xAA303437;
		int WHITE = 0xAAC5C6C7;

		int[] colors = new int[3];
		colors[0] = BLACK;
		colors[1] = WHITE;
		colors[2] = BLACK;

		LinearGradient gradient;
		if (gr) gradient = new LinearGradient(0, 0, w, 0, colors, null, android.graphics.Shader.TileMode.CLAMP);
		else gradient = new LinearGradient(0, 0, 0, h, colors, null, android.graphics.Shader.TileMode.CLAMP);
		paint.setShader(gradient);

		RectF rect = new RectF();
		rect.left = -xOff;
		rect.top = -yOff;
		rect.right = w;
		rect.bottom = h;
		float rx = dipToPixels(context, 20);
		float ry = dipToPixels(context, 20);

		canvas.drawRoundRect(rect, rx, ry, paint);

		if (border)
		{
			paint.setStyle(Paint.Style.STROKE);
			paint.setShader(null);
			paint.setColor(0xff000000);
			paint.setStrokeWidth(dipToPixels(context, 3));
			canvas.drawRoundRect(rect, rx, rx, paint);
		}

		return bitmap;
	}

	// draw masked
	public Bitmap maskFilter(Bitmap mainImage, Bitmap maskImage)
	{
		Canvas canvas = new Canvas();
		Bitmap result = Bitmap.createBitmap(mainImage.getWidth(), mainImage.getHeight(), Bitmap.Config.ARGB_8888);

		canvas.setBitmap(result);
		Paint paint = new Paint();
		paint.setFilterBitmap(false);

		canvas.drawBitmap(mainImage, 0, 0, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(maskImage, 0, 0, paint);
		paint.setXfermode(null);

		return result;
	}

	// copy file from assets
	public void copyAssets(String asset)
	{
		AssetManager assetManager = context.getAssets();
		String[] files = null;
		try
		{
			files = assetManager.list(asset);
		}
		catch (IOException e)
		{
			Log.i(TAG, "Error in copyAssets");
		}

		String sTmpFolder = projectDir + "/" + asset;
		File tmpFile = new File(Environment.getExternalStorageDirectory(), sTmpFolder);
		if (!tmpFile.exists())
		{
			// Failed to create directory
			if (!tmpFile.mkdirs()) return;
		}
		//else return;

		for (String filename : files)
		{
			InputStream in = null;
			OutputStream out = null;
			try
			{
				in = assetManager.open(asset + "/" + filename);
				tmpFile = new File(Environment.getExternalStorageDirectory() + "/" + sTmpFolder + "/" + filename);
				if (!tmpFile.exists())
				{
					out = new FileOutputStream(tmpFile);
					copyFile(in, out);
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
				}
			}
			catch (Exception ex)
			{
				Log.i(TAG, "Error in copyFile: " + ex);
			}
		}
	}

	// copy file
	private void copyFile(InputStream fin, OutputStream fout) throws IOException
	{
		byte[] b = new byte[65536];
		int noOfBytes = 0;

		// read bytes from source file and write to destination file
		while ((noOfBytes = fin.read(b)) != -1)
			fout.write(b, 0, noOfBytes);
	}

	// select toll palette for item
	public void setTools(NoteItem item)
	{
		lastSelectedItem = item;
		if(item != null)
		{
			mainTool.setAlpha(item.alpha);
			mainTool.stretch = item.stretch;
			mainTool.frameType = item.frameType;
		}

		if(item.state == NoteItem.STATE_SELECT) mainTool.showMainTool(item);
		else if(item.state == NoteItem.STATE_HAND_DRAW) paintTool.showPaintTool(item);
		else if(item.state == NoteItem.STATE_POLY_DRAW) polyTool.showPolyTool(item);
	}

	// image list adapter for image gallery
	public class imageListAdapter extends BaseAdapter
	{
		private Context context;

		public imageListAdapter(Context c)
		{
			context = c;
		}

		public int getCount()
		{
			return mBitmaps.size();
		}

		public Object getItem(int position)
		{
			return position;
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (mBitmaps == null) return null;

			ImageView iv = new ImageView(context);
			iv.setBackgroundResource(R.drawable.btn_states);

			try
			{
				Bitmap bitmap = mBitmaps.get(position);
				iv.setImageBitmap(bitmap);
			}
			catch (OutOfMemoryError e)
			{
				Log.e(TAG, "Out of memory error in imageListAdapter!");
				System.gc();
			}
			catch (Exception ex)
			{
				Log.e(TAG, ex.toString());
			}

			iv.setAdjustViewBounds(true);
			iv.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			return iv;
		}
	}
}
