package com.example.quanlyfile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.Toast;

import com.example.quanlyfile.adapters.fileAdapter;
import com.example.quanlyfile.model.itemModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FileFragment.OpenSubPathFragment, IUpdateTrace {

    ActionBar actionBar;
    SearchView txtSearchValue;
    List<String> rootPath;
    Spinner spinnerRoot;
    int pos; //cho biết là thẻ micrSd(1) hay là sd(0)
    String tracePath;
    String pathcopy = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Toast toast=Toast.makeText(getApplicationContext(),"Error Permission Denied", Toast.LENGTH_LONG);
//        toast.setMargin(0,0);
//        toast.show();
        tracePath = "";
        spinnerRoot = findViewById(R.id.spinner_rootpath);
        rootPath = new ArrayList<>();

        actionBar = getSupportActionBar();
        actionBar.setTitle("Quản lý danh mục");
        actionBar.setDisplayShowCustomEnabled(true);
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setDisplayShowHomeEnabled(true);
        // actionBar.setHomeButtonEnabled(true);
        if (Build.VERSION.SDK_INT >= 23) {
            //cấp quyền ghi thì không cần thêm quyền đọc nữa
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permisstion Denied. Asking for permission.");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
            }
        }
        if (Build.VERSION.SDK_INT >= 23) {
            //cấp quyền ghi thì không cần thêm quyền đọc nữa
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                // kiểm tra bộ nhớ vật lý
                if (Build.VERSION.SDK_INT >= 19) {
                    File files[] = getExternalFilesDirs(null); // Nó trả về các đường dẫn tuyệt đối đến các thư mục dành riêng cho ứng dụng(thể microSd và bộ nhớ của máy)
                    for (File file1 : files) {
                        String tmp = file1.getAbsolutePath();
                        String rootPathItem = tmp.substring(0, tmp.indexOf("Android/data"));
                        Log.v("TAG", file1.getAbsolutePath());
                        rootPath.add(rootPathItem);
                    }
                }

                List<String> root = new ArrayList<>();
                root.add("Bộ nhớ trong");
                if (rootPath.size() == 2) {

                    root.add("Thẻ SD");
                }
                spinnerRoot.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, root));
                spinnerRoot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position >= 0) {
                            pos = position;
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            FileFragment fileFragment = FileFragment.newInstance(rootPath.get(pos), "");
                            ft.replace(R.id.main_layout_fragment, fileFragment);
                            ft.commit();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                //xử lý ở đây


            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        txtSearchValue = (SearchView) menu.findItem(R.id.action_search).getActionView();

        txtSearchValue.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1234) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission Granted");
            } else {
                Log.v("Tag", "Permission Denied");
            }

        } else {

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //actionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_search) {
            Log.v("TAG", "select is btn search");

        } else if (id == R.id.action_add_directory) {
            showCustomerAlertDialogBox(1);
            Log.v("TAG", "select is add_directory:" + tracePath);
        } else if (id == R.id.action_add_file) {
            showCustomerAlertDialogBox(2);
            Log.v("TAG", "select is btn add file");
        } else if (id == R.id.action_save) {
            if (!pathcopy.equals("")) {
                copyFile();
            } else {
                Toast toast = Toast.makeText(this, "Bạn cần copy file trước", Toast.LENGTH_LONG);
                toast.setMargin(0, 0);
                toast.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void openSubPath(String subpath) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FileFragment fileFragment = FileFragment.newInstance(rootPath.get(pos), subpath + "/");
        ft.replace(R.id.main_layout_fragment, fileFragment, "FILE_SUB");//bắt buộc phải là replace nếu add thì nó sẽ bị đè lên nhau
        ft.addToBackStack("FILE_SUB");
        ft.commit();
    }

    public void showCustomerAlertDialogBox(final int stt) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.customer_name_layout);
        TextView textView = dialog.findViewById(R.id.txt_title);
        if (stt == 1) {
            textView.setText("Tạo thư mục mới");
        } else if (stt == 2) {
            textView.setText("Tạo file mới");
        }
        final EditText editText = dialog.findViewById(R.id.txt_name_new);

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editText.getText().toString();
                if (stt == 1 && !name.equals("")) {
                    createDirectory(name);
                } else if (stt == 2 && !name.equals("")) {
                    createFile(name);
                }
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    }

    @Override
    public void updateTrace(String tracePath) {
        this.tracePath = tracePath;
    }

    @Override
    public void pathCopy(String pathcopy) {
        this.pathcopy = pathcopy;
    }

    public void copyFile() {
        //pathcopy là đường dẫn tới file muốn copy
        //tracepath là đường dẫn hiện thời - nơi mà file copy sẽ nằm tại đây
        OutputStream out = null;
        InputStream in = null;
        try {
            int index = 0;
            while (pathcopy.indexOf("/", index) != -1) index = pathcopy.indexOf("/", index) + 1;
            String tmp = pathcopy.substring(index, pathcopy.length());
            String namecpy = tracePath + tmp;
            File src = new File(pathcopy);
            File dst = new File(namecpy);
            if (!dst.exists()) {
                out = new FileOutputStream(dst);
                in = new FileInputStream(src);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                int count = getSupportFragmentManager().getBackStackEntryCount();
                if (count == 0) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    FileFragment fileFragment = FileFragment.newInstance(rootPath.get(pos), "");
                    ft.replace(R.id.main_layout_fragment, fileFragment);//do thay thế nên nó sẽ hiện lại từ đầu
                    ft.commit();
                } else {
                    FileFragment fileFragment = (FileFragment) getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);
                    fileFragment.additem(tracePath + tmp);
                }
                Toast toast = Toast.makeText(this, "Sao chép thành công", Toast.LENGTH_LONG);
                toast.setMargin(0, 0);
                toast.show();
            } else {
                Toast toast = Toast.makeText(this, "File đã tồn tại", Toast.LENGTH_LONG);
                toast.setMargin(0, 0);
                toast.show();
            }

        } catch (IOException e) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {

            }
        }
    }

    public void createDirectory(String name) {
        File file = new File(tracePath + name);
        Boolean success = file.mkdir();//chỉ tạo thư mục con thôi
        //file.mkdirs();//tạo cả thư mục con nếu nó chưa có
        if (success) {
            int count = getSupportFragmentManager().getBackStackEntryCount();
            if (count == 0) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                FileFragment fileFragment = FileFragment.newInstance(rootPath.get(pos), "");
                ft.replace(R.id.main_layout_fragment, fileFragment);//do thay thế nên nó sẽ hiện lại từ đầu
                ft.commit();
            } else {
                FileFragment fileFragment = (FileFragment) getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);
                fileFragment.additem(tracePath + name);
                Log.v("TAG", "c1" + fileFragment.subPath);
                Log.v("TAG", " đường dẫn thư mục " + tracePath + name);
            }
        } else {
            Toast toast = Toast.makeText(this, "Không tạo được thư mục", Toast.LENGTH_LONG);
            toast.setMargin(0, 0);
            toast.show();
        }

    }

    public void createFile(String name) {
        name=name.concat(".txt");
        Intent intent = new Intent(MainActivity.this, OpenFileActivity.class);
        intent.putExtra("param1", tracePath);
        intent.putExtra("param2", name);
        intent.putExtra("status",0);//=1mở và chỉnh sửa =0 là tạo file mới rồi ghi vào
        startActivityForResult(intent, 1111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1111)
        {
            if(resultCode==Activity.RESULT_OK)
            {
                //cập nhật lại
                Bundle bundle=data.getExtras();
                int success=bundle.getInt("success");
                String name=bundle.getString("name");
                int status=bundle.getInt("status");

                if (success==1) {
                    int count = getSupportFragmentManager().getBackStackEntryCount();
                    if (count == 0) {
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        FileFragment fileFragment = FileFragment.newInstance(rootPath.get(pos), "");
                        ft.replace(R.id.main_layout_fragment, fileFragment);//do thay thế nên nó sẽ hiện lại từ đầu
                        ft.commit();
                    } else {
                        FileFragment fileFragment = (FileFragment) getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);
                        if(status==0) {
                            fileFragment.additem(tracePath + name);
                            Log.v("TAG", "c" + fileFragment.subPath);
                            Log.v("TAG", "dường dẫn: " + tracePath + name);
                        }
                        else if(status==1)
                        {
                            int pos=bundle.getInt("position");
                            fileFragment.changeItem(tracePath+name,pos);
                        }
                    }
                    Toast toast = null;
                    if(status==0) {
                        toast = Toast.makeText(this, "Tạo file thành công", Toast.LENGTH_LONG);
                    }
                    else if(status==1)
                    {
                        toast = Toast.makeText(this, "Cập nhật file thành công", Toast.LENGTH_LONG);
                    }
                    toast.setMargin(0, 0);
                    toast.show();
                } else {
                    Toast toast = null;
                    if(status==0) {
                        toast = Toast.makeText(this, "Không tạo được file", Toast.LENGTH_LONG);
                    }
                    else if(status==1)
                    {
                        toast = Toast.makeText(this, "Không cập nhật được file", Toast.LENGTH_LONG);
                    }
                    toast.setMargin(0, 0);
                    toast.show();
                }

            }
        }
    }
}

