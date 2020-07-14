package co.kr.emgram.mobilpackfieldtest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import io.apptik.widget.MultiSlider
import kotlinx.android.synthetic.main.activity_seekbar.*
import kotlinx.android.synthetic.main.activity_seekbar.view.*
import java.util.*

class SeekBarActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seekbar)


        range_btn.setOnClickListener {
            val range = custom_rsb.getRange()
            Log.d("range: ", "${range.first}, ${range.second}")
        }
        //val thu = slider.addThumbOnPos(2)
        //thu.setThumb(resources.getDrawable(R.drawable.calendar_selector, null))
        //thu.isEnabled = false
//        slider.setOnTrackingChangeListener(object: MultiSlider.OnTrackingChangeListener {
//            override fun onStartTrackingTouch(
//                multiSlider: MultiSlider?,
//                thumb: MultiSlider.Thumb?,
//                value: Int
//            ) {
//                thumb?.
//            }
//
//            override fun onStopTrackingTouch(
//                multiSlider: MultiSlider?,
//                thumb: MultiSlider.Thumb?,
//                value: Int
//            ) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//        })
//        slider.setOnThumbValueChangeListener { multiSlider, thumb, thumbIndex, value ->
//            slider.getThumb(thumbIndex).isEnabled = true
//
//        }

//        slider.max = 100
//        slider.min = 0
//        slider.step = 10

//        slider.getThumb().thumb = resources.getDrawable(R.drawable.ic_launcher_background, null)
//        slider.setTrackDrawable()
//        custom_rsb.steps = 10

//        custom_rsb.setStepsDrawable(
//            Arrays.asList(
//            R.drawable.ic_launcher_background,
//            R.drawable.ic_launcher_background,
//            R.drawable.ic_launcher_background,
//            R.drawable.ic_launcher_background,
//            R.drawable.ic_launcher_background,
//            R.drawable.ic_launcher_background,
//            R.drawable.ic_launcher_background,
//            R.drawable.ic_launcher_background,
//            R.drawable.ic_launcher_background,
//            R.drawable.ic_launcher_background,
//            R.drawable.ic_launcher_background))
    }
}