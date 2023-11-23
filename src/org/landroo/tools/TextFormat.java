package org.landroo.tools;

import org.landroo.simnote.NoteItem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.util.Log;

public class TextFormat
{
	private static final String TAG = "TextFormat";
	
	private Context context;
	
	private int iw;
	private int ih;
	
	private int topMargin = 5;
	private int leftMargin = 5;
	private int rightMargin = 5;
	private int paragraphOffset = 8;
	
	private Paint textPaint = new Paint();
	
	public int width = 0;
	public int height = 0;
	public int lineNum = 0;
	
	public int fontSize = 18;
	public int mode = 4;// 1 left, 2 right, 3 center, 4 fill
	
	public int frameType = 0;
	public int textColor = NoteItem.COLOR_BLACK;
	public int backColor = 0x00FFFFFF;
	
	private boolean bold;
	private boolean italic;
	private boolean filled;
	private boolean shadow;

	public TextFormat(Context context, int width, int height, int mode)
	{
		this.context = context;
		
		this.iw = width;
		this.ih = height;
		
		this.width = iw;
		this.height = ih;
		
		this.mode = mode;
	}
	
	// set font
	public void setPaint(String fontList, int textColor, int backColor)
	{
		String[] fontArr = fontList.split("\t");
		if(fontArr.length > 1)
		{
			String font = fontArr[0];
			fontSize = Integer.parseInt(fontArr[1]);
			bold = fontArr[2].equals("true") ? true: false;
			italic = fontArr[3].equals("true") ? true: false;
			filled = fontArr[4].equals("true") ? true: false;
			shadow = fontArr[5].equals("true") ? true: false;
			
			int stroke = fontSize / 20;
			if(stroke < 1) stroke = 1; 
			
			Typeface tf;
			if(font.indexOf("/") == -1) tf = Typeface.create(font, Typeface.NORMAL);
			else tf = Typeface.createFromFile(font);
			
			if(bold) tf = Typeface.create(tf, Typeface.BOLD);
			if(italic) tf = Typeface.create(tf, Typeface.ITALIC);
	
			textPaint.setAntiAlias(true);
			textPaint.setStrokeWidth(stroke);
			textPaint.setTextSize(fontSize);
			textPaint.setTypeface(tf);
			textPaint.setColor(textColor);
			textPaint.setAntiAlias(true);
			textPaint.setFakeBoldText(true);
			
			if(shadow) textPaint.setShadowLayer(3, 3, 3, Color.BLACK);
			if(filled) textPaint.setStyle(Style.STROKE);
			else textPaint.setStyle(Style.FILL);
			
			this.textColor = textColor;
			this.backColor = backColor;
			
			topMargin = fontSize / 2;
			leftMargin = fontSize / 2;
			rightMargin = fontSize / 2;
			paragraphOffset = fontSize / 2;
		}
	}
	
	// calculate the width and the height of the bitmap
	private void calcSize(String text, boolean resize)
	{
		int h = fontSize + topMargin;
		int w = 0;
		
		lineNum = 0;
		
		float minSpaceWidth = textPaint.measureText("i");
		float fMes;
		
		int iLine = 0;
		float lineWidth = 0;
		float spaceWidth;
		
		String sTxt;
		String[] sWords;
		String[] sParags;

		// split paragraph
		sParags = text.split("[\n]");
		for(int paragCnt = 0; paragCnt < sParags.length; paragCnt++)
		{
			// get a paragraph
			sTxt = sParags[paragCnt];
			// split words
			sWords = sTxt.split("[ ]");
		
			// measure the paragraph get the line length
			fMes = textPaint.measureText(sTxt);
			
			if (sWords.length > 0)
			{
				for (int wordcnt = 0; wordcnt < sWords.length; wordcnt++)
				{
					iLine = 0;
					lineWidth = 0;
					
					// new paragraph
					if (wordcnt == 0) lineWidth = minSpaceWidth;
					
					// calculate line width
					while (lineWidth < iw && wordcnt + iLine < sWords.length)
					{
						sTxt = sWords[wordcnt + iLine];
						fMes = textPaint.measureText(sTxt);
	
						if (lineWidth + fMes > iw - rightMargin) break;
	
						lineWidth += fMes + minSpaceWidth;
						iLine++;

						lineNum++;
					}

					if(lineWidth > w) w = (int)(lineWidth);
					
					// wrong line
					if (iLine == 0)
					{
						if(lineWidth + fMes > iw || lineWidth > iw)
						{
							iLine = 1;
							lineWidth = iw;
							spaceWidth = 0;
						}
						else 
						{
							Log.i(TAG, "Wrolg line: " + sTxt);
							continue;
						}
					}
					else
					{
						// calculate space width
						spaceWidth = minSpaceWidth;
						if(mode == 4) spaceWidth = (iw - rightMargin - lineWidth) / (iLine - 1) + minSpaceWidth;
					}

					// check maximum space width
					if (spaceWidth > minSpaceWidth * 4 || spaceWidth < minSpaceWidth) spaceWidth = minSpaceWidth;
	
					h += fontSize;
					wordcnt += iLine - 1;
					
					if(lineWidth > w) w = (int)lineWidth;
				}
			}
			else h += fontSize;
			
			if(lineNum > 1) h += paragraphOffset;
		}
		
		h -= paragraphOffset;
		
		if(resize == false)
		{
			height = h;
			width = w + leftMargin + rightMargin;
		}
		
		return;
	}
	
