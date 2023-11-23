package org.landroo.toolbar;

import org.landroo.simnote.NoteItem;
import org.landroo.simnote.R;

import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PaintTool
{
	private SlidingDrawer leftView;
	private Handler handler;
	private PaletteTool palette;
	
	public int lastLineSize = 0;
	public int lastBackColor = Color.TRANSPARENT;
	public int lastForeColor = Color.RED;
	
	public PaintTool (SlidingDrawer leftView, Handler handler, PaletteTool palette)
	{
		this.leftView = leftView;
		this.handler = handler;
		this.palette = palette;
	}

	public void showPaintTool(NoteItem item)
	{
		LinearLayout ll = (LinearLayout) this.leftView.findViewById(R.id.mainView);
		ll.setVisibility(View.GONE);
		ll = (LinearLayout) this.leftView.findViewById(R.id.leftview_paint);
		ll.setVisibility(View.VISIBLE);
		ll = (LinearLayout) this.leftView.findViewById(R.id.leftview_poly);
		ll.setVisibility(View.GONE);
		
		// undo button
		ImageButton imbtn = (ImageButton) this.leftView.findViewById(R.id.paintUndoBtn);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				handler.sendEmptyMessage(111);
				leftView.animateClose();
			}
		});
		
		// back color palette
		lastBackColor = item.fingerPaint.color_back;
		imbtn = (ImageButton) this.leftView.findViewById(R.id.paintPaletteBtn1);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				leftView.animateClose();
				palette.showPalettePopup(112, lastBackColor);
			}
		});
			
		// line color palette
		lastForeColor = item.fingerPaint.color_line;
		imbtn = (ImageButton) this.leftView.findViewById(R.id.paintPaletteBtn2);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				leftView.animateClose();
				palette.showPalettePopup(113, lastForeColor);
			}
		});
		
		// emboss filter
		imbtn = (ImageButton) this.leftView.findViewById(R.id.paintEmbossBtn);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				handler.sendEmptyMessage(114);
				leftView.animateClose();
			}
		});

		// blur filter
		imbtn = (ImageButton) this.leftView.findViewById(R.id.paintBlurBtn);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				handler.sendEmptyMessage(115);
				leftView.animateClose();
			}
		});
		
		// draw
		imbtn = (ImageButton) this.leftView.findViewById(R.id.paintDrawBtn);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				handler.sendEmptyMessage(110);
				leftView.animateClose();
			}
		});
		
		// erase
		imbtn = (ImageButton) this.leftView.findViewById(R.id.paintEraseBtn);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				handler.sendEmptyMessage(116);
				leftView.animateClose();
			}
		});
		
		// flood fill
		imbtn = (ImageButton) this.leftView.findViewById(R.id.paintFillBtn);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				handler.sendEmptyMessage(118);
				leftView.animateClose();
			}
		});
		
		// paint save
		imbtn = (ImageButton) this.leftView.findViewById(R.id.paintSaveBtn);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				handler.sendEmptyMessage(201);
				leftView.animateClose();
			}
		});
		
		// line width seek bar
		SeekBar lineSizeSeekbar = (SeekBar) this.leftView.findViewById(R.id.paintlinesize_seekBar);
		lineSizeSeekbar.setProgress(item.fingerPaint.lineSize);
		lineSizeSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
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
				lastLineSize = progress;
				handler.sendEmptyMessage(119);
			}
		});
	}
}
