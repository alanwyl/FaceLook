package hku.facelook;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {
//	private Bitmap bm = FaceLook.tempFaces[0];	

	private TextView nameTextView, mobileTextView, emailTextView;
	private String nameStr, mobileStr, emailStr;
	private ImageButton phoneImageButton, emailImageButton, backImageButton;

	
	static int hist[] = new int[Calculation.Histogram.length];
	private int foundMatch =0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		hist = FaceLook.hist;
		
		setContentView(R.layout.activity_search);

		Intent intent = getIntent();
		final String vector = intent.getStringExtra("vector");

		ImageView imageview = (ImageView) findViewById (R.id.imageView1);
//		imageview.setImageBitmap(bm);


		nameTextView = (TextView) findViewById (R.id.textview_name);
		mobileTextView = (TextView) findViewById (R.id.textview_mobile);
		emailTextView = (TextView) findViewById (R.id.textview_email);
		

		int resultId = matching(vector);


		String line = "";
		String ret = "";
		try {
			InputStream in = openFileInput("datafff.txt");
			if (in != null) {
				InputStreamReader input = new InputStreamReader(in);
				BufferedReader bufferedreader = new BufferedReader(input);
				while (( line = bufferedreader.readLine()) != null) {
					String[] lines = line.split("\t");
					if (Integer.parseInt(lines[0]) == resultId){
						ret = line;
						lines = ret.split("\t");
						nameTextView.setText(lines[1]);
						mobileTextView.setText(lines[2]);
						emailTextView.setText(lines[3]);
						
						
						nameStr = lines[1];
						mobileStr = lines[2];
						emailStr = lines[3];
						
						
						Log.i("FaceLook", "ret: " + ret);
						Log.i("FaceLook", "line: " + line);
						Log.i("FaceLook", "lines[0]: "+ lines[0]);
						Log.i("FaceLook", "lines[1]: "+ lines[1]);
						Log.i("FaceLook", "lines[2]: "+ lines[2]);
						Log.i("FaceLook", "lines[3]: "+ lines[3]);
						break;
					}
				}
				in.close();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		

		phoneImageButton = (ImageButton) findViewById(R.id.imageButton_phone);
		phoneImageButton.setOnClickListener(
				new View.OnClickListener(){
					@Override
					public void onClick(View v){
						 String mobileUri = "tel:" + mobileStr;
	       				 Intent intent = new Intent(android.content.Intent.ACTION_DIAL, Uri.parse(mobileUri));
						
	       				 try
	       				 {
	       					 startActivity(intent);
	       				 }
	       				 catch(android.content.ActivityNotFoundException e)
	       				 {
	       					 Toast.makeText(getBaseContext(), "phone service not found" + mobileUri + "  ", Toast.LENGTH_SHORT).show();
	       				 }
					}
				}
				);
		
		emailImageButton = (ImageButton) findViewById(R.id.imageButton_email);
		emailImageButton.setOnClickListener(
				new View.OnClickListener(){
					@Override
					public void onClick(View v){
	       				 Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
	       				 intent.setType("text/plain");
	       				 
	       				 String uriText = "mailto:" + Uri.encode(emailStr) +
	       						 "?subject=" + Uri.encode("FaceLook want to say Hi!") +
	       						 "&body=" + Uri.encode("Hi "+ nameStr +", I'm using FaceLook! It's cool!");
	       				 Uri uri = Uri.parse(uriText);
	       				 intent.setData(uri);
	       				 
//	       				 intent.putExtra(Intent.EXTRA_EMAIL, emailStr);
//	       				 intent.putExtra(Intent.EXTRA_SUBJECT, "FaceLook want to say Hi!");
//	       				 intent.putExtra(Intent.EXTRA_TEXT, "Hi "+ nameStr +", I'm using FaceLook! It's cool!");
//	       				 
	       				 Log.i("email", "emailStr: "+ emailStr);
						
	       				 try
	       				 {
	       					startActivity(intent);
//	       					startActivity(Intent.createChooser(intent, "Send mail..."));
	       				 }
	       				 catch(android.content.ActivityNotFoundException e)
	       				 {
	       					 Toast.makeText(getBaseContext(), "email service not found" + emailStr + "  ", Toast.LENGTH_SHORT).show();
	       				 }
					}
				}
				);
		
		backImageButton = (ImageButton) findViewById(R.id.imageButton_back);
		backImageButton.setOnClickListener(
				new View.OnClickListener(){
					@Override
					public void onClick(View v){
						finish();
					}
				}
				);
		
		


	}







	public int matching(String vector){
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
					persons.add(new Person(Integer.parseInt(lines[0]),lines[4]));

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
			//			Log.d(TAG, "" +chiSquare);
		}

		for(int i=0;i<chiSquareArray.length;i++){
			System.out.println("Similarity Measure with "+(i+1)+ " is " +chiSquareArray[i]);
		}
		for(int i=0;i<chiSquareArray.length;i++){
			if(chiSquareArray[foundMatch]>chiSquareArray[i])
				foundMatch  = i;
		}

		
		Log.i("FaceLook", "foundMatch: "+ foundMatch);
		
//		String name = new Integer(persons.get(foundMatch).getId()).toString();

//		Log.i("FaceLook", "No of Rows in Database = " +count +
//				"\nBased on Assumption that the subject is part of the Database" +
//				"\n\n" +
//				"\nNearest Match Found : \nRow ID = " + (foundMatch) + " :ChiSquare Value : "+ chiSquareArray[foundMatch] +
//				"\n Name: ");
		//TODO: enhance this


		return foundMatch;


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
