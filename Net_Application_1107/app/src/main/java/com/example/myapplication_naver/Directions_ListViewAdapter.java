package com.example.myapplication_naver;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class Directions_ListViewAdapter extends BaseAdapter {
    // 변수 선언
    Context mContext;
    LayoutInflater inflater;
    private ArrayList<String> deplist =  new ArrayList<String>();
    private ArrayList<String> arrlist =  new ArrayList<String>();
    private boolean textRaise;

    public Directions_ListViewAdapter(Context context, ArrayList<String> deplist, ArrayList<String> arrlist, boolean textRaise) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.deplist.addAll(deplist);
        this.arrlist.addAll(arrlist);
        this.textRaise = textRaise;
    }

    public class ViewHolder {
        TextView dep;
        TextView arr;
    }

    @Override
    public int getCount() {
        return deplist.size();
    }

    @Override
    public String getItem(int position) {
        return deplist.get(position) + "→" + arrlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final Directions_ListViewAdapter.ViewHolder holder;
        if (view == null) {
            holder = new Directions_ListViewAdapter.ViewHolder();

            view = inflater.inflate(R.layout.direc_recent_listview_items, null);

            // list_view_item.xml에서 TextViews 찾기
            holder.dep = (TextView) view.findViewById(R.id.dep);
            holder.arr = (TextView) view.findViewById(R.id.arr);

            if(textRaise){
                holder.dep.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                holder.arr.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            } else {
                holder.dep.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                holder.arr.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            }

            view.setTag(holder);
        } else {
            holder = (Directions_ListViewAdapter.ViewHolder) view.getTag();
        }
        // 결과를 TextViews로 설정합니다.
        holder.dep.setText(deplist.get(position));
        holder.arr.setText(arrlist.get(position));
        return view;
    }
}
