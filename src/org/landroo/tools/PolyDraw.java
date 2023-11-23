package org.landroo.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.landroo.simnote.NoteItem;
import org.landroo.simnote.R;
import org.landroo.simnote.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.util.Log;

public class PolyDraw
{
	private static final String TAG = "PolyDraw";
	public static final int TOLERANCE = 40;
	public static final int POLYGON = 1;
	public static final int POLYLINE = 2;

	// vector
	public ArrayList<PointF> points = new ArrayList<PointF>();
	public ArrayList<PointF> tmppnt = new ArrayList<PointF>();
	public ArrayList<PointF> midPoints = new ArrayList<PointF>();
	private ArrayList<EditingStates> editStates = new ArrayList<EditingStates>();
	
	public boolean midpointSelected = false;
	public boolean vertexSelected = false;
	private int insertIndex;
	
	public float ox = 1;
	public float oy = 1;
	
	public int lineMode = POLYGON;
	
	public int lineSize = 5;
	private int dotSize = 10;
	
	private Paint linePaint;
	private Paint polyPaint;
	
	public int color_fill1 = NoteItem.COLOR_GREEN;
	public int color_fill2 = NoteItem.COLOR_BLUE;
	public int color_line = 0xFF000000;
	
	public int lineType = 0;// 0 line, 1 smooted, 2 dashed, 3 border,
	
	public float lastX, lastY;
	
	private PathEffect[] mEffects = new PathEffect[6];
	
	public Bitmap fillBmp;
	
