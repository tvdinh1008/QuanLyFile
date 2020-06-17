package com.example.quanlyfile.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlyfile.FileFragment;
import com.example.quanlyfile.R;
import com.example.quanlyfile.model.itemModel;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class fileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<itemModel> items;
    FileFragment.OpenSubPathFragment fragment;
    String subPath;
    IOpenFile openFile;

    public fileAdapter(List<itemModel> items) {
        //this.items = items;//2 cai cùng trỏ vào 1 vị trí ->clear lỗi
        this.items=new ArrayList<>();
        this.items.addAll(items);
    }

    public fileAdapter(List<itemModel> items, FileFragment.OpenSubPathFragment fragment, String subPath,IOpenFile openFile) {
        this.items = items;
        this.fragment = fragment;
        this.subPath=subPath;
        this.openFile=openFile;
    }

    public void showRoot(List<itemModel> items)
    {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item,parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder=(ItemViewHolder)holder;
        itemModel item=items.get(position);
        viewHolder.textView.setText(item.getName());
        viewHolder.textView.setTextColor(Color.BLACK);
        viewHolder.lastmodified.setText(item.getLastModified());
        if(item.getProperties()==1)
        {
            viewHolder.imageButton.setImageResource(R.drawable.ic_folder);
            viewHolder.sizefile.setText(String.valueOf(item.getCountFile()+ " mục"));
        }
        else if (item.getProperties()==2)
        {
            viewHolder.imageButton.setImageResource(R.drawable.ic_file);


            double bytes = item.getSizeFile();
            double kilobytes = (bytes / 1024); //điện thoại chuyển là 1000 thực tế
            double megabytes = (kilobytes / 1024);

            DecimalFormat dcf;
            DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
            formatSymbols.setDecimalSeparator('.');
            formatSymbols.setGroupingSeparator(',');
            dcf = new DecimalFormat("###,###,###,###.##", formatSymbols);
            if(bytes<1024)
            {
                viewHolder.sizefile.setText(String.valueOf(dcf.format(bytes))+ " B");
            }
            else if(bytes>=1024 && bytes< 1024*1024)
            {
                viewHolder.sizefile.setText(String.valueOf(dcf.format(kilobytes))+ " KB");
            }
            else
            {
                viewHolder.sizefile.setText(String.valueOf(dcf.format(megabytes))+ " MB");
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener
    {
        ImageButton imageButton;
        TextView textView;
        RelativeLayout layout_item;
        TextView sizefile;
        TextView lastmodified;


        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textView=itemView.findViewById(R.id.txt_item);
            imageButton=itemView.findViewById(R.id.image_item);
            sizefile=itemView.findViewById(R.id.sizefile);
            lastmodified=itemView.findViewById(R.id.lastmodified);
            layout_item=itemView.findViewById(R.id.layout_item);

            layout_item.setLongClickable(true);// khi click hoặc giữ 1 lúc
            layout_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(items.get(getAdapterPosition()).getProperties()==1) //là thư mục
                    {
                        fragment.openSubPath(subPath+items.get(getAdapterPosition()).getName());
                        Log.v("TAG"," selected pos " + getAdapterPosition()+" "+ items.get(getAdapterPosition()).getName());
                        Log.v("TAG","selected pos"+subPath);
                    }else //là file
                    {
                        openFile.openFile(getAdapterPosition());
                    }
                }
            });
            layout_item.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            itemModel itemModel=items.get(this.getAdapterPosition());
            menu.setHeaderTitle(itemModel.getName());
            if(itemModel.getProperties()==1)
            {
                menu.add(this.getAdapterPosition(),0,0,"Xóa");
                menu.add(this.getAdapterPosition(),1,0,"Đổi tên");
            }
            else
            {
                menu.add(this.getAdapterPosition(),0,0,"Xóa");
                menu.add(this.getAdapterPosition(),1,0,"Đổi tên");
                menu.add(this.getAdapterPosition(),3,0,"Sao chép");
            }
        }
    }
    public void removeItem(int position)
    {
        items.remove(position);
        notifyItemRemoved(position);
    }
    public void renameItem(int position,String name)
    {
        items.get(position).setName(name);
        notifyItemChanged(position);
    }
    public void addItem(itemModel item)
    {
        items.add(item);
        notifyDataSetChanged();
    }
    public void changeItem(itemModel itemModel,int pos)
    {
        itemModel item=items.get(pos);
        item.setName(itemModel.getName());
        item.setCountFile(itemModel.getCountFile());
        item.setSizeFile(itemModel.getSizeFile());
        item.setProperties(itemModel.getProperties());
        item.setLastModified(itemModel.getLastModified());
        notifyItemChanged(pos,item);
    }
    public interface IOpenFile
    {
        void openFile(int pos);
    }
 }
