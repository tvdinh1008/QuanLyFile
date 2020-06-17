package com.example.quanlyfile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class OpenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String path = intent.getStringExtra("param1");
        setContentView(R.layout.activity_open_image);
        ImageView iv = (ImageView)findViewById(R.id.image_file);
        //BitmapFactory.Options options = new BitmapFactory.Options();
        // will results in a much smaller image than the original
        //options.inSampleSize = 8;
        //final Bitmap b = BitmapFactory.decodeFile(path, options);
        final Bitmap b = BitmapFactory.decodeFile(path, null);
        iv.setImageBitmap(b);
    }
}
