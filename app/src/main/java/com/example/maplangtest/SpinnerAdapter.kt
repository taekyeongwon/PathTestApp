//package com.example.maplangtest
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.BaseAdapter
//import android.widget.TextView
//import java.util.*
//
//class SpinnerAdapter(val context: Context) : BaseAdapter(){
//    private val carInfoList: ArrayList<String> = ArrayList()
//    private val inflater: LayoutInflater
//    init {
//        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        carInfoList.add("선택")
//    }
//
//    fun setCarInfoData(carInfoData: List<String>) {
//        carInfoList.addAll(carInfoData)
//        notifyDataSetChanged()
//    }
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view: View = convertView ?: inflater.inflate(R.layout.spinner_custom, parent, false)
//
//        val text = carInfoList[position]
//
//        if(position == 0) {
//            (view.findViewById(R.id.sp_custom_tv) as TextView).text = ""
//            (view.findViewById(R.id.sp_custom_tv) as TextView).hint = carInfoList[position]
//        } else {
//            (view.findViewById(R.id.sp_custom_tv) as TextView).text = text
//        }
//
//        return view
//    }
//
//    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view: View = convertView ?: inflater.inflate(R.layout.spinner_view, parent, false)
//
//        val text = carInfoList[position]
//        if(position == 0) {
//            (view.findViewById(R.id.sp_tv) as TextView).visibility = View.GONE
//        } else {
//            (view.findViewById(R.id.sp_tv) as TextView).visibility = View.VISIBLE
//            (view.findViewById(R.id.sp_tv) as TextView).text = text
//        }
//        return view
//    }
//
//    override fun getItem(position: Int): String {
//        return carInfoList[position]
//    }
//
//    override fun getItemId(p0: Int): Long {
//        return p0.toLong()
//    }
//
//    override fun getCount(): Int {
//        return carInfoList.size
//    }
//}