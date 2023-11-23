package org.landroo.tools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.landroo.simnote.NoteItem;
import org.landroo.simnote.Utils;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.Log;
import android.widget.Toast;

public class FingerPaint
{
	private static final String TAG = "FingerPaint";
	public static final float TOUCH_TOLERANCE = 4;
	
	private Paint mPaint;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    
    public Bitmap mBitmap = null;
    private Canvas mCanvas;
    private Path mPath;
    public List<UndoState> undoStates = new ArrayList<UndoState>();
    private List<PointF> points = new ArrayList<PointF>();
    
    private float mX;
    private float mY;
    
    public boolean floodFill = false;
    
    public int lineSize = 12;
	public int color_back = 0x00FFFFFF;
	public int color_line = NoteItem.COLOR_YELLOW;
	public int filter = 0;
	
	public float lastX, lastY;
	
	public int frameType = 0;
    
	// constructor
    public FingerPaint()
    {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(color_line);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(lineSize);
        
        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);

        mPath = new Path();
    }
    
    public void setBitmap(Bitmap bitmap)
    {
    	this.mBitmap = bitmap;
    	mBitmap.eraseColor(color_back);
        mCanvas = new Canvas(mBitmap);
    }
    
    // set back color
	public void setColorBack(int color)
	{
		color_back = color;
		redraw(mBitmap);
	}
	
	//set line color
	public void setColorLine(int color)
	{
		color_line = color;
		mPaint.setColor(color_line);
	}
	
	// set stoke style
	public void setLineSize(int size)
	{
		lineSize = size;
		mPaint.setStrokeWidth(lineSize);
	}
    
	// draw to canvas
    public void Draw(Canvas canvas) 
    {
        mCanvas.drawPath(mPath, mPaint);
    }
    
    // start a new line
    public void touch_start(float x, float y) 
    {
    	lastX = x;
    	lastY = y;
    	if(floodFill)
    	{
    		Point node = new Point((int)x, (int)y);
    		int color_fill = mBitmap.getPixel((int)x, (int)y);
    		floodFill(mBitmap, node, color_fill, color_line);
    		PointF pnt = new PointF(x, y);
    		addUndoState(null, null, mPaint, pnt, color_back, color_line, filter, lineSize, color_fill);
    	}
    	else
    	{
    		points.clear();
	        mPath.reset();
	        mPath.moveTo(x, y);
	        mX = x;
	        mY = y;
	        if(isInsied(mX, mY)) points.add(new PointF(mX, mY));
    	}
    }
    
    // draw line
    public void touch_move(float x, float y) 
    {
    	lastX = x;
    	lastY = y;
    	if(floodFill == false)
    	{
	        float dx = Math.abs(x - mX);
	        float dy = Math.abs(y - mY);
	        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) 
	        {
	            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
	            mX = x;
	            mY = y;
	            if(isInsied(mX, mY)) points.add(new PointF(mX, mY));
	        }
    	}
    }
    
    // finish line
    public void touch_up() 
    {
    	if(floodFill)
    	{
    		floodFill = false;
    	}
    	else
    	{
	    	if(points.size() > 1)
	    	{
	    		mPath.lineTo(mX, mY);
	    	}
	    	else
	    	{
	    		mCanvas.drawCircle(lastX, lastY, lineSize / 4, mPaint);
	    	}
	    	addUndoState(points, mPath, mPaint, null, color_back, color_line, filter, lineSize, 0);
			mPath.reset();
    	}
    }
    
    // set paint style
    public void setPropeties(int id)
    {
    	if(filter == id)
    	{
        	mPaint.setMaskFilter(null);
        	mPaint.setXfermode(null);
    	}
    	else
    	{
	    	filter = id;
	        switch (id) 
	        {
	        case 0:
	        	mPaint.setMaskFilter(null);
	        	mPaint.setXfermode(null);
	        	break;
	        case 1: // emboss effect
	            if (mPaint.getMaskFilter() != mEmboss) mPaint.setMaskFilter(mEmboss);
	            else mPaint.setMaskFilter(null);
	            break;
	        case 2: // blur effect
	            if (mPaint.getMaskFilter() != mBlur) mPaint.setMaskFilter(mBlur);
	            else mPaint.setMaskFilter(null);
	            break;
	        case 3: // erase
	            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
	            break;
	        case 4: // soft color
	            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
	            mPaint.setAlpha(0x80);
	            break;
	        }
    	}
    }
	
    // redraw all line except last 
	public void undo()
	{
		Log.i(TAG, "undo: " + undoStates.size());
		if(undoStates.size() > 0)
		{
			UndoState us = undoStates.get(undoStates.size() - 1);
			undoStates.remove(us);
		}

		redraw(mBitmap);
	}
	
	// redraw all line
	public void redraw(Bitmap bmp)
	{
		Canvas canvas = new Canvas(bmp);
		bmp.eraseColor(color_back);
		for(UndoState us: undoStates)
		{
			if(us.fillPnt != null)
			{
				Point pnt = new Point((int)us.fillPnt.x, (int)us.fillPnt.y);
				floodFill(bmp, pnt, us.color_fill, us.color_line);
			}
			else if(us.points != null)
			{
		    	if(us.points.size() > 1)
		    		canvas.drawPath(us.path, us.paint);
		    	else if(us.points.size() == 1)
		    		canvas.drawCircle(us.points.get(0).x, us.points.get(0).y, us.lineSize / 4, us.paint);
			}
			this.mPaint = us.paint;
		}
		
		return;
	}
	
	// draw frame
	public void drawFrame(Bitmap bitmap, boolean nextFrame)
	{
		redraw(bitmap);
		
		RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
		
		int stroke = bitmap.getWidth() / 32;
		if(stroke < 1) stroke = 1;
		
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        if(((color_back >> 24) & 0xFF) > 0) paint.setColor(color_back);
        else paint.setColor(color_line);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(stroke);
		
		Canvas canvas = new Canvas(bitmap);

		if(nextFrame) frameType++;
		if(frameType > 6) frameType = 0;
		
		float cen = bitmap.getWidth() / 10;
		if(rect.height() < rect.width()) cen = rect.height() / 8;
		else cen = rect.width() / 8;
		switch(frameType)
		{
		case 0:// no frame
			paint.setPathEffect(null);
			break;
		case 1:// rect frame
			canvas.drawRect(rect, paint);
			break;
		case 2:// rect frame dashed
			paint.setPathEffect(new DashPathEffect(new float[] {50, 40}, 5));
			canvas.drawRect(rect, paint);
			break;
		case 3:// pipe
			paint.setPathEffect(new PathDashPathEffect(makePathDash(), 12, 5, PathDashPathEffect.Style.MORPH));
			canvas.drawRect(rect, paint);
			break;
		case 4:// rounded frame
			canvas.drawRoundRect(rect, cen, cen, paint);
			break;
		case 5:// rounded frame dashed
			paint.setPathEffect(new DashPathEffect(new float[] {50, 40}, 5));
			canvas.drawRoundRect(rect, cen, cen, paint);
			break;
		case 6:// rounded frame dashed			
			paint.setPathEffect(new PathDashPathEffect(makePathDash(), 12, 5, PathDashPathEffect.Style.MORPH));
			canvas.drawRoundRect(rect, cen, cen, paint);
			break;
		}
		//Log.i(TAG, "frame: " + frameType);
		
		//setPropeties(filter);
		//mPaint.setPathEffect(null);
		
		return;
	}
	
	// path effect
	private static Path makePathDash() 
	{
        Path p = new Path();
        p.moveTo(-6, 4);
        p.lineTo(6,4);
        p.lineTo(6,3);
        p.lineTo(-6, 3);
        p.close();
        p.moveTo(-6, -4);
        p.lineTo(6,-4);
        p.lineTo(6,-3);
        p.lineTo(-6, -3);
        return p;
    }
	
	// flood fill
	public void floodFill(Bitmap bitmap, Point node, int oldColor, int newColor)
	{
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		
		if (oldColor != newColor)
		{
			Queue<Point> queue = new LinkedList<Point>();
			do
			{
				int x = node.x;
				int y = node.y;
				while (x > 0 && bitmap.getPixel(x - 1, y) == oldColor)
				{
					x--;
				}
				boolean spanUp = false;
				boolean spanDown = false;
				
				while (x < width && bitmap.getPixel(x, y) == oldColor)
				{
					bitmap.setPixel(x, y, newColor);
					if (!spanUp && y > 0 && bitmap.getPixel(x, y - 1) == oldColor)
					{
						queue.add(new Point(x, y - 1));
						spanUp = true;
					}
					else if (spanUp && y > 0 && bitmap.getPixel(x, y - 1) != oldColor)
					{
						spanUp = false;
					}
					if (!spanDown && y < height - 1 && bitmap.getPixel(x, y + 1) == oldColor)
					{
						queue.add(new Point(x, y + 1));
						spanDown = true;
					}
					else if (spanDown && y < height - 1 && bitmap.getPixel(x, y + 1) != oldColor)
					{
						spanDown = false;
					}
					x++;
				}
			}
			while ((node = queue.poll()) != null);
		}
		
		return;
	}

	// serialisation item to json  
	public String saveItem()
	{
		boolean bFirst = true;
		StringBuilder sb = new StringBuilder();
		sb.append("\"paint\":{");
		
		sb.append("\"lineSize\":\"");
		sb.append(lineSize);
		sb.append("\",\"color_back\":\"");
		sb.append(color_back);
		sb.append("\",\"color_line\":\"");
		sb.append(color_line);
		sb.append("\",\"filter\":\"");
		sb.append(filter);
		sb.append("\"");		
		if(undoStates.size() > 0)
		{
			sb.append(",\"undo\":[{");
			for(UndoState us: undoStates)
			{
				if(!bFirst) sb.append(",{");
				sb.append("\"color_back\":\"");
				sb.append(us.color_back);
				sb.append("\",\"color_line\":\"");
				sb.append(us.color_line);
				sb.append("\",\"color_fill\":\"");
				sb.append(us.color_fill);
				sb.append("\",\"filter\":\"");
				sb.append(us.filter);
				sb.append("\",\"lineSize\":\"");
				sb.append(us.lineSize);
				if(us.fillPnt != null)
				{
					sb.append("\",\"fillPnt\":\"");
					sb.append(us.fillPnt.x);
					sb.append(",");
					sb.append(us.fillPnt.y);
				}
				else
				{
					sb.append("\",\"path\":\"");
					for(PointF pnt: us.points)
					{
						sb.append(pnt.x);
						sb.append(",");
						sb.append(pnt.y);
						sb.append(";");
					}
				}
				sb.append("\"}");
				bFirst = false;
			}
			sb.append("]}");
		}
		else
		{
			sb.append("}");
		}

		return sb.toString();
	}
	
	// add to undo list
	public void addUndoState(List<PointF> points, Path path, Paint paint, PointF fillPnt, int color_back, int color_line, int filter, int lineSize, int color_fill)
	{
        UndoState us = new FingerPaint.UndoState(points, path, paint, fillPnt, color_back, color_line, filter, lineSize, color_fill);
        switch (filter) 
        {
        case 1: // emboss effect
            us.paint.setMaskFilter(mEmboss);
            break;
        case 2: // blur effect
            us.paint.setMaskFilter(mBlur);
            break;
        case 3: // erase
            us.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            break;
        case 4: // draw top
            us.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
            us.paint.setAlpha(0x80);
            break;
        }
        if((points != null && points.size() > 0) || fillPnt != null) undoStates.add(us);
        
        return;
	}

	// states class
	public class UndoState
	{
		public List<PointF> points = new ArrayList<PointF>();
		public Path path;
		public Paint paint;
		public PointF fillPnt = null;
		public int color_back;
		public int color_line;
		public int color_fill;
		public int filter;
		public int lineSize;

		public UndoState(List<PointF> points, Path path, Paint paint, PointF fillPnt, int color_back, int color_line, int filter, int lineSize, int color_fill) 
		{
			if(points != null) this.points.addAll(points);
			if(path != null) this.path = new Path(path);
			this.paint = new Paint(paint);
			if(fillPnt != null) this.fillPnt = new PointF(fillPnt.x, fillPnt.y);
			this.color_back = color_back;
			this.color_line = color_line;
			this.color_fill = color_fill;
			this.filter = filter;
			this.lineSize = lineSize;
		}
		
		public UndoState copy()
		{
			UndoState edt = new UndoState(points, path, paint, fillPnt, color_back, color_line, filter, lineSize, color_fill);
			
			return edt;
		}
	}

	// copy myself
	public FingerPaint copy()
	{
		FingerPaint newItem = new FingerPaint();
		
		Bitmap bitmap;
    	try
    	{
            bitmap = Bitmap.createBitmap(this.mBitmap.getWidth(), this.mBitmap.getHeight(), Bitmap.Config.ARGB_4444);
    	}
    	catch(OutOfMemoryError ex)
    	{
    		Log.i(TAG, "" + ex);
    		//Toast.makeText(getActivity(), getString(R.string.fetchDataFailed), Toast.LENGTH_LONG).show();
    		return null;
    	}
    	catch(Exception ex)
    	{
    		Log.i(TAG, "" + ex);
    		return null;
    	}
    	newItem.setBitmap(bitmap);
		
		for(UndoState us: undoStates) newItem.undoStates.add(us.copy());
		for(PointF pnt: points) newItem.points.add(pnt);
		newItem.lineSize = lineSize;
		newItem.color_back = color_back;
		newItem.color_line = color_line;
		newItem.filter = filter;
		newItem.frameType = frameType;

		return newItem;
	}
	
	// move paint to a new position
	public void relocate(float xDiff, float yDiff)
	{
		for(UndoState us: undoStates)
		{
			boolean bFirst = true;
			if(us.points != null)
			{
				us.path = new Path();
				for(PointF p: us.points)
				{
					p.x -= xDiff;
					p.y -= yDiff;
					
					if(bFirst) us.path.moveTo(p.x, p.y);
					else us.path.lineTo(p.x, p.y);
					bFirst = false;
				}
			}
			else if(us.fillPnt != null)
			{
				us.fillPnt.x -= xDiff;
				us.fillPnt.y -= yDiff;
			}
		}
	}
	
	public void relocate()
	{
		float xDiff = Float.MAX_VALUE;
		float yDiff = Float.MAX_VALUE;
		for(UndoState us: undoStates)
		{
			if(us.points != null)
			{
				for(PointF p: us.points)
				{
					if(p.x < xDiff) xDiff = p.x;
					if(p.y < yDiff) yDiff = p.y;
				}
			}
			else if(us.fillPnt != null)
			{
				if(us.fillPnt.x < xDiff) xDiff = us.fillPnt.x;  
				if(us.fillPnt.y < yDiff) yDiff = us.fillPnt.y;
			}
		}
		Log.i(TAG, "relocate " + xDiff + " " + yDiff);
		
		for(UndoState us: undoStates)
		{
			boolean bFirst = true;
			if(us.points != null)
			{
				us.path = new Path();
				for(PointF p: us.points)
				{
					p.x -= xDiff;
					p.y -= yDiff;
					
					if(bFirst) us.path.moveTo(p.x, p.y);
					else us.path.lineTo(p.x, p.y);
					bFirst = false;
				}
			}
			else if(us.fillPnt != null)
			{
				us.fillPnt.x -= xDiff;
				us.fillPnt.y -= yDiff;
			}
		}
	}

	
	// scale paint
	public void rescale(float xRat, float yRat)
	{
		for(UndoState us: undoStates)
		{
			boolean bFirst = true;
			if(us.points != null)
			{
				us.path = new Path();
				for(PointF p: us.points)
				{
					p.x *= xRat;
					p.y *= yRat;
					
					if(bFirst) us.path.moveTo(p.x, p.y);
					else us.path.lineTo(p.x, p.y);
					bFirst = false;
					//Log.i(TAG, "" + p.x + " " + xRat);
				}
			}
			else if(us.fillPnt != null)
			{
				us.fillPnt.x *= xRat;
				us.fillPnt.y *= yRat;
			}
		}

		return;
	}

	// rotate paint points
	public void rotate(float u, float v, float ang)
	{
		PointF pnt;
		for(UndoState us: undoStates)
		{
			boolean bFirst = true;
			if(us.points != null)
			{
				us.path = new Path();
				for(PointF p: us.points)
				{
					pnt = Utils.rotatiePnt(u, v, p.x, p.y, ang);
					p.x = pnt.x;
					p.y = pnt.y;
					
					if(bFirst) us.path.moveTo(p.x, p.y);
					else us.path.lineTo(p.x, p.y);
					bFirst = false;
				}
			}
			else if(us.fillPnt != null)
			{
				pnt = Utils.rotatiePnt(u, v, us.fillPnt.x, us.fillPnt.y, ang);
				us.fillPnt.x = pnt.x;
				us.fillPnt.y = pnt.y;
			}
		}

		return;
	}
	
	// calculate bounding rectangle
	public RectF calculateRect()
	{
		RectF rect = new RectF();
		
		for(UndoState us: undoStates)
		{
			float f = Float.MAX_VALUE;
			for(PointF pnt: us.points)
				if(f > pnt.x) f = pnt.x;
			rect.left = f;
			
			f = 0;
			for(PointF pnt: us.points)
				if(f < pnt.x) f = pnt.x;
			rect.right = f;
			
			f = Float.MAX_VALUE;
			for(PointF pnt: us.points)
				if(f > pnt.y) f = pnt.y;
			rect.top = f;
	
			f = 0;
			for(PointF pnt: us.points)
				if(f < pnt.y) f = pnt.y;
			rect.bottom = f;
		}
		//Log.i(TAG, "paint " + rect.left + " " + rect.top + " " + rect.right + " " + rect.bottom);
		
		return rect;
	}
	
	private boolean isInsied(float x, float y)
	{
		if(x >= 0 && x < mBitmap.getWidth() && y >= 0 && y < mBitmap.getHeight()) return true;
		
		return false;
	}
	
	// call after resize
	public void applySize(float x, float y, float w, float h)
	{
		mBitmap.recycle();
		mBitmap = Bitmap.createBitmap((int)w, (int)h, Bitmap.Config.ARGB_4444);
		mBitmap.eraseColor(this.color_back);
	}
}
