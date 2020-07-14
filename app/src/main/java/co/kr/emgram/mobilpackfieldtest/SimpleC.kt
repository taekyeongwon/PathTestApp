package co.kr.emgram.mobilpackfieldtest

import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import com.jaygoo.widget.*
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.floor

class SimpleC
    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    :VerticalRangeSeekBar(context, attrs), OnRangeChangedListener{
    private val stepDivRect = RectF()
    private val tickMarkTextRect = Rect();

    private var leftIntValue = 0
    private var rightIntValue = 0
    val leftValue = 10.0f
    val rightValue = 50.0f
    val count = 10

    init {
        setRange(leftValue, rightValue)
        setProgress(leftValue, rightValue)
        steps = ((rightValue - leftValue) / count).toInt()
        setLeftRight(leftValue.toInt(), rightValue.toInt())
        setOnRangeChangedListener(this)
    }

    fun getRange(): Pair<Int, Int> {
        return Pair(leftIntValue, rightIntValue)
    }

    override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

    }

    override fun onRangeChanged(
        view: RangeSeekBar?,
        leftValue: Float,
        rightValue: Float,
        isFromUser: Boolean
    ) {
        setLeftRight(leftValue.toInt(), rightValue.toInt())
        Log.d("Seekbar", "left: "+leftValue.toInt()+", right: "+rightValue.toInt())
    }

    override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

    }

    private fun setLeftRight(left: Int, right: Int) {

        this.leftIntValue = ceil(left.toDouble() / count).toInt() - 1
        this.rightIntValue = floor(right.toDouble() / count).toInt() - 1

        Log.d("Simple", "left: "+leftIntValue+", right: "+rightIntValue)
    }

    override fun onDrawSteps(canvas: Canvas?, paint: Paint?) {
        super.onDrawSteps(canvas, paint)
        if (!verifyStepsMode()) return
        val stepMarks = progressWidth / steps
        val extHeight = (stepsHeight - progressHeight) / 2f
        modStepsBitmap(leftIntValue, rightIntValue)
        for (k in leftIntValue..rightIntValue) {
            val x = progressLeft + k * stepMarks - stepsWidth / 2f
            stepDivRect.set(x, progressTop - extHeight, x + stepsWidth, progressBottom + extHeight)
            if (stepsBitmaps.isEmpty() || stepsBitmaps.size <= k) {
                paint?.setColor(stepsColor)
                canvas?.drawRoundRect(stepDivRect, stepsRadius, stepsRadius, paint!!)
            } else {
                canvas?.drawBitmap(stepsBitmaps[k], null, stepDivRect, paint)
            }
        }
    }

    private fun verifyStepsMode(): Boolean {
        return if (steps < 1 || stepsHeight <= 0 || stepsWidth <= 0) false else true
    }

    private fun modStepsBitmap(leftValue: Int, rightValue: Int) {
        if (!verifyStepsMode()) return

        stepsDrawableId = R.drawable.calendar_selector
        for(k in leftValue..rightValue) {
            stepsBitmaps[k] = Utils.drawableToBitmap(
                context,
                stepsWidth.toInt(),
                stepsHeight.toInt(),
                R.drawable.ic_launcher_background
            )
        }
    }

    override fun onDrawTickMark(canvas: Canvas?, paint: Paint?) {
        if (tickMarkTextArray != null) {
            val arrayLength = tickMarkTextArray.size
            val trickPartWidth = progressWidth / (arrayLength - 1)
            for (i in 0 until arrayLength) {
                val text2Draw = tickMarkTextArray[i].toString()
                if (TextUtils.isEmpty(text2Draw)) continue
                paint?.getTextBounds(text2Draw, 0, text2Draw.length, tickMarkTextRect)
                paint?.setColor(tickMarkTextColor)
                //平分显示
                val num = Utils.parseFloat(text2Draw)
                val states = modRangeSeekBarState()
                if (Utils.compareFloat(num, states[0].value) != -1 && Utils.compareFloat(
                        num,
                        states[1].value
                    ) != 1 && seekBarMode == RangeSeekBar.SEEKBAR_MODE_RANGE
                ) {
                    paint?.setColor(tickMarkInRangeTextColor)
                }
                //按实际比例显示
                val x = progressLeft + progressWidth * (num - minProgress) / (maxProgress - minProgress) - tickMarkTextRect.width() / 2f
                val y = (progressTop - tickMarkTextMargin).toFloat()

                var degrees = 90
                val rotateX = x + tickMarkTextRect.width() / 2f
                val rotateY = y - (tickMarkTextRect.height() / 2f)

                if (degrees != 0) {
                    canvas?.rotate(degrees.toFloat(), rotateX, rotateY)
                }
                canvas?.drawText(text2Draw, x, y, paint!!)
                if (degrees != 0) {
                    canvas?.rotate((-degrees).toFloat(), rotateX, rotateY)
                }
            }

            for (i in 0 until arrayLength) {
                val text2Draw = tickMarkTextArray[i].toString()
                if (TextUtils.isEmpty(text2Draw)) continue
                paint?.getTextBounds(text2Draw, 0, text2Draw.length, tickMarkTextRect)
                paint?.setColor(tickMarkTextColor)
                //平分显示
                val num = Utils.parseFloat(text2Draw)
                val states = modRangeSeekBarState()
                if (Utils.compareFloat(num, states[0].value) != -1 && Utils.compareFloat(
                        num,
                        states[1].value
                    ) != 1 && seekBarMode == RangeSeekBar.SEEKBAR_MODE_RANGE
                ) {
                    paint?.setColor(tickMarkInRangeTextColor)
                }
                //按实际比例显示
                val x = progressLeft + progressWidth * (num - minProgress) / (maxProgress - minProgress) - tickMarkTextRect.width() / 2f
                val y2 = (progressBottom + tickMarkTextMargin + tickMarkTextRect.height()).toFloat()

                var degrees = 90
                val rotateX = x + tickMarkTextRect.width() / 2f
                val rotateY = y2 - (tickMarkTextRect.height() / 2f)

                if (degrees != 0) {
                    canvas?.rotate(degrees.toFloat(), rotateX, rotateY)
                }
                canvas?.drawText(text2Draw, x, y2, paint!!)
                if (degrees != 0) {
                    canvas?.rotate((-degrees).toFloat(), rotateX, rotateY)
                }
            }
        }
    }

    fun modRangeSeekBarState(): Array<SeekBarState> {
        val state = rangeSeekBarState
        val dFormat = DecimalFormat(".#")
        val leftSeekBarState = SeekBarState()
        val rightSeekBarState = SeekBarState()

        leftSeekBarState.value = dFormat.format(state[0].value).toFloat()
        rightSeekBarState.value = dFormat.format(state[1].value).toFloat()

        return arrayOf(leftSeekBarState, rightSeekBarState)
    }

