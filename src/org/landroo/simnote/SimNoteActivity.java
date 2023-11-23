package org.landroo.simnote;
/*
Simple Note

When I go to a meeting, I always take my note papers with me to draw something or make a notification of the meeting. But I always take my smart phone too.
Why do I this on papers when I have a high capability device. So I make this application. 
It is a simple notation creator for creating memo or report of meetings with picture, texts, paints and vector graphics.
Support the team work on same document by the NoteServer.
Support picture, text, vector and manual graphic items.  
You can select, edit, move, rotate, scale, and resize the notification items.
You can scrolling and zooming the document.
You can link some item together, which move together and jump each other by long click.
Export to html format. 

v 1.0

Simple Note

Ez egy egyszerű feljegyzés készítő alkalmazás, melynek segítségével feljegyzéseket vagy jelentéseket készíthetünk a megbeszélésekről képek, szövegek, rajzok és vektor grafika hozzáadásával.
Lehetővé teszi a csoport munkát, azaz egyazon dokumentumon egyszerre többen is dolgozhatunk a NoteServer segítségével.
Támogatja kép, szöveg, vektor és kézi grafika hozzáadását.
Az elemek kiválaszthatók, szerkeszthetők, mozgathatók, forgathatók, nagyíthatók, méretezhetők.
A dokumentum zoomolható és scrollozható.
Az elemek összekapcsolhatóak.
A dokumentum htm formátumba exportálható.

v 1.0

sonic symphony savior of mankind 	ivan torrent one of us	Vivien Chebbah	Armando Morabito

planned games:
puzzle game			__	card game			__	chess				OK	mill game			__	backgammon			__
queen				__	dice				__	torpedo				__	tangram				__	hang over			__
domino				__	master mind			__	

next features:
group editor		__	polygon texture		OK	picture texture		__	text texture		__	morf/animate item	__
basier curve		__	diagram editor		__	data table			__	news letter			__	zoom to linked		__
play video, audio	__	undo last state		__	popup animation		__	edit more point

tasks:
copy item			OK	draw polygon		OK	polygon tools		OK	save document		OK	load document		OK
undo drawing		OK	drawing tools		OK	color palette		OK	item properties 	OK	picture alpha		OK
picture color		OK	send to back		OK	text alignment		OK	text stretch		OK	text properties		OK
network support		OK	grid align			OK	scroll bars			OK	item label			OK	item link			OK
set background		OK	line type			OK	save task			OK	paint undo			OK	change detect		OK
load note			OK	append note			OK	paint x,y			OK	vector x,y			OK	save vector			OK
save bitmap			OK	scroll to linked	OK	icon size			OK	html export			OK	select linked		OK
thumb nails			OK	item stretch/size	OK	bitmap border		OK	direct connect		__	take photo			OK
help				OK	state setting		OK	load font			OK	polygon templates	__	poly point delete	OK
show state strings	OK	remove empty vector OK	add arrow vector	OK	linked indicator	OK	select poly item	OK
static background	OK	static emblem		__	arrow draw order	OK	zoom to full		__	picture grid view	OK
add more texture	OK	check items on add	OK	save document as	OK	add capsule			OK	finish poly button	__
long tap indicator	__	button states		OK	save linked polygon	__

errors 1.01:
photo preview		OK	save html			OK	load text rotation	OK	reloaded paint		OK	linked object move	__
photo temp delete	OK	palette icon size	OK	photo alpha export	OK	export positions	__	splat text			OK
new background		OK	landscape doc		OK	palette cancel		OK	resize grid align	OK	save empty note		OK
rotate text			OK	vector bound 		OK	text border types	OK	text left margin	OK	texture browser		OK
remove empty item	OK	text stretch		OK	text color change	OK	error after photo	__	one line text load	OK
more line text		OK	menu button tablet	OK	copy poly line size OK	reload poly color	OK	text border font	OK
deleted liked item	OK	modify loaded paint	OK	load background		OK	paint undo count	OK	set input folder	OK
one word size		__	textedit long click	OK	stretched text		__	html back image		OK	

errors 1.02
append document		OK

*/
//TODO
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.landroo.toolbar.LabelTool;
import org.landroo.toolbar.MessageTool;
import org.landroo.toolbar.StateTool;
import org.landroo.toolbar.UserListTool;
import org.landroo.tools.CameraClass;
import org.landroo.tools.PolyDraw;
import org.landroo.tools.TextFormat;
import org.landroo.tools.VectorItem;
import org.landroo.tools.VideoClass;
import org.landroo.ui.UI;
import org.landroo.ui.UIInterface;

