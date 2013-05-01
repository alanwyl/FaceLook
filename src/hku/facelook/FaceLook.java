package hku.facelook;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Display;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;

public class FaceLook extends Activity {
	protected static final String TAG = null;
	private byte[] data;
	private Camera mCamera;
	private CameraPreview mPreview;
	private Camera.Parameters cp;
	private List<Size> supportedSize;
	public static final int MEDIA_TYPE_IMAGE = 1;
	private Button captureButton, backButton, recButton, saveButton;
	private Bitmap sourceBitmap, scaledBitmap;
	public static Bitmap[] tempFaces;
	private static final int REQUEST_CODE = 0;
	public static boolean rec = false;

	public int screenWidth, screenHeight, cameraWidth, cameraHeight;

	public byte[] getData(){
		return data;
	}
	
	public void editButtonsToggle(boolean visible){
		if (visible){
			captureButton.setVisibility(View.GONE);
//			saveButton.setVisibility(View.GONE);
			saveButton.setVisibility(View.VISIBLE);

			backButton.setVisibility(View.VISIBLE);

		} else {
			captureButton.setVisibility(View.VISIBLE);
			saveButton.setVisibility(View.GONE);
			backButton.setVisibility(View.GONE);
		}
	}

	public boolean dispatchKeyEvent(KeyEvent event){
		switch(event.getKeyCode()){
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
			capture();
			return true;
		default:
			return super.dispatchKeyEvent(event);
		}
	}

	private void capture(){
		editButtonsToggle(true);
		mCamera.autoFocus(new Camera.AutoFocusCallback(){
			public void onAutoFocus(boolean success, Camera camera){
				camera.takePicture(
						null,
						null, 
						new PictureCallback(){
							public void onPictureTaken(byte[] _data, Camera camera){
								data = _data;
							}
						}
				);
				

			}
		});
	}


	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;

