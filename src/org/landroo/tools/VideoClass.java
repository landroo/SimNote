package org.landroo.tools;

import java.io.IOException;

import org.landroo.simnote.R;
import org.landroo.simnote.R.string;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class VideoClass
{
	private static final String TAG = "VideoClass";
	
	private MediaPlayer mMediaPlayer = null;
	
	private String projectDir;
	private Handler handle;
	private int callBack;
	private Context context;
	
	public VideoView videoView;
	
	public int width = 400;
	public int height = 400;
	
    public VideoClass(Context context, Handler handle, int callBack) 
    {
		this.context = context;
		this.handle = handle;
		this.callBack = callBack;
		
		projectDir = context.getResources().getString(R.string.project_dir);
		
		mMediaPlayer = new MediaPlayer();
    }
    
	public SurfaceView getVideoView()
	{
		videoView = new VideoView(context);
		
		return videoView;
	}
	
	public class VideoView extends SurfaceView implements SurfaceHolder.Callback 
	{
		private SurfaceHolder mHolder;
		
	    public VideoView(Context context)
		{
	    	super(context);
	    	
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
		{
		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0)
		{
		}
		
		public void startVideo(String file, int width, int height)
		{
			try
			{
				mMediaPlayer.setDataSource(file);
				mMediaPlayer.prepare();
			}
			catch (Exception ex)
			{
				Log.i(TAG, "" + ex);
			}

		    //Get the dimensions of the video
		    int videoWidth = mMediaPlayer.getVideoWidth();
		    int videoHeight = mMediaPlayer.getVideoHeight();

		    //Get the width of the screen
		    int screenWidth = width;

		    //Get the SurfaceView layout parameters
		    //android.view.ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();

		    //Set the width of the SurfaceView to the width of the screen
		    //lp.width = screenWidth;

		    //Set the height of the SurfaceView to match the aspect ratio of the video 
		    //be sure to cast these as floats otherwise the calculation will likely be 0
		    //lp.height = (int) (((float)videoHeight / (float)videoWidth) * (float)screenWidth);

		    //Commit the layout parameters
		    //mSurfaceView.setLayoutParams(lp);
		    
		    mMediaPlayer.setDisplay(mHolder);

		    //Start video
		    mMediaPlayer.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0)
		{
		}
	}
}
