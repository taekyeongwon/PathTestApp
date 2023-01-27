package co.kr.emgram.mobilpackfieldtest.listadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.kr.emgram.mobilpackfieldtest.databinding.ItemAdjustBinding
import kotlinx.android.synthetic.main.item_adjust.view.*

class AdjustAdapter()
    : ListAdapter<AdjustListItem, AdjustAdapter.AdjustViewHolder>(DiffCallback()) {
//    private val adjustList = ArrayList<AdjustListItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdjustViewHolder {
        val binding = ItemAdjustBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdjustViewHolder(binding, listener)
    }

    private val listener = object: OnClick {
        override fun onClick(position: Int) {
            getItem(position).drive_price = 100000   //왜 스크롤 해야 바뀌지?
            submitList(currentList.toMutableList())
        }
    }

    override fun onBindViewHolder(holder: AdjustViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class AdjustViewHolder(val binding: ItemAdjustBinding, val clickListener: OnClick): RecyclerView.ViewHolder(binding.root) {
        private var item: AdjustListItem? = null
        init {
            with(binding.root) {
                binding.root.tv_title.setOnClickListener {
                    clickListener.onClick(adapterPosition)
                }
            }
        }
        fun onBind(data: AdjustListItem) {
            this.item = data
            binding.root.tv_title.text = "${data.drive_dt}, ${data.drive_price}"
        }
    }

    interface OnClick {
        fun onClick(position: Int)
    }
}