//    fun initRangeSeekBarState(): Array<SeekBarState> {
//        val leftSeekBarState = SeekBarState()
//        leftSeekBarState.value = (leftIntValue * steps).toFloat()
//
//        leftSeekBarState.indicatorText = leftSeekBarState.value.toString()
//        if (Utils.compareFloat(leftSeekBarState.value, minProgress) == 0) {
//            leftSeekBarState.isMin = true
//        } else if (Utils.compareFloat(leftSeekBarState.value, maxProgress) == 0) {
//            leftSeekBarState.isMax = true
//        }
//
//        val rightSeekBarState = SeekBarState()
//        if (seekBarMode == SEEKBAR_MODE_RANGE) {
//            rightSeekBarState.value = (rightIntValue * steps).toFloat()
//            rightSeekBarState.indicatorText = rightSeekBarState.value.toString()
//            if (Utils.compareFloat(rightSeekBarState.value, minProgress) == 0) {
//                rightSeekBarState.isMin = true
//            } else if (Utils.compareFloat(rightSeekBarState.value, maxProgress) == 0) {
//                rightSeekBarState.isMax = true
//            }
//        }
//
//        return arrayOf(leftSeekBarState, rightSeekBarState)
//    }

    //    override fun drawActiveThumbs(canvas: Canvas) {
//        //super.drawActiveThumbs(canvas)
//        val bitmap = context.getDrawable(R.drawable.ic_launcher_background)?.toBitmap()
//
//        val resizeWidth = 20
//        val ratio = bitmap!!.height / bitmap.width
//        val targetHeight = resizeWidth * ratio
//        val result = Bitmap.createScaledBitmap(bitmap, resizeWidth, targetHeight, false)
//
//        canvas.drawBitmap(result, 0f, 0f, null)
//    }
}