	public PolyDraw(int size, Bitmap bitmap)
	{
		lineSize = size;
		
		makeEffects(5);
		
		linePaint = new Paint();
		linePaint.setColor(color_line);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(lineSize);
		linePaint.setPathEffect(mEffects[lineType]);
		
		int[] colors = new int[2];
		colors[0] = color_fill1;
		colors[1] = color_fill2;

		LinearGradient gradient = new LinearGradient(0, 0, 0, 10, colors, null, android.graphics.Shader.TileMode.CLAMP);
		
		polyPaint = new Paint();
		polyPaint.setStyle(Paint.Style.FILL);
		polyPaint.setShader(gradient);
	
		fillBmp = bitmap;
		if(fillBmp != null)
		{
			BitmapShader fillBMPshader = new BitmapShader(fillBmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
			polyPaint.setShader(fillBMPshader);
			int a = (color_fill1 >> 24) & 0xFF;
			polyPaint.setAlpha(a);
		}
	}
	
	public void setFillPattern(Bitmap bitmap)
	{
		fillBmp = bitmap;
		BitmapShader fillBMPshader = new BitmapShader(fillBmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		polyPaint.setShader(fillBMPshader);
		int a = (color_fill1 >> 24) & 0xFF;
		polyPaint.setAlpha(a);
	}
	
	public void setColorFill(int color, int type)
	{
		if(type == 1) color_fill1 = color;
		else  color_fill2 = color;
		
		int[] colors = new int[2];
		colors[0] = color_fill1;
		colors[1] = color_fill2;
		
		RectF rect = calculateRect();
		
		polyPaint.setShader(null);
		if(fillBmp != null) fillBmp.recycle();
		fillBmp = null;

		LinearGradient gradient = new LinearGradient(0, 0, 0, rect.height(), colors, null, android.graphics.Shader.TileMode.CLAMP);
		polyPaint.setShader(gradient);
	}
	
	public void setColorLine(int color)
	{
		color_line = color;
		linePaint.setColor(color_line);
	}
	
	public void setLineSize(int size)
	{
		lineSize = size;
		linePaint.setStrokeWidth(lineSize);
	}
	
	public void setLineType(int type)
	{
		if(type > 5) type = 0;
		lineType = type;
		linePaint.setPathEffect(mEffects[lineType]);
	}

	private void makeEffects(float phase) 
	{
		mEffects[0] = null;     // no effect
		mEffects[1] = new CornerPathEffect(10);
		mEffects[2] = new DashPathEffect(new float[] {10, 5, 5, 5}, phase);
		mEffects[3] = new PathDashPathEffect(NoteItem.makePathPattern(lineSize * 4, lineSize * 2), lineSize * 4, phase, PathDashPathEffect.Style.ROTATE);
		mEffects[4] = new ComposePathEffect(mEffects[2], mEffects[1]);
		mEffects[5] = new ComposePathEffect(mEffects[3], mEffects[1]);
    }
	
	// draw polygon or polyline
	public void drawPolygon(Canvas canvas, float zx, float zy) 
	{
		if(points.size() <= 1) return;
		
		Path multipath = new Path();
		if(tmppnt.size() > 0)
		{
			multipath.moveTo(tmppnt.get(0).x * zx, tmppnt.get(0).y * zy);
			for(int i = 1; i < tmppnt.size(); i++) 
				multipath.lineTo(tmppnt.get(i).x * zx, tmppnt.get(i).y * zy);
			if(lineMode == POLYGON) multipath.lineTo(tmppnt.get(0).x * zx, tmppnt.get(0).y * zy);
		}
		else
		{
			multipath.moveTo(points.get(0).x * zx, points.get(0).y * zy);
			for(int i = 1; i < points.size(); i++) 
				multipath.lineTo(points.get(i).x * zx, points.get(i).y * zy);
			if(lineMode == POLYGON) multipath.lineTo(points.get(0).x * zx, points.get(0).y * zy);
		}
		
		if(lineMode == POLYGON) canvas.drawPath(multipath, polyPaint);
		
		canvas.drawPath(multipath, linePaint);
	}
	
	// draw mid point of the lines
	public void drawMidPoints(Canvas canvas, float zx, float zy) 
	{
		int index;

		// draw mid-point
		if(points.size() > 1) 
		{
			midPoints.clear();
			for(int i = 1; i < points.size(); i++) 
			{
				PointF p1 = points.get(i - 1);
				PointF p2 = points.get(i);
				midPoints.add(new PointF((p1.x * zx + p2.x * zx) / 2, (p1.y * zy + p2.y * zy) / 2));
			}
			
			// complete the circle
			if(lineMode == POLYGON) 
			{ 
				PointF p1 = points.get(0);
				PointF p2 = points.get(points.size() - 1);
				midPoints.add(new PointF((p1.x * zx + p2.x * zx) / 2, (p1.y * zy + p2.y * zy) / 2));
			}
			
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);
			
			RectF rect = new RectF(0, 0, dotSize * 2, dotSize * 2);
			
			index = 0;
			for(PointF pnt: midPoints) 
			{
				rect.left = pnt.x - dotSize;
				rect.top = pnt.y - dotSize;
				rect.right = pnt.x + dotSize;
				rect.bottom = pnt.y + dotSize;
				if(midpointSelected && insertIndex == index) paint.setColor(Color.RED);
				else paint.setColor(Color.GREEN);
				
				canvas.drawRect(rect, paint);
				index++;
			}
		}
	}
	
	// draw points
	public void drawVertices(Canvas canvas, float zx, float zy) 
	{
		int index = 0;
		
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		
		for(PointF pnt: points) 
		{
			if(vertexSelected && index == insertIndex) paint.setColor(Color.RED);
			else if(index == points.size() - 1) paint.setColor(Color.BLUE);			
			else paint.setColor(Color.BLACK);
			
			canvas.drawCircle(pnt.x * zx, pnt.y * zy, dotSize, paint);
			index++;
		}
	}

	// clear
	public void clear() 
	{
		points.clear();
		midPoints.clear();
		midpointSelected = false;
		vertexSelected = false;
		insertIndex = 0;
		
		editStates.clear();
	}
	
	public void addEditingStates(ArrayList<PointF> points, boolean midpointselected, int insertingindex)
	{
		editStates.add(new EditingStates(points, midpointselected, insertingindex));
	}

	// add a new point or select an existing or a midpoint
	public void onTouch(float x, float y)
	{
		lastX = x; 
		lastY = y;
		
		if(!midpointSelected && !vertexSelected) 
		{ 
			// check if user tries to select an existing point.
			int idx1 = getSelectedIndex(x, y, midPoints);
			if(idx1 != -1) 
			{
				midpointSelected = true;
				insertIndex = idx1;
			}

			if(midpointSelected == false) 
			{ 
				// check vertices
				int idx2 = getSelectedIndex(x, y, points);
				if(idx2 != -1) 
				{
					vertexSelected = true;
					insertIndex = idx2;
				}
				
				if(vertexSelected == false) 
				{ 
					//Log.i(TAG, "no match, add new vertex at the location " + point.x + " " + point.y);
					points.add(new PointF(x, y));
					editStates.add(new EditingStates(points, midpointSelected, insertIndex));
				}
			}
		}
		
		return;
	}
	
	// move corner point
	public void movePoint(float x, float y) 
	{
		lastX = x; 
		lastY = y;
		
		PointF point = new PointF(x, y);
		
		if(midpointSelected) 
		{
			points.add(insertIndex + 1, point);
			midpointSelected = false;
			vertexSelected = true;
			insertIndex++;
		} 
		else if(vertexSelected) 
		{
			ArrayList<PointF> temp = new ArrayList<PointF>();
			for(int i = 0; i < points.size(); i++) 
			{
				if(i == insertIndex) temp.add(point);
				else temp.add(points.get(i));
			}
			points.clear();
			points.addAll(temp);
		}
	}
	
	// finish drawing
	public void onUp()
	{
		if(midpointSelected) midpointSelected = false;
		if(vertexSelected) vertexSelected = false;
		editStates.add(new EditingStates(points, midpointSelected, insertIndex));
	}
	
	// check point selected
	private int getSelectedIndex(double x, double y, ArrayList<PointF> points1) 
	{
		if(points1 == null || points1.size() == 0) return -1;
		
		int index = -1;
		double distSQ_Small = Double.MAX_VALUE;
		for(int i = 0; i < points1.size(); i++) 
		{
			PointF p = points1.get(i);
			double diffx = p.x - x;
			double diffy = p.y - y;
			double distSQ = diffx * diffx + diffy * diffy;
			if(distSQ < distSQ_Small) 
			{
				index = i;
				distSQ_Small = distSQ;
			}
		}

		if(distSQ_Small < (TOLERANCE * TOLERANCE)) return index;
		
		return -1;
	}
	
	// undo last action
	public void undoLast()
	{
		if(editStates.size() > 0)
		{
			editStates.remove(editStates.size() - 1);
			points.clear();
			if(editStates.size() > 0)
			{
				EditingStates state = editStates.get(editStates.size() - 1);
				points.addAll(state.points);
				midpointSelected = state.midpointSelected;
				insertIndex = state.insertIndex;
				//Log.d(TAG, "# of points = " + points.size());
			}
		}
	}
	
	// delete a point
	public void deletePoint()
	{
		if(points.size() > 0)
		{
			// remove last vertex
			if(!vertexSelected) points.remove(points.size() - 1); 
			else points.remove(insertIndex);
			
			midpointSelected = false;
			vertexSelected = false;
			editStates.add(new EditingStates(points, midpointSelected, insertIndex));
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
		
		//Log.i(TAG, "" + rect.left + " " + rect.top + " " + rect.right + " " + rect.bottom);
		
		return rect;
	}
	
	// shift polygon
	public void relocate(float x, float y)
	{
		for(PointF pnt: points)
			pnt.set(pnt.x + x, pnt.y + y);
	}
	
	// apply rotation
	public void applyRotate(float u, float v, float ang)
	{
		for(PointF pnt: points)
			pnt.set(Utils.rotatiePnt(u, v, pnt.x, pnt.y, ang));
	}
	
	// apply new scale
	public void applyScale(float zoom)
	{
		points.clear();
		points.addAll(tmppnt);
		tmppnt.clear();
	}
	
	// real time scale
	public void Scale(float zoom)
	{
		tmppnt.clear();
		for(PointF pnt: points)
			tmppnt.add(new PointF(pnt.x * zoom, pnt.y * zoom));
	}
	
	// apply new size
	public void applySize(float xRat, float yRat)
	{
		points.clear();
		points.addAll(tmppnt);
		tmppnt.clear();
	}
	
	// realtime resize
	public void ReSize(float xRat, float yRat)
	{
		tmppnt.clear();
		for(PointF pnt: points)
			tmppnt.add(new PointF(pnt.x * xRat, pnt.y * yRat));
	}
	
	// states class
	public class EditingStates 
	{
		public ArrayList<PointF> points = new ArrayList<PointF>();
		public boolean midpointSelected = false;
		public int insertIndex;

		public EditingStates(ArrayList<PointF> points, boolean midpointselected, int insertingindex) 
		{
			this.points.addAll(points);
			this.midpointSelected = midpointselected;
			this.insertIndex = insertingindex;
		}
		
		public EditingStates copy()
		{
			EditingStates edt = new EditingStates(points, midpointSelected, insertIndex);
			
			return edt;
		}
	}

	// copy myself
	public PolyDraw copy()
	{
		PolyDraw polyDraw;
		if(this.fillBmp != null) polyDraw = new PolyDraw(this.dotSize, Bitmap.createBitmap(this.fillBmp));
		else polyDraw = new PolyDraw(this.dotSize, null);
		
		for(PointF pnt: points) polyDraw.points.add(new PointF(pnt.x, pnt.y));
		for(PointF pnt: midPoints) polyDraw.midPoints.add(new PointF(pnt.x, pnt.y));
		for(EditingStates edts: editStates) polyDraw.editStates.add(edts.copy());
		polyDraw.color_fill1 = this.color_fill1;
		polyDraw.color_fill2 = this.color_fill2;
		polyDraw.color_line = this.color_line;
		polyDraw.lineMode = this.lineMode;
		polyDraw.lineSize = this.lineSize;
		polyDraw.insertIndex = this.insertIndex;
		
		if(fillBmp != null)
		{
			BitmapShader fillBMPshader = new BitmapShader(polyDraw.fillBmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
			polyDraw.polyPaint.setShader(fillBMPshader);
			int a = (polyDraw.color_fill1 >> 24) & 0xFF;
			polyDraw.polyPaint.setAlpha(a);
		}
		
		polyDraw.linePaint.setColor(polyDraw.color_line);
		polyDraw.linePaint.setStrokeWidth(polyDraw.lineSize);
		
		return polyDraw;
	}

	// save item to json
	public String saveItem()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\"vector\":{");
		
		sb.append("\"lineSize\":\"");
		sb.append(lineSize);
		sb.append("\",\"color_fill1\":\"");
		sb.append(color_fill1);
		sb.append("\",\"color_fill2\":\"");
		sb.append(color_fill2);
		sb.append("\",\"color_line\":\"");
		sb.append(color_line);
		sb.append("\",\"lineMode\":\"");
		sb.append(lineMode);
		sb.append("\",\"lineType\":\"");
		sb.append(lineType);
		if(fillBmp != null)
		{
			sb.append("\",\"fillBmp\":\"");
			sb.append(Utils.bitmapToBase64(fillBmp));
		}
		sb.append("\",\"points\":\"");
		for(PointF pnt: points)
		{
			sb.append(pnt.x);
			sb.append(",");
			sb.append(pnt.y);
			sb.append(";");
		}
		sb.append("\"");
		sb.append("}");
		
		return sb.toString();
	}
	
	// log for debug
	private void logPoints(boolean tmp)
	{
		if(tmp)
			for(PointF pnt: tmppnt)
				Log.i(TAG, "x: " + pnt.x + " y: " + pnt.y); 
		else
			for(PointF pnt: points)
				Log.i(TAG, "x: " + pnt.x + " y: " + pnt.y); 
	}
	
	public void saveMe(File file, String name)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("{\n");
		sb.append("\t\"name\":\"");
		if(name.equals("")) sb.append(file.getName().substring(0, file.getName().lastIndexOf(".")));
		else sb.append(name);
		sb.append("\",\n");
		sb.append("\t\"lineSize\":\"");
		sb.append(lineSize);
		sb.append("\",\n");
		sb.append("\t\"color_fill1\":\"#");
		sb.append(String.format("%x", color_fill1).toUpperCase());
		sb.append("\",\n");
		sb.append("\t\"color_fill2\":\"#");
		sb.append(String.format("%x", color_fill2).toUpperCase());
		sb.append("\",\n");
		sb.append("\t\"color_line\":\"#");
		sb.append(String.format("%x", color_line).toUpperCase());
		sb.append("\",\n");
		sb.append("\t\"lineMode\":\"");
		sb.append(lineMode);
		sb.append("\",\n");
		sb.append("\t\"lineType\":\"");
		sb.append(lineType);
		sb.append("\",\n");
		if(fillBmp != null)
		{
			sb.append("\t\"fillBmp\":\"");
			sb.append(Utils.bitmapToBase64(fillBmp));
			sb.append("\",\n");
		}
		sb.append("\t\"points\":\"");
		for(PointF pnt: points)
		{
			sb.append(pnt.x);
			sb.append(",");
			sb.append(pnt.y);
			sb.append(";");
		}
		sb.append("\"\n");
		sb.append("}\n");
		
		FileOutputStream out;
		try
		{
			out = new FileOutputStream(file);
			out.write(sb.toString().getBytes());
			out.flush();
			out.close();
		}
		catch (Exception ex)
		{
			Log.i(TAG, "" + ex);
		}
	}
	
