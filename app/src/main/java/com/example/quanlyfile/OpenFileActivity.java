package com.example.quanlyfile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OpenFileActivity extends AppCompatActivity {

    TextView txtName;
    EditText txtContent;
    Button btnSave;
    Button btnClose;
    String path;
    String name;
    Intent intent;
    int status;
    int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);

        ImageView imageView=findViewById(R.id.image_view);

        /*
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,112);
*/


        intent = getIntent();
        path = intent.getStringExtra("param1");
        name = intent.getStringExtra("param2");
        path += name;
        status=intent.getIntExtra("status",0);
        if(status==1)
        {
            pos=intent.getIntExtra("position",-1);

        }
        txtName = findViewById(R.id.txt_name);
        txtContent = findViewById(R.id.txt_content);
        btnClose = findViewById(R.id.btn_close);
        btnSave = findViewById(R.id.btn_save);

        txtName.setText(name);
        if(status==0)
        {
            //File file = new File(path + name);
        }
        else if(status==1)
        {
            // FileReader fr = null;
            try {
                //fr = new FileReader(path);
                File fileDir=new File(path);
                BufferedReader reader =  new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "UTF-8"));
                StringBuffer buffer = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");
                txtContent.setText(new String(buffer.toString().getBytes(),"UTF8"));
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file=new File(path);
                try {
                    FileOutputStream fos=new FileOutputStream(file);
                    fos.write(txtContent.getText().toString().getBytes());
                    intent.putExtra("status",status);
                    intent.putExtra("name",name);
                    if(status==1)
                    {
                        intent.putExtra("position",pos);
                    }
                    intent.putExtra("success",1);
                    setResult(Activity.RESULT_OK,intent);
                    finish();
                } catch (IOException e) {
                    Toast toast=null;
                    if(status==0) {
                        toast = Toast.makeText(OpenFileActivity.this, "Không cập nhật được file", Toast.LENGTH_LONG);
                    }
                    else if(status==1)
                    {
                        toast = Toast.makeText(OpenFileActivity.this, "Không thêm được file", Toast.LENGTH_LONG);
                    }
                    toast.setMargin(0, 0);
                    toast.show();
                    e.printStackTrace();
                }
                finally {

                }
            }
        });


        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("success",0);
                setResult(Activity.RESULT_CANCELED,intent);
                finish();
            }
        });
    }
}
