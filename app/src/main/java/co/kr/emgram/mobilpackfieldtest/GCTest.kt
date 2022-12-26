package co.kr.emgram.mobilpackfieldtest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import co.kr.emgram.mobilpackfieldtest.databinding.ActivityMainBinding

class GCTest: AppCompatActivity() {
    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var outer: Outer? = Outer()

        outer?.sampleMethod()
//        Log.d("test", "outer" + outer.toString())
        outer = null

//        System.gc()
//        Log.d("test", "outer after gc" + outer.toString())
//        binding.btnTest.setOnClickListener {
//            System.gc()
//        }
    }


}