	// select the polygon or line 
	public boolean isInside(float x, float y, float zx, float zy, float ox, float oy)
	{
		if(points.size() < 3) lineMode = PolyDraw.POLYLINE;
		
		boolean bIn = false;
		if(lineMode == POLYGON)
		{
			float[] px = new float[points.size()];
			float[] py = new float[points.size()];
			int i = 0;
			for(PointF pnt: points)
			{
				px[i] = (pnt.x + ox) * zx;
				py[i] = (pnt.y + oy) * zy;
				i++;
			}
			
			bIn = Utils.ponitInPoly(points.size(), px, py, x, y);
		}
		else
		{
			PointF pnt = new PointF(x, y);
			PointF A = new PointF();
			PointF B = new PointF();
			Float dist;
			for(int i = 0; i < points.size() - 1; i++)
			{
				A.x = (points.get(i).x + ox) * zx;
				A.y = (points.get(i).y + oy) * zy;
				B.x = (points.get(i + 1).x + ox) * zx;
				B.y = (points.get(i + 1).y + oy) * zy;
				dist = Utils.distToPoint(A, B, pnt);
				//Log.i(TAG, "" + dist);
				if(dist < TOLERANCE / 2)
				{
					bIn = true;
					break;
				}
			}
		}
		
		return bIn;
	}
}
