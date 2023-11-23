package org.landroo.toolbar;

import org.landroo.simnote.NoteItem;
import org.landroo.simnote.R;
import org.landroo.simnote.ToolBarClass;
import org.landroo.tools.PaletteView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PaletteTool
{
	private ViewGroup view;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private int displayWidth;
	private int displayHeight;
	private PopupWindow popupWindow;
	private Context context;
	private int callBack = 0;
	private PaletteView palettelView;
	private SeekBar redSeekbar;
	private SeekBar greenSeekbar;
	private SeekBar blueSeekbar;
	private SeekBar alphaSeekbar;
	
	public int lastColor = NoteItem.COLOR_BLACK;
	public boolean cancel = false;
	
	public PaletteTool(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.view = v;
		this.layoutInflater = inflater;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;

	}
	
	public void showPalettePopup(int call, int color)
	{
		lastColor = color;
		cancel = false;
		
		View popupView = layoutInflater.inflate(R.layout.palette_popup, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight - displayHeight / 10;
		popupWindow = new PopupWindow(popupView, w, h);
		popupWindow.setFocusable(true);

		Bitmap bitmap = ToolBarClass.drawBack(w, h, 0, 0, false, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);
		
		callBack = call;
		
		// palette
		palettelView = (PaletteView) popupView.findViewById(R.id.color_palette);
		PaletteView.OnColorChangedListener listener = new PaletteView.OnColorChangedListener()
		{
			public void colorChanged(int color)
			{
				int r = palettelView.getRed();
				int g = palettelView.getGreen();
				int b = palettelView.getBlue();
				
				redSeekbar.setProgress(r);
				greenSeekbar.setProgress(g);
				blueSeekbar.setProgress(b);
			}
			public void colorSelected(int color)
			{
				lastColor = color;
				if(callBack != 0) handler.sendEmptyMessage(callBack);
				
				popupWindow.dismiss();
			}
		};
		palettelView.SetOnColorChangedListener(listener);
		palettelView.setColor(color);
		
		// red seek bar
		redSeekbar = (SeekBar) popupView.findViewById(R.id.color_red_seekBar);
		redSeekbar.setProgress((color >> 16) & 0xFF);
		redSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				int r = progress;
				int g = greenSeekbar.getProgress();
				int b = blueSeekbar.getProgress();
				int a = alphaSeekbar.getProgress();
				
				r = (r << 16) & 0x00FF0000;
			    g = (g << 8) & 0x0000FF00;
			    b = b & 0x000000FF;
			    a = (a << 24) & 0xFF000000;
				
				palettelView.setColor(a | r | g | b);
			}
		});
		
		// green seekbar
		greenSeekbar = (SeekBar) popupView.findViewById(R.id.color_green_seekBar);
		greenSeekbar.setProgress((color >> 8) & 0xFF);
		greenSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				int r = redSeekbar.getProgress();
				int g = progress;
				int b = blueSeekbar.getProgress();
				int a = alphaSeekbar.getProgress();
				
				r = (r << 16) & 0x00FF0000;
			    g = (g << 8) & 0x0000FF00;
			    b = b & 0x000000FF;
			    a = (a << 24) & 0xFF000000;
				
				palettelView.setColor(a | r | g | b);
			}
		});
		
		// blue seek bar
		blueSeekbar = (SeekBar) popupView.findViewById(R.id.color_blue_seekBar);
		blueSeekbar.setProgress((color >> 0) & 0xFF);
		blueSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				int r = redSeekbar.getProgress();
				int g = greenSeekbar.getProgress();
				int b = progress;
				int a = alphaSeekbar.getProgress();
				
				r = (r << 16) & 0x00FF0000;
			    g = (g << 8) & 0x0000FF00;
			    b = b & 0x000000FF;
			    a = (a << 24) & 0xFF000000;
				
				palettelView.setColor(a | r | g | b);
			}
		});
		
		// alpha seek bar
		alphaSeekbar = (SeekBar) popupView.findViewById(R.id.color_alpha_seekBar);
		alphaSeekbar.setProgress((color >> 24) & 0xFF);
		alphaSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				int r = redSeekbar.getProgress();
				int g = greenSeekbar.getProgress();
				int b = blueSeekbar.getProgress();
				int a = progress;
				
				r = (r << 16) & 0x00FF0000;
			    g = (g << 8) & 0x0000FF00;
			    b = b & 0x000000FF;
			    a = (a << 24)  & 0xFF000000;
				
				palettelView.setColor(a | r | g | b);
			}
		});

		// cancel selecting color 
		ImageButton btnTextCancel = (ImageButton) popupView.findViewById(R.id.color_dismiss);
		btnTextCancel.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				cancel = true;
				if(callBack != 0) handler.sendEmptyMessage(callBack);
				popupWindow.dismiss();
			}
		});

		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);

		return;
	}

}
