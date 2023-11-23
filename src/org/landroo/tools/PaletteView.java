package org.landroo.tools;

import org.landroo.simnote.R;
import org.landroo.simnote.R.styleable;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PaletteView extends View
{
	private View mValue;
	private ImageView mImage;

	private int mValueColor;

	private Paint mPaint;
	private Paint mCenterPaint;
	private int[] mColors;

	private boolean mTrackingCenter;
	private boolean mHighlightCenter;

	private int CENTER_X;
	private int CENTER_Y;
	private int CENTER_RADIUS = 32;

	private OnColorChangedListener mListener;

	public interface OnColorChangedListener
	{
		void colorSelected(int color);
		void colorChanged(int color);
	}

	public PaletteView(Context context)
	{
		super(context, null);
		init(context, null);
	}

	public PaletteView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}

	public PaletteView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public void init(Context context, AttributeSet attrs)
	{
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Options, 0, 0);
		mValueColor = ta.getColor(R.styleable.Options_valueColor, android.R.color.holo_blue_light);
		ta.recycle();
		
		mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterPaint.setColor(mValueColor);
		mCenterPaint.setStrokeWidth(5);

		mColors = new int[] { 0xFF000000, 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFFFFFF, 0xFF000000 };
		Shader s = new SweepGradient(0, 0, mColors, null);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setShader(s);
	}

	public void setValueColor(int color)
	{
		mValue.setBackgroundColor(color);
	}

	public void setImageVisible(boolean visible)
	{
		mImage.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		float r = CENTER_X / 2 - mPaint.getStrokeWidth() * 0.5f;

		canvas.translate(CENTER_X / 2, CENTER_X / 2);

		canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
		canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);

		if (mTrackingCenter)
		{
			int c = mCenterPaint.getColor();
			mCenterPaint.setStyle(Paint.Style.STROKE);

			if (mHighlightCenter) mCenterPaint.setAlpha(0xFF);
			else mCenterPaint.setAlpha(0x80);

			canvas.drawCircle(0, 0, CENTER_RADIUS + mCenterPaint.getStrokeWidth(), mCenterPaint);

			mCenterPaint.setStyle(Paint.Style.FILL);
			mCenterPaint.setColor(c);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int w = MeasureSpec.getSize(widthMeasureSpec);
		int h = MeasureSpec.getSize(heightMeasureSpec);
		int parentWidth = w - w / 10;
		if(h < w) parentWidth = h - h / 10;
		int parentHeight = parentWidth;
		this.setMeasuredDimension(parentWidth, parentHeight);
		
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(parentWidth / 5);

		CENTER_X = parentWidth;
		CENTER_Y = parentHeight;
		CENTER_RADIUS = parentWidth / 6;
	}

	private int floatToByte(float x)
	{
		int n = java.lang.Math.round(x);
		return n;
	}

	private int pinToByte(int n)
	{
		if (n < 0) n = 0;
		else if (n > 255) n = 255;

		return n;
	}

	private int ave(int s, int d, float p)
	{
		return s + java.lang.Math.round(p * (d - s));
	}

	private int interpColor(int colors[], float unit)
	{
		if (unit <= 0) return colors[0];
		if (unit >= 1) return colors[colors.length - 1];

		float p = unit * (colors.length - 1);
		int i = (int) p;
		p -= i;

		// now p is just the fractional part [0...1) and i is the index
		int c0 = colors[i];
		int c1 = colors[i + 1];
		int a = ave(Color.alpha(c0), Color.alpha(c1), p);
		int r = ave(Color.red(c0), Color.red(c1), p);
		int g = ave(Color.green(c0), Color.green(c1), p);
		int b = ave(Color.blue(c0), Color.blue(c1), p);

		return Color.argb(a, r, g, b);
	}

	private int rotateColor(int color, float rad)
	{
		float deg = rad * 180 / 3.1415927f;
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);

		ColorMatrix cm = new ColorMatrix();
		ColorMatrix tmp = new ColorMatrix();

		cm.setRGB2YUV();
		tmp.setRotate(0, deg);
		cm.postConcat(tmp);
		tmp.setYUV2RGB();
		cm.postConcat(tmp);

		final float[] a = cm.getArray();

		int ir = floatToByte(a[0] * r + a[1] * g + a[2] * b);
		int ig = floatToByte(a[5] * r + a[6] * g + a[7] * b);
		int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);

		return Color.argb(Color.alpha(color), pinToByte(ir), pinToByte(ig), pinToByte(ib));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		float x = event.getX() - CENTER_X / 2;
		float y = event.getY() - CENTER_Y / 2;
		boolean inCenter = java.lang.Math.sqrt(x * x + y * y) <= CENTER_RADIUS;

		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			mTrackingCenter = inCenter;
			if (inCenter)
			{
				mHighlightCenter = true;
				invalidate();
				break;
			}
		case MotionEvent.ACTION_MOVE:
			if (mTrackingCenter)
			{
				if (mHighlightCenter != inCenter)
				{
					mHighlightCenter = inCenter;
					invalidate();
				}
			}
			else
			{
				float angle = (float) java.lang.Math.atan2(y, x);
				// need to turn angle [-PI ... PI] into unit [0....1]
				float unit = angle / (2 * (float) Math.PI);
				if (unit < 0)
				{
					unit += 1;
				}
				mCenterPaint.setColor(interpColor(mColors, unit));
				
				if (mListener != null)
				{
					mValueColor = mCenterPaint.getColor();
					mListener.colorChanged(mValueColor);
				}

				invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mTrackingCenter)
			{
				if (inCenter && mListener != null)
				{
					mValueColor = mCenterPaint.getColor();
					mListener.colorSelected(mValueColor);
				}
				mTrackingCenter = false; // so we draw w/o halo
				invalidate();
			}
			break;
		}
		return true;
	}
	
	public int getRed()
	{
		 return (mValueColor >> 16) & 0xFF;
	}
	
	public int getGreen()
	{
		 return (mValueColor >> 8) & 0xFF; 
	}

	public int getBlue()
	{
		 return mValueColor & 0xFF;
	}
	
	public void SetOnColorChangedListener(OnColorChangedListener listener)
	{
		mListener = listener;
	}
	
	public void setColor(int color)
	{
		mValueColor = color; 
		mCenterPaint.setColor(color);
		invalidate();
	}

}
