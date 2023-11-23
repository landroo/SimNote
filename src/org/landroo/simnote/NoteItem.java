package org.landroo.simnote;

import java.util.UUID;

import org.landroo.tools.FingerPaint;
import org.landroo.tools.PolyDraw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class NoteItem
{
	private static final String TAG = "NoteItem";
	private static final float DEGTORAD = 0.0174532925199432957f;
	private static final float RADTODEG = 57.295779513082320876f;
	
	// colors
	public static final int COLOR_RED = 0xFFF15F74;
	public static final int COLOR_ORANGE = 0xFFF76D3C;
	public static final int COLOR_GRAY = 0xFF839098;
	public static final int COLOR_YELLOW = 0xFFF7D842;
	public static final int COLOR_PURPLE = 0xFF913CCD;
	public static final int COLOR_GREEN = 0xFF98CB4A;
	public static final int COLOR_BLUE = 0xFF5481E6;
	public static final int COLOR_CYAN = 0xFF2CA8C2;
	public static final int COLOR_BLACK = 0xFF303437;
	public static final int COLOR_WHITE = 0xFFC5C6C7;

	// note item states
	public static final int STATE_NONE = 0;
	public static final int STATE_SELECT = 1;
	public static final int STATE_MOVE = 2;
	public static final int STATE_ROTATE = 3;
	public static final int STATE_ZOOM = 4;
	public static final int STATE_RESIZE = 5;
	public static final int STATE_HAND_DRAW = 6;
	public static final int STATE_POLY_DRAW = 7;
	public static final int STATE_HIDDEN = 8;

	// note item types
	public static final int TYPE_BITMAP = 1;
	public static final int TYPE_TEXT = 2;
	public static final int TYPE_VECTOR = 3;
	public static final int TYPE_PAINT = 4;

	// corners
	public static final int LEFT_TOP = 1;
	public static final int RIGHT_TOP = 2;
	public static final int RIGHT_BOTTOM = 3;
	public static final int LEFT_BOTTOM = 4;

	// positions
	public float px = 10;
	public float py = 10;
	public float lx = 10;
	public float ly = 10;

	// size
	public float iw = 1;
	public float ih = 1;
	public float ow = 1;
	public float oh = 1;

	public Bitmap bitmap = null;
	public Drawable drawable = null;

	public float mZoom = 1;// scale
	public float mRotation = 0;// rotation angle in degree
	public float mLastRot = 0;// last rotation angle in degree

	public int type = 0;// 1-bitmap, 2-text, 3-polygon, 4-draw
	public int state = 0;// 0 nothing, 1-select, 2-move, 3-rotate, 4-zoom, 5-resize, 6-handdraw, 7-polydraw

	// properties
	private String label = "";
	public RectF bound = new RectF();
	public int alpha = 255;
	public int foreColor = COLOR_RED;
	public int backColor = 0x00FFFFFF;
	public String id;
	public String user = "";
	
	// text properties
	public String fileName = "";
	public String text = "";
	public boolean stretch = false;
	public int frameType = 0;
	public int textAlign = 0;
	public String font = "";

	public Paint movePaint = new Paint();
	public Paint rotatePaint = new Paint();
	public Paint zoomPaint = new Paint();
	public Paint pointPaint = new Paint();
	public Paint selectPaint = new Paint();
	public Paint drawPaint = new Paint();
	public Paint resizePaint = new Paint();

	private PathEffect effect;
	public int pathSize = 4;

	public FingerPaint fingerPaint;
	public PolyDraw polyDraw;
	
	private NoteItem linked = null;// linked item
	private NoteItem linker = null;// linker item
	public String linkedID = "";
	public String linkerID = "";
	
	private long checkSum;
	public boolean changed = false;
	
	public boolean camera = false;
	
	public int[] states = {1, 1, 1, 1, 1, 1};
	
	public float labelSize = 0;
	
	public boolean deleted = false;

	// constructor
	public NoteItem(Bitmap bitmap, float x, float y, int type, String text, int w, int h, String user)
	{
		this.id = "" + UUID.randomUUID();

		this.type = type;
		this.text = text;

		this.lx = x;
		this.ly = y;
		this.px = x;
		this.py = y;
		
		this.user = user;

		effect = new PathDashPathEffect(makePathPattern(pathSize * 4, pathSize * 2), pathSize * 4, 0.0f,
				PathDashPathEffect.Style.ROTATE);

		movePaint.setColor(COLOR_RED);
		movePaint.setPathEffect(effect);
		movePaint.setStyle(Paint.Style.STROKE);
		movePaint.setStrokeWidth(pathSize);

		rotatePaint.setColor(COLOR_GREEN);
		rotatePaint.setPathEffect(effect);
		rotatePaint.setStyle(Paint.Style.STROKE);
		rotatePaint.setStrokeWidth(pathSize);

		zoomPaint.setColor(COLOR_BLUE);
		zoomPaint.setPathEffect(effect);
		zoomPaint.setStyle(Paint.Style.STROKE);
		zoomPaint.setStrokeWidth(pathSize);

		selectPaint.setColor(COLOR_BLACK);
		selectPaint.setPathEffect(effect);
		selectPaint.setStyle(Paint.Style.STROKE);
		selectPaint.setStrokeWidth(pathSize);

		drawPaint.setColor(COLOR_YELLOW);
		drawPaint.setPathEffect(effect);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeWidth(pathSize);

		resizePaint.setColor(COLOR_PURPLE);
		resizePaint.setPathEffect(effect);
		resizePaint.setStyle(Paint.Style.STROKE);
		resizePaint.setStrokeWidth(pathSize);

		pointPaint.setColor(0xffff0000);

		if (type == TYPE_PAINT)
		{
			fingerPaint = new FingerPaint();
	        if(bitmap != null)
	        {
	        	fingerPaint.setBitmap(bitmap);
	        }
	        else
	        {
	        	try
	        	{
		            bitmap = Bitmap.createBitmap((int)w, (int)h, Bitmap.Config.ARGB_4444);
	        	}
	        	catch(OutOfMemoryError ex)
	        	{
	        		Log.i(TAG, "" + ex);
	        		return;
	        	}
	        	catch(Exception ex)
	        	{
	        		Log.i(TAG, "" + ex);
	        		return;
	        	}
	        	fingerPaint.setBitmap(bitmap);
	        }
			setBitmap(bitmap, true);
		}
		else if (type == TYPE_VECTOR)
		{
			polyDraw = new PolyDraw(5, null);
			this.bound.set(-pathSize, -pathSize, iw + pathSize, ih + pathSize);
		}
		else
		{
			setBitmap(bitmap, true);
		}
	}

	// set bitmap
	public void setBitmap(Bitmap bitmap, boolean resize)
	{
		if (bitmap != null)
		{
			this.bitmap = bitmap;

			if (resize)
			{
				this.iw = bitmap.getWidth();
				this.ih = bitmap.getHeight();

				this.ow = iw;
				this.oh = ih;
				//Log.i(TAG, "ow: " + ow + " oh: " + oh);
			}

			this.drawable = new BitmapDrawable(bitmap);
			this.drawable.setBounds(0, 0, (int) iw, (int) ih);
			this.drawable.setAlpha(alpha);

			this.bound.set(-pathSize, -pathSize, iw + pathSize, ih + pathSize);
		}
	}

	// click inside
	public boolean isInside(float x, float y, float zx, float zy)
	{
		boolean bIn = false;

		double ang = mRotation * DEGTORAD;
		double u = px + getWidth() / 2;
		double v = py + getHeight() / 2;

		PointF pnt;
		float[] x1 = new float[4];
		float[] y1 = new float[4];
		pnt = Utils.rotatiePnt(u, v, px, py, ang);
		x1[0] = pnt.x * zx;
		y1[0] = pnt.y * zy;
		pnt = Utils.rotatiePnt(u, v, px + getWidth(), py, ang);
		x1[1] = pnt.x * zx;
		y1[1] = pnt.y * zy;
		pnt = Utils.rotatiePnt(u, v, px + getWidth(), py + getHeight(), ang);
		x1[2] = pnt.x * zx;
		y1[2] = pnt.y * zy;
		pnt = Utils.rotatiePnt(u, v, px, py + getHeight(), ang);
		x1[3] = pnt.x * zx;
		y1[3] = pnt.y * zy;

		bIn = Utils.ponitInPoly(4, x1, y1, x, y);

		return bIn;
	}
	
	// item selected
	public boolean isInsideBound(float x, float y, float zx, float zy, float ox, float oy)
	{
		RectF rect = new RectF(bound);
		rect.left *= zx * mZoom;
		rect.top *= zy * mZoom;
		rect.right *= zx * mZoom;
		rect.bottom *= zy * mZoom;
		
		rect.left -= ox;
		rect.top -= oy;
		rect.right += ox;
		rect.bottom += oy;

		boolean bIn = rect.contains(x - px * zx, y - py * zy);

		if (type == TYPE_VECTOR && state == STATE_POLY_DRAW) bIn = false;

		return bIn;
	}

	// corner selected
	public int isCorner(float x, float y, float zx, float zy, float tol)
	{
		double ang = mRotation * DEGTORAD;
		double u = px + getWidth() / 2;
		double v = py + getHeight() / 2;

		PointF pnt = Utils.rotatiePnt(u, v, px, py, ang);
		if (pnt.x * zx - tol <= x && pnt.x * zx + tol >= x && pnt.y * zy - tol <= y && pnt.y * zy + tol >= y) return 1;
		pnt = Utils.rotatiePnt(u, v, px + getWidth(), py, ang);
		if (pnt.x * zx - tol <= x && pnt.x * zx + tol >= x && pnt.y * zy - tol <= y && pnt.y * zy + tol >= y) return 2;
		pnt = Utils.rotatiePnt(u, v, px + getWidth(), py + getHeight(), ang);
		if (pnt.x * zx - tol <= x && pnt.x * zx + tol >= x && pnt.y * zy - tol <= y && pnt.y * zy + tol >= y) return 3;
		pnt = Utils.rotatiePnt(u, v, px, py + getHeight(), ang);
		if (pnt.x * zx - tol <= x && pnt.x * zx + tol >= x && pnt.y * zy - tol <= y && pnt.y * zy + tol >= y) return 4;

		return 0;
	}

	// tap on border with tolerance
	// return 1 left-top 2 right-top 3 right-bottom 4 left-bottom
	public int isBorder(float x, float y, float zx, float zy, float tol)
	{
		double ang = mRotation * DEGTORAD;
		float w = bound.width() * mZoom;
		float h = bound.height() * mZoom;
		double u = px + w / 2;
		double v = py + h / 2;

		PointF pnt = Utils.rotatiePnt(u, v, px, py, ang);
		if (pnt.x * zx - tol <= x && pnt.x * zx + tol >= x && pnt.y * zy - tol <= y && pnt.y * zy + tol >= y) return 1;
		pnt = Utils.rotatiePnt(u, v, px + w, py, ang);
		if (pnt.x * zx - tol <= x && pnt.x * zx + tol >= x && pnt.y * zy - tol <= y && pnt.y * zy + tol >= y) return 2;
		pnt = Utils.rotatiePnt(u, v, px + w, py + h, ang);
		if (pnt.x * zx - tol <= x && pnt.x * zx + tol >= x && pnt.y * zy - tol <= y && pnt.y * zy + tol >= y) return 3;
		pnt = Utils.rotatiePnt(u, v, px, py + h, ang);
		if (pnt.x * zx - tol <= x && pnt.x * zx + tol >= x && pnt.y * zy - tol <= y && pnt.y * zy + tol >= y) return 4;

		return 0;
	}

	// 0 nothing, 1-select, 2-move, 3-rotate, 4-zoom, 5-resize, 6-drawing, 7-polygon
	public void nextState()
	{
		this.state++;
		if (this.state > 5) this.state = 0;
		if(states[state] == 0) nextState();
	}

	// create selection border pattern
	public static Path makePathPattern(float w, float h)
	{
		Path path = new Path();
		path.moveTo(0, 0);
		path.lineTo(w / 2, 0);
		path.lineTo(w, h);
		path.lineTo(w / 2, h);
		path.close();

		return path;
	}

	// zoomed the bound rectangle
	public RectF zoomRect(float zx, float zy)
	{
		RectF rect = new RectF(this.bound);
		rect.left *= zx * mZoom;
		rect.top *= zy * mZoom;
		rect.right *= zx * mZoom;
		rect.bottom *= zy * mZoom;

		return rect;
	}

	// return with width
	public float getWidth()
	{
		if (type == TYPE_VECTOR) return bound.width() * mZoom;
		else return iw * mZoom;
	}

	// return with height
	public float getHeight()
	{
		if (type == TYPE_VECTOR) return bound.height() * mZoom;
		else return ih * mZoom;
	}

	// new handle draw
	public void setFingerPaint()
	{
		fingerPaint = new FingerPaint();
        if(bitmap != null)
        {
        	fingerPaint.setBitmap(bitmap);
        }
        else
        {
        	try
        	{
	            bitmap = Bitmap.createBitmap((int)iw, (int)ih, Bitmap.Config.ARGB_8888);
	        	//mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        	}
        	catch(OutOfMemoryError ex)
        	{
        		Log.i(TAG, "" + ex);
        	}
        	catch(Exception ex)
        	{
        		Log.i(TAG, "" + ex);
        	}
        	fingerPaint.setBitmap(bitmap);
        }

        return;
	}

	// resize item
	public void applySize(boolean crop)
	{
		float w = bound.width();
		float h = bound.height();

		if (type == TYPE_VECTOR)
		{
			float xRat = iw / w;
			float yRat = ih / h;
			polyDraw.applySize(xRat, yRat);
		}
		else
		{
			try
			{
				Rect src = new Rect();
				Rect dsc = new Rect();
				
				if(crop)
				{
					src.left = (int) bound.left;
					src.top = (int) bound.top;
					src.right = (int) bound.right;
					src.bottom = (int) bound.bottom;
	
					dsc.left = 0;
					dsc.top = 0;
					dsc.right = (int) w;
					dsc.bottom = (int) h;
				}
				else
				{
					src.left = 0;
					src.top = 0;
					src.right = bitmap.getWidth();
					src.bottom = bitmap.getHeight();

					dsc.left = 0;
					dsc.top = 0;
					dsc.right = (int) w;
					dsc.bottom = (int) h;
				}
				
				if(src.left != dsc.left || src.top != dsc.top || src.right != dsc.right || src.bottom != dsc.bottom)
				{
					Bitmap bmp = Bitmap.createBitmap((int) w, (int) h, Bitmap.Config.ARGB_4444);
					Canvas cnv = new Canvas(bmp);
					Paint pnt = new Paint();

					// hand draw item
					if(type == NoteItem.TYPE_PAINT)
					{
						if(w > bitmap.getWidth() || h > bitmap.getHeight())
						{
							//TODO
							fingerPaint.applySize(0, 0, w, h);
						}
						
						// relocate undo list
						if(crop)
						{
							float xDiff = src.left - dsc.left;
							float yDiff = src.top - dsc.top;
							fingerPaint.relocate(xDiff, yDiff);
						}
						else
						{
							float xRat = (float)dsc.width() / (float)src.width();
							float yRat = (float)dsc.height() / (float)src.height();
							fingerPaint.rescale(xRat, yRat);
						}

						fingerPaint.drawFrame(bmp, false);
					}
					else
					{
						// draw original content
						cnv.drawBitmap(bitmap, src, dsc, pnt);
					}
					
					this.bitmap = bmp;
					this.drawable = new BitmapDrawable(bitmap);
					this.drawable.setBounds(0, 0, (int) w, (int) h);
	
					this.iw = w;
					this.ih = h;
	
					this.px += bound.left;
					this.py += bound.top;
	
					this.bound.left = 0;
					this.bound.top = 0;
					this.bound.right = w;
					this.bound.bottom = h;
				}
			}
			catch (OutOfMemoryError ex)
			{
				Log.e(TAG, "Out of memory: " + ex.getMessage());
			}
			catch (Exception ex)
			{
				Log.e(TAG, "applySize: " + ex.getMessage());
			}
		}

		this.state = STATE_NONE;
	}

	// scale item
	public void applyScale(float zoom)
	{
		if (type == TYPE_VECTOR)
		{
			polyDraw.applyScale(mZoom);
			setBound(polyDraw.calculateRect());
			mZoom = 1;
		}
		else
		{
			int width = this.bitmap.getWidth();
			int height = this.bitmap.getHeight();
			int newWidth = (int) iw;
			int newHeight = (int) ih;
			
			if (zoom != 1)
			{
				newWidth = (int) (iw * zoom);
				newHeight = (int) (ih * zoom);
			}

			// calculate the scale - in this case = 0.4f
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;

			// create a matrix for the manipulation
			Matrix matrix = new Matrix();
			// resize the bit map
			matrix.postScale(scaleWidth, scaleHeight);

			try
			{
				Bitmap bmp = Bitmap.createBitmap(this.bitmap, 0, 0, width, height, matrix, true);

				this.bitmap = bmp;
				this.drawable = new BitmapDrawable(bitmap);
				this.drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
				
				this.iw = bitmap.getWidth();
				this.ih = bitmap.getHeight();
			}
			catch (OutOfMemoryError ex)
			{
				Log.e(TAG, "Out of memory: " + ex.getMessage());
			}
			catch (Exception ex)
			{
				Log.e(TAG, "applyScale: " + ex.getMessage());
			}
		}

		this.state = STATE_NONE;
	}

	// rotate item
	public void applyRotate(boolean redraw)
	{
		if (type == TYPE_VECTOR)
		{
			polyDraw.applyRotate(bound.width() / 2, bound.height() / 2, mRotation * DEGTORAD);
			setBound(polyDraw.calculateRect());
		}
		else
		{
			float width = bitmap.getWidth();
			float height = bitmap.getHeight();
			float newWidth = (int) iw;
			float newHeight = (int) ih;
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;

			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			matrix.postRotate(mRotation);
			try
			{
				this.bitmap = Bitmap.createBitmap(this.bitmap, 0, 0, (int)width, (int)height, matrix, true);
				if (type == TYPE_PAINT)
				{
					fingerPaint.rotate(width / 2, height / 2, mRotation * DEGTORAD);
					
					this.iw = bitmap.getWidth();
					this.ih = bitmap.getHeight();
					
					this.bound.left = 0;
					this.bound.top = 0;
					this.bound.right = iw;
					this.bound.bottom = ih;
					
					float dx = (width - iw) / 2;
					float dy = (height - ih) / 2;
					
					fingerPaint.relocate(dx, dy);
					fingerPaint.drawFrame(bitmap, false);
				
					//Log.i(TAG, "" + px + " " + dx + " " + py + " " + dy);
					this.px -= dx / 2;
					this.py -= dy / 2;
				}
	
				if(redraw == false)
				{
					this.iw = bitmap.getWidth();
					this.ih = bitmap.getHeight();
					
					this.bound.left = 0;
					this.bound.top = 0;
					this.bound.right = iw;
					this.bound.bottom = ih;
					
					this.px += (width - iw) / 2;
					this.py += (height - ih) / 2;
				}
	
				this.drawable = new BitmapDrawable(bitmap);
				this.drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
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

		if(redraw == false) this.mLastRot += mRotation;
		if (mLastRot < 0) mLastRot += 360;
		if (mLastRot > 360) mLastRot -= 360;
		this.mRotation = 0;

		this.state = STATE_NONE;
	}

	// get my type name
	public String getType(Context context)
	{
		String sRet = "";

		switch (type)
		{
		case NoteItem.TYPE_BITMAP:
			sRet = context.getResources().getString(R.string.bitmap);
			break;
		case NoteItem.TYPE_TEXT:
			sRet = context.getResources().getString(R.string.text);
			break;
		case NoteItem.TYPE_VECTOR:
			sRet = context.getResources().getString(R.string.polygon);
			break;
		case NoteItem.TYPE_PAINT:
			sRet = context.getResources().getString(R.string.draw);
			break;
		}

		return sRet;
	}

	// copy myself
	public NoteItem copyItem()
	{
		NoteItem item = new NoteItem(this.bitmap, this.px, this.py, this.type, this.text, (int) this.iw, (int) this.ih, new String(this.user));
		item.mZoom = this.mZoom;
		item.mRotation = this.mRotation = 0;
		item.mLastRot = this.mLastRot = 0;
		item.alpha = this.alpha;
		item.bound = new RectF(this.bound);
		if(this.linked != null)
		{
			item.linkedID = new String(this.linkedID);
			item.linked = this.linked;
		}
		if(this.linker != null)
		{
			item.linkerID = new String(this.linkerID);
			item.linker = this.linker;
		}
		if (type == TYPE_PAINT)
		{
			item.fingerPaint = this.fingerPaint.copy();
		}
		if (type == TYPE_VECTOR)
		{
			item.polyDraw = polyDraw.copy();
		}
		return item;
	}

	// set bounding box
	public void setBound(RectF rect)
	{
		if (type == TYPE_VECTOR)
		{
			float x = rect.left;
			float y = rect.top;
			polyDraw.relocate(-x, -y);
			lx = px;
			ly = py;
			px += x;
			py += y;

			this.bound = polyDraw.calculateRect();
			this.iw = bound.width();
			this.ih = bound.height();
		}
		else if (type == TYPE_PAINT)
		{
			float xDiff = bound.left - rect.left;
			float yDiff = bound.top - rect.top;
			fingerPaint.relocate(xDiff, yDiff);

			lx = px;
			ly = py;
			px += xDiff;
			py += yDiff;
			
			this.bound = rect;
			this.iw = bound.width();
			this.ih = bound.height();
		}
		else
		{
			this.bound = rect;
			this.iw = bound.width();
			this.ih = bound.height();
		}
	}

	// check point to polygon distance
	public boolean checkPoly(float dist)
	{
		state = NoteItem.STATE_POLY_DRAW;

		if (polyDraw.points.size() > 0)
		{
			double x1 = this.bound.centerX();
			double y1 = this.bound.centerY();

			double x2 = polyDraw.points.get(polyDraw.points.size() - 1).x;
			double y2 = polyDraw.points.get(polyDraw.points.size() - 1).y;

			double d = Utils.getDist(x1, y1, x2, y2);
			if (d > dist)
			{
				polyDraw.undoLast();
				setBound(polyDraw.calculateRect());
				state = NoteItem.STATE_NONE;
				//Log.i(TAG, "" + d + " " + dist);
			}
		}

		return true;
	}

	// mask image
	private Bitmap maskImage(Bitmap image, Bitmap mask, int color)
	{
		int width = image.getWidth();
		int height = image.getHeight();

		// result image
		Bitmap resImage = null;
		try
		{
			resImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(resImage);
	
			Paint imagePaint = new Paint();
			imagePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			imagePaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
	
			canvas.drawBitmap(mask, 0, 0, null);
			canvas.drawBitmap(image, 0, 0, imagePaint);
    	}
    	catch(OutOfMemoryError ex)
    	{
    		Log.i(TAG, "" + ex);
    	}
    	catch(Exception ex)
    	{
    		Log.i(TAG, "" + ex);
    	}
		
		return resImage;
	}

	// texture image
	private Bitmap applyTexture(Bitmap image, Bitmap texture, int color)
	{
		// create mask
		int width = image.getWidth();
		int height = image.getHeight();

		Bitmap maskedBitmap = null;
		try
		{
			Bitmap maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas maskCanvas = new Canvas(maskBitmap);
			Paint maskPaint = new Paint();
			maskPaint.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN));
			maskCanvas.drawBitmap(image, 0, 0, maskPaint);
	
			// apply texture on mask
			maskedBitmap = maskImage(texture, maskBitmap, color);
			Canvas maskedCanvas = new Canvas(maskedBitmap);
			Paint maskedPaint = new Paint();
			maskedPaint.setAlpha(128);
	
			// emboss filter image
			Bitmap embossBitmap = processingBitmap_Emboss(image);
	
			// apply emboss on masked image
			maskedCanvas.drawBitmap(embossBitmap, 0, 0, maskedPaint);
    	}
    	catch(OutOfMemoryError ex)
    	{
    		Log.i(TAG, "" + ex);
    	}
    	catch(Exception ex)
    	{
    		Log.i(TAG, "" + ex);
    	}
		
		return maskedBitmap;
	}

	// add emboss filter to bitmap
	private Bitmap processingBitmap_Emboss(Bitmap src)
	{
		int width = src.getWidth();
		int height = src.getHeight();

		Paint paintEmboss = new Paint();

		Bitmap dest = null;
		try
		{
			dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(dest);
	
			Bitmap alpha = src.extractAlpha();
	
			float ambientValue = 0.5f;
			float specularValue = 0.5f;
			float blurRadiusValue = 3.0f;
	
			EmbossMaskFilter embossMaskFilter = new EmbossMaskFilter(new float[] { 1, 1, 1 }, ambientValue, specularValue,
					blurRadiusValue);
	
			paintEmboss.setMaskFilter(embossMaskFilter);
			canvas.drawBitmap(alpha, 0, 0, paintEmboss);
    	}
    	catch(OutOfMemoryError ex)
    	{
    		Log.i(TAG, "" + ex);
    	}
    	catch(Exception ex)
    	{
    		Log.i(TAG, "" + ex);
    	}
		
		return dest;
	}
	
	// save item to json format
	public String saveItem()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("{");
		sb.append("\"px\":\"");
		sb.append(px);
		sb.append("\",\"py\":\"");
		sb.append(py);
		sb.append("\",\"lx\":\"");
		sb.append(lx);
		sb.append("\",\"ly\":\"");
		sb.append(ly);
		sb.append("\",\"iw\":\"");
		sb.append(iw);
		sb.append("\",\"ih\":\"");
		sb.append(ih);
		sb.append("\",\"ow\":\"");
		sb.append(ow);
		sb.append("\",\"oh\":\"");
		sb.append(oh);
		sb.append("\",\"zoom\":\"");
		sb.append(mZoom);
		sb.append("\",\"rotation\":\"");
		sb.append(mRotation);
		sb.append("\",\"lastrot\":\"");
		sb.append(mLastRot);
		sb.append("\",\"type\":\"");
		sb.append(type);
		sb.append("\",\"state\":\"");
		sb.append(state);
		sb.append("\",\"text\":\"");
		if(text != null) sb.append(text.replaceAll("\"", "'"));
		sb.append("\",\"stretch\":\"");
		sb.append(stretch);
		sb.append("\",\"frameType\":\"");
		sb.append(frameType);
		sb.append("\",\"align\":\"");
		sb.append(textAlign);
		sb.append("\",\"font\":\"");
		sb.append(font);
		sb.append("\",\"bound\":\"");
		sb.append(bound.left);
		sb.append(";");
		sb.append(bound.top);
		sb.append(";");
		sb.append(bound.right);
		sb.append(";");
		sb.append(bound.bottom);
		sb.append("\",\"alpha\":\"");
		sb.append(alpha);
		sb.append("\",\"foreColor\":\"");
		sb.append(foreColor);
		sb.append("\",\"backColor\":\"");
		sb.append(backColor);
		sb.append("\",\"id\":\"");
		sb.append(id);
		sb.append("\",\"states\":\"");
		for(int i = 0; i < states.length; i++) sb.append(states[i]);
		sb.append("\",\"linked\":\"");
		if(linked != null) sb.append(linked.id);
		sb.append("\",\"linker\":\"");
		if(linker != null) sb.append(linker.id);
		sb.append("\",\"label\":\"");
		if(label != null) sb.append(label.replaceAll("\"", "'"));
		sb.append("\"");
		if(bitmap != null && type == TYPE_BITMAP)
		{
			sb.append(",\"bitmap\":\"");
			sb.append(Utils.bitmapToBase64(bitmap));
			sb.append("\"");
		}
		if(fingerPaint != null)
		{
			sb.append(",");
			sb.append(fingerPaint.saveItem());
		}
		if(polyDraw != null)
		{
			sb.append(",");
			sb.append(polyDraw.saveItem());
		}
		sb.append("}");
		
		changed = false;
		String sRes = sb.toString();
		byte[] b = sRes.getBytes();
		long sum = 0;
		for(int i = 0; i < b.length; i++) sum += b[i];
		if(checkSum != sum) changed = true;
		checkSum = sum;
		//Log.i(TAG, id + " " + changed);

		return sRes;
	}
	
	// export item to html tag format
	public String exportItem(boolean turn)
	{
		StringBuilder sb = new StringBuilder();
		boolean bLink = false;
		
		if(iw > 1 && ih > 1)
		{
		
			// if web link
			if(label.length() > 5 && label.substring(0, 5).equals("http:"))
			{
				sb.append("<a href=\"");
				sb.append(label);
				sb.append("\"");
				sb.append(" style=\"position:absolute;");
				sb.append("width:");
				sb.append((int)iw);
				sb.append("px;");
				
				sb.append("height:");
				sb.append((int)ih);
				sb.append("px;");
				
				sb.append("top:");
				if(turn) sb.append((int)(py));
				else sb.append((int)(px));
				sb.append("px;");
				
				sb.append("left:");
				if(turn) sb.append((int)(px));
				else sb.append((int)(py));
				sb.append("px;");
				sb.append("\">");

				bLink = true;
			}
			
			sb.append("<img id=\"");
			sb.append(id);
			sb.append("\"");
			
			sb.append(" style=\"position:absolute;");
			sb.append("width:");
			sb.append((int)iw);
			sb.append("px;");
			
			sb.append("height:");
			sb.append((int)ih);
			sb.append("px;");
			if(!bLink)
			{
				sb.append("top:");
				if(turn) sb.append((int)(py));
				else sb.append((int)(px));
				sb.append("px;");
				
				sb.append("left:");
				if(turn) sb.append((int)(px));
				else sb.append((int)(py));
				sb.append("px;");
			}
			
			if(type == TYPE_BITMAP && alpha != 255)
			{
				sb.append("opacity:");
				sb.append((float)alpha / 255);
				sb.append(";");
			}
			
			sb.append("\"");
			
			if(bitmap != null)
			{
				sb.append(" src=\"data:image/png;base64,");
				sb.append(Utils.bitmapToBase64(bitmap));
				sb.append("\"");
			}
			else if(type == TYPE_VECTOR)
			{
				try
				{
					Bitmap bmp = Bitmap.createBitmap((int)iw, (int)ih, Bitmap.Config.ARGB_8888);
					Canvas canv = new Canvas(bmp);
					polyDraw.drawPolygon(canv, mZoom, mZoom); 
					
					sb.append(" src=\"data:image/png;base64,");
					sb.append(Utils.bitmapToBase64(bmp));
					sb.append("\"");
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
			
			sb.append(">");
			
			if(bLink) sb.append("</a>");
		}
		
		return sb.toString();
	}
	
	// set bitmap color
	public void setColor(int color)
	{
		this.foreColor = color;
		
		// result image 
		try
		{
			Bitmap resImage = Bitmap.createBitmap((int)iw, (int)ih, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(resImage);
	
			Paint imagePaint = new Paint();
			imagePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			imagePaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC));
	
			canvas.drawBitmap(this.bitmap, 0, 0, null);
			canvas.drawBitmap(this.bitmap, 0, 0, imagePaint);
			
			this.bitmap = Bitmap.createBitmap(resImage);
			this.drawable = new BitmapDrawable(bitmap);
			this.drawable.setBounds(0, 0, (int) iw, (int) ih);
    	}
    	catch(OutOfMemoryError ex)
    	{
    		Log.i(TAG, "" + ex);
    	}
    	catch(Exception ex)
    	{
    		Log.i(TAG, "" + ex);
    	}
		
		return;
	}
	
	// draw frame
	public void drawFrame(Bitmap bitmap)
	{
		RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
		
		int lineSize = (int)ih / 50;
		if(iw > ih) lineSize = (int)iw / 50;
		
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(foreColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(lineSize);
    	paint.setMaskFilter(null);
    	paint.setXfermode(null);
    	
    	Bitmap bmp = null;
    	Canvas canvas = null;
    	if(bitmap.isMutable())
    	{
    		try
    		{
	    		bmp = Bitmap.createBitmap((int)iw, (int)ih, Bitmap.Config.ARGB_8888);
	    		canvas = new Canvas(bmp);
	    		canvas.drawBitmap(bitmap, 0, 0, paint);
	    		this.bitmap = bmp;
				this.drawable = new BitmapDrawable(bitmap);
				this.drawable.setBounds(0, 0, (int) iw, (int) ih);
				this.drawable.setAlpha(alpha);
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
    	else
    	{
    		canvas = new Canvas(bitmap);
    		// erase frame
    		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    	}
    	
    	if(bmp != null)
    	{
			canvas.drawRect(rect, paint);
			
			float cen = 40;
			if(rect.height() < rect.width()) cen = rect.height() / 8;
			else cen = rect.width() / 8;
			switch(frameType)
			{
			case 0:// no frame
				break;
			case 1:// rect frame
				canvas.drawRect(rect, paint);
				break;
			case 2:// rect frame dashed
				paint.setPathEffect(new DashPathEffect(new float[] {10, 5, 5, 5}, 5));
				canvas.drawRect(rect, paint);
				break;
			case 3:// rounded frame
				canvas.drawRoundRect(rect, cen, cen, paint);
				break;
			case 4:// rounded frame dashed
				paint.setPathEffect(new DashPathEffect(new float[] {10, 5, 5, 5}, 5));
				canvas.drawRoundRect(rect, cen, cen, paint);
				break;
			}
    	}
    	
    	return;
	}

	// set the item label
	public void setLabel(String newLabel, Paint paint)
	{
		if(newLabel != null)
		{
			label = newLabel;
			labelSize = paint.measureText(label);
		}
	}
	
	// return with item label
	public String getLabel()
	{
		return label;
	}
	
	public void setLinked(NoteItem linked)
	{
		this.linked = linked;
		if(linked != null)
		{
			this.linkedID = linked.id;
			linked.setLinker(this);
		}
	}
	
	public NoteItem getLinked()
	{
		return linked;
	}
	
	public void setLinker(NoteItem linker)
	{
		this.linker = linker;
		this.linkerID = linker.id;
	}
	
	public NoteItem getLinker()
	{
		return linker;
	}
	
	
}
