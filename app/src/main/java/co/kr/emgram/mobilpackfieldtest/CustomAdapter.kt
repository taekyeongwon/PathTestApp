package co.kr.emgram.mobilpackfieldtest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.custom_item.view.*

class CustomAdapter: RecyclerView.Adapter<CustomAdapter.CustomViewHolder>(), OnItemMoved {
    private val list: ArrayList<Int> = ArrayList()

    fun setList(list: ArrayList<Int>) {
        this.list.addAll(list)
        notifyDataSetChanged()
    }
    fun insertList(list: ArrayList<Int>) {
        this.list.addAll(list)
        notifyItemRangeInserted(this.list.size - list.size, list.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.custom_item, parent, false))
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.itemView.custom_tv.text = list[position].toString()
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        if(fromPosition < toPosition) {
            for(i in fromPosition until toPosition) {
                list.set(i, list.set(i+1, list.get(i)))
            }
        } else {
            for (i in fromPosition..toPosition + 1) {
                list.set(i, list.set(i-1, list.get(i)))
            }
        }

        notifyItemMoved(fromPosition, toPosition)
    }

    inner class CustomViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        init {
            with(itemView) {

            }
        }
    }
}