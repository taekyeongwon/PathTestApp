package co.kr.emgram.mobilpackfieldtest.listadapter

import androidx.recyclerview.widget.DiffUtil

class DiffCallback<T: BaseData>: DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem === newItem  //이전 어댑터와 바뀌는 어댑터의 아이템이 동일한지 확인
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem   //이전 어댑터와 바뀌는 어댑터의 아이템 내 내용 비교. areItemsTheSame이 true가 나오는 경우 추가 비교하기 위한 메소드
    }
}