	// write text to bitmap
	public Bitmap formatText(String text, boolean resize)
	{
		calcSize(text, resize);
		
		Bitmap bitmap = null;
		try
		{
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
    	}
    	catch(OutOfMemoryError ex)
    	{
    		Log.i(TAG, "" + ex);
    		return null;
    	}
    	catch(Exception ex)
    	{
    		Log.i(TAG, "" + ex);
    		return null;
    	}
		
		if(frameType != 0) drawFrame(bitmap, false);
		else bitmap.eraseColor(backColor);
		Canvas canvas = new Canvas(bitmap);
		
		float minSpaceWidth = textPaint.measureText("i");
		float fMes;
		float x = 0;
		float y = fontSize + topMargin;
		float m;		
		
		int iLine = 0;
		float lineWidth = 0;
		float spaceWidth;
		
		String sTxt;
		String[] sWords;
		String[] sParags;

		// split paragraph
		sParags = text.split("[\n]");
		for(int paragCnt = 0; paragCnt < sParags.length; paragCnt++)
		{
			// get a paragraph
			sTxt = sParags[paragCnt];
			// split words
			sWords = sTxt.split("[ ]");
		
			// measure the paragraph get the line length
			fMes = textPaint.measureText(sTxt);
			
			if (sWords.length > 0)
			{
				for (int wordcnt = 0; wordcnt < sWords.length; wordcnt++)
				{
					iLine = 0;
					lineWidth = 0;
					spaceWidth = minSpaceWidth;

					// new paragraph
					if (wordcnt == 0) lineWidth = minSpaceWidth;

					// calculate line width
					while (lineWidth < iw && wordcnt + iLine < sWords.length)
					{
						sTxt = sWords[wordcnt + iLine];
						fMes = textPaint.measureText(sTxt);

						// if line wider than the screen !!!
						if (lineWidth + fMes > iw - rightMargin) break;

						lineWidth += fMes + minSpaceWidth;
						iLine++;
					}

					// wrong line
					if (iLine == 0)
					{
						if(lineWidth + fMes > iw || lineWidth > iw)
						{
							iLine = 1;
							lineWidth = iw;
						}
						else 
						{
							Log.i(TAG, "Wrolg line: " + sTxt);
							continue;
						}
					}
					else
					{
						// calculate space width
						spaceWidth = minSpaceWidth;
						if(mode == 4) spaceWidth = (iw - leftMargin - rightMargin - lineWidth) / (iLine - 1) + minSpaceWidth;
					}

					// check maximum space width
					if (spaceWidth > minSpaceWidth * 4 || spaceWidth < minSpaceWidth) spaceWidth = minSpaceWidth;

					// begin line
					x = 0;
					if(mode == 2) x = iw - lineWidth + leftMargin;
					else if(mode == 3) x = leftMargin + (iw - rightMargin - lineWidth) / 2;
					else x = leftMargin;

					// new paragraph
					if (wordcnt == 0 && lineNum > 1 && mode == 4) x = minSpaceWidth * 3;

					// draw words
					for (int i = 0; i < iLine; i++)
					{
						sTxt = sWords[wordcnt + i];
						m = textPaint.measureText(sTxt);

						// new paragraph
						if (i > 0 && lineNum > 1) x += spaceWidth;

						// draw text on the x, y position!!!
						canvas.drawText(sTxt, x, y, textPaint);

						x += m;
					}

					y += fontSize;
					wordcnt += iLine - 1;
				}
				
			}
			else y += fontSize;

			y += paragraphOffset;
		}
		
		return bitmap;
	}
	
	// draw rounded frame
	public void drawFrame(Bitmap bitmap, boolean nextFrame)
	{
		RectF rect = new RectF(fontSize / 8, fontSize / 8, bitmap.getWidth() - fontSize / 8, bitmap.getHeight() - fontSize / 8);
		
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        if(((backColor >> 24) & 0xFF) > 0) paint.setColor(backColor);
        else paint.setColor(textColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(fontSize / 5);

		int stroke = fontSize / 20;
		if(stroke < 1) stroke = 1;
		// draw line or fill background
        if(filled == true) paint.setStrokeWidth(stroke);
        else if(((backColor >> 24) & 0xFF) > 0) paint.setStyle(Paint.Style.FILL);

		Canvas canvas = new Canvas(bitmap);

		if(nextFrame) frameType++;
		if(frameType > 6) frameType = 0;
		
		float cen = 40;
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
			paint.setPathEffect(new DashPathEffect(new float[] {50, 25}, 5));
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
			paint.setPathEffect(new DashPathEffect(new float[] {50, 25}, 5));
			canvas.drawRoundRect(rect, cen, cen, paint);
			break;
		case 6:// rounded frame dashed			
			paint.setPathEffect(new PathDashPathEffect(makePathDash(), 12, 5, PathDashPathEffect.Style.MORPH));
			canvas.drawRoundRect(rect, cen, cen, paint);
			break;
		}
		//Log.i(TAG, "frame: " + frameType);
		
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
	
}