import org.landroo.http.HttpServer;
import org.landroo.http.WebClass;
import org.landroo.simnote.R;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class SimNoteActivity extends Activity implements UIInterface
{
	private static final String TAG = "SimNoteActivity";
	private static final float DEGTORAD = 0.0174532925199432957f;
	private static final float RADTODEG = 57.295779513082320876f;
	private static final int SCROLL_INTERVAL = 10;
	private static final int HTTP_PORT = 8484;// local http server port
	private static final String HTTP_NAME = "simnote";
	private static final int SCROLL_SIZE = 10;
	private static final int SCROLL_ALPHA = 500;
	
	private UI ui = null;
	private ScaleView scaleView;
	private SimNoteViewGroup simNoteView;
	private int displayWidth;
	private int displayHeight;
	
	private int papersSizeX = 4;
	private int papersSizeY = 4;
	
	private List<NoteItem> noteItemList = new ArrayList<NoteItem>();// main item list
	
	private ToolBarClass toolbarClass;
	
	private int tileSize = 80;
	private int round = 10;// rounded positioning
	private boolean showLabel = true;
	
	private float rotation;
	private float rx;
	private float ry;
	
	private float sX = 0;
	private float sY = 0;
	private float mX = 0;
	private float mY = 0;
	private double sAng;
	private double sDist;
	private int corner = 0;
	private boolean isDraw = false;
	private boolean afterMove = false;
	
	private float pictureWidth = 0;
	private float pictureHeight = 0;
	
	private float xPos;
	private float yPos;
	
	private boolean isZoom = false;
	private float zoomX = 1;
	private float zoomY = 1;
	
	private float scrollX = 0;
	private float scrollY = 0;
	private float scrollMul = 1;
	private Timer scrollTimer = null;

	private float xDest;
	private float yDest;
	private int halfX;
	private int halfY;
	private boolean scrollTo = false;
		
	private Bitmap backBitmap;
	private Drawable backDrawable;// background bitmap drawable					
	private int backColor = NoteItem.COLOR_WHITE;// background color
	private String backFileName = "grid.png";// background bitmap name
	private boolean staticBack = false;// fox or scrollable background 
	
	private Paint infoPaint = new Paint();
	private Paint scrollPaint = new Paint();
	
	private RectF rect;
	private int scrollAplha = SCROLL_ALPHA;
	
	private NoteItem lastSelectedItem = null;
	private NoteItem prevSelectedItem = null;
	
	private String fileName = "";// actual notation file name
	
	private WebClass webClass;
	private String userName = "User";
	//private String address = "landroo.dynu.com";
	//private String ipAddressPort = "192.168.0.116:8080";
	private String ipAddressPort = "192.168.0.191:8080";
	//private String ipAddressPort = "192.168.0.190:8080";
	private int errorCnt = 0;
	private String sNetError = "Error access server: ";
	private String sUserError = "Username alredy in use!";
	private String sUserSuccess = "Login success!";
	private String sNameError = "Please change your ninck name in settings!";
	private String sFirstError = "First you must login!";
	private String sAlreadyError = "You are alredy logged in!";
	private String sNoPartner = "No partner logged in!";

	private UserListTool userlistTool;
	private LabelTool labelTool;
	private MessageTool messageTool;
	private StateTool stateTool;
	
	private HttpServer http;
	
	private SurfaceView cameraView;
	private Rect cameraRect;
	private CameraClass cameraClass;
	
	private SurfaceView videoView;
	private Rect videoRect;
	private VideoClass videoClass;
	
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			int typ = msg.what / 10;
			int sub = msg.what % 10;
			
			xPos = scaleView.xPos();
			yPos = scaleView.yPos();
			
			String text = msg.getData().getString("text");
			if(text != null)
			{
				int id = msg.getData().getInt("id");
				switch(id)
				{
				case 1:
					break;
				}
			}
			else switch (typ)
			{
			case 1:// picture loaded
				addPictureItem(sub);
				break;
			case 2:// text added
				addTextItem(sub);
				break;
			case 3:// polygon added
				addPolyItem(sub);
				break;
			case 4:// finger paint added
				addPaintItem(sub);
				break;
			case 5:// select back picture or color
				handleBackGround(sub);
				break;
			case 6:// font popup
				handleFontPopup(sub);
				break;
			case 7:// main bar
				handleMainBar(sub);
				break;
			case 8:// picture selection tool list
				if(toolbarClass.fileTool.cancel == false)
				{
					String[] sPath = toolbarClass.fileTool.lastFile.split("\t");
					toolbarClass.pictureTool.lastFile = sPath[2];
				}
				toolbarClass.pictureTool.showPicturePoup(sub, 11);
				break;
			case 9:// Take a photo
				handleCamera(sub);
				break;
			case 10:// polygon
				handlePolyBar(sub);
				break;
			case 11:// finger paint
				handlePaintBar(sub);
				break;
			case 12:// network
				handleNetwork(sub);
				break;
			case 13:// show menu if menu button is not available
				SimNoteActivity.this.openOptionsMenu();
				break;
			case 14:// link note items
				handleLink(sub);
				break;
			case 15:// set item states
				handleState(sub);
				break;
			case 16:// append note file
				if(toolbarClass.fileTool.cancel == false)
				{
					loadItems(toolbarClass.fileTool.lastFile);
					new saveTask().execute();
				}
				break;
			case 17:// add vector draw
				addVectorItem(sub);
				break;
			case 18:// add message text to the document
				addMessageText(sub);
				break;
			case 19:// add caller user items to the document
				handleNetworkMessages(sub);
				break;
			case 20:// save paint or vector file
				handleFileSave(sub);
				break;
			}
		}
	};

	// main view group 
	private class SimNoteViewGroup extends ViewGroup
	{
		public SimNoteViewGroup(Context context)
		{
			super(context);
		}

		// draw items
		@Override
		protected void dispatchDraw(Canvas canvas)
		{
			if(isDraw) return;
			isDraw = true;
			
			drawBack(canvas, false);
			drawItems(canvas, false);
			drawInfo(canvas);
			drawScrollBars(canvas);
				
			super.dispatchDraw(canvas);
			
			isDraw = false;
			
			return;
		}

		// set the sliding windows
		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b)
		{
	    	View child;
	    	
	    	// top slider bar
	    	child = this.getChildAt(0);
	    	child.layout(toolbarClass.topViewRect.left, toolbarClass.topViewRect.top, toolbarClass.topViewRect.right, toolbarClass.topViewRect.bottom);
	    	
	    	// left slider bar
	    	child = this.getChildAt(1);
	    	child.layout(toolbarClass.leftViewRect.left, toolbarClass.leftViewRect.top, toolbarClass.leftViewRect.right, toolbarClass.leftViewRect.bottom);
	    	
	    	// camera surface view
	    	child = this.getChildAt(2);
	    	if(child != null) child.layout(cameraRect.left, cameraRect.top, cameraRect.right, cameraRect.bottom);

	    	// video surface view
	    	child = this.getChildAt(3);
	    	if(child != null) child.layout(videoRect.left, videoRect.top, videoRect.right, videoRect.bottom);
		}
		
		// measure sliding windows
		@Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
		{
			setMeasuredDimension(displayWidth, displayHeight);
	    	View child;

	    	// top slider bar
	    	child = this.getChildAt(0);
    		measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
    		
    		// left slider bar
	    	child = this.getChildAt(1);
    		measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Display display = getWindowManager().getDefaultDisplay();
		displayWidth = display.getWidth();
		displayHeight = display.getHeight();
		
		simNoteView = new SimNoteViewGroup(this);
		setContentView(simNoteView);
		
		infoPaint.setColor(0xFF444444);
		if(displayWidth < displayHeight) infoPaint.setTextSize(displayWidth / 20);
		else infoPaint.setTextSize(displayHeight / 20);
		
		scrollPaint.setColor(NoteItem.COLOR_GRAY);
		scrollPaint.setAntiAlias(true);
		scrollPaint.setDither(true);
		scrollPaint.setStyle(Paint.Style.STROKE);
		scrollPaint.setStrokeJoin(Paint.Join.ROUND);
		scrollPaint.setStrokeCap(Paint.Cap.ROUND);
		scrollPaint.setStrokeWidth(SCROLL_SIZE);
		
		ui = new UI(this);
		
		LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		toolbarClass = new ToolBarClass(this, simNoteView, layoutInflater, displayWidth, displayHeight, handler);
		
		simNoteView.addView(toolbarClass.topView);
		simNoteView.addView(toolbarClass.leftView);
		if(toolbarClass.leftView.isOpened()) toolbarClass.leftView.close();
		toolbarClass.leftView.setVisibility(View.INVISIBLE);
		toolbarClass.initGallery();

		// setup camera view
		cameraRect = new Rect(0, 0, displayWidth / 2, displayHeight / 2);
		cameraClass = new CameraClass((Context)this, handler, 92);
		cameraView = cameraClass.getPrewView();
		if(cameraView != null)
		{
			cameraView.setVisibility(View.INVISIBLE);
			simNoteView.addView(cameraView);
		}
		
		videoRect = new Rect(0, 0, displayWidth / 2, displayHeight / 2);
		videoClass = new VideoClass((Context)this, handler, 93);
		videoView = videoClass.getVideoView();
		if(videoView != null)
		{
			videoView.setVisibility(View.INVISIBLE);
			simNoteView.addView(videoView);
		}
		
		findViewById(R.id.gallery);
        
		scrollTimer = new Timer();
		scrollTimer.scheduleAtFixedRate(new ScrollTask(), 0, SCROLL_INTERVAL);
		
		sNetError = getResources().getString(R.string.net_error);
		sUserError = getResources().getString(R.string.user_error);
		sUserSuccess = getResources().getString(R.string.net_success);
		sNameError = getResources().getString(R.string.name_error);
		sAlreadyError = getResources().getString(R.string.already_error);
		sFirstError = getResources().getString(R.string.first_login);
		sNoPartner = getResources().getString(R.string.no_player);
		
		userlistTool = new UserListTool(this, simNoteView, layoutInflater, displayWidth, displayHeight, handler);
		labelTool = new LabelTool(this, simNoteView, layoutInflater, displayWidth, displayHeight, handler);
		messageTool = new MessageTool(this, simNoteView, layoutInflater, displayWidth, displayHeight, handler);
		stateTool = new StateTool(this, simNoteView, layoutInflater, displayWidth, displayHeight, handler);
		
		String sTmpFolder = getResources().getString(R.string.project_dir);
		File tmpFile = new File(Environment.getExternalStorageDirectory(), sTmpFolder);
		if (!tmpFile.exists())
		{
			// Failed to create directory
			if (!tmpFile.mkdirs()) Log.i(TAG, "Directory error!");
		}
	}
	
	private void initApp(int px, int py)
	{
		rx = displayWidth / 2;
		ry = displayHeight / 2;

		if(pictureWidth == 0 || papersSizeX != px) pictureWidth = displayWidth * papersSizeX;
		if(pictureHeight == 0 || papersSizeY != py) pictureHeight = displayHeight * papersSizeY;
		
		scaleView = new ScaleView(displayWidth, displayHeight, (int)pictureWidth, (int)pictureHeight, simNoteView);

		return;
	}
	
	@Override
	public void openOptionsMenu() 
	{
	    Configuration config = getResources().getConfiguration();

	    if((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE) 
	    {
	        int originalScreenLayout = config.screenLayout;
	        config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
	        super.openOptionsMenu();
	        config.screenLayout = originalScreenLayout;

	    } 
	    else 
	    {
	        super.openOptionsMenu();
	    }
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sim_note, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem login = menu.findItem(R.id.menu_login);
		MenuItem logout = menu.findItem(R.id.menu_logout);
		if (webClass != null && webClass.loggedIn)
		{
			login.setVisible(false);
			logout.setVisible(true);
		}
		else
		{
			login.setVisible(true);
			logout.setVisible(false);
		}
		
		boolean internet = WebClass.isConnected(this);
		
		MenuItem userList = menu.findItem(R.id.menu_list);
		if(internet)
		{
			login.setEnabled(true);
			logout.setEnabled(true);
			userList.setEnabled(true);
		}
		else
		{
			login.setEnabled(false);
			logout.setEnabled(false);
			userList.setEnabled(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
		case R.id.menu_append:// load or append note show thumb nails
			toolbarClass.fileTool.showFilePopup(160, ".note");
			return true;
		case R.id.menu_export:// export html
			exportItems();
			return true;
		case R.id.menu_new:// clear items
			newDocument();
			return true;
		// settings
		case R.id.menu_settings:
			Intent SettingsIntent = new Intent(this, SettingsScreen.class);
			startActivity(SettingsIntent);
			return true;
			// login
		case R.id.menu_login:
			login();
			return true;
		// logout
		case R.id.menu_logout:
			logout();
			return true;
		// user list
		case R.id.menu_list:
			userlist();
			return true;			
		}
		
		return false;
	}
	
	// main touch event
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//TODO maybe there is a better solution 
		int x = (int)event.getX();
		int y = (int)event.getY();
		if(toolbarClass.checkTopView(x, y)) return true;
		else if(toolbarClass.checkLeftView(x, y)) return true;
		else return ui.tapEvent(event);
	}	

	// touch down
	@Override
	public void onDown(float x, float y)
	{
		afterMove = false;
		scrollAplha = SCROLL_ALPHA;
		
		scaleView.onDown(x, y);
		
		sX = x / zoomX;
		sY = y / zoomY;
		
		mX = x / zoomX;
		mY = y / zoomY;
		
		for(NoteItem item:noteItemList)
		{	
			if(item != null)
			{
				switch(item.state)
				{
				case NoteItem.STATE_SELECT:
					if(lastSelectedItem == null) selectLastItem(item);
					break;
				case NoteItem.STATE_MOVE:
					if(round > 1)
					{
						item.px = (((int)(item.px / round)) * round);
						item.py = (((int)(item.py / round)) * round);
					}
					break;
				case NoteItem.STATE_ROTATE:
					if(sAng == 0)
						sAng = Utils.getAng(item.px * zoomX + xPos + item.getWidth() / 2, item.py * zoomY + yPos + item.getHeight() / 2, mX * zoomX, mY * zoomY);
					else
						item.nextState();
					break;
				case NoteItem.STATE_ZOOM:
					// calculate the distance between the center of the item and touch point
					if(sDist == 0)
						sDist = Utils.getDist(item.px * zoomX + xPos + item.getWidth() / 2, item.py * zoomY + yPos + item.getHeight() / 2, mX * zoomX, mY * zoomY);
					else
						item.nextState();
					break;
				case NoteItem.STATE_RESIZE:
					corner = item.isBorder(x - xPos, y - yPos, zoomX, zoomY, 30);
					break;
				case NoteItem.STATE_HAND_DRAW:
					item.fingerPaint.touch_start((mX * zoomX - (item.px * zoomX + xPos)) / zoomX, (mY * zoomY - (item.py * zoomY + yPos)) / zoomY);
					break;
				case NoteItem.STATE_POLY_DRAW:
					break;
				}
			}
		}
	}

	// end touch
	@Override
	public void onUp(float x, float y)
	{
		scaleView.onUp(x, y);
		
		scrollX = 0;
		scrollY = 0;
		scrollMul = 1;

		for(NoteItem item:noteItemList)
		{	
			switch(item.state)
			{
			case NoteItem.STATE_MOVE:
				afterMove = true;
				break;
			case NoteItem.STATE_ROTATE:
				afterMove = true;
				if(sAng == 0) item.state = NoteItem.STATE_NONE;
				break;
			case NoteItem.STATE_ZOOM:
				afterMove = true;
				if(sDist == 0) item.state = NoteItem.STATE_NONE;
				break;
			case NoteItem.STATE_RESIZE:
				afterMove = true;
				corner = 0;
				if(item.camera)
				{
					cameraClass.cameraPreview.stopPreview();
					cameraClass.cameraPreview.startPreview((int)lastSelectedItem.bound.width(), (int)lastSelectedItem.bound.height());
				}
				break;
			case NoteItem.STATE_HAND_DRAW:
				afterMove = true;
				item.fingerPaint.touch_up();
				break;
			case NoteItem.STATE_POLY_DRAW:
				afterMove = true;
				item.polyDraw.onUp();
				break;
			}
		}
		
		sAng = 0;
		sDist = 0;
		isZoom = false;

		return;
	}

	// on single tap
	@Override
	public void onTap(float x, float y)
	{
		scrollAplha = SCROLL_ALPHA;
		
		boolean bOK = true;
		for(NoteItem item:noteItemList)
		{	
			if(item.state == NoteItem.STATE_POLY_DRAW)
			{
				item.polyDraw.onTouch((mX * zoomX - (item.px * zoomX + xPos)) / zoomX, (mY * zoomY - (item.py * zoomY + yPos)) / zoomY);
				item.setBound(item.polyDraw.calculateRect());

				bOK = false;
				break;
			}
			else if(item.state == NoteItem.STATE_HAND_DRAW && item.isInside(x - xPos, y - yPos, zoomX, zoomY))
			{
				bOK = false;
				break;
			}
		}
		
		if(bOK) selectItem((int)(x - xPos), (int)(y - yPos));
	}

	// long tap
	@Override
	public void onHold(float x, float y)
	{
		scrollAplha = SCROLL_ALPHA;
		
		// finish polyline or go to linked
		if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_POLY_DRAW)
		{
			finishPolyDraw();
		}
		else
		{
			NoteItem note;
			String label;
	    	for(int i = noteItemList.size() - 1; i > -1; i--)
	    	{
	    		note = noteItemList.get(i);
	    		if(note.isInside(x - xPos, y - yPos, zoomX, zoomY))
				{
	    			if(note.getLinked() != null)
	    			{
	    				scrollToItem(note.getLinked());
	    			}
	    			label = note.getLabel();
	    			if(label.length() > 5 && label.substring(0, 5).equals("http:"))
					{
						Uri uri = Uri.parse(label);
						Intent browserIntent = new Intent(Intent.ACTION_VIEW);
					    browserIntent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
					    browserIntent.setData(uri);
					    startActivity(browserIntent);
					}
					break;
				}
	    	}
		}
		
    	return;
	}

	// move finger
	@Override
	public void onMove(float x, float y)
	{
		scrollAplha = SCROLL_ALPHA;
		
		mX = x / zoomX;
		mY = y / zoomY;
		
		boolean found = false;
		for(NoteItem item:noteItemList)
		{
			switch(item.state)
			{
			case NoteItem.STATE_MOVE:
				if(item.isInside(x - xPos, y - yPos, zoomX, zoomY))
				{
					found = true;
					moveSelectedItems(item);
				}
				break;
			case NoteItem.STATE_ROTATE:
				if(!item.isInside(x - xPos, y - yPos, zoomX, zoomY))
				{
					found = true;
					rotateItem(item);
				}
				break;
			case NoteItem.STATE_ZOOM:
				found = true;
				zoomItem(item);
				break;
			case NoteItem.STATE_RESIZE:
				// set offsets threshold
				int threshold = displayWidth > displayHeight ? displayHeight / 10: displayWidth / 10;
				if(item.isInsideBound(x - xPos, y - yPos, zoomX, zoomY, threshold, threshold))
				{
					resizeItem(item);
					found = true;
				}
				break;
			case NoteItem.STATE_HAND_DRAW:
				if(item.isInside(x - xPos, y - yPos, zoomX, zoomY))
				{
					found = true;
					item.fingerPaint.touch_move((mX * zoomX - (item.px * zoomX + xPos)) / zoomX, (mY * zoomY - (item.py * zoomY + yPos)) / zoomY);
				}
				break;
			case NoteItem.STATE_POLY_DRAW:
				if(item.polyDraw.midpointSelected || item.polyDraw.vertexSelected)
				{
					found = true;
					float px = (mX * zoomX - (item.px * zoomX + xPos)) / zoomX;
					float py = (mY * zoomY - (item.py * zoomY + yPos)) / zoomY;
					if(round > 1)
					{
						px = (((int)(px / round)) * round);
						py = (((int)(py / round)) * round);
					}
					item.polyDraw.movePoint(px, py);
					item.setBound(item.polyDraw.calculateRect());
				}
				break;
 			}
		}
		
		if(!found) scaleView.onMove(x, y);
		else simNoteView.postInvalidate();
		
		sX = mX;
		sY = mY;

		return;
	}

	// swipe
	@Override
	public void onSwipe(int direction, float velocity, float x1, float y1, float x2, float y2)
	{
		boolean bOK = true;
		for(NoteItem item:noteItemList)
		{	
			if(item.state == NoteItem.STATE_HAND_DRAW
			|| item.state == NoteItem.STATE_RESIZE
			|| item.state == NoteItem.STATE_ROTATE)
			{
				bOK = false;
				break;
			}			
		}
		
		if(bOK && !afterMove) scaleView.onSwipe(direction, velocity, x1, y1, x2, y2);
	}

	// on double tap set 100 zoom or finish polygon or jump to lined 
	@Override
	public void onDoubleTap(float x, float y)
	{
		boolean bOK = true;
		// finish polygon on double tap
		if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_POLY_DRAW)
		{
			lastSelectedItem.polyDraw.undoLast();
			finishPolyDraw();
			
			bOK = false;
		}			
		
		if(bOK)
		{
			scaleView.onDoubleTap(x, y);
			
			xPos /= zoomX;
			yPos /= zoomY;
			
			scaleView.setPos(xPos, yPos);
			
			zoomX = scaleView.getZoomX();
			zoomY = scaleView.getZoomY();
		}
		
		simNoteView.postInvalidate();

		return;
	}

	// on zoom
	@Override
	public void onZoom(int mode, float x, float y, float distance, float xdiff, float ydiff)
	{
		scaleView.onZoom(mode, x, y, distance, xdiff, ydiff);
		
		zoomX = scaleView.getZoomX();
		zoomY = scaleView.getZoomY();
		
		isZoom = true;
		
		return;
	}

	@Override
	public void onRotate(int mode, float x, float y, float angle)
	{
		//rx = x;
		//ry = y;
		//rotation = angle + 90;
	}

	@Override
	public void onFingerChange()
	{
	}
	
	// select linked recursion
	private void selectLinked(NoteItem item)
	{
		item.state = NoteItem.STATE_SELECT;
		if(item.getLinked() != null) selectLinked(item.getLinked());
	}
	
	// TODO select or deselect one or all item
    private void selectItem(int x, int y)
    {
    	boolean redraw = false;
    	boolean found = false;
    	NoteItem note;
    	Boolean sel = false;
    	// check the tap is inside an item
    	//for(NoteItem item:noteItemList)
    	for(int i = noteItemList.size() - 1; i > -1; i--)
    	{
    		note = noteItemList.get(i);
    		if(note.type == NoteItem.TYPE_VECTOR) sel = note.polyDraw.isInside(x, y, zoomX, zoomY, note.px, note.py);
    		else sel = note.isInsideBound(x, y, zoomX, zoomY, 1, 1);
    		if(sel)
    		{
    			note.nextState();
   				redraw = true;
   				found = true;
   				
   				if(note.state == NoteItem.STATE_SELECT)
   				{
   					toolbarClass.leftView.setVisibility(View.VISIBLE);
   					toolbarClass.setTools(note);
   					selectLastItem(note);
   					selectLinked(note);
   				}
   				else
   				{
   					if(toolbarClass.leftView.isOpened()) toolbarClass.leftView.close();
   					toolbarClass.leftView.setVisibility(View.INVISIBLE);
   				}
   				break;
    		}
    	}
    	
    	// if outside then deselect all item
    	boolean isDel = false;
    	if(!found)
    	{
        	for(NoteItem item:noteItemList)
        	{
        		// what to do on close item
        		if(item.state == NoteItem.STATE_RESIZE && item.stretch == false && (item.type == NoteItem.TYPE_BITMAP || item.type == NoteItem.TYPE_PAINT)) item.applySize(true);
        		else if(item.state == NoteItem.STATE_RESIZE && item.camera == true) cameraClass.takePhoto();
        		else if(item.state == NoteItem.STATE_RESIZE) item.applySize(false);
        		else if(item.state == NoteItem.STATE_ROTATE) item.applyRotate(false);
        		else if(item.state == NoteItem.STATE_ZOOM) item.applyScale(1);
        		else item.state = NoteItem.STATE_NONE;
        		
        		// delete empty items
        		if(item.type == NoteItem.TYPE_PAINT && item.fingerPaint.undoStates.size() == 0)
        		{
        			item.deleted = true;
        			isDel = true;
        		}
        		if(item.type == NoteItem.TYPE_VECTOR && item.polyDraw.points.size() == 1)
        		{
        			item.deleted = true;
        			isDel = true;
        		}

        	}
        	
        	if(isDel) deleteItem(null);
        	
        	selectLastItem(null);
        	
        	if(toolbarClass.leftView.isOpened()) toolbarClass.leftView.close();
        	toolbarClass.leftView.setVisibility(View.INVISIBLE);
        	
        	new saveTask().execute();
        	
        	redraw = true;
    	}
    	
    	if(redraw) simNoteView.postInvalidate();
    	
    	return;
    }
    
    // scroll task for scrolling background
	class ScrollTask extends TimerTask
	{
		public void run()
		{
			boolean redraw = false;
			
			// scroll to selected object
			if(scrollMul < .05f)
			{
				scrollTo = false;
				scrollX = 0;
				scrollY = 0;
			}
			else
			{
				if(scrollTo)
				{
					if((int)Math.abs(xDest - xPos) < halfX || (int)Math.abs(yDest - yPos) < halfY) scrollMul -= 0.05f;
					else scrollMul += 0.05f;
					redraw = true;
				}
			}
			
			// left and top scroll in zoomed
			if(xPos + scrollX < displayWidth - pictureWidth * zoomX || xPos + scrollX > 0) scrollX = 0;
			if(yPos + scrollY < displayHeight - pictureHeight * zoomY || yPos + scrollY > 0) scrollY = 0;
			
			// auto scroll paper
			if (scrollX != 0 || scrollY != 0)
			{
				xPos += scrollX * scrollMul;
				yPos += scrollY * scrollMul;
			
				if(scrollTo == false)
				{

					for(NoteItem item:noteItemList)
					{
						if(item.state == NoteItem.STATE_MOVE)
						{
							item.px -= scrollX * scrollMul / zoomX;
							item.py -= scrollY * scrollMul / zoomY;
							redraw = true;
						}
					}
					
					if(scrollMul < 10) scrollMul += 0.05f;
				}
				
				scaleView.setPos(xPos, yPos);
			}
	    	
	    	if(scrollAplha > 32)
	    	{
	    		scrollAplha--;
	    		if(scrollAplha > 255) scrollPaint.setAlpha(255);
	    		else scrollPaint.setAlpha(scrollAplha);
	    		redraw = true;
	    	}
	    	
	    	if(redraw) simNoteView.postInvalidate();
		}
	}
	
	// draw background
	private void drawBack(Canvas canvas, boolean thumb)
	{
		if(backDrawable != null)
		{
			// static back or tiles
			if(staticBack)
			{
				backDrawable.setBounds(0, 0, (int)(displayWidth), (int)(displayHeight));
				backDrawable.draw(canvas);
			}
			else for(float x = 0; x < pictureWidth; x += tileSize)
			{
				for(float y = 0; y < pictureHeight; y += tileSize)
				{
					// distance of the tile center from the rotation center
					final float dis = (float)Utils.getDist(rx * zoomX, ry * zoomY, (x + tileSize / 2) * zoomX, (y + tileSize / 2) * zoomY);
					// angle of the tile center from the rotation center
					final float ang = (float)Utils.getAng(rx * zoomX, ry * zoomY, (x + tileSize / 2) * zoomX, (y + tileSize / 2) * zoomY);
					
					// coordinates of the block after rotation
					final float cx = dis * (float)Math.cos((rotation + ang) * DEGTORAD) + rx * zoomX + xPos;
					final float cy = dis * (float)Math.sin((rotation + ang) * DEGTORAD) + ry * zoomY + yPos;
					
					if(cx >= -tileSize && cx <= displayWidth + tileSize && cy >= -tileSize && cy <= displayHeight + tileSize)
					{
						backDrawable.setBounds(0, 0, (int)(tileSize * zoomX) + 1, (int)(tileSize * zoomY) + 1);
	
						canvas.save();
						//canvas.rotate(tile.tilRot, ((tile.offPosX + tile.tilPosX) * zoomX) + xPos + tile.stoneBitmap.getWidth() * (zoomX) / 2, 
						//		((tile.offPosY + tile.tilPosY) * zoomY) + yPos + tile.stoneBitmap.getHeight() * zoomY / 2);
						canvas.rotate(rotation, rx * zoomX + xPos, ry * zoomY + yPos);
						canvas.translate(x * zoomX + xPos, y * zoomY + yPos);
						backDrawable.draw(canvas);
						canvas.restore();
					}
				}
			}
		}
		else
		{
			canvas.drawColor(backColor);
		}

		return;
	}
	
	// draw items
	private void drawItems(Canvas canvas, boolean thumb)
	{
		float dx;
		float dy;
		float ix;
		
		if (scaleView != null)
		{
			xPos = scaleView.xPos();
			yPos = scaleView.yPos();
		}
		
		for(NoteItem item:noteItemList)
		{
			if(item != null)
			{
				dx = item.px * zoomX + xPos;
				dy = item.py * zoomY + yPos;
				//Log.i(TAG, "" + dx + " " + dy);
				
				if(dx > -item.getWidth() * zoomX && dx < displayWidth && dy > -item.getHeight() * zoomY && dy < displayHeight)
				{
					if(item.drawable != null) item.drawable.setBounds(0, 0, (int)(item.getWidth() * zoomX), (int)(item.getHeight() * zoomY));
					
					rect = item.zoomRect(zoomX, zoomY);

					canvas.save();
					canvas.rotate(item.mRotation, dx + item.getWidth() * zoomX / 2, dy + item.getHeight() * zoomY / 2);
					canvas.translate(dx, dy);
					//canvas.rotate(item.deg, item.ox, item.oy);
					
					// draw item
					if(item.type == NoteItem.TYPE_VECTOR)
					{
						// draw vector
						item.polyDraw.drawPolygon(canvas, zoomX, zoomY);
					}
					else
					{
						// draw paint
						if(item.drawable != null) item.drawable.draw(canvas);
						else Log.i(TAG, "Item error: " + item.type);

						// draw camera preview
						if(item.camera && item.type == NoteItem.TYPE_BITMAP)
						{
							cameraRect.left = (int)(dx + item.pathSize); 
							cameraRect.top = (int)(dy + item.pathSize);
							cameraRect.right = (int)(dx + item.bound.width() * zoomX- item.pathSize * 2); 
							cameraRect.bottom = (int)(dy + item.bound.height() * zoomY - item.pathSize * 2);
							cameraView.layout(cameraRect.left, cameraRect.top, cameraRect.right, cameraRect.bottom);
						}
					}
					
					// draw bound
					if(item.state == NoteItem.STATE_SELECT)
					{
						canvas.drawRoundRect(rect, 20, 20, item.selectPaint);
					}
					else if(item.state == NoteItem.STATE_MOVE)
					{
						canvas.drawRoundRect(rect, 20, 20, item.movePaint);
					}
					else if(item.state == NoteItem.STATE_ROTATE)
					{
						// border
						canvas.drawRoundRect(rect, 20, 20, item.rotatePaint);
						// center
						canvas.drawCircle(item.getWidth() / 2 * zoomX, item.getHeight() / 2 * zoomY, 10, item.pointPaint);// mid point
					}
					else if(item.state == NoteItem.STATE_ZOOM)
					{
						// border
						canvas.drawRoundRect(rect, 20, 20, item.zoomPaint);
						// center
						canvas.drawCircle(item.getWidth() / 2 * zoomX, item.getHeight() / 2 * zoomY, 10, item.pointPaint);
					}
					else if(item.state == NoteItem.STATE_RESIZE)
					{
						canvas.drawRoundRect(rect, 20, 20, item.resizePaint);
						if(!item.stretch)
						{
							// corners
							canvas.drawCircle(rect.left, rect.top, 10, item.pointPaint);
							canvas.drawCircle(rect.right, rect.top, 10, item.pointPaint);
							canvas.drawCircle(rect.left, rect.bottom, 10, item.pointPaint);
						}
						// right bottom corner
						canvas.drawCircle(rect.right, rect.bottom, 10, item.pointPaint);
					}
					else if(item.type == NoteItem.TYPE_PAINT && item.state == NoteItem.STATE_HAND_DRAW)
					{
						item.fingerPaint.Draw(canvas);
						canvas.drawRoundRect(rect, 20, 20, item.drawPaint);
					}
					else if(item.type == NoteItem.TYPE_VECTOR && item.state == NoteItem.STATE_POLY_DRAW)
					{
						// midpoints
						item.polyDraw.drawMidPoints(canvas, zoomX, zoomY);
						// corners
						item.polyDraw.drawVertices(canvas, zoomX, zoomY);
						
						canvas.drawRoundRect(rect, 20, 20, item.drawPaint);
					}
					
					// label
					ix = 0;
					if(rect.width() > item.labelSize) ix = (rect.width() - item.labelSize) / 2;
					if(item.getLabel() != "" && showLabel) canvas.drawText(item.getLabel(), ix, rect.bottom + infoPaint.getTextSize(), infoPaint);
					if(item.getLinked() != null && showLabel) canvas.drawText("+", 0, 0, infoPaint);

					canvas.restore();
					
					// show finger point
					if(item.state == NoteItem.STATE_ROTATE)
					{
						canvas.drawLine(dx + item.getWidth() / 2 * zoomX, dy + item.getHeight() / 2 * zoomY, mX * zoomX, mY * zoomY, item.rotatePaint);
						// touch point
						canvas.drawCircle(mX * zoomX, mY * zoomY, 10, item.pointPaint);
					}
					else if(item.state == NoteItem.STATE_ZOOM)
					{
						canvas.drawLine(dx + item.getWidth() / 2 * zoomX, dy + item.getHeight() / 2 * zoomY, mX * zoomX, mY * zoomY, item.zoomPaint);
						// touch point
						canvas.drawCircle(mX * zoomX, mY * zoomY, 10, item.pointPaint);
					}
				}
			}
		}
		
		return;
	}

	// show item information
	private void drawInfo(Canvas canvas)
	{
		if(isZoom)
		{
			canvas.drawText((int)(zoomX * 100) + "%", 10, 40, infoPaint);
		}
		
		if(lastSelectedItem != null)
		{
			String st = "";
			switch(lastSelectedItem.state)
			{
			case NoteItem.STATE_SELECT:
				st = getResources().getString(R.string.select);
				break;
			case NoteItem.STATE_MOVE:
				st = getResources().getString(R.string.move);
				canvas.drawText("x: " + (int)lastSelectedItem.px, 10, 40, infoPaint);
				canvas.drawText("y: " + (int)lastSelectedItem.py, 10, 80, infoPaint);
				break;
			case NoteItem.STATE_ROTATE:
				st = getResources().getString(R.string.rotate);
				canvas.drawText((int)lastSelectedItem.mRotation + "°", 10, 40, infoPaint);
				break;
			case NoteItem.STATE_ZOOM:
				st = getResources().getString(R.string.zoom);
				canvas.drawText((int)(lastSelectedItem.mZoom * 100) + "%", 10, 40, infoPaint);
				break;
			case NoteItem.STATE_RESIZE:
				st = getResources().getString(R.string.resize);
				canvas.drawText("w: " + (int)lastSelectedItem.iw, 10, 40, infoPaint);
				canvas.drawText("h: " + (int)lastSelectedItem.ih, 10, 80, infoPaint);
				break;
			case NoteItem.STATE_HAND_DRAW:
				st = getResources().getString(R.string.paint);
				canvas.drawText("x: " + (int)lastSelectedItem.fingerPaint.lastX, 10, 40, infoPaint);
				canvas.drawText("y: " + (int)lastSelectedItem.fingerPaint.lastY, 10, 80, infoPaint);
				break;
			case NoteItem.STATE_POLY_DRAW:
				st = getResources().getString(R.string.poly_draw);
				canvas.drawText("x: " + (int)lastSelectedItem.polyDraw.lastX, 10, 40, infoPaint);
				canvas.drawText("y: " + (int)lastSelectedItem.polyDraw.lastY, 10, 80, infoPaint);
				break;
			}
			
			if(!st.equals("")) canvas.drawText(st, displayWidth - infoPaint.measureText(st) - 10, 40, infoPaint);
		}
		
		return;
	}
	
	// show position indicators
	private void drawScrollBars(Canvas canvas)
	{
		float x, y;
		float xSize = displayWidth / ((pictureWidth * zoomX) / displayWidth);
		float ySize = displayHeight / ((pictureHeight * zoomY) / displayHeight);

		x = (displayWidth / (pictureWidth * zoomX)) * -xPos;
		y = displayHeight - SCROLL_SIZE - 2;
		canvas.drawLine(x, y, x + xSize, y, scrollPaint);

		x = displayWidth - SCROLL_SIZE - 2;
		y = (displayHeight / (pictureHeight * zoomY)) * -yPos;
		canvas.drawLine(x, y, x, y + ySize, scrollPaint);
	}

	// on move - relocate item
	private void moveItem(NoteItem item)
	{
		float dx = mX - sX;
		float dy = mY - sY;
		
		if(round > 1)
		{
			dx = (((int)(mX / round)) * round) - (((int)(sX / round)) * round);
			dy = (((int)(mY / round)) * round) - (((int)(sY / round)) * round);
		}
		
		if(item.state == NoteItem.STATE_SELECT)
		{
			item.px += dx;
			item.py += dy;
			
			return;
		}
				
		scrollX = 0;
		scrollY = 0;
		
		if (pictureWidth > displayWidth || pictureHeight > displayHeight)
		{
			// item inside the paper
			if(xPos + (item.px * zoomX) + dx > pictureWidth && dx > 0) dx = 0;
			if(xPos + (item.px * zoomX) + dx + round < 0 && dx < 0) dx = 0;
			if(yPos + (item.py * zoomY) + dy > pictureHeight && dy > 0) dx = 0;
			if(yPos + (item.py * zoomY) + dy + round < 0 && dy < 0) dy = 0;
			
			float scx = scrollX;
			float scy = scrollY;

			// scroll background under item
			if(xPos + (item.px + item.getWidth()) * zoomX >= displayWidth) scrollX = -1f;// scroll left
			if(xPos + item.px * zoomX <= 0) scrollX = 1f;// scroll right
			if(yPos + (item.py + item.getHeight()) * zoomY >= displayHeight) scrollY = -1f;// scroll up
			if(yPos + item.py * zoomY <= 0) scrollY = 1f;// scroll down
		
			if(scx != scrollX) scrollMul = 1;
			if(scy != scrollY) scrollMul = 1;
		}
		else
		{
			//TODO if picture smaller than the display
			//if(xPos + dx > displayWidth - pictureWidth || xPos + dx < 0) dx = 0;
			//if(yPos + dy > displayHeight - pictureHeight || yPos + dy < 0) dy = 0;
		}
		
		item.px += dx;
		item.py += dy;
		
		return;
	}
	
	// on move - rotate item
	private void rotateItem(NoteItem item)
	{
		double dAng = Utils.getAng(item.px * zoomX + xPos + item.getWidth() / 2, item.py * zoomY + yPos + item.getHeight() / 2, mX * zoomX, mY * zoomY);
		float max = round;
		if(max > 90) max = 90;
		if(max > 1) dAng = (((int)(dAng / max)) * max);
		item.mRotation += (float)(dAng - sAng);
		sAng = dAng;
		
		return;
	}
	
	// on move - zoom item
	private void zoomItem(NoteItem item)
	{
		// TODO soft zoom
		float px = (item.px + item.iw / 2) * zoomX + xPos;
		float py = (item.py + item.ih / 2) * zoomY + yPos;
		
		if(round > 1)
		{
			px = (((int)(px / round)) * round);
			py = (((int)(py / round)) * round);
		}
		
		double dDist = Utils.getDist(px, py, mX * zoomX, mY * zoomY) / ((item.iw + item.ih) / 2);
		if(dDist > 0.5f && dDist < 2)
		{
			float w1 = item.getWidth();
			float h1 = item.getHeight();
			
			item.mZoom = (float)dDist;
			
			float w2 = item.getWidth();
			float h2 = item.getHeight();

			item.px -= (w2 - w1) / 2;
			item.py -= (h2 - h1) / 2;
			
			if(item.type == NoteItem.TYPE_VECTOR) item.polyDraw.Scale(item.mZoom);
		}
		sDist = dDist;
		
		return;
	}
	
	// on move - resize item
	private void resizeItem(NoteItem item)
	{
		float dx = (mX - sX) / item.mZoom;
		float dy = (mY - sY) / item.mZoom;
		float w, h;
		
		if(round > 1)
		{
			dx = ((((int)(mX / round)) * round) - (((int)(sX / round)) * round)) / item.mZoom;
			dy = ((((int)(mY / round)) * round) - (((int)(sY / round)) * round)) / item.mZoom;
		}
		
		switch(item.type)
		{
		case NoteItem.TYPE_BITMAP:// image
			w = item.bound.width() + dx;
			h = item.bound.height() + dy;
			if(w <= item.ow / 5) dx = 0;
			if(h <= item.oh / 5) dy = 0;

			if(item.stretch)
			{
				if(corner == NoteItem.RIGHT_BOTTOM)
				{
					// zoomed, rotated
					item.bound.right += dx;
					item.bound.bottom += dy;
					
					item.iw = item.bound.width();
					item.ih = item.bound.height();
				}
			}
			else
			{
				// TODO second tap
				switch(corner)
				{
				case NoteItem.LEFT_TOP:
					item.bound.left += dx;
					item.bound.top += dy;
					break;
				case NoteItem.RIGHT_TOP:
					item.bound.right += dx;
					item.bound.top += dy;
					break;
				case NoteItem.RIGHT_BOTTOM:
					item.bound.right += dx;
					item.bound.bottom += dy;
					break;
				case NoteItem.LEFT_BOTTOM:
					item.bound.left += dx;
					item.bound.bottom += dy;
					break;
				}				
			}
			break;
		case NoteItem.TYPE_TEXT:// text
			if(corner == NoteItem.RIGHT_BOTTOM)
			{
				w = item.bound.width() + dx;
				h = item.bound.height() + dy;
				if(w <= item.ow / 5) dx = 0;
				if(h <= item.oh / 5) dy = 0;
				
				// realign the text or stretch bitmap 
				if(item.stretch == false)
				{
					TextFormat tf = new TextFormat(this, (int)(item.iw + dx), (int)(item.ih + dy), item.textAlign);
					tf.setPaint(item.font, item.foreColor, item.backColor);
					tf.frameType = item.frameType;
					Bitmap bmp = tf.formatText(item.text, true);
					if(bmp != null) item.setBitmap(bmp, true);
				}
				else
				{
					item.bound.right += dx;
					item.bound.bottom += dy;
					
					item.iw = item.bound.width();
					item.ih = item.bound.height();
				}
			}
			break;
		case NoteItem.TYPE_PAINT:// finger paint
			w = item.bound.width() + dx;
			h = item.bound.height() + dy;
			if(w <= item.iw / 5) dx = 0;
			if(h <= item.ih / 5) dy = 0;
			
			if(item.stretch)
			{
				if(corner == NoteItem.RIGHT_BOTTOM)
				{
					// zoomed, rotated
					item.bound.right += dx;
					item.bound.bottom += dy;
					
					item.iw = item.bound.width();
					item.ih = item.bound.height();
				}
			}
			else
			{
				// TODO second tap
				switch(corner)
				{
				case NoteItem.LEFT_TOP:
					item.bound.left += dx;
					item.bound.top += dy;
					break;
				case NoteItem.RIGHT_TOP:
					item.bound.right += dx;
					item.bound.top += dy;
					break;
				case NoteItem.RIGHT_BOTTOM:
					item.bound.right += dx;
					item.bound.bottom += dy;
					break;
				case NoteItem.LEFT_BOTTOM:
					item.bound.left += dx;
					item.bound.bottom += dy;
					break;
				}
			}
			break;
		case NoteItem.TYPE_VECTOR:// polygon
			w = item.bound.width() + dx;
			h = item.bound.height() + dy;
			if(w <= 10) dx = 0;
			if(h <= 10) dy = 0;
			
			item.bound.right += dx;
			item.bound.bottom += dy;
			
			float xRat = item.bound.width() / item.iw;
			float yRat = item.bound.height() / item.ih;
			
			item.polyDraw.ReSize(xRat, yRat);			
			break;
		}
		
		return;
	}
	
	// delete a note item
	private synchronized void deleteItem(NoteItem selItem)
	{
		if(lastSelectedItem == null)
		{
			List<NoteItem> newList = new ArrayList<NoteItem>();
			List<NoteItem> list = new ArrayList<NoteItem>();
			for(NoteItem li:noteItemList)
			{
				if(li.state == NoteItem.STATE_SELECT)
				{
					list.add(li);
					if(li.linkerID.equals("") == false) updLiked(li.linkerID);
				}
				else if(li.deleted == false) newList.add(li);
				else if(li.linkerID.equals("") == false) updLiked(li.linkerID);
			}
			noteItemList.removeAll(list);
			noteItemList = newList; 
		}
		else
		{
			List<NoteItem> newList = new ArrayList<NoteItem>();
			for(NoteItem li:noteItemList)
			{
				if(li != selItem && li.deleted == false) newList.add(li);
				else if(li.linkerID.equals("") == false) updLiked(li.linkerID);
			}
			noteItemList.remove(selItem);
			noteItemList = newList;
		}
		
		lastSelectedItem = null;
		prevSelectedItem = null;
		
		return;
	}
	
	private void updLiked(String id)
	{
		for(NoteItem linker:noteItemList)
		{
			Log.i(TAG, linker.id + " " + id);
			if(linker.id.equals(id))
			{
				linker.setLinked(null);
				break;
			}
		}

		return;
	}

	// stop activity
	@Override
	public void onStop()
	{
		if(webClass != null && webClass.loggedIn) webClass.logout();
		if(http != null) http.stop();
		if(cameraClass != null) cameraClass.mCamera.release();

		super.onStop();
	}
	
	// pause activity
	@Override
	public synchronized void onPause()
	{
		saveState();
		if (webClass != null && webClass.loggedIn) logout();
		new saveTask().execute();

		super.onPause();
	}
	
	// resume activity
	@Override
	public synchronized void onResume()
	{
		loadState();
		
		super.onResume();
	}
	
	// login to the game server
	private void login()
	{
		if (http == null)
		{
			try
			{
				http = new HttpServer(HTTP_PORT, handler);
			}
			catch (Exception e)
			{
				Log.i(TAG, e.getMessage());
			}
		}
		
		if(webClass == null) webClass = new WebClass(ipAddressPort, HTTP_NAME, userName, handler, HTTP_PORT);

		if (userName.equals("User")) Toast.makeText(this, sNameError, Toast.LENGTH_LONG).show();
		else if (webClass.loggedIn) Toast.makeText(this, sAlreadyError, Toast.LENGTH_LONG).show();
		else webClass.login();

		return;
	}
	
	// userlist
	private void userlist()
	{
		if (!webClass.loggedIn) Toast.makeText(this, sFirstError, Toast.LENGTH_LONG).show();
		else if(webClass.userList.size() > 0) userlistTool.showUserlistPopup(webClass.getUserList(), webClass.activeUsers);
		else Toast.makeText(this, sNoPartner, Toast.LENGTH_LONG).show();

		return;
	}
	
	// logout
	private void logout()
	{
		if (http != null) http.stop();
		if (webClass != null && webClass.loggedIn) webClass.logout();

		return;
	}
	
	// save document in the background
	private class saveTask extends AsyncTask<String, Integer, Long>
	{
		protected Long doInBackground(String... sParams)
		{
			// save document
			saveThumbNail();
			saveItems(false);
			
			// send changes to partners
			if(webClass != null && webClass.loggedIn && webClass.activeUsers.size() > 0) webClass.Update(saveItems(true));

			return (long)0;
		}
	}
	
	// save item list to json format
	private synchronized String saveItems(boolean changed)
	{
		StringBuilder sb = new StringBuilder();

		String back;
		if(backDrawable != null)
		{
			if(backFileName.indexOf("/") == -1)
			{
				String projectDir = getResources().getString(R.string.project_dir);
				backFileName = Environment.getExternalStorageDirectory().getPath() + "/" + projectDir + "/textures/" + backFileName;
				Bitmap bitmap = BitmapFactory.decodeFile(backFileName);
				back = Utils.bitmapToBase64(bitmap);
			}
			else
			{
				back = Utils.bitmapToBase64(backBitmap);
			}
		}
		else back = "#" + String.format("%X", backColor);
		
		boolean bFirst = true;
		int cnt = 0;
		sb.append("{");
		sb.append("\"width\":\"");
		sb.append(pictureWidth);
		sb.append("\",\"height\":\"");
		sb.append(pictureHeight);
		sb.append("\",\"background\":\"");
		sb.append(back);
		sb.append("\",\"items\":[");
		for(NoteItem item:noteItemList)
		{
			if(!bFirst) sb.append(",");
			if(changed)
			{
				if(item.changed)
				{
					sb.append(item.saveItem());
					cnt++;
					bFirst = false;
				}
			}
			else
			{
				sb.append(item.saveItem());
				cnt++;
				bFirst = false;
			}
		}
		sb.append("]}");
		
		// clear if no item
		if(cnt == 0 && changed == true) sb = new StringBuilder();
		
		if(changed == false)
		{	
			File file = new File(fileName);
			if(fileName.equals("") || !file.exists())
			{
				file = toolbarClass.pictureTool.getOutputMediaFile("Note", ".note");
				String projectDir = getResources().getString(R.string.project_dir);
				fileName = Environment.getExternalStorageDirectory().getPath() + "/" + projectDir + "/" +  file.getName();
			}
			
			file = new File(fileName);
			fileName = file.getAbsolutePath();
			
			try
			{
				FileOutputStream out = new FileOutputStream(file);
				out.write(sb.toString().getBytes());
				out.flush();
				out.close();
			}
			catch (Exception e)
			{
				Log.i(TAG, e.toString());
			}
		}
		
		return sb.toString();
	}
	
	// load item from file
	private void loadItems(String fName)
	{
		File file = new File(fName);
		if(!file.exists())
		{
			String projectDir = getResources().getString(R.string.project_dir);
			fName = Environment.getExternalStorageDirectory().getPath() + "/" + projectDir + fName;
		}
		
		// load last document
		if(file.exists())
		{
			JsonClass jc = new JsonClass(this);
			jc.parseFile(fName, noteItemList, userName);
			
			pictureWidth = jc.width;
			pictureHeight = jc.height;
			String back = jc.backBmp;
			if(back != null && !back.equals("") && !back.equals("null"))
			{
				if(back.substring(0, 1).equals("#")) setBack(back);
				else
				{
					try
					{
						byte[] imageAsBytes = Base64.decode(back.getBytes(), Base64.DEFAULT);
						Bitmap bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
						backBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
			        	Canvas canvas = new Canvas(backBitmap);
			        	canvas.drawBitmap(bitmap, 0, 0, new Paint());
			        	bitmap.recycle();
						backDrawable = new BitmapDrawable(backBitmap);
						backDrawable.setBounds(0, 0, tileSize, tileSize);
						backFileName = "/";
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
			}
			else
			{
				String dir = getResources().getString(R.string.project_dir);
				String grid = Environment.getExternalStorageDirectory().getPath() + "/" + dir + "/textures/grid.png";
				setBack(grid);
			}
			
			simNoteView.postInvalidate();
		}
		else
		{
			Log.i(TAG, "File not foud: " + fName);
		}
		
		return;
	}
	
	// save values to preferences 
	private void saveState()
	{
		SharedPreferences settings = getSharedPreferences("org.landroo.simnote_preferences", MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("fileName", fileName);
		editor.putString("userName", userName);
		editor.putInt("roundPos", round);
		editor.putInt("tileSize", tileSize);
		editor.putInt("papersSizeX", papersSizeX);
		editor.putInt("papersSizeY", papersSizeY);
		editor.putBoolean("showLabel", showLabel);
		editor.commit();

		return;
	}
	
	// load values form preferences
	private void loadState()
	{
		boolean change = false;
		SharedPreferences settings = getSharedPreferences("org.landroo.simnote_preferences", MODE_PRIVATE);
		
		String str;
		int i;
		boolean b;

		str = settings.getString("fileName", "");
		if (!this.fileName.equals(str)) change = true;
		this.fileName = str;
		
		str = settings.getString("userName", "User");
		if (!this.userName.equals(str)) change = true;
		this.userName = str;
		if(webClass != null) webClass.myname = this.userName;
		
		str = settings.getString("server", "www.landroo.dynu.com");
		if (!this.ipAddressPort.equals(str)) change = true;
		this.ipAddressPort = str;
		
		i = settings.getInt("roundPos", 1);
		if (this.round != i) change = true;
		this.round = i;
		
		i = settings.getInt("tileSize", 80);
		if (this.tileSize != i) change = true;
		this.tileSize = i;
		
		i = settings.getInt("papersSizeX", 4);
		int px = this.papersSizeX;
		if (this.papersSizeX != i) change = true;
		this.papersSizeX = i;
		
		i = settings.getInt("papersSizeY", 4);
		int py = this.papersSizeY;
		if (this.papersSizeY != i) change = true;
		this.papersSizeY = i;
		
		i = settings.getInt("backColor", NoteItem.COLOR_WHITE); 
		if (this.backColor != i) change = true;
		this.backColor = i;
		
		b = settings.getBoolean("showLabel", true); 
		if (this.showLabel != b) change = true;
		this.showLabel = b;
		
		b = settings.getBoolean("staticBackground", false); 
		if (this.staticBack != b) change = true;
		this.staticBack = b;

		if (change)
		{
			noteItemList.clear();
			loadItems(fileName);
		}
		
		initApp(px, py);
		
		return;
	}
	
	// show a message from a partner
	private synchronized void processMessage()
	{
		if(webClass.messages.size() > 0)
		{
			String param = webClass.messages.get(0).replace("\t", ": ");
			messageTool.showMessagePoup(param, 181, 182, 4);
		}
	}
	
	// show an invitation
	private synchronized void showInvitation()
	{
		if(webClass.invites.size() > 0)
		{
			String inv = getResources().getString(R.string.invite_you);
			String user = webClass.invites.get(0).split("\t")[0];
			messageTool.showMessagePoup(user + " " + inv, 191, 192, 1);
		}
	}

	// process invite accept
	private void processInvite()
	{
		if(webClass.invites.size() > 0)
		{
			String invite = webClass.invites.get(0);
			String[] data = invite.split("\t");
			String user = data[0];
			String items = data[1];
			
			webClass.responseInvite(user, saveItems(false));
			webClass.activeUsers.add(user);
			
			JsonClass jc = new JsonClass(this);
			jc.parseArray(items, noteItemList, user);
			
			// if paper bigger than mine
			if(jc.width > pictureWidth || jc.height > pictureHeight) 
			{
				pictureWidth = jc.width;
				pictureHeight = jc.height;
				scaleView.setSize(displayWidth, displayHeight, (int)pictureWidth, (int)pictureHeight);
			}
			
			String back = jc.backBmp;
			if(back.substring(0, 1).equals("#")) setBack(back);
			else
			{
				try
				{
					byte[] imageAsBytes = Base64.decode(back.getBytes(), Base64.DEFAULT);
					Bitmap bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
					backBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		        	Canvas canvas = new Canvas(backBitmap);
		        	canvas.drawBitmap(bitmap, 0, 0, new Paint());
		        	bitmap.recycle();
					backDrawable = new BitmapDrawable(backBitmap);
					backDrawable.setBounds(0, 0, tileSize, tileSize);
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
			
			if(jc.lastItem != null)
			{
				//TODO changed item position
				scrollToItem(jc.lastItem);
			
				simNoteView.postInvalidate();
			}
		}
	}
	
	// process response
	private void processResponse()
	{
		for(int i = 0; i < webClass.responses.size(); i++)
		{
			String invite = webClass.responses.get(i);
			String[] data = invite.split("\t"); 
			String user = data[0];
			String items = data[1];
			
			webClass.activeUsers.add(user);
			
			String msg = user + " " + getResources().getString(R.string.invite_accept);
			Toast.makeText(SimNoteActivity.this, msg, Toast.LENGTH_LONG).show();
			
			JsonClass jc = new JsonClass(this);
			jc.parseArray(items, noteItemList, user);
			
			simNoteView.postInvalidate();
		}
	}
	
	// process update
	private void processUpdate()
	{
		boolean refresh = false;
		for(int i = 0; i < webClass.updates.size(); i++)
		{
			String update = webClass.updates.get(i);
			String[] data = update.split("\t");
			if(data.length == 2)
			{
				String user = data[0];
				String items = data[1];
				
				String msg = user +  getResources().getString(R.string.update);
				Toast.makeText(SimNoteActivity.this, msg, Toast.LENGTH_LONG).show();
				
				JsonClass jc = new JsonClass(this);
				jc.parseArray(items, noteItemList, user);
				
				refresh = true;
			}
			else Log.i(TAG, "refresh error: " + update);
		}
		if(refresh) simNoteView.postInvalidate();
		
		return;
	}
	
	// move linked items in recursion
	private void moveSelectedItems(NoteItem item)
	{
		moveItem(item);
		if(item.getLinked() != null) moveSelectedItems(item.getLinked());
	}
	
	// set last selected item
	private void selectLastItem(NoteItem item)
	{
		prevSelectedItem = lastSelectedItem;
		lastSelectedItem = item;
		if(item == null) prevSelectedItem = null;
	}
	
	// scroll to center the linked item
	private void scrollToItem(NoteItem item)
	{
		// position of the linked item
		xDest = (displayWidth / 2) - (item.px * zoomX) - (item.bound.width() / 2);
		yDest = (displayHeight / 2) - (item.py * zoomY) - (item.bound.height() / 2);
		//scaleView.setPos(xDest, yDest);
		
		// velocity
		halfX = (int) Math.abs((xDest - xPos) / 2);
		halfY = (int) Math.abs((yDest - yPos) / 2);

		// x, y steps
		scrollX = (xDest - xPos) / 200;
		scrollY = (yDest - yPos) / 200;
		
		//Log.i(TAG, "" + xDest + " " + yDest + " " + xPos + " " + yPos + " " + (xDest - (xPos / zoomX)) + " " + (yDest - (yPos / zoomY)));
		//Log.i(TAG, "" + scrollX + " " + scrollY + " " + halfX + " " + halfY);

		scrollMul = .05f;
		
		scrollTo = true;
	}
	
	// load background image or set color
	private void setBack(String back)
	{
		if(back.subSequence(0, 1).equals("#"))
		{
			// set color
			long color = Long.parseLong(back.substring(1), 16);
			backColor = (int)color;
			backDrawable = null;
		}
		else
		{
			// load file
			File file = new File(back);
			if(file.exists()) backBitmap = BitmapFactory.decodeFile(back);
			else backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.grid);
	
			backDrawable = new BitmapDrawable(backBitmap);
			backDrawable.setBounds(0, 0, tileSize, tileSize);
		}
		
		simNoteView.postInvalidate();
	}
	
	// save document thumb nail
	private synchronized void saveThumbNail()
	{
		try
		{
			// create bitmap
			Bitmap bitmap = Bitmap.createBitmap(displayWidth, displayHeight, Bitmap.Config.ARGB_4444);
			Canvas canvas = new Canvas(bitmap);
			
			// draw visible items to bitmap canvas
			drawBack(canvas, true);
			drawItems(canvas, true);
			
			// save thumb nail
			if(fileName.equals(""))
			{
				File file;
				file = toolbarClass.pictureTool.getOutputMediaFile("Note", ".note");
				fileName = file.getName();
			}
			String name = fileName.substring(0, fileName.length() - 5) + ".jpg";
			File inFile = new File(name);
			try 
			{
				FileOutputStream fos = new FileOutputStream(inFile);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
				fos.close();
			} 
			catch (Exception ex)
			{
				Log.d(TAG, "Error accessing file: " + ex.getMessage());
			}
			
			// shrink image
			int w = displayWidth / 4;
			int h = displayHeight / 4;
			cameraClass.resizeBitmap(name, w, h);
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

	// export item list to html format
	public void exportItems()
	{
		StringBuilder sb = new StringBuilder();

		String back= "";
		String backCol = "";
		if(backDrawable != null)
		{
			if(backFileName.indexOf("/") == -1)
			{
				String projectDir = getResources().getString(R.string.project_dir);
				backFileName = Environment.getExternalStorageDirectory().getPath() + "/" + projectDir + "/textures/" + backFileName;
				Bitmap bitmap = BitmapFactory.decodeFile(backFileName);
				back = Utils.bitmapToBase64(bitmap);
			}
			else
			{
				back = Utils.bitmapToBase64(backBitmap);
			}
		}
		else backCol = "#" + String.format("%X", backColor).substring(2);
		
		File file = toolbarClass.pictureTool.getOutputMediaFile("Note", ".html");
		String title = file.getName();
		title = title.substring(title.lastIndexOf("/") + 1);
		
		boolean turn = pictureWidth < pictureHeight;
		
		int cnt = 0;
		sb.append("<!DOCTYPE html>\n");
		sb.append("<html>\n");
		sb.append("\t<head>\n");
		sb.append("\t\t<title>");
		sb.append(title);
		sb.append("</title>\n");
		sb.append("\t</head>\n");
		
		sb.append("\t<style>\n");
		sb.append("\ta:hover {\n");
		sb.append("\t\tborder: 3px solid #000000;\n");
		sb.append("\t\tborder-radius: 8px;\n");
		sb.append("\t}\n");
		sb.append("\t</style>\n");
		
		sb.append("\t<body>\n");
		sb.append("\t\t<img id=\"back\" style=\"");
		if(!backCol.equals(""))
		{
			sb.append("background-color:");
			sb.append(backCol);
			sb.append(";");
		}
		else if(!back.equals(""))
		{
			sb.append("background-image:url(data:image/png;base64,");
			sb.append(back);
			sb.append(");");
		}
		sb.append("height:");
		if(turn) sb.append((int)pictureHeight);
		else sb.append((int)pictureWidth);
		sb.append("px;");
		sb.append("width:");
		if(turn) sb.append((int)pictureWidth);
		else sb.append((int)pictureHeight);
		sb.append("px;float:left;\"");
		sb.append(">\n");
		
		String exp;
		for(NoteItem item:noteItemList)
		{
			exp = item.exportItem(turn);
			if(!exp.equals(""))
			{
				sb.append("\t\t");
				sb.append(exp);
				sb.append("\n");
				cnt++;
			}
		}
		
		sb.append("\t<body>\n");
		sb.append("</html>\n");
		
		if(cnt != 0)
		{	
			try
			{
				FileOutputStream out = new FileOutputStream(file);
				out.write(sb.toString().getBytes());
				out.flush();
				out.close();
				
				String msg = file.getName() + " " + getResources().getString(R.string.html_exported);
				Toast.makeText(SimNoteActivity.this, msg, Toast.LENGTH_LONG).show();
			}
			catch (Exception e)
			{
				Log.i(TAG, e.toString());
			}
		}
		
		return;
	}
	
	// clear document and logo
	private void newDocument()
	{
		saveItems(false);
		noteItemList.clear();
		simNoteView.postInvalidate();
		fileName = "";
		backColor = NoteItem.COLOR_WHITE;
		backFileName = getResources().getString(R.string.project_dir) + "/textures/grid.png";
		File file = new File(Environment.getExternalStorageDirectory(), backFileName);
		backFileName = file.getAbsolutePath();
		backBitmap = BitmapFactory.decodeFile(backFileName);
		backDrawable = new BitmapDrawable(backBitmap);
		backDrawable.setBounds(0, 0, tileSize, tileSize);
		
		TextFormat tf = new TextFormat(SimNoteActivity.this, displayWidth / 2, displayHeight / 4 * 3, 3);
		int textColor = NoteItem.COLOR_BLACK;
		int backColor = 0x00FFFFFF;
		String textFont = "sans\t80\tfalse\tfalse\ttrue\tfalse";
		tf.setPaint(textFont, textColor, backColor);
		tf.frameType = 4;
		Bitmap bitmap = tf.formatText("Simple Note", false);
		if(bitmap != null)
		{
			float px = (((displayWidth - bitmap.getWidth()) / 2) - xPos) / zoomX;
			float py = (((displayHeight - bitmap.getHeight()) / 2) - yPos) / zoomY;
			NoteItem item = new NoteItem(bitmap, px, py, NoteItem.TYPE_TEXT, messageTool.resText, displayWidth - 20, displayHeight - 20, userName);
			noteItemList.add(item);
			simNoteView.postInvalidate();
		}
		
		saveItems(false);
		
		return;
	}
	
	// 1 add picture item handler
	private void addPictureItem(int sub)
	{
		String path = toolbarClass.pictureTool.pathName;
		if(path != null && !path.equals(""))
		{
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			if(lastSelectedItem != null && lastSelectedItem.type == NoteItem.TYPE_BITMAP)
			{
				lastSelectedItem.setBitmap(bitmap, false);
			}
			else
			{
				float px = (((displayWidth - bitmap.getWidth()) / 2) - xPos) / zoomX;
				float py = (((displayHeight - bitmap.getHeight()) / 2) - yPos) / zoomY;
				NoteItem item = new NoteItem(bitmap, px, py, NoteItem.TYPE_BITMAP, path, displayWidth - 20, displayHeight - 20, userName);
				noteItemList.add(item);
			}
			toolbarClass.lastMode = toolbarClass.pictureTool.lastMode;

			// if photo then delete temp TEMP_NAME
			if(path.indexOf("temp") != -1)
			{
				File file = new File(path);
				file.delete();
			}
			
			simNoteView.postInvalidate();
		}

		return;
	}
	
	// 2 add or edit text item
	private void addTextItem(int sub)
	{
		// modify selected or new item
		if(lastSelectedItem != null && lastSelectedItem.type == NoteItem.TYPE_TEXT)
		{
			if(lastSelectedItem.stretch == true)
				lastSelectedItem.setBitmap(toolbarClass.textTool.resBitmap, false);
			else
				lastSelectedItem.setBitmap(toolbarClass.textTool.resBitmap, true);
			lastSelectedItem.text = toolbarClass.textTool.resText;
			lastSelectedItem.frameType = toolbarClass.textTool.frameType;
			lastSelectedItem.font = toolbarClass.textTool.textFont;
			lastSelectedItem.textAlign = toolbarClass.textTool.align;
			lastSelectedItem.foreColor = toolbarClass.textTool.textColor;
			lastSelectedItem.backColor = toolbarClass.textTool.backColor;
			if(lastSelectedItem.mLastRot != 0)
			{
				lastSelectedItem.mRotation = lastSelectedItem.mLastRot;
				lastSelectedItem.applyRotate(true);
			}
			lastSelectedItem.state = NoteItem.STATE_NONE;
		}
		else if(!toolbarClass.textTool.resText.equals(""))				
		{
			float px = (((displayWidth - toolbarClass.textTool.resBitmap.getWidth()) / 2) - xPos) / zoomX;
			float py = (((displayHeight - toolbarClass.textTool.resBitmap.getHeight()) / 2) - yPos) / zoomY;
			NoteItem newItem = new NoteItem(toolbarClass.textTool.resBitmap, px, py, NoteItem.TYPE_TEXT, toolbarClass.textTool.resText, displayWidth - 20, displayHeight - 20, userName);
			newItem.text = toolbarClass.textTool.resText;
			newItem.frameType = toolbarClass.textTool.frameType;
			newItem.font = toolbarClass.textTool.textFont;
			newItem.textAlign = toolbarClass.textTool.align;
			newItem.foreColor = toolbarClass.textTool.textColor;
			newItem.backColor = toolbarClass.textTool.backColor;
			noteItemList.add(newItem);
		}
		simNoteView.postInvalidate();

		return;
	}
	
	// 3 add polygon item
	private void addPolyItem(int sub)
	{
		if(lastSelectedItem != null && lastSelectedItem.type == NoteItem.TYPE_VECTOR)
		{
			lastSelectedItem.state = NoteItem.STATE_POLY_DRAW;
			toolbarClass.setTools(lastSelectedItem);
		}
		else				
		{
			float px = -xPos / zoomX;
			float py = -yPos / zoomY;
			NoteItem item = new NoteItem(null, px, py, NoteItem.TYPE_VECTOR, "", displayWidth - 20, displayHeight - 20, userName);
			item.state = NoteItem.STATE_POLY_DRAW;
			item.stretch = true;
			selectLastItem(item);
			toolbarClass.setTools(item);
			noteItemList.add(item);
		}
		toolbarClass.leftView.setVisibility(View.VISIBLE);
		simNoteView.postInvalidate();

		return;
	}
	
	// 4 add finger paint item
	private void addPaintItem(int sub)
	{
		if(lastSelectedItem != null && lastSelectedItem.type == NoteItem.TYPE_PAINT)
		{
			lastSelectedItem.state = NoteItem.STATE_HAND_DRAW;
			//lastSelectedItem.fingerPaint.mBitmap.eraseColor(0xFFFFFFFF);
			//lastSelectedItem.fingerPaint.redraw(lastSelectedItem.fingerPaint.mBitmap);
			//Log.i(TAG, String.format("%X", lastSelectedItem.foreColor));
			toolbarClass.setTools(lastSelectedItem);
		}
		else
		{
			float px = (displayWidth / 8 - xPos) / zoomX;
			float py = (displayHeight / 8 - yPos) / zoomY;
			NoteItem item = new NoteItem(null, px, py, 4, "fingerPaint", displayWidth / 4 * 3, displayHeight / 4 * 3, userName);
			item.state = NoteItem.STATE_HAND_DRAW;
			selectLastItem(item);
			noteItemList.add(item);
			toolbarClass.setTools(item);
		}
		toolbarClass.leftView.setVisibility(View.VISIBLE);
		simNoteView.postInvalidate();

		return;
	}
	
	// 5
	private void handleBackGround(int sub)
	{
		switch(sub)
		{
		case 1:// set background
			backFileName = toolbarClass.pictureTool.pathName;
			setBack(backFileName);
			new saveTask().execute();
			break;
		case 2:// change picture folder
			toolbarClass.selectTool.showSelectPoup(R.array.picture_folder_names, 80);
			break;
		case 3:// show palette tool for background
			toolbarClass.paletteTool.showPalettePopup(54, backColor);
			break;
		case 4:// set backbground color
			if(toolbarClass.pictureTool.lastMode == 3)
			{
				if(!toolbarClass.paletteTool.cancel) backColor = toolbarClass.paletteTool.lastColor;
				backDrawable = null;
				simNoteView.postInvalidate();
				new saveTask().execute();
			}
			break;
		}

		return;
	}
	
	// 6 set text properties
	private void handleFontPopup(int sub)
	{
		switch(sub)
		{
		case 1:// show font popup from text tool
			toolbarClass.fontTool.setFont(toolbarClass.textTool.textFont);
			toolbarClass.fontTool.fontColor = toolbarClass.textTool.textColor;
			toolbarClass.fontTool.showFontPopup(62);
			break;
		case 2:// show text popup from font window
			toolbarClass.textTool.textFont = toolbarClass.fontTool.getFont();
			toolbarClass.textTool.textColor = toolbarClass.fontTool.fontColor;
			toolbarClass.textTool.showTextPoup(toolbarClass.textTool.resText);
			break;
		case 3:// show palette tool for text background color
			toolbarClass.paletteTool.showPalettePopup(65, toolbarClass.textTool.backColor);
			break;
		case 4:// show palette tool from font color
			toolbarClass.paletteTool.showPalettePopup(66, toolbarClass.textTool.textColor);
			break;
		case 5:// show text tool from palatte tool
			if(!toolbarClass.paletteTool.cancel) toolbarClass.textTool.backColor = toolbarClass.paletteTool.lastColor;
			toolbarClass.textTool.showTextPoup(toolbarClass.textTool.resText);
			break;
		case 6:// show font popup after font color
			if(!toolbarClass.paletteTool.cancel) toolbarClass.fontTool.fontColor = toolbarClass.paletteTool.lastColor;			
			toolbarClass.fontTool.showFontPopup(62);
			break;
		}
		
		return;
	}
	
	// 7 handle main tool bar
	private void handleMainBar(int sub)
	{
		switch(sub)
		{
		case 1:// copy item
			List<NoteItem> list = new ArrayList<NoteItem>();
			for(NoteItem item:noteItemList)
			{
				if(item.state == NoteItem.STATE_SELECT)
				{
					NoteItem newItem = item.copyItem();
					newItem.px = item.px + item.iw / 2;
					newItem.py = item.py + item.ih / 2;
					list.add(newItem);
					
					item.state = NoteItem.STATE_NONE;
				}
			}
			if(list.size() > 0)
			{
				noteItemList.addAll(list);
				
				toolbarClass.leftView.animateClose();
				simNoteView.postInvalidate();
			}
			break;
		case 2:// send to back
			for(NoteItem item:noteItemList)
			{
				if(item.state == NoteItem.STATE_SELECT)
				{
					int num = 0;
					for(NoteItem note:noteItemList)
					{
						if(item == note) break;
						num++;
					}
					NoteItem tmpItem = noteItemList.get(num);
					for(int i = num; i > 0 ; i--)
						noteItemList.set(i, noteItemList.get(i - 1));
					noteItemList.set(0, tmpItem);
					
					toolbarClass.leftView.animateClose();
					simNoteView.postInvalidate();
				}
			}					
			break;
		case 3:// delete selected item
			if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_SELECT)
			{
				deleteItem(lastSelectedItem);
				toolbarClass.leftView.animateClose();
				toolbarClass.leftView.setVisibility(View.INVISIBLE);
				simNoteView.postInvalidate();
				new saveTask().execute();
			}
			break;
		case 4:// set item label
			if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_SELECT)
			{
				lastSelectedItem.setLabel(toolbarClass.labelTool.resText, infoPaint);
				simNoteView.postInvalidate();
			}
			break;
		case 5:// set bitmap color
			if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_SELECT)
			{
				toolbarClass.paletteTool.showPalettePopup(77, lastSelectedItem.foreColor);
			}
			break;
		case 6:// set bitmap alpha
			if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_SELECT && lastSelectedItem.drawable != null)
			{
				lastSelectedItem.alpha = toolbarClass.mainTool.alpha;
				lastSelectedItem.drawable.setAlpha(lastSelectedItem.alpha);
				simNoteView.postInvalidate();
			}
			break;
		case 7:// set bitmap color
			if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_SELECT)
			{
				if(!toolbarClass.paletteTool.cancel) lastSelectedItem.setColor(toolbarClass.paletteTool.lastColor);
				simNoteView.postInvalidate();
			}
			break;
		case 8:// border
			if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_SELECT)
			{
				lastSelectedItem.frameType = toolbarClass.mainTool.frameType;
				switch(lastSelectedItem.type)
				{
				case NoteItem.TYPE_BITMAP:// image
					lastSelectedItem.drawFrame(lastSelectedItem.bitmap);
					break;
				case NoteItem.TYPE_TEXT:// text
					TextFormat tf = new TextFormat(SimNoteActivity.this, displayWidth / 4 * 3, displayHeight / 4 * 3, lastSelectedItem.textAlign);
					tf.setPaint(lastSelectedItem.font, lastSelectedItem.foreColor, lastSelectedItem.backColor);
					tf.frameType = lastSelectedItem.frameType;
					tf.setPaint(lastSelectedItem.font, lastSelectedItem.foreColor, lastSelectedItem.backColor);

					Bitmap bmp = tf.formatText(lastSelectedItem.text, false);
					if(bmp != null)
					{
						lastSelectedItem.setBitmap(bmp, false);
						if(lastSelectedItem.mLastRot != 0)
						{
							lastSelectedItem.mRotation = lastSelectedItem.mLastRot;
							lastSelectedItem.applyRotate(true);
						}
					}
					break;
				case NoteItem.TYPE_PAINT:// finger paint
					lastSelectedItem.fingerPaint.drawFrame(lastSelectedItem.bitmap, true);
					break;
				case NoteItem.TYPE_VECTOR:// polygon
					break;
				}
				lastSelectedItem.state = NoteItem.STATE_NONE;
				toolbarClass.leftView.animateClose();
				toolbarClass.leftView.setVisibility(View.INVISIBLE);
				simNoteView.postInvalidate();
			}
			break;
		case 9:// stretch
			if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_SELECT)
			{
				lastSelectedItem.stretch = toolbarClass.mainTool.stretch;
				lastSelectedItem.state = NoteItem.STATE_NONE;
				toolbarClass.leftView.animateClose();
				toolbarClass.leftView.setVisibility(View.INVISIBLE);
				simNoteView.postInvalidate();
			}
			break;
		}
		
		return;
	}
	
	//TODO 9 handle camera events
	private void handleCamera(int sub)
	{
		switch(sub)
		{
		case 1:// start camera preview 
			if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_SELECT && lastSelectedItem.type == NoteItem.TYPE_BITMAP && cameraView != null)
			{
				lastSelectedItem.camera = true;
				lastSelectedItem.stretch = true;
				lastSelectedItem.state = NoteItem.STATE_RESIZE;
				cameraView.setVisibility(View.VISIBLE);
				cameraClass.cameraPreview.startPreview((int)lastSelectedItem.bound.width(), (int)lastSelectedItem.bound.height());
			}
			else
			{
				// add an empty bitmap
				try
				{
					Bitmap bitmap = Bitmap.createBitmap(cameraClass.size.width, cameraClass.size.height, Bitmap.Config.ARGB_8888);
					float px = (((displayWidth - bitmap.getWidth()) / 2) - xPos) / zoomX;
					float py = (((displayHeight - bitmap.getHeight()) / 2) - yPos) / zoomY;
					NoteItem item = new NoteItem(bitmap, px, py, NoteItem.TYPE_BITMAP, "camera", displayWidth - 20, displayHeight - 20, userName);
					noteItemList.add(item);
					item.camera = true;
					item.stretch = true;
					item.state = NoteItem.STATE_RESIZE;
					cameraView.setVisibility(View.VISIBLE);
					cameraClass.cameraPreview.startPreview((int)item.bound.width(), (int)item.bound.height());
					selectLastItem(item);
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
			simNoteView.postInvalidate();
			break;
		case 2:// take a picture
			cameraClass.cameraPreview.stopPreview();
			Bitmap bitmap = BitmapFactory.decodeFile(cameraClass.lastFile);
			for(NoteItem item:noteItemList)
			{
				if(item.camera)
				{
					item.camera = false;
					item.state = NoteItem.STATE_NONE;
					item.setBitmap(bitmap, false);
					break;
				}
			}
			cameraView.setVisibility(View.INVISIBLE);
			File file = new File(cameraClass.lastFile);
			file.delete();
			new saveTask().execute();
			break;
		}
		
		return;
	}
	
	// 10 polygon draw tool bar handler
	private void handlePolyBar(int sub)
	{
		if(lastSelectedItem.state == NoteItem.STATE_POLY_DRAW)
		{
			switch(sub)
			{
			case 1:// undo
				lastSelectedItem.polyDraw.undoLast();
				lastSelectedItem.setBound(lastSelectedItem.polyDraw.calculateRect());
				toolbarClass.leftView.animateClose();
				break;
			case 2:// polygon
				lastSelectedItem.polyDraw.lineMode = PolyDraw.POLYGON;
				toolbarClass.leftView.animateClose();
				break;
			case 3:// polyline
				lastSelectedItem.polyDraw.lineMode = PolyDraw.POLYLINE;
				toolbarClass.leftView.animateClose();
				break;
			case 4:// fill color 1
				if(!toolbarClass.paletteTool.cancel) lastSelectedItem.polyDraw.setColorFill(toolbarClass.paletteTool.lastColor, 1);
				toolbarClass.leftView.animateClose();
				break;
			case 5:// line color
				if(!toolbarClass.paletteTool.cancel) lastSelectedItem.polyDraw.setColorLine(toolbarClass.paletteTool.lastColor);
				toolbarClass.leftView.animateClose();
				break;
			case 6:// line thickness
				lastSelectedItem.polyDraw.setLineSize(toolbarClass.polyTool.lineSize);
				break;
			case 7:// line type
				lastSelectedItem.polyDraw.setLineType(toolbarClass.polyTool.lineType);
				toolbarClass.leftView.animateClose();
				break;
			case 8:// fill color 2
				if(!toolbarClass.paletteTool.cancel) lastSelectedItem.polyDraw.setColorFill(toolbarClass.paletteTool.lastColor, 2);
				toolbarClass.leftView.animateClose();
				break;
			case 9:// delete selected point
				lastSelectedItem.polyDraw.deletePoint();
				lastSelectedItem.setBound(lastSelectedItem.polyDraw.calculateRect());
				toolbarClass.leftView.animateClose();
				break;
			}
			simNoteView.postInvalidate();
		}

		return;
	}
	
	// 11 handle paint tool bar
	private void handlePaintBar(int sub)
	{
		if(lastSelectedItem.state == NoteItem.STATE_HAND_DRAW)
		{
			switch(sub)
			{
			case 0:// draw
				lastSelectedItem.fingerPaint.setPropeties(0);
				break;
			case 1:// undo
				lastSelectedItem.fingerPaint.undo();
				break;
			case 2:// back color
				if(!toolbarClass.paletteTool.cancel) lastSelectedItem.fingerPaint.setColorBack(toolbarClass.paletteTool.lastColor);
				break;
			case 3:// line color
				if(!toolbarClass.paletteTool.cancel) lastSelectedItem.fingerPaint.setColorLine(toolbarClass.paletteTool.lastColor);
				break;
			case 4:// emboss filter
				lastSelectedItem.fingerPaint.setPropeties(1);
				break;
			case 5:// blur filter
				lastSelectedItem.fingerPaint.setPropeties(2);
				break;
			case 6:// erase filter 
				lastSelectedItem.fingerPaint.setPropeties(3);
				break;
			case 7: 
				break;
			case 8: // flood fill
				lastSelectedItem.fingerPaint.floodFill = true;
				break;
			case 9:// line size
				lastSelectedItem.fingerPaint.setLineSize(toolbarClass.paintTool.lastLineSize);
				break;
			}
			simNoteView.postInvalidate();
		}
		
		return;
	}
	
	//
	private void handleNetwork(int sub)
	{
		String message = "";
		switch(sub)
		{
		case 0:// Error access server
			if (errorCnt == 0) Toast.makeText(SimNoteActivity.this, sNetError + ipAddressPort, Toast.LENGTH_LONG).show();
			break;
		case 1:// process message
			processMessage();
			errorCnt = 0;
			break;
		case 2:// User name already in use!
			Toast.makeText(SimNoteActivity.this, sUserError.replace("x", userName), Toast.LENGTH_LONG).show();
			break;
		case 3:// Login success!
			Toast.makeText(SimNoteActivity.this, sUserSuccess, Toast.LENGTH_LONG).show();
			break;
		case 4:// logout if 5 times network error
			if (errorCnt++ > 4) logout(); 
			break;
		case 5://send message
			webClass.selUser = userlistTool.selectedUser;
			labelTool.showLabelPoup("", 127);
			break;
		case 6://invite partner
			webClass.selUser = userlistTool.selectedUser;
			webClass.invite(saveItems(false));
			break;
		case 7://send message
			message = labelTool.resText;
			if(!message.equals("")) webClass.message(message);
			break;
		case 8://show invitation
			showInvitation();
			break;
		}

		return;
	}
	
	//
	private void handleLink(int sub)
	{
		if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_SELECT
		&& prevSelectedItem != null && prevSelectedItem.state == NoteItem.STATE_SELECT)
		{
			lastSelectedItem.setLinked(prevSelectedItem);
			for(NoteItem item:noteItemList)
				item.state = NoteItem.STATE_NONE;
			simNoteView.postInvalidate();
		}
		else if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_SELECT && prevSelectedItem == null)
		{
			lastSelectedItem.setLinked(null);
			for(NoteItem item:noteItemList)
				item.state = NoteItem.STATE_NONE;
			simNoteView.postInvalidate();
		}
		
		return;
	}
	
	// 15 set item states
	private void handleState(int sub)
	{
		switch(sub)
		{
		case 1:// show states window
			for(int i = 0; i < lastSelectedItem.states.length; i++) stateTool.states[i] = lastSelectedItem.states[i];
			stateTool.showFontPopup(152);
			break;
		case 2:// set item states
			if(lastSelectedItem != null && lastSelectedItem.state == NoteItem.STATE_SELECT)
			{
				for(int i = 0; i < lastSelectedItem.states.length; i++) lastSelectedItem.states[i] = stateTool.states[i];
				for(NoteItem item:noteItemList) item.state = NoteItem.STATE_NONE;
				simNoteView.postInvalidate();
			}
			break;
		}
		
		return;
	}
	
	// add vector item to the document
	private void addVectorItem(int sub)
	{
		VectorItem vi = new VectorItem();
		PolyDraw pd;
		float px, py;
		RectF rect;
		if(toolbarClass.vectorTool.fileName.equals("circle"))
		{
			pd = vi.addCircle(displayWidth / 4, toolbarClass.vectorTool.density);
			rect = vi.calculateRect();
			pd.relocate(Math.abs(rect.top), Math.abs(rect.left));
		}
		else if(toolbarClass.vectorTool.fileName.equals("capsule"))
		{
			pd = vi.addCapsule(displayWidth / 2);
		}
		else if(toolbarClass.vectorTool.fileName.equals("square"))
		{
			pd = vi.addSquare(displayWidth / 2);
		}
		else if(toolbarClass.vectorTool.fileName.equals("arrow"))
		{
			pd = vi.addArrow(displayWidth / 2);
		}
		else 
		{
			pd = vi.loadPolygon(toolbarClass.vectorTool.fileName);
		}
		rect = vi.calculateRect();
		px = -xPos / zoomX + (displayWidth - rect.width()) / 2;
		py = -yPos / zoomY + (displayHeight - rect.height()) / 2;

		NoteItem item = new NoteItem(null, px, py, NoteItem.TYPE_VECTOR, toolbarClass.vectorTool.itemName, (int)rect.width(), (int)rect.height(), userName);
		item.type = NoteItem.TYPE_VECTOR;
		item.state = NoteItem.STATE_NONE;
		item.polyDraw = pd;
		item.setBound(pd.calculateRect());
		item.stretch = true;
		noteItemList.add(item);
		selectLastItem(item);
		simNoteView.postInvalidate();

		return;
	}
	
	// add partner message as text item to the document
	private void addMessageText(int sub)
	{
		if(sub == 1)
		{
			TextFormat tf = new TextFormat(SimNoteActivity.this, displayWidth / 4 * 3, displayHeight / 4 * 3, 1);
			Bitmap bitmap = tf.formatText(messageTool.resText, false);
			if(bitmap != null)
			{
				float px = (((displayWidth - bitmap.getWidth()) / 2) - xPos) / zoomX;
				float py = (((displayHeight - bitmap.getHeight()) / 2) - yPos) / zoomY;
				NoteItem item = new NoteItem(bitmap, px, py, NoteItem.TYPE_TEXT, messageTool.resText, displayWidth - 20, displayHeight - 20, userName);
				noteItemList.add(item);
				simNoteView.postInvalidate();
			}
		}
		webClass.messages.remove(0);
		processMessage();
		
		return;
	}

	// 
	private void handleNetworkMessages(int sub)
	{
		switch(sub)
		{
		case 1:// accept invitation
			processInvite();
			webClass.invites.remove(0);
			showInvitation();
			break;
		case 2:// reject invitation
			webClass.invites.remove(0);
			break;
		case 3:// apply response
			processResponse();
			webClass.responses.remove(0);
			break;
		case 4:// apply update
			processUpdate();
			webClass.updates.remove(0);
			break;
		}
		if(sub == 2) webClass.invites.remove(0);
		
		return;
	}
	
	// 20 save paint and polygon
	private void handleFileSave(int sub)
	{
		switch(sub)
		{
		case 1:// get save bitmap name
			String paint = getResources().getString(R.string.paint);
			paint = toolbarClass.pictureTool.getOutputMediaFile(paint, ".png").getName();
			labelTool.showLabelPoup(paint, 202);
			break;
		case 2:// save bitmap
			String projectDir = getResources().getString(R.string.project_dir) + "/" + labelTool.resText;
			File pictureFile = new File(Environment.getExternalStorageDirectory(), projectDir);
			try 
			{
				FileOutputStream fos = new FileOutputStream(pictureFile);
				lastSelectedItem.bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.close();
			} 
			catch (Exception ex)
			{
				Log.d(TAG, "Error accessing file: " + ex.getMessage());
			} 
			break;
		case 3:// get save polygon name
			String vector = getResources().getString(R.string.vector);
			vector = toolbarClass.pictureTool.getOutputMediaFile(vector, ".json").getName();
			labelTool.showLabelPoup(vector, 204);
			toolbarClass.leftView.animateClose();
			break;
		case 4:// save polygon
			projectDir = getResources().getString(R.string.project_dir) + "/vector/" + labelTool.resText;
			File vectorFile = new File(Environment.getExternalStorageDirectory(), projectDir);
			lastSelectedItem.polyDraw.saveMe(vectorFile, lastSelectedItem.getLabel());
			break;
		case 5:// get polygon pattern
			toolbarClass.pictureTool.showPicturePoup(3, 206);
			toolbarClass.leftView.animateClose();
			break;
		case 6:// set polygon pattern
			String path = toolbarClass.pictureTool.pathName;
			if(path != null && !path.equals(""))
			{
				Bitmap bitmap = BitmapFactory.decodeFile(path);
				lastSelectedItem.polyDraw.setFillPattern(bitmap);
				simNoteView.postInvalidate();
			}
			break;
		}

		return;
	}
	
	// finish editing polygon
	private void finishPolyDraw()
	{
		lastSelectedItem.setBound(lastSelectedItem.polyDraw.calculateRect());
		lastSelectedItem.state = NoteItem.STATE_NONE;
		//lastSelectedItem.polyDraw.setColorFill(lastSelectedItem.polyDraw.color_fill1, 1);
		
		lastSelectedItem = null;
		
		toolbarClass.leftView.close();
		toolbarClass.leftView.setVisibility(View.INVISIBLE);
		
		new saveTask().execute();		
	}
}