		Log.i("FaceLook", "Screen Width: "+screenWidth + " Screen Height: "+screenHeight);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_face_look);

		if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			try{
				mCamera = Camera.open();
				cp = mCamera.getParameters();
				supportedSize = cp.getSupportedPictureSizes();
				cameraWidth = supportedSize.get(0).width;
				cameraHeight = supportedSize.get(0).height;

				cp.setPictureSize(cameraWidth, cameraHeight);

				Log.i("FaceLook", "cameraWidth: "+cameraWidth+" camerHeight: "+cameraHeight);

				mCamera.setParameters(cp);
			} catch (Exception e){
				Toast.makeText(FaceLook.this, "Camera not accessible", Toast.LENGTH_SHORT).show();
			}

			// Create our Preview view and set it as the content of our activity.
			mPreview = new CameraPreview(this, mCamera);
			FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
			preview.addView(mPreview);
			//setContentView(mPreview);
		}		

		captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(
				new View.OnClickListener(){
					@Override
					public void onClick(View v){
						capture();
						
						
					}
				}
		);


		backButton = (Button) findViewById(R.id.button_back);
		backButton.setOnClickListener(
				new View.OnClickListener(){
					@Override
					public void onClick(View v){
						editButtonsToggle(false);
						mCamera.startPreview();
					}
				}
		);

		saveButton = (Button) findViewById(R.id.button_save);
		saveButton.setOnClickListener(
				new View.OnClickListener(){
					@Override
					public void onClick(View v){
						save();
					}
				}
				);


		recButton = (Button) findViewById(R.id.button_rec);
		recButton.setOnClickListener(
				new View.OnClickListener(){
					@Override
					public void onClick(View v){
						rec = true;
					}
				}
				);

		editButtonsToggle(false);
	}


	//save function
	public void save(){
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "AWCamera");


		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()){
			Toast.makeText(FaceLook.this, "Creating a directory named AWCamera...", Toast.LENGTH_SHORT).show();
			if (!mediaStorageDir.mkdirs()){
				Toast.makeText(FaceLook.this, "Storage not accessible", Toast.LENGTH_SHORT).show();
			}
		}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File pictureFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");	        

		String tempCameraPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/AWCamera" +  File.separator + "IMG_"+ timeStamp + ".jpg";

		Log.i("FaceLook", "tempCameraPath: " + tempCameraPath);

		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.flush();
			FaceLook.this.getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(pictureFile)));
			//sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ mediaStorageDir)));
			fos.close();
		} catch (FileNotFoundException e){
			Toast.makeText(FaceLook.this, "CameraActivity onCreate - FileNotFoundException", Toast.LENGTH_SHORT).show();
		} catch (IOException e){
			Toast.makeText(FaceLook.this, "CameraActivity onCreate - IOException", Toast.LENGTH_SHORT).show();
		} finally {
			Toast.makeText(FaceLook.this, "Image saved to " + mediaStorageDir.getPath(), Toast.LENGTH_SHORT).show();
			editButtonsToggle(false);
			mCamera.startPreview();

			//To analyze view
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			BitmapFactory.decodeFile(tempCameraPath,o);

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inPreferredConfig = Bitmap.Config.RGB_565;
			sourceBitmap = BitmapFactory.decodeFile(tempCameraPath,o2);

			//detect code
			FaceDetector mFaceDetector = new FaceDetector(cameraWidth, cameraHeight, 16);
			FaceDetector.Face[] mFaces = new FaceDetector.Face[16]; //max 64
			FaceDetector.Face mFace = null;

			mFaceDetector.findFaces(sourceBitmap, mFaces);

			PointF tempPoint = new PointF();
			int count=0;
			for (int i = 0; i < mFaces.length; i++){
				try{
					mFaces[i].getMidPoint(tempPoint);

					if (tempPoint != null){
						count++;
					}
				}catch(NullPointerException e){
					Log.e("FaceLook", "this is "+ i +"-th iteration");	
				}

			}

			Bitmap[] croppedFaces = new Bitmap[count]; 

			tempFaces = new Bitmap[count];
			
//			Bitmap tempFace = null;
			for (int i = 0; i < count; i++)
			{
				mFace = mFaces[i];
				try {
					PointF eyesMP = new PointF();
					mFace.getMidPoint(eyesMP);

					float eyesDist = mFace.eyesDistance();
					PointF eyesMidPts = eyesMP;

					tempFaces[i] = Bitmap.createBitmap(sourceBitmap,
							(int) (eyesMidPts.x - eyesDist),
							(int) (eyesMidPts.y - eyesDist),
							(int) (2 * eyesDist),
							(int) (2.5 * eyesDist)
					);

					croppedFaces[i] = Bitmap.createScaledBitmap(tempFaces[i], 100,100,true);

//					tempFaces[i].recycle();

					String croppedPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/AWCamera" +  File.separator + "CROP_" + timeStamp + "_" + i +  ".jpg";

					Log.i("FaceLook", "croppedPath: " + croppedPath);

					Log.i("FaceLook", "height: " + croppedFaces[i].getHeight() +" width: " + croppedFaces[i].getWidth());

					FileOutputStream fos = new FileOutputStream(croppedPath);
					croppedFaces[i].compress(Bitmap.CompressFormat.JPEG, 90, fos);
					fos.flush();
					FaceLook.this.getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(pictureFile)));
					fos.close();


				}catch (Exception e){
					Log.e("FaceLook", "catch count "+ i);
				}
			}

			sourceBitmap.recycle();

			calculateVector(croppedFaces);
			
			if (rec){
				matching();
			}
		}
	}

	
	public void calculateVector(Bitmap[] bitmapFaces){
		Log.i("FaceLook", "Number of faces detected: "+ bitmapFaces.length);

		String output = "";
		
		for (int i=0; i<bitmapFaces.length; i++){
			Calculation.HistogramReset();
			Calculation.featureVectorExtraction(bitmapFaces[i]);
			int Hist1[] = new int[Calculation.Histogram.length];
			for(int j = 0; j < Calculation.Histogram.length; j++){
				Hist1[j] = Calculation.Histogram[j];
				System.out.print(Hist1[j] + ", ");
			}
			String vector = convertArrayToString(Hist1);
			Intent intent = new Intent(getBaseContext(), PicturePrint.class);
        	intent.putExtra("vector", vector);
        	intent.putExtra("i", i);
        	startActivity(intent);
		}
		
		finish();
	}
	
	private List<Person> loadFromFile(){
		List<Person> persons = new ArrayList<Person>();

		String line = "";
		String ret = "";
		try {
			InputStream in = openFileInput("datafff.txt");
			if (in != null) {
				InputStreamReader input = new InputStreamReader(in);
				BufferedReader bufferedreader = new BufferedReader(input);
				while (( line = bufferedreader.readLine()) != null) {
					String[] lines = line.split("\t");
					persons.add(new Person(Integer.parseInt(lines[0]),lines[1]));

					line += ret + '\n';
				}
				in.close();
			}
		} catch(Exception e){
			e.printStackTrace();
		}

		return persons;
	}


	public static void setRec(boolean x){
		rec = x;
	}


	public static String convertArrayToString(int[] array){
		String str = "";
		for (int i = 0;i<array.length; i++) {
			str = str+array[i];
			// Do not append comma at the end of last element
			if(i<array.length-1){
				str = str+",";
			}
		}
		return str;
	}

	@Override
	protected void onPause(){
		super.onPause();
		if (mCamera != null){
			mCamera.release();
			mCamera = null;
		}
	}	

	static int hist[] = new int[Calculation.Histogram.length];
	private int foundMatch =0;


	//Matching
	public void matching(){
		int count=0,start=0,end = 0;
		String Hist2String;

		//load from file
		List<Person> persons = new ArrayList<Person>();

		String line = "";
		String ret = "";
		try {
			InputStream in = openFileInput("datafff.txt");
			if (in != null) {
				InputStreamReader input = new InputStreamReader(in);
				BufferedReader bufferedreader = new BufferedReader(input);
				while (( line = bufferedreader.readLine()) != null) {
					String[] lines = line.split("\t");
					persons.add(new Person(Integer.parseInt(lines[0]),lines[1]));

					line += ret + '\n';
				}
				in.close();
			}
		} catch(Exception e){
			e.printStackTrace();
		}



		//			lbp.open();
		count = persons.size();
		//			System.out.println(count);
		double chiSquareArray[] = new double[count];

		for(int i=0;i<count;i++)
		{
			//				Hist2String = lbp.getUri(i+1);
			Hist2String = persons.get(i).getVector();
			Log.d(TAG, Hist2String);

			convertStringToArray(Hist2String);

			double chiSquare = 0.0;

			long temp,temp1;
			double temp2;
			start=0;
			end=0;
			for(int k=0;k<2;k++)
			{
				start=end;
				end=end+256;
				for(int j=start;j<end;j++)
				{
					if(hist[j] == 0 && Calculation.Histogram[j] == 0)
						continue;
					else
					{
						temp = hist[j]-Calculation.Histogram[j];
						temp = temp*temp;
						temp1 = hist[j]+Calculation.Histogram[j];
						temp2= temp/temp1;
						chiSquare = chiSquare + temp2;
					}
				}
				start=end;
				end=end+118;
				for(int j=start;j<end;j++)
				{
					if(hist[j] == 0 && Calculation.Histogram[j] == 0)
						continue;
					else
					{
						temp = hist[j]-Calculation.Histogram[j];
						temp = temp*temp;
						temp1 = hist[j]+Calculation.Histogram[j];
						temp2= temp/temp1;
						chiSquare = chiSquare + 2*temp2;
					}
				}

				start=end;
				end=end+118;
				for(int j=start;j<end;j++)
				{
					if(hist[j] == 0 && Calculation.Histogram[j] == 0)
						continue;
					else
					{
						temp = hist[j]-Calculation.Histogram[j];
						temp = temp*temp;
						temp1 = hist[j]+Calculation.Histogram[j];
						temp2= temp/temp1;
						chiSquare = chiSquare + temp2;
					}
				}
				start=end;
				end=end+59;

				start=end;
				end=end+118;
				for(int j=start;j<end;j++)
				{
					if(hist[j] == 0 && Calculation.Histogram[j] == 0)
						continue;
					else
					{
						temp = hist[j]-Calculation.Histogram[j];
						temp = temp*temp;
						temp1 = hist[j]+Calculation.Histogram[j];
						temp2= temp/temp1;
						chiSquare = chiSquare + temp2;
					}
				}

				start=end;
				end=end+59;

				start=end;
				end=end+236;
				for(int j=start;j<end;j++)
				{
					if(hist[j] == 0 && Calculation.Histogram[j] == 0)
						continue;
					else
					{
						temp = hist[j]-Calculation.Histogram[j];
						temp = temp*temp;
						temp1 = hist[j]+Calculation.Histogram[j];
						temp2= temp/temp1;
						chiSquare = chiSquare + 4*temp2;
					}
				}
				start=end;
				end=end+59;
				for(int j=start;j<end;j++)
				{
					if(hist[j] == 0 && Calculation.Histogram[j] == 0){
						continue;
					} else{
						temp = hist[j]-Calculation.Histogram[j];
						temp = temp*temp;
						temp1 = hist[j]+Calculation.Histogram[j];
						temp2= temp/temp1;
						chiSquare = chiSquare + temp2;
					}
				}
				start=end;
				end=end+118;
				for(int j=start;j<end;j++)
				{
					if(hist[j] == 0 && Calculation.Histogram[j] == 0)
						continue;
					else
					{
						temp = hist[j]-Calculation.Histogram[j];
						temp = temp*temp;
						temp1 = hist[j]+Calculation.Histogram[j];
						temp2= temp/temp1;
						chiSquare = chiSquare + 3*temp2;
					}
				}
				start=end;
				end=end+59;
				for(int j=start;j<end;j++)
				{
					if(hist[j] == 0 && Calculation.Histogram[j] == 0)
						continue;
					else
					{
						temp = hist[j]-Calculation.Histogram[j];
						temp = temp*temp;
						temp1 = hist[j]+Calculation.Histogram[j];
						temp2= temp/temp1;
						chiSquare = chiSquare + temp2;
					}
				}
				start=end;
				end=end+59;

				start=end;
				end=end+118;
				for(int j=start;j<end;j++)
				{
					if(hist[j] == 0 && Calculation.Histogram[j] == 0)
						continue;
					else
					{
						temp = hist[j]-Calculation.Histogram[j];
						temp = temp*temp;
						temp1 = hist[j]+Calculation.Histogram[j];
						temp2= temp/temp1;
						chiSquare = chiSquare + 2*temp2;
					}
				}
				start=end;
				end=end+59;
			}
			chiSquareArray[i]=chiSquare;
			Log.d(TAG, "" +chiSquare);
		}

		for(int i=0;i<chiSquareArray.length;i++){
			System.out.println("Similarity Measure with "+(i+1)+ " is " +chiSquareArray[i]);
		}
		for(int i=0;i<chiSquareArray.length;i++){
			if(chiSquareArray[foundMatch]>chiSquareArray[i])
				foundMatch  = i;
		}

		String name = new Integer(persons.get(foundMatch).getId()).toString();

		Log.i("FaceLook", "No of Rows in Database = " +count +
				"\nBased on Assumption that the subject is part of the Database" +
				"\n\n" +
				"\nNearest Match Found : \nRow ID = " + (foundMatch) + " :ChiSquare Value : "+ chiSquareArray[foundMatch] +
				"\n Name: " +name);



		FaceLook.setRec(false);

	}
	public static int[] convertStringToArray(String str){
		String[] arr = str.split(",");

		for(int i=0;i<arr.length;i++){
			hist[i]=Integer.parseInt(arr[i]);
			//Log.d(TAG, " " + hist[i]);
		}

		return hist;
	}
}
