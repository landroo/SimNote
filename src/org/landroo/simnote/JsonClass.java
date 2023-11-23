package org.landroo.simnote;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.landroo.tools.FingerPaint;
import org.landroo.tools.TextFormat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.util.Log;

public class JsonClass 
{
	private static final String TAG = "JsonClass";
	
	public Context context;
	public float width;
	public float height;
	public String backBmp;
	public NoteItem lastItem = null;
	
	public JsonClass (Context context)
	{
		this.context = context;
	}
	
	// add items to list form a json file
	public void parseFile(String fName, List<NoteItem> itemList, String user)
	{
		String jString = loadFile(fName);
		
		try
		{
			JSONObject jobj = new JSONObject(jString);
			width = Float.parseFloat(jobj.getString("width"));
			height = Float.parseFloat(jobj.getString("height"));
			backBmp = jobj.getString("background");
			
			JSONArray items = jobj.getJSONArray("items");
			for(int i = 0; i < items.length(); i++)
			{
				JSONObject item = items.getJSONObject(i);
				NoteItem newItem = parseItem(item, user);
				if(checkItem(newItem)) itemList.add(newItem);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		setLinked(itemList);
		
		return;
	}
	
	// load json file to text
	private String loadFile(String fName)
    {
		String jsontext = "";
        try
        {
        	File file = new File(fName);
            InputStream is = new FileInputStream(file); 
            byte [] buffer = new byte[is.available()];
            while(is.read(buffer) != -1);
            	jsontext = new String(buffer, "utf-8");
            is.close();
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
        
        return jsontext;
    }
	
	// create note item from json object
	public NoteItem parseItem(JSONObject item, String user)
	{
		NoteItem newItem = null;
		Paint infoPaint = new Paint();
		infoPaint.setColor(0xFF444444);
		infoPaint.setTextSize(36);

		try
		{
			Bitmap bitmap, bmp = null;
			float x = Float.parseFloat(item.getString("px"));
			float y = Float.parseFloat(item.getString("py"));
			int type = Integer.parseInt(item.getString("type"));
			String text = item.getString("text");
			float w = Float.parseFloat(item.getString("iw"));
			float h = Float.parseFloat(item.getString("ih"));
			
			try
			{
				String base64 = item.getString("bitmap");
				byte[] imageAsBytes = Base64.decode(base64.getBytes(), Base64.DEFAULT);
				bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
				bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
	        	Canvas canvas = new Canvas(bmp);
	        	canvas.drawBitmap(bitmap, 0, 0, new Paint());
	        	bitmap.recycle();
        	}
        	catch(OutOfMemoryError ex)
        	{
        		Log.i(TAG, "" + ex);
        	}
        	catch(Exception ex)
        	{
        		Log.i(TAG, "" + ex);
        	}
			
			newItem = new NoteItem(bmp, x, y, type, text, (int)w, (int)h, user);
			
			newItem.lx = Float.parseFloat(item.getString("lx")); 
			newItem.ly = Float.parseFloat(item.getString("ly"));
			newItem.ow = Float.parseFloat(item.getString("ow"));
			newItem.oh = Float.parseFloat(item.getString("oh"));
	
			newItem.mZoom = Float.parseFloat(item.getString("zoom"));
			newItem.mRotation = Float.parseFloat(item.getString("rotation"));
			newItem.mLastRot = Float.parseFloat(item.getString("lastrot"));
			//newItem.state = Integer.parseInt(item.getString("state"));
	
			String[] bounds = item.getString("bound").split(";");
			newItem.bound.left = Float.parseFloat(bounds[0]);
			newItem.bound.top = Float.parseFloat(bounds[1]);
			newItem.bound.right = Float.parseFloat(bounds[2]);
			newItem.bound.bottom = Float.parseFloat(bounds[3]);
	
			newItem.alpha = Integer.parseInt(item.getString("alpha"));
			newItem.foreColor = Integer.parseInt(item.getString("foreColor"));
			newItem.backColor = Integer.parseInt(item.getString("backColor"));
			newItem.id = item.getString("id");
			
			try
			{
				newItem.linkedID = item.getString("linked");
			}
			catch(Exception ex){};
			try
			{
				newItem.linkerID = item.getString("linker");
			}
			catch(Exception ex){};
			try
			{
				newItem.setLabel(item.getString("label"), infoPaint);
			}
			catch(Exception ex){};
			
			newItem.stretch = item.getString("stretch").equals("false") ? false: true;
			newItem.frameType = Integer.parseInt(item.getString("frameType"));
			
			// enabled states
			String states = item.getString("states");
			for(int i = 0; i < states.length(); i++)
				if(states.substring(i, i).equals("0")) newItem.states[i] = 0;

			if(newItem.drawable != null) newItem.drawable.setAlpha(newItem.alpha);
			
			// text object
			newItem.textAlign = Integer.parseInt(item.getString("align"));
			newItem.font = item.getString("font");
			if(type == NoteItem.TYPE_TEXT)
			{
				TextFormat tf = new TextFormat(context, (int)newItem.ow, (int)newItem.oh, newItem.textAlign);
				tf.setPaint(newItem.font, newItem.foreColor, newItem.backColor);
				tf.frameType = newItem.frameType;
				newItem.bitmap = tf.formatText(newItem.text, !newItem.stretch);
				newItem.iw = newItem.ow;
				newItem.ih = newItem.oh;
				if(newItem.mLastRot != 0)
				{
					newItem.mRotation = newItem.mLastRot;
					newItem.applyRotate(true);
				}
				newItem.iw = newItem.bound.width();
				newItem.ih = newItem.bound.height();
				newItem.drawable = new BitmapDrawable(newItem.bitmap);
				newItem.drawable.setBounds(0, 0, (int) newItem.bound.width(), (int) newItem.bound.height());
			}
			
			// finger paint picture from undo list
			if(type == NoteItem.TYPE_PAINT)
			{
				JSONObject subItem = item.getJSONObject("paint");
				newItem.fingerPaint.lineSize = Integer.parseInt(subItem.getString("lineSize"));
				newItem.fingerPaint.color_back = Integer.parseInt(subItem.getString("color_back"));
				newItem.fingerPaint.color_line = Integer.parseInt(subItem.getString("color_line"));
				newItem.fingerPaint.frameType = newItem.frameType;
				newItem.fingerPaint.filter = Integer.parseInt(subItem.getString("filter"));
				newItem.fingerPaint.lineSize = Integer.parseInt(subItem.getString("lineSize"));
				// undo
				newItem.fingerPaint.undoStates = new ArrayList<FingerPaint.UndoState>();
				try
				{
					JSONArray undoList = subItem.getJSONArray("undo");
					for(int i = 0; i < undoList.length(); i++)
					{
						JSONObject undo = undoList.getJSONObject(i);
						int color_back = Integer.parseInt(undo.getString("color_back"));
						int color_line = Integer.parseInt(undo.getString("color_line"));
						int color_fill = Integer.parseInt(undo.getString("color_fill"));
						int filter = Integer.parseInt(undo.getString("filter"));
						int lineSize = Integer.parseInt(undo.getString("lineSize"));
						
				        Paint paint = new Paint();
				        paint.setAntiAlias(true);
				        paint.setDither(true);
				        paint.setColor(color_line);
				        paint.setStyle(Paint.Style.STROKE);
				        paint.setStrokeJoin(Paint.Join.ROUND);
				        paint.setStrokeCap(Paint.Cap.ROUND);
				        paint.setStrokeWidth(lineSize);
				        
				        // fill
				        PointF fillPnt = null;
				        try
				        {
				        	String fill = undo.getString("fillPnt");
				        	String[] point = fill.split(",");
				        	fillPnt = new PointF();
				        	fillPnt.x = Float.parseFloat(point[0]);
				        	fillPnt.y = Float.parseFloat(point[1]);
				        }
				        catch(Exception ex){};
				        
				        Path path = new Path();
				        boolean bFirst = true;
						List<PointF> pointList = null;
				        if(fillPnt == null)
				        {
				        	pointList = new ArrayList<PointF>();
							String[] points = undo.getString("path").split(";");
							for(int j = 0; j < points.length; j++)
							{
								String[] point = points[j].split(",");
								if(point.length == 2)
								{
									PointF pnt = new PointF();
									pnt.x = Float.parseFloat(point[0]);
									pnt.y = Float.parseFloat(point[1]);
									pointList.add(pnt);
									if(bFirst) path.moveTo(pnt.x, pnt.y);
									else path.lineTo(pnt.x, pnt.y);
									bFirst = false;
								}
							}
				        }
				        
				        newItem.fingerPaint.addUndoState(pointList, path, paint, fillPnt, color_back, color_line, filter, lineSize, color_fill);
					}
					
					// redraw item
					if(bmp == null)
					{
						try
						{
							bmp = Bitmap.createBitmap((int)newItem.iw, (int)newItem.ih, Bitmap.Config.ARGB_4444);
							newItem.setBitmap(bmp, false);
						}
			        	catch(OutOfMemoryError ex)
			        	{
			        		Log.i(TAG, "" + ex);
			        	}
			        	catch(Exception ex)
			        	{
			        		Log.i(TAG, "" + ex);
			        	}						
					}
					// redraw from undo list
					newItem.fingerPaint.setBitmap(bmp);
					newItem.fingerPaint.drawFrame(bmp, false);
			    }
				catch(Exception ex)
				{
					Log.i(TAG, ex.toString());
				}
			}
			
			// vector type
			if(type == NoteItem.TYPE_VECTOR)
			{
				JSONObject subItem = item.getJSONObject("vector");
				newItem.polyDraw.lineSize = Integer.parseInt(subItem.getString("lineSize"));
				newItem.polyDraw.color_fill1 = Integer.parseInt(subItem.getString("color_fill1"));
				newItem.polyDraw.color_fill2 = Integer.parseInt(subItem.getString("color_fill2"));
				newItem.polyDraw.color_line = Integer.parseInt(subItem.getString("color_line"));
				newItem.polyDraw.lineType = Integer.parseInt(subItem.getString("lineType"));
				newItem.polyDraw.lineMode = Integer.parseInt(subItem.getString("lineMode"));
				
				String[] points = subItem.getString("points").split(";");
				for(int i = 0; i < points.length; i++)
				{
					String[] point = points[i].split(",");
					PointF pnt = new PointF();
					pnt.x = Float.parseFloat(point[0]);
					pnt.y = Float.parseFloat(point[1]);
					newItem.polyDraw.points.add(pnt);
					newItem.polyDraw.addEditingStates(newItem.polyDraw.points, false, i);
				}
				
				newItem.polyDraw.setColorFill(newItem.polyDraw.color_fill1 , 1);
				newItem.polyDraw.setColorFill(newItem.polyDraw.color_fill2 , 2);
				newItem.polyDraw.setColorLine(newItem.polyDraw.color_line);
				newItem.polyDraw.setLineType(newItem.polyDraw.lineType);
				
				newItem.iw = newItem.bound.width();
				newItem.ih = newItem.bound.height();
				
				try
				{
					String base64 = subItem.getString("fillBmp");
					byte[] imageAsBytes = Base64.decode(base64.getBytes(), Base64.DEFAULT);
					bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
					//bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
					newItem.polyDraw.setFillPattern(bitmap);	
				}
				catch(Exception ex){};
			}
			
			lastItem = newItem;
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}

		return newItem;
	}
	
	// add or replace items
	public void parseArray(String jString, List<NoteItem> itemList, String user)
	{
		try
		{
			JSONObject jobj = new JSONObject(jString);
			width = Float.parseFloat(jobj.getString("width"));
			height = Float.parseFloat(jobj.getString("height"));
			backBmp = jobj.getString("background");			
			JSONArray items = jobj.getJSONArray("items");
			for(int i = 0; i < items.length(); i++)
			{
				JSONObject item = items.getJSONObject(i);
				NoteItem newItem = parseItem(item, user);
				if(!findItem(newItem.id, itemList)) itemList.add(newItem);
				else updateItem(newItem, itemList);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		setLinked(itemList);
		
		return;
	}

	// check item is exist in the list
	private boolean findItem(String id, List<NoteItem> itemList)
	{
		for(NoteItem item: itemList)
			if(item.id.equals(id)) return true;
		
		return false;
	}

	// replace item a modified one
	private void updateItem(NoteItem newItem, List<NoteItem> itemList)
	{
		for(NoteItem item: itemList)
		{
			if(item.id.equals(newItem.id))
			{
				itemList.remove(item);
				itemList.add(newItem);
				break;
			}
		}
		
		return;
	}
	
	// set linked object reference
	private void setLinked(List<NoteItem> itemList)
	{
		for(NoteItem item: itemList)
		{
			if(!item.linkedID.equals(""))
			{
				for(NoteItem note: itemList)
				{
					if(note.id.equals(item.linkedID))
					{
						item.setLinked(note);
						break;
					}
				}
			}
		}
		
		return;
	}
	
	// check item before add to item list
	private boolean checkItem(NoteItem item)
	{
		if(item.type == NoteItem.TYPE_BITMAP && item.bitmap == null) return false;
		if(item.type == NoteItem.TYPE_PAINT && item.fingerPaint == null) return false;
		if(item.type == NoteItem.TYPE_TEXT && item.text.equals("")) return false;
		if(item.type == NoteItem.TYPE_VECTOR && item.polyDraw == null) return false;
		
		return true;
	}
}
