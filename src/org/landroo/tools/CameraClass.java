package org.landroo.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.landroo.simnote.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class CameraClass
{
	private static final String TAG = "CameraClass";
	
	private String projectDir;
	private Handler handle;
	private int callBack;
	private Context context;
	private int rotation;
	
	public String lastFile;
	public CameraPreview cameraPreview;
	public Camera mCamera;
	public int width;
	public int height;
	
	private List<Size> supportedSizes;
	public Size size;
	
	// get camera instance
	public static Camera getCameraInstance()
	{
		Camera c = null;
		try
		{
			c = Camera.open(); // attempt to get a Camera instance
			if(c == null) c = Camera.open(0);
		}
		catch (Exception e)
		{
			Log.i(TAG, "Camera is not available (in use or does not exist)");
		}
		
		return c;
	}

	// take a picture
	private PictureCallback mPicture = new PictureCallback()
	{
		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{
			File pictureFile = getOutputMediaFile("photo", ".jpg");
			if (pictureFile == null)
			{
				Log.d(TAG, "Error creating media file, check storage permissions ");
				return;
			}
			
			lastFile = Environment.getExternalStorageDirectory().getPath() + "/" + projectDir + "/" + pictureFile.getName();

			try
			{
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				
				rotateBitmap(lastFile, rotation);
				resizeBitmap(lastFile, width, height);
				
				// call back
				handle.sendEmptyMessage(callBack);
			}
			catch (OutOfMemoryError e)
			{
				Log.d(TAG, "Out of memory: " + e.getMessage());
			}
			catch (Exception e)
			{
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
		}
	};
	
	// constructor
	public CameraClass(Context context, Handler handle, int callBack)
	{
		this.context = context;
		this.handle = handle;
		this.callBack = callBack;
		
		projectDir = context.getResources().getString(R.string.project_dir);
		
		// Create an instance of Camera
		mCamera = getCameraInstance();
		if(mCamera != null)
		{
			rotation = setCameraDisplayOrientation((Activity)context, 0, mCamera);
			//Log.i(TAG, "rotation " + rotation);

			// select smallest picture size
			Camera.Parameters params = mCamera.getParameters();
			supportedSizes = params.getSupportedPictureSizes();
			int w = Integer.MAX_VALUE;
			for(Size s:supportedSizes)
				if(s.width < w) w = s.width;
			int i = 0;
			for(Size s:supportedSizes)
			{
				if(s.width == w) break;
				i++;
			}
			size = supportedSizes.get(i);
			//Log.i(TAG, "size " + size.width + " " + size.height);
			
			params.setPictureSize(size.width, size.height);
			params.setPreviewSize(size.width, size.height);
			params.setPictureFormat(ImageFormat.JPEG);
			mCamera.setParameters(params);
		}
		else
		{
			Toast.makeText(context, "A kamera nem elérhető!", Toast.LENGTH_LONG).show();
		}
	}
	
	// Create our Preview view and set it as the content of our activity.
	public SurfaceView getPrewView()
	{
		
		if(mCamera != null) cameraPreview = new CameraPreview(context, mCamera);
		
		return cameraPreview;
	}
	
	// create unique filename
	public File getOutputMediaFile(String name, String ext)
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), projectDir);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists())
		{
			// Failed to create directory
			if (!mediaStorageDir.mkdirs()) return null;
		}

		// Create a media file name
		File mediaFile;
		if (name == null) name = "";
		mediaFile = new File(mediaStorageDir.getPath() + File.separator + name + "_" + timeStamp + ext);

		return mediaFile;
	}
	
	// rotate bitmap
	public void rotateBitmap(String fName, int rotate)
	{
		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			
			File imgFile = new File(fName);
			Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
			
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
	
			// Setting pre rotate
			Matrix matrix = new Matrix();
			matrix.preRotate(rotate);
	
			// Rotating Bitmap & convert to ARGB_8888
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, false);
			bitmap = bitmap.copy(Bitmap.Config.ARGB_4444, true);
			
			FileOutputStream out = new FileOutputStream(fName);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		}
		catch (OutOfMemoryError e)
		{
			Log.d(TAG, "Out of memory: " + e.getMessage());
		}
		catch (Exception e)
		{
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		}
	}
	
	// resize bitmap
	public void resizeBitmap(String fName, int w, int h)
	{
		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			
			File imgFile = new File(fName);
			Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
			
			Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
			Canvas cnv = new Canvas(bmp);
			Paint pnt = new Paint();
			
			Rect src = new Rect();
			src.left = 0;
			src.top = 0;
			src.right = bitmap.getWidth();
			src.bottom = bitmap.getHeight();

			Rect dsc = new Rect();
			dsc.left = 0;
			dsc.top = 0;
			dsc.right = (int) w;
			dsc.bottom = (int) h;

			cnv.drawBitmap(bitmap, src, dsc, pnt);
			
			FileOutputStream out = new FileOutputStream(fName);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		}
		catch (OutOfMemoryError e)
		{
			Log.d(TAG, "Out of memory: " + e.getMessage());
		}
		catch (Exception e)
		{
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		}
	}

	// click
	public void takePhoto()
	{
		if(mCamera != null)
		{
			try
			{
				mCamera.takePicture(null, null, mPicture);
			}
			catch (Exception e)
			{
				Log.d(TAG, "Error take picture: " + e.getMessage());
			}
		}
	}
	
	// surface view class
	public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
	{
		private SurfaceHolder mHolder;
		private Camera mCamera;

		// Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
		public CameraPreview(Context context, Camera camera)
		{
			super(context);
			mCamera = camera;

			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		// The Surface has been created, now tell the camera where to draw the preview.
		public void surfaceCreated(SurfaceHolder holder)
		{
		}

		// empty. Take care of releasing the Camera preview in your activity.
		public void surfaceDestroyed(SurfaceHolder holder)
		{
		}

		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
		{
			if (mHolder.getSurface() == null)
			{
				// preview surface does not exist
				return;
			}
		}

		// start preview
		public void startPreview(int w, int h)
		{
			width = w;
			height = h;
			
			if(mCamera != null)
			{
				//TODO set picture size
				/*Camera.Parameters params = mCamera.getParameters();
				List<Size> supportedSizes = params.getSupportedPictureSizes();
				for(Size s:supportedSizes)
				{
					if(height > s.height || width > s.width)
					{
						params.setPictureSize(s.width, s.height);
						params.setPreviewSize(s.width, s.height);
					}
				}*/
				
				try
				{
//					mCamera.setParameters(params);
					mCamera.setPreviewDisplay(mHolder);
					mCamera.startPreview();
				}
				catch (Exception e)
				{
					Log.d(TAG, "Error setting camera preview: " + e.getMessage());
				}
			}
		}
		
		// stop preview
		public void stopPreview()
		{
			if(mCamera != null)
			{
				try
				{
					mCamera.stopPreview();
				}
				catch (Exception e)
				{
					// ignore: tried to stop a non-existent preview
				}
			}
		}
	}
	
	// set camera display orientation
	public static int setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) 
	{
	     android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	     int degrees = 0;
	     switch (rotation) 
	     {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) 
	     {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } 
	     else 
	     {  
	    	 // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	     
	     return result;
	 }

}
