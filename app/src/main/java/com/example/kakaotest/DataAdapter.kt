package com.example.kakaotest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class DataAdapter(val context: Context, val dataList:ArrayList<SearchData>): BaseAdapter() {



    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.data_list, null)

        /* 위에서 생성된 view를 res-layout-main_lv_item.xml 파일의 각 View와 연결하는 과정이다. */
        val dataphoto = view.findViewById<ImageView>(R.id.data_image)
        val dataname = view.findViewById<TextView>(R.id.data_name)
        val dataaddress = view.findViewById<TextView>(R.id.data_place)

        /* ArrayList<Dog>의 변수 dog의 이미지와 데이터를 ImageView와 TextView에 담는다. */
        val data = dataList[position]
        val resourceId = context.resources.getIdentifier("point", "drawable", context.packageName)
        dataphoto.setImageResource(resourceId)
        dataname.text = data.id
        dataaddress.text = data.address

        return view
    }
    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }




}