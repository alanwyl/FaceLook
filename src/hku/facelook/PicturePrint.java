package hku.facelook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

public class PicturePrint extends Activity {
	private Bitmap bm;
	private ImageButton addImageButton, searchImageButton, deleteImage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_picture);
		
		Intent intent = getIntent();
		final String vector = intent.getStringExtra("vector");
		int i = intent.getIntExtra("i", 0);

		bm = FaceLook.tempFaces[i];	
		
		ImageView imageview = (ImageView) findViewById (R.id.imageView2);
		imageview.setImageBitmap(bm);
		
		addImageButton = (ImageButton) findViewById(R.id.imageButton_add);
		addImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent2 = new Intent(getBaseContext(), AddActivity.class);
	        	intent2.putExtra("vector", vector);
	        	startActivity(intent2);
			}
		});
		
		searchImageButton = (ImageButton) findViewById(R.id.imageButton_search);
		searchImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent3 = new Intent(getBaseContext(), SearchActivity.class);
	        	intent3.putExtra("vector", vector);
	        	startActivity(intent3);
			}
		});
		
		deleteImage = (ImageButton) findViewById(R.id.imageButton_trash);
		deleteImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

	}

}
