package hku.facelook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.FaceDetector;
import android.os.Environment;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
	private SurfaceHolder mHolder;
    private Camera mCamera;
    
    //from MonkeyCam
    private Bitmap mWorkBitmap;
    private FaceDetector mFaceDetector;
    private FaceDetector.Face[] mFaces = new FaceDetector.Face[16]; //max 64
    private FaceDetector.Face face = null;
    
    public PointF eyesMidPts[] = new PointF[16];
    public float  eyesDistance[] = new float[16];
    
    
    private Paint tmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    
    public boolean detected = false;
    
    
//    private int picWidth, picHeight;
//    private float ratio, xRatio, yRatio;
    //-----MonkeyCam 
    
    private CanvasThread _thread;
    
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        mHolder = getHolder();
        mHolder.addCallback(this);
      
        tmpPaint.setStyle(Paint.Style.STROKE);
        tmpPaint.setColor(Color.RED);
        
        textPaint = new Paint(); 
        textPaint.setColor(Color.WHITE); 
        textPaint.setTextSize(40);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            String TAG = null;
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
        setWillNotDraw(false); //Allows us to use invalidate() to call onDraw()

        _thread = new CanvasThread(holder, this); //Start the thread that
        _thread.setRunning(true);                     //will make calls to 
        _thread.start();                              //onDraw()
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    	try {
    		_thread.setRunning(false);                //Tells thread to stop
    		_thread.join();                           //Removes thread from mem.
    	} catch (InterruptedException e) {}
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
    	if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
        	Camera.Parameters parameters = mCamera.getParameters();

            List<Size> sizes = parameters.getSupportedPreviewSizes();
            Size optimalSize = getOptimalPreviewSize(sizes, w, h);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            mCamera.setParameters(parameters);

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            
            // Setup the objects for the face detection
            mWorkBitmap = Bitmap.createBitmap(optimalSize.width, optimalSize.height, Bitmap.Config.RGB_565);
            mFaceDetector = new FaceDetector(optimalSize.width, optimalSize.height, 16);

            int bufSize = optimalSize.width * optimalSize.height * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
            byte[] cbBuffer = new byte[bufSize];
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.addCallbackBuffer(cbBuffer);

        } catch (Exception e){
        	String TAG = null;
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// face detection: first convert the image from NV21 to RGB_565
        YuvImage yuv = new YuvImage(data, ImageFormat.NV21, mWorkBitmap.getWidth(), mWorkBitmap.getHeight(), null);
        Rect rect = new Rect(0, 0, mWorkBitmap.getWidth(), mWorkBitmap.getHeight());
        // TODO: make rect a member and use it for width and height values above

        // TODO: use a threaded option or a circular buffer for converting streams?  see http://ostermiller.org/convert_java_outputstream_inputstream.html
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        if (!yuv.compressToJpeg(rect, 100, baout)) {
            Log.e("Preview", "compressToJpeg failed");
        }
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inPreferredConfig = Bitmap.Config.RGB_565;
        mWorkBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(baout.toByteArray()), null, bfo);

        // Dev only, save the bitmap to a file for visual inspection
        // Also remove the WRITE_EXTERNAL_STORAGE permission from the manifest
        /*String path = Environment.getExternalStorageDirectory().toString() + "/monkeyCam";
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }

            FileOutputStream out = new FileOutputStream(path + "/monkeyCamCapture" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".jpg");
            mWorkBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        Arrays.fill(mFaces, null);	// use arraycopy instead?
        Arrays.fill(eyesMidPts, null);	// use arraycopy instead?
        mFaceDetector.findFaces(mWorkBitmap, mFaces);

        for (int i = 0; i < mFaces.length; i++)
        {
            face = mFaces[i];
            try {
                PointF eyesMP = new PointF();
                face.getMidPoint(eyesMP);
                eyesDistance[i] = face.eyesDistance();
                eyesMidPts[i] = eyesMP;
                
               
                
                //Toast.makeText(getContext(), "Face " + i + " detected", 50).show();
                Log.i("Face",
                        i +  " " + face.confidence() + " " + face.eyesDistance() + " "
                        + "Pose: ("+ face.pose(FaceDetector.Face.EULER_X) + ","
                        + face.pose(FaceDetector.Face.EULER_Y) + ","
                        + face.pose(FaceDetector.Face.EULER_Z) + ")"
                        + "Eyes Midpoint: ("+eyesMidPts[i].x + "," + eyesMidPts[i].y +")"
                );
                
                
                
            }
            catch (Exception e)
            {
                //if (DEBUG) Log.e("Face", i + " is null");
            }
        }
        
        invalidate(); // use a dirty Rect?

        // Requeue the buffer so we get called again
        mCamera.addCallbackBuffer(data);
	}
	
	@Override
	public void invalidate() {
	    super.invalidate();
	    Log.d("", "invalidate executed");
	}
	
	@Override
    protected void onDraw(Canvas canvas){
        Log.d("Preview","onDraw");
    	super.onDraw(canvas);
    	
    	if(mWorkBitmap != null){
	    	int count = 0;
	        for (int i = 0; i < mFaces.length; i++){
	            if (eyesMidPts[i] != null){
	            	count++;
//	                ratio = eyesDistance[i] * 4.0f / picWidth;
//	                RectF scaledRect = new RectF((eyesMidPts[i].x - picWidth * ratio / 2.0f) * xRatio,
//	                                             (eyesMidPts[i].y - picHeight * ratio / 2.0f) * yRatio,
//	                                             (eyesMidPts[i].x + picWidth * ratio / 2.0f) * xRatio,
//	                                             (eyesMidPts[i].y + picHeight * ratio / 2.0f) * yRatio);
//	                canvas.drawBitmap(mMonkeyImage, null, scaledRect, tmpPaint);
	                
//	                canvas.drawRect((int) (eyesMidPts[i].x - eyesDistance[i] * 2),
//			                		(int) (eyesMidPts[i].y - eyesDistance[i] * 2),
//			                		(int) (eyesMidPts[i].x + eyesDistance[i] * 2),
//			                		(int) (eyesMidPts[i].y + eyesDistance[i] * 2),
//			                		tmpPaint);
//	                
	            	
	                int left = (int) (eyesMidPts[i].x - eyesDistance[i]);
	                int top = (int) (eyesMidPts[i].y - eyesDistance[i]);
	                int right = (int) (eyesMidPts[i].x + eyesDistance[i]);
	                int bottom = (int) (eyesMidPts[i].y + 1.5 * eyesDistance[i]);
	                
	                

	                Log.i("Preview", left+" "+top+" "+right+" "+bottom);
	                
	                canvas.drawRect(left,top,right,bottom,tmpPaint);
	            	
	                
	            	

	            }
	        }
	        
	        
	        canvas.drawText(count + " faces " + FaceLook.rec, 200, 40, textPaint);
    	}
    	
    }
	
	
	
	boolean crop = false;
	
	public void crop(){
		crop = true;
        //testing
        //TODO: enhance this 
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());    	
    	String mPath = Environment.getExternalStorageDirectory().toString() + "/" + "SceenCrop_" + timeStamp + ".jpg";
    	
    	View v1 = this.getRootView();
    	//View v1 = mCurrentUrlMask.getRootView();
    	v1.setDrawingCacheEnabled(true);
    	Bitmap bitmap = v1.getDrawingCache();
    	int x = v1.getWidth()/10;
    	int y = v1.getHeight()/10;
    	int width = bitmap.getWidth() - 5*x;
    	int height = bitmap.getHeight() - 5*y;
    	bitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
    	v1.setDrawingCacheEnabled(false);
    	
    	OutputStream fout = null;
    	File imageFile = new File(mPath);
    	
    	try{
    		fout = new FileOutputStream(imageFile);
    		bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
    		fout.flush();
    		fout.close();
    	} catch (FileNotFoundException e){
    		e.printStackTrace();
    	} catch (IOException e){
    		e.printStackTrace();
    	}
    	//testing end here
	}
	
	
}