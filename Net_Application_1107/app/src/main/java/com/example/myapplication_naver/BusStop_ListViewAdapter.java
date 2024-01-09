package com.example.myapplication_naver;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BusStop_ListViewAdapter extends BaseAdapter {
    // 변수 선언
    Context mContext;
    LayoutInflater inflater;
    //private ArrayList<String> busNamesList = null;
    private ArrayList<String> busNumlist =  new ArrayList<String>();
    private ArrayList<String> nowPoslist =  new ArrayList<String>();
    private ArrayList<String> extimeMinList =  new ArrayList<String>();
    private ArrayList<String> msgList =  new ArrayList<String>();
    private boolean textRaise;

    public BusStop_ListViewAdapter(Context context, ArrayList<String> busNumlist, ArrayList<String> nowPoslist, ArrayList<String> extimeMinList, ArrayList<String> msgList, boolean textRaise) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.busNumlist.addAll(busNumlist);
        this.nowPoslist.addAll(nowPoslist);
        this.extimeMinList.addAll(extimeMinList);
        this.msgList.addAll(msgList);
        this.textRaise = textRaise;
    }

    public class ViewHolder {
        TextView busNum;
        TextView nowPosition;
        TextView extimeMin;
        TextView msg;
    }

    @Override
    public int getCount() {
        return busNumlist.size();
    }

    @Override
    public String getItem(int position) {
        //return busNumlist.get(position) + "-" + nowPoslist.get(position) + "-" + extimeMinList.get(position);
        return busNumlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();

            view = inflater.inflate(R.layout.busstop_listview_items, null);

            // list_view_item.xml에서 TextViews 찾기
            holder.busNum = (TextView) view.findViewById(R.id.busNum);
            holder.nowPosition = (TextView) view.findViewById(R.id.nowPosition);
            holder.extimeMin = (TextView) view.findViewById(R.id.extimeMin);
            holder.msg = (TextView) view.findViewById(R.id.msg);

            if(textRaise){
                holder.busNum.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                holder.nowPosition.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                holder.extimeMin.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
            } else{
                holder.busNum.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
                holder.nowPosition.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                holder.extimeMin.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            }

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // 결과를 TextViews로 설정합니다.
        holder.busNum.setText(busNumlist.get(position));
        switch (msgList.get(position)){
            case "06":
                holder.nowPosition.setVisibility(View.GONE);
                holder.extimeMin.setVisibility(View.GONE);
                holder.msg.setVisibility(View.VISIBLE);
                holder.msg.setText("진입중");
                break;
            case "07":
                holder.nowPosition.setVisibility(View.GONE);
                holder.extimeMin.setVisibility(View.GONE);
                holder.msg.setVisibility(View.VISIBLE);
                holder.msg.setText("운행 대기중");
                break;
            default:
                if (nowPoslist.get(position).equals("1")) {  //도착 한정거장 남았을 경우
                    holder.nowPosition.setVisibility(View.GONE);
                    holder.extimeMin.setVisibility(View.GONE);
                    holder.msg.setVisibility(View.VISIBLE);
                    holder.msg.setText("잠시 후 도착");
                    break;
                } else {
                    holder.nowPosition.setVisibility(View.VISIBLE);
                    holder.extimeMin.setVisibility(View.VISIBLE);
                    holder.msg.setVisibility(View.GONE);
                    holder.nowPosition.setText(nowPoslist.get(position) + " 정거장 남음");
                    holder.extimeMin.setText(extimeMinList.get(position) + " 분");
                    break;
                }
        }

        return view;
    }
}
