package co.kr.emgram.mobilpackfieldtest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import io.apptik.widget.MultiSlider
import kotlinx.android.synthetic.main.activity_drag.*
import kotlinx.android.synthetic.main.activity_seekbar.*
import java.util.*

class DragActivity: AppCompatActivity() {
    private var adapter: CustomAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drag)

        adapter = CustomAdapter()
        drag_rv.adapter = adapter

        btn_insert.setOnClickListener {
            insertTest()
        }

        val list = ArrayList<Int>()
        list.add(1)
        list.add(2)
        list.add(3)
        list.add(4)
        list.add(5)
        list.add(6)
        list.add(7)
        list.add(8)
        adapter!!.setList(list)


        val callback = DragManageAdapter(adapter!!, this,
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0)

        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(drag_rv)
    }
    private fun insertTest() {
        val list = ArrayList<Int>()
        list.add(1)
        list.add(2)
        list.add(3)
        list.add(4)
        list.add(5)
        list.add(6)
        list.add(7)
        list.add(8)
        adapter?.insertList(list)
    }
}