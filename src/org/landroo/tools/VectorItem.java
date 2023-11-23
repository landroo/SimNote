package org.landroo.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONObject;
import org.landroo.simnote.NoteItem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Base64;

public class VectorItem
{
	public static final int POLYGON = 1;
	public static final int POLYLINE = 2;

	public ArrayList<PointF> points = new ArrayList<PointF>();
	
	public float width = 80;
	public float height = 80;
	
	public int color_fill1 = NoteItem.COLOR_GREEN;
	public int color_fill2 = NoteItem.COLOR_BLUE;
	public int color_line = NoteItem.COLOR_GREEN;
	
	public int lineSize = 5;
	public int lineType = 0;
	public int lineMode = 1;
	
	public String name = "";
	
	public Bitmap bitmap;
	public Bitmap fillBmp;
	
	private PathEffect[] mEffects = new PathEffect[6];
	
	// constructor
	public VectorItem()
	{
		makeEffects(5);
	}
	
	// constructor
	public VectorItem(String fileName, int maxSize)
	{
		makeEffects(5);
		
		loadItem(fileName, maxSize);
	}
	
	// line effects
	private void makeEffects(float phase) 
	{
		mEffects[0] = null;     // no effect
		mEffects[1] = new CornerPathEffect(10);
		mEffects[2] = new DashPathEffect(new float[] {10, 5, 5, 5}, phase);
		mEffects[3] = new PathDashPathEffect(NoteItem.makePathPattern(lineSize * 4, lineSize * 2), lineSize * 4, phase, PathDashPathEffect.Style.ROTATE);
		mEffects[4] = new ComposePathEffect(mEffects[2], mEffects[1]);
		mEffects[5] = new ComposePathEffect(mEffects[3], mEffects[1]);
    }
	
	// load a vector file and make a thumb nail
	private void loadItem(String fileName, int maxSize)
	{
		String json = loadFile(fileName);
		parseFile(json);
		RectF rect = calculateRect();
		width = (int)rect.width();
		height = (int)rect.height();
		
		float rat = maxSize / width;
		width = width * rat;
		height = height * rat;

		if(points.size() > 1)
		{
			bitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_4444);
			Canvas canvas = new Canvas(bitmap);
			
			Paint linePaint = new Paint();
			linePaint = new Paint();
			linePaint.setColor(color_line);
			linePaint.setStyle(Paint.Style.STROKE);
			linePaint.setStrokeWidth(lineSize);
			linePaint.setPathEffect(mEffects[lineType]);
			
			int[] colors = new int[2];
			colors[0] = color_fill1;
			colors[1] = color_fill2;
			
			LinearGradient gradient = new LinearGradient(0, 0, 0, rect.height(), colors, null, android.graphics.Shader.TileMode.CLAMP);
			
			Paint polyPaint = new Paint();
			polyPaint = new Paint();
			polyPaint.setStyle(Paint.Style.FILL);
			polyPaint.setShader(gradient);
			if(fillBmp != null) 
			{
				BitmapShader fillBMPshader = new BitmapShader(fillBmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
				polyPaint.setShader(fillBMPshader);
			}
			
			Path multipath = new Path();
			multipath.moveTo(points.get(0).x * rat, points.get(0).y * rat);
			for(int i = 1; i < points.size(); i++) 
				multipath.lineTo(points.get(i).x * rat, points.get(i).y * rat);
			
			if(lineMode == POLYLINE)
				canvas.drawPath(multipath, linePaint);
			else
			{
				multipath.lineTo(points.get(0).x * rat, points.get(0).y * rat);
				canvas.drawPath(multipath, polyPaint);
				canvas.drawPath(multipath, linePaint);
			}
		}
	}
	
	// load a file
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
	
	// json parser
	private void parseFile(String json)
	{
		String color;
		try
		{
			JSONObject jobj = new JSONObject(json);
			name = jobj.getString("name");
			color = jobj.getString("color_fill1");
			if(color.substring(0, 1).equals("#")) color_fill1 = (int)Long.parseLong(color.substring(1), 16);
			else color_fill1 = Integer.parseInt(color);
			color = jobj.getString("color_fill2");
			if(color.substring(0, 1).equals("#")) color_fill2 = (int)Long.parseLong(color.substring(1), 16);
			else color_fill2 = Integer.parseInt(color);
			color = jobj.getString("color_line");
			if(color.substring(0, 1).equals("#")) color_line = (int)Long.parseLong(color.substring(1), 16);
			else color_line = Integer.parseInt(color);
			lineMode = Integer.parseInt(jobj.getString("lineMode"));
			lineSize = Integer.parseInt(jobj.getString("lineSize"));
			lineType = Integer.parseInt(jobj.getString("lineType"));
			
			String[] points = jobj.getString("points").split(";");
			for(int i = 0; i < points.length; i++)
			{
				String[] point = points[i].split(",");
				PointF pnt = new PointF();
				pnt.x = Float.parseFloat(point[0]);
				pnt.y = Float.parseFloat(point[1]);
				this.points.add(pnt);
			}
			
			try
			{
				String base64 = jobj.getString("fillBmp");
				byte[] imageAsBytes = Base64.decode(base64.getBytes(), Base64.DEFAULT);
				fillBmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
			}
			catch(Exception ex){};

		}
		catch (Exception e)
		{
		}
	}
	
	// calculate bounding rectangle
	public RectF calculateRect()
	{
		RectF rect = new RectF();
		
		float f = Float.MAX_VALUE;
		for(PointF pnt: points)
			if(f > pnt.x) f = pnt.x;
		rect.left = f;
		
		f = 0;
		for(PointF pnt: points)
			if(f < pnt.x) f = pnt.x;
		rect.right = f;
		
		f = Float.MAX_VALUE;
		for(PointF pnt: points)
			if(f > pnt.y) f = pnt.y;
		rect.top = f;

		f = 0;
		for(PointF pnt: points)
			if(f < pnt.y) f = pnt.y;
		rect.bottom = f;
		
		return rect;
	}
	
	// create vector item form the selected
	public PolyDraw loadPolygon(String fileName)
	{
		String json = loadFile(fileName);
		parseFile(json);
		
		return createProlyDraw();
	}
	
	private PolyDraw createProlyDraw()
	{
		PolyDraw polyDraw = new PolyDraw(5, fillBmp);
		polyDraw.lineSize = lineSize;
		polyDraw.color_fill1 = color_fill1;
		polyDraw.color_fill2 = color_fill2;
		polyDraw.color_line = color_line;
		polyDraw.lineMode = lineMode;
		polyDraw.lineType = lineType;
		
		int i = 0;
		for(PointF pnt:points)
		{
			polyDraw.points.add(pnt);
			polyDraw.addEditingStates(polyDraw.points, false, i++);
		}		
		
		return polyDraw;
	}
	
	// draw a circle
	public PolyDraw addCircle(int size, int pnts)
	{
		float x, y;
		if(pnts < 3) pnts = 3;
		float s = (float)Math.PI * 2 / pnts;
		for(float i = 0; i < Math.PI * 2; i += s)
		{
			x = (float)Math.sin(i) * size;
			y = (float)Math.cos(i) * size;
			
			points.add(new PointF(x, y));
		}
		
		return createProlyDraw();
	}
	
	public PolyDraw addSquare(int size)
	{
		int pnts = 5;
		float x, y;
		float ox = (size / 4) * 3;
		float oy = (size / 4) * 3;
		float s = (float)Math.PI / 2 / pnts;
		for(float i = 0; i < Math.PI / 2; i += s)
		{
			x = ox + (float)Math.sin(i) * (size / 4);
			y = oy + (float)Math.cos(i) * (size / 4);
			
			points.add(new PointF(x, y));
		}
		
		ox = (size / 4) * 3;
		oy = (size / 4);
		for(float i = (float)Math.PI / 2; i < Math.PI; i += s)
		{
			x = ox + (float)Math.sin(i) * (size / 4);
			y = oy + (float)Math.cos(i) * (size / 4);
			
			points.add(new PointF(x, y));
		}
		
		ox = (size / 4);
		oy = (size / 4);
		for(float i = (float)Math.PI; i < Math.PI + Math.PI / 2; i += s)
		{
			x = ox + (float)Math.sin(i) * (size / 4);
			y = oy + (float)Math.cos(i) * (size / 4);
			
			points.add(new PointF(x, y));
		}

		ox = (size / 4);
		oy = (size / 4) * 3;
		for(float i = (float)(Math.PI + Math.PI / 2); i < Math.PI * 2; i += s)
		{
			x = ox + (float)Math.sin(i) * (size / 4);
			y = oy + (float)Math.cos(i) * (size / 4);
			
			points.add(new PointF(x, y));
		}

		return createProlyDraw();
	}
	
	// draw an arrow
	public PolyDraw addArrow(int size)
	{
		lineMode = POLYLINE;
		float x, y;

		x = size - size / 10;
		y = size / 5;
		points.add(new PointF(x, y));

		x = size;
		y = size / 10;
		points.add(new PointF(x, y));
		
		x = size - size / 10;
		y = 0;
		points.add(new PointF(x, y));
		
		x = size;
		y = size / 10;
		points.add(new PointF(x, y));
		
		x = 0;
		y = size / 10;
		points.add(new PointF(x, y));

		return createProlyDraw();
	}
	
	// add capsule
	public PolyDraw addCapsule(int size)
	{
		int pnts = 5;
		float x, y;
		float ox = (size / 4) * 3;
		float oy = (size / 4);
		float s = (float)Math.PI / 2 / pnts;
		for(float i = 0; i < Math.PI; i += s)
		{
			x = ox + (float)Math.sin(i) * (size / 4);
			y = oy + (float)Math.cos(i) * (size / 4);
			
			points.add(new PointF(x, y));
		}
		
		ox = (size / 4);
		oy = (size / 4);
		for(float i = (float)Math.PI; i < Math.PI * 2; i += s)
		{
			x = ox + (float)Math.sin(i) * (size / 4);
			y = oy + (float)Math.cos(i) * (size / 4);
			
			points.add(new PointF(x, y));
		}

		return createProlyDraw();
	}

}
