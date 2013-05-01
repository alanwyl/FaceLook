package hku.facelook;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class AddActivity extends Activity {
//	private Bitmap bm = FaceLook.tempFaces[0];	

	private EditText nameEditText, mobileEditText, emailEditText;
	private String nameStr, mobileStr, emailStr;
	private ImageButton tickImageButton, crossImageButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

		Intent intent = getIntent();
		final String vector = intent.getStringExtra("vector");
		
		ImageView imageview = (ImageView) findViewById (R.id.imageView1);
//		imageview.setImageBitmap(bm);


		nameEditText = (EditText) findViewById (R.id.edittext_name);
		mobileEditText = (EditText) findViewById (R.id.edittext_mobile);
		emailEditText = (EditText) findViewById (R.id.edittext_email);

		tickImageButton = (ImageButton) findViewById(R.id.imageButton_tick);
		tickImageButton.setOnClickListener(
				new View.OnClickListener(){
					@Override
					public void onClick(View v){
						nameStr = nameEditText.getText().toString();
						mobileStr = mobileEditText.getText().toString();
						emailStr = emailEditText.getText().toString();
						if (nameStr.length() == 0 || mobileStr.length() == 0 || emailStr.length() == 0){
							Log.i("FaceLook", "Some of the input are not valid.");
						} else {
							int count = 0;
							try {
								InputStream in = openFileInput("datafff.txt");
								String line;
								if (in != null) {
									InputStreamReader input = new InputStreamReader(in);
									BufferedReader bufferedreader = new BufferedReader(input);
									while (( line = bufferedreader.readLine()) != null) {
										count++;
										Log.i("FaceLook", "in while loop count: " + count);
									}
									in.close();
								}
							} catch(Exception e){
								e.printStackTrace();
							}
							
							Log.i("FaceLook", "outside loop count: " + count);
							
							String output = new Integer(count).toString() + "\t" + nameStr + "\t" + mobileStr + "\t" + emailStr + "\t" + vector + "\n" ;
							Log.i("FaceLook", "output: " + output);
							
							try {
								FileOutputStream fos = openFileOutput("datafff.txt", MODE_APPEND);
								fos.write(output.getBytes());
								fos.flush();
								fos.close();
							} catch (IOException e) {
								Log.e("FaceLook", "write to file exception");
							}
							finish();

						}
					}
				}
				);
		
		crossImageButton = (ImageButton) findViewById(R.id.imageButton_cross);
		crossImageButton.setOnClickListener(
				new View.OnClickListener(){
					@Override
					public void onClick(View v){
						finish();
					}
				}
				);

	}

}
