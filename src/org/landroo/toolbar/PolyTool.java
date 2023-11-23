package org.landroo.toolbar;

import org.landroo.simnote.NoteItem;
import org.landroo.simnote.R;

import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PolyTool
{
	private SlidingDrawer leftView;
	private Handler handler;
	private PaletteTool palette;
	private ImageButton btnPoly;
	private ImageButton btnLine;
	private int color_fill1 = NoteItem.COLOR_GREEN;
	private int color_fill2 = NoteItem.COLOR_GREEN;
	private int color_line = 0xFF00FF00;
	
	public int lineMode = 1;
	public int lineSize = 0;
	public int lineType = 0;
	
	public PolyTool(SlidingDrawer leftView, Handler handler, PaletteTool palette)
	{
		this.leftView = leftView;
		this.handler = handler;
		this.palette = palette;
	}
	
	public void showPolyTool(NoteItem item)
	{
		LinearLayout ll = (LinearLayout) this.leftView.findViewById(R.id.mainView);
		ll.setVisibility(View.GONE);
		ll = (LinearLayout) this.leftView.findViewById(R.id.leftview_paint);
		ll.setVisibility(View.GONE);
		ll = (LinearLayout) this.leftView.findViewById(R.id.leftview_poly);
		ll.setVisibility(View.VISIBLE);
		
		color_fill1 = item.polyDraw.color_fill1;
		color_fill2 = item.polyDraw.color_fill2;
		color_line = item.polyDraw.color_line;
		
		lineSize = item.polyDraw.lineSize;
		lineType = item.polyDraw.lineType;
		lineMode = item.polyDraw.lineMode;
		
		// undo
		ImageButton imbtn = (ImageButton) this.leftView.findViewById(R.id.poly_undo);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				handler.sendEmptyMessage(101);
			}
		});
		
		// filled polygon
		btnPoly = (ImageButton) this.leftView.findViewById(R.id.polygon);
		if(lineMode == 1) imbtn.setBackgroundResource(R.drawable.border);
		else imbtn.setBackgroundColor(0x00000000);
		btnPoly.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				btnPoly.setBackgroundResource(R.drawable.border);
				btnLine.setBackgroundColor(0x00000000);
				handler.sendEmptyMessage(102);
			}
		});
			
		// poly line 
		btnLine = (ImageButton) this.leftView.findViewById(R.id.poly_line);
		if(lineMode == 1) imbtn.setBackgroundResource(R.drawable.border);
		else imbtn.setBackgroundColor(0x00000000);
		btnLine.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				btnPoly.setBackgroundColor(0x00000000);
				btnLine.setBackgroundResource(R.drawable.border);
				handler.sendEmptyMessage(103);
			}
		});

		// fill1 color palette
		imbtn = (ImageButton) this.leftView.findViewById(R.id.poly_palette1);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				palette.showPalettePopup(104, color_fill1);
			}
		});
		
		// fill2 color palette
		imbtn = (ImageButton) this.leftView.findViewById(R.id.poly_palette3);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				palette.showPalettePopup(108, color_fill2);
			}
		});
			
		// line color palette
		imbtn = (ImageButton) this.leftView.findViewById(R.id.poly_palette2);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				palette.showPalettePopup(105, color_line);
			}
		});
		
		// line type button
		imbtn = (ImageButton) this.leftView.findViewById(R.id.poly_line_type);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				lineType++;
				if(lineType > 5) lineType = 0;
				handler.sendEmptyMessage(107);
			}
		});
		
		// save polygon file
		imbtn = (ImageButton) this.leftView.findViewById(R.id.poly_save);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				handler.sendEmptyMessage(203);
			}
		});
		
		// delete polygon point
		imbtn = (ImageButton) this.leftView.findViewById(R.id.poly_delete);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				handler.sendEmptyMessage(109);
			}
		});
		
		// set background pattern
		imbtn = (ImageButton) this.leftView.findViewById(R.id.poly_pattern);
		imbtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				handler.sendEmptyMessage(205);
			}
		});

		// line width seek bar
		SeekBar lineSizeSeekbar = (SeekBar) this.leftView.findViewById(R.id.polyline_size_seek_bar);
		lineSizeSeekbar.setProgress(lineSize);
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
				lineSize = progress;
				handler.sendEmptyMessage(106);
			}
		});

	}
}
