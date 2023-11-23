package org.landroo.toolbar;

import org.landroo.simnote.NoteItem;
import org.landroo.simnote.R;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainTool
{
	private SlidingDrawer leftView;
	private Handler handler;
	private LabelTool label;
	private Context context;
	private SeekBar alphaSeekbar;
	
	// tool bar setting and return values
	public String resText;
	public int alpha = 0;
	public int frameType = 0;
	public boolean stretch = false;

	// constructor
	public MainTool(SlidingDrawer leftView, Handler handler, Context context, LabelTool label)
	{
		this.leftView = leftView;
		this.handler = handler;
		this.context = context;
		this.label = label;
	}
	
	// show left, main, info tool bar 
	public void showMainTool(NoteItem item)
	{
		LinearLayout ll = (LinearLayout) this.leftView.findViewById(R.id.mainView);
		ll.setVisibility(View.VISIBLE);
		ll = (LinearLayout) this.leftView.findViewById(R.id.leftview_paint);
		ll.setVisibility(View.GONE);
		ll = (LinearLayout) this.leftView.findViewById(R.id.leftview_poly);
		ll.setVisibility(View.GONE);

		StringBuilder sb = new StringBuilder();
		sb.append(context.getResources().getString(R.string.type));
		sb.append(": ");
		sb.append(item.getType(context));
		sb.append("\nx: ");
		sb.append((int)item.px);
		sb.append("\ny: ");
		sb.append((int)item.py);
		sb.append("\n");
		sb.append(context.getResources().getString(R.string.width));
		sb.append(": ");
		sb.append((int)item.iw);
		sb.append("\n");
		sb.append(context.getResources().getString(R.string.width));
		sb.append(": ");
		sb.append((int)item.ih);
		sb.append("\n");
		sb.append(context.getResources().getString(R.string.rotation));
		sb.append(": ");
		sb.append((int)item.mLastRot);
		sb.append("°\n");
		sb.append(context.getResources().getString(R.string.zoom));
		sb.append(": ");
		sb.append((int)(item.mZoom * 100));
		sb.append("%");
		if(item.getLinked() != null)
		{
			sb.append("\n");
			sb.append(context.getResources().getString(R.string.linked));
		}
		
		TextView tw = (TextView) this.leftView.findViewById(R.id.main_view_text);
		tw.setText(sb.toString());
		
		LinearLayout ial = (LinearLayout) this.leftView.findViewById(R.id.main_alpha_layer);
		if(item.type == NoteItem.TYPE_BITMAP) ial.setVisibility(View.VISIBLE);
		else ial.setVisibility(View.GONE);
		
		// picture alpha
		alphaSeekbar = (SeekBar) this.leftView.findViewById(R.id.main_alpha);
		alphaSeekbar.setProgress(alpha);
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
				alpha = progress;
				handler.sendEmptyMessage(76);
			}
		});		
		
		// send to back button
		ImageButton imgBtn = (ImageButton) this.leftView.findViewById(R.id.main_send_back);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// send to back
				handler.sendEmptyMessage(72);
			}
		});

		// copy item button
		imgBtn = (ImageButton) this.leftView.findViewById(R.id.main_copy);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// copy item
				handler.sendEmptyMessage(71);				
			}
		});

		// delete item
		imgBtn = (ImageButton) this.leftView.findViewById(R.id.main_delete);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				handler.sendEmptyMessage(73);
			}
		});

		// set label text
		resText = item.getLabel();
		imgBtn = (ImageButton) this.leftView.findViewById(R.id.main_label);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				leftView.animateClose();
				label.showLabelPoup(resText, 74);
			}
		});
		
		// set item color
		imgBtn = (ImageButton) this.leftView.findViewById(R.id.main_palette);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				leftView.animateClose();
				handler.sendEmptyMessage(75);
			}
		});
		ll = (LinearLayout) this.leftView.findViewById(R.id.palette_layout);
		if(item.type == NoteItem.TYPE_BITMAP) ll.setVisibility(View.VISIBLE);
		else ll.setVisibility(View.GONE);
		
		// border
		imgBtn = (ImageButton) this.leftView.findViewById(R.id.main_border);
		if(frameType != 0) imgBtn.setBackgroundResource(R.drawable.border);
		else imgBtn.setBackgroundColor(0);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				frameType++;
				if (frameType > 6) frameType = 0;
				if(frameType != 0) v.setBackgroundResource(R.drawable.border);
				else v.setBackgroundColor(0);
				handler.sendEmptyMessage(78);
			}
		});
		ll = (LinearLayout) this.leftView.findViewById(R.id.border_layout);
		if(item.type == NoteItem.TYPE_BITMAP || item.type == NoteItem.TYPE_VECTOR) ll.setVisibility(View.GONE);
		else ll.setVisibility(View.VISIBLE);

		// stretch check
		imgBtn = (ImageButton) this.leftView.findViewById(R.id.main_stretch);
		if(stretch) imgBtn.setBackgroundResource(R.drawable.border);
		else imgBtn.setBackgroundColor(0);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				stretch = !stretch;
				if(stretch) v.setBackgroundResource(R.drawable.border);
				else v.setBackgroundColor(0);
				handler.sendEmptyMessage(79);
			}
		});
		ll = (LinearLayout) this.leftView.findViewById(R.id.stretch_layout);
		if(item.type == NoteItem.TYPE_VECTOR) ll.setVisibility(View.GONE);
		else ll.setVisibility(View.VISIBLE);
		
		// set item states
		imgBtn = (ImageButton) this.leftView.findViewById(R.id.main_state);
		imgBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				leftView.animateClose();
				handler.sendEmptyMessage(151);
			}
		});

		return;
	}
	
	// set alpha progress
	public void setAlpha(int alpha)
	{
		if(alphaSeekbar != null) alphaSeekbar.setProgress(alpha);		
	}

}
