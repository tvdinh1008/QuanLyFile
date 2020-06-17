package com.example.quanlyfile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanlyfile.adapters.fileAdapter;
import com.example.quanlyfile.model.itemModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class FileFragment extends Fragment implements fileAdapter.IOpenFile {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    String rootPath;
    String subPath;
    List<itemModel> items;
    int position;
    fileAdapter adapter;
    IUpdateTrace updateTrace;

    public FileFragment() {
        // Required empty public constructor
    }

    public static FileFragment newInstance(String param1, String param2) {
        FileFragment fragment = new FileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    //chỉ gọi 1 lần (không gọi lại khi cho vào stack)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rootPath = getArguments().getString(ARG_PARAM1);
            subPath = getArguments().getString(ARG_PARAM2);
            /*
            Bug vì khi ta đang đứng ở thư mục A tạo thư mục B->tracePath ở main đc cập nhật
            Nhưng khi ta vào thư mục B sau đó back thì nó sẽ ko đc update lại tracePath mà nó hiểu là tracePath=A/B
            Do ta sử dụng replace nên khi back nó sẽ gọi lại hàm onCreateView(ta update trong này là OK)
            String path=rootPath + subPath;
            updateTrace=(IUpdateTrace) getActivity();
            updateTrace.updateTrace(path);
            */
        }
    }

    //gọi lại nếu sử dụng stack khi backstack
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        items = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_file, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.list_file_item);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        //Lấy dữ liệu và cập nhật lại tracePath
        String path = rootPath + subPath;
        updateTrace = (IUpdateTrace) getActivity();
        updateTrace.updateTrace(path);

        File file = new File(path);
        items.clear();
        for (File fileItem : file.listFiles()) {
            items.add(getItem(fileItem));
        }
        //Hiển thị giao diện
        adapter = new fileAdapter(items, (OpenSubPathFragment) getActivity(), subPath, this);// truyền interface vào
        recyclerView.setAdapter(adapter);
        // Inflate the layout for this fragment
        return view;
    }

    public void additem(String path) {
        File file = new File(path);
        itemModel itemModel = this.getItem(file);
        adapter.addItem(itemModel);
    }

    public void changeItem(String path, int pos) {
        File file = new File(path);
        itemModel itemModel = this.getItem(file);
        adapter.changeItem(itemModel, pos);
    }

    public itemModel getItem(File fileItem) {
        itemModel item = new itemModel();
        item.setName(fileItem.getName());
        if (fileItem.isDirectory()) {
            item.setProperties(1);
            item.setCountFile(fileItem.list().length);
        } else if (fileItem.isFile()) {
            item.setProperties(2);
        } else if (fileItem.isHidden()) {
            item.setProperties(3);
        } else item.setProperties(0);

        //lấy ngày sửa đổi file lần cuối
        Date date = new Date(fileItem.lastModified());
        //String df=new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(date);
        String df = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
        item.setLastModified(df);
        //lấy kích thước file byte;
        item.setSizeFile(fileItem.length());//trả về số byte
        return item;
    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Log.v("TAG", " selected item long click " + item.getItemId());
        switch (item.getItemId()) {
            case 0: //xóa
                showDeleteMyAlterDialog(item.getGroupId());
                return true;
            case 1: //đổi tên
                showCustomerAlertDialogBox(item.getGroupId());
                return true;
            case 3: //sao chép
                updateTrace.pathCopy(rootPath + subPath + items.get(item.getGroupId()).getName());
                displayMessage("file copy ...");
                return true;
            default:
                return super.onContextItemSelected(item);

        }
    }

    public void displayMessage(String message) {
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_LONG);
        toast.setMargin(0, 0);
        toast.show();
    }

    public void showDeleteMyAlterDialog(final int pos) {
        position = pos;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        AlertDialog dialog = builder.setTitle("Xóa thư mục")
                .setMessage("Bạn có chắc chắn muốn xóa?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //xóa ở đây
                        File file = new File(rootPath + subPath + items.get(position).getName());
                        boolean success = false;
                        success=deleteDir(file);
                        if (success) {
                            adapter.removeItem(position); // do ta gắn this.getAdapterPosition() vào mỗi item vì thế ta có thể lấy ra làm vị trí của item cần xóa
                            displayMessage("Xóa thành công!");
                        } else {
                            displayMessage("Xóa thất bại");
                        }
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    public static Boolean deleteDir(File file) {
        // neu file la thu muc thi xoa het thu muc con va file cua no
        if (file.isDirectory()) {
            // liet ke tat ca thu muc va file
            String[] files = file.list();
            for (String child : files) {
                File childDir = new File(file, child);
                if (childDir.isDirectory()) {
                    // neu childDir la thu muc thi goi lai phuong thuc deleteDir()
                    deleteDir(childDir);
                } else {
                    // neu childDir la file thi xoa
                    childDir.delete();
                }
            }
            // Check lai va xoa thu muc cha
            if (file.list().length == 0) {
                file.delete();
            }

        } else {
            // neu file la file thi xoa
            file.delete();
        }
        return true;
    }

    public void showCustomerAlertDialogBox(final int pos) {
        final Dialog dialog = new Dialog(getActivity());
        position = pos;
        dialog.setContentView(R.layout.customer_name_layout);
        TextView textView = dialog.findViewById(R.id.txt_title);
        textView.setText("Đổi tên file");
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
                if (!name.equals("")) {
                    String tmp = items.get(position).getName();
                    int index = tmp.lastIndexOf(".");
                    String d = "";
                    if (index > 0) {
                        d = tmp.substring(index);
                    }
                    File file = new File(rootPath + subPath + tmp);
                    File dest = new File(rootPath + subPath + name + d);
                    if (!dest.exists()) {
                        file.renameTo(dest);
                        adapter.renameItem(position, name + d);
                        displayMessage("Đổi tên thành công");
                    } else {
                        displayMessage("Tên đã tồn tại");
                    }
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
    public void openFile(int pos) {
        String path = rootPath + subPath + items.get(pos).getName();
        final Bitmap b = BitmapFactory.decodeFile(path, null);
        if (b == null) {
            Intent intent = new Intent(getActivity(), OpenFileActivity.class);
            intent.putExtra("param1", rootPath + subPath);
            intent.putExtra("param2", items.get(pos).getName());
            intent.putExtra("status", 1); //=1mở và chỉnh sửa =0 là tạo file mới rồi ghi vào
            intent.putExtra("position", pos);
            getActivity().startActivityForResult(intent, 1111);
        } else {
            Intent intent = new Intent(getActivity(), OpenImageActivity.class);
            intent.putExtra("param1", path);
            getActivity().startActivityForResult(intent, 1112);
        }
    }


    //khi click vào 1 item thì nó sẽ truyền cho activity rồi mở fragment mới lên
    public interface OpenSubPathFragment {
        void openSubPath(String subpath);
    }
}
