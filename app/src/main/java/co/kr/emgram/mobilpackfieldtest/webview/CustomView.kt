package co.kr.emgram.mobilpackfieldtest.webview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import co.kr.emgram.mobilpackfieldtest.databinding.CustomLayoutBinding
import kotlinx.android.synthetic.main.custom_layout.view.*

class CustomView @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr){
        private val binding: CustomLayoutBinding
        init {
//            binding = CustomLayoutBinding.inflate(LayoutInflater.from(context), this, true) //현재 최상위 뷰 ConstraintLayout에 attachToParent true : 바로 붙임 / false : 이 커스텀 뷰 안에서 따로 addView 해줘야됨.
//            binding = CustomLayoutBinding.inflate(LayoutInflater.from(context), null, false) //null로 했을 땐? 얘는 attachToParent true면 앱 죽고 false면 addView 해줘야 함, 최상위 뷰 속성 안가져가므로 width값이 적용 안됨. 직접 LayoutParams 줘도 안먹음.. 방법은 없나?
            binding = CustomLayoutBinding.inflate(LayoutInflater.from(context), this, true) //엥간하면 이 형태로 inflate 시켜놓고 필요한 곳에서 addView하거나, xml 선언해서 addView없이 사용
//            binding.root.btn_test.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            addView(binding.root)
            binding.root.btn_test.setOnClickListener {
                Toast.makeText(context, "click", Toast.LENGTH_SHORT).show()
                binding.root.webview.loadUrl("www.naver.com")
            }
        }

}