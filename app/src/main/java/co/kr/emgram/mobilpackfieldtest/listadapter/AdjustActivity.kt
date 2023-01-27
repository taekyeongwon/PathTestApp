package co.kr.emgram.mobilpackfieldtest.listadapter

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import co.kr.emgram.mobilpackfieldtest.R
import kotlinx.android.synthetic.main.activity_adjust.*
import java.util.Collections.addAll

class AdjustActivity: AppCompatActivity() {
    private var adjustAdapter: AdjustAdapter? = null
    private var adjustList = arrayListOf<AdjustListItem>().apply {
        add(AdjustListItem("a", 1000))
        add(AdjustListItem("b", 2000))
        add(AdjustListItem("c", 3000))
        add(AdjustListItem("d", 4000))
        add(AdjustListItem("e", 5000))
        add(AdjustListItem("f", 6000))
        add(AdjustListItem("g", 7000))
        add(AdjustListItem("h", 8000))
        add(AdjustListItem("i", 9000))
    }
    private var addList = ArrayList<AdjustListItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjust)

        adjustAdapter = AdjustAdapter()
        rv_adjust.adapter = adjustAdapter

        btn_more.setOnClickListener {
            addList.addAll(adjustList.shuffled())
            adjustAdapter!!.submitList(addList.toMutableList())
        }
        substrText("asdfasdf")
    }

    private fun substrText(str: String) {
        val s = str.substring(0 until 8)
        Log.d("test", s)
    }
}