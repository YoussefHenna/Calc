package com.gooofystudios.calc

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.animation.addListener
import androidx.core.view.setPadding
import com.fathzer.soft.javaluator.DoubleEvaluator
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {

    private var velocityTracker: VelocityTracker? = null;
    private var isAnimating = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initDragAction()
        initViews()
        initExpandAnim()



    }



    private fun initViews(){
        darkenForeground.visibility = View.GONE
        switchButton.setOnClickListener {
            if(!isAnimating) {
                if (bottomContainer.currentState == R.id.start) {
                    bottomContainer.transitionToEnd()
                } else {
                    bottomContainer.transitionToStart()
                }
                isAnimating = true;
                Handler().postDelayed({
                    isAnimating = false
                },500)
            }
        }

        var before = ""
        mainBigText.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                before = s.toString()

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(mainBigText.lineCount > mainBigText.maxLines ){
                    mainBigText.text = before
                    Toast.makeText(this@MainActivity,"Max character count reached",Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    public fun onButtonClicked(v: View){
        val tv = v as TextView
        when(tv.text.toString()){
            "C" ->{
                mainSmallText.text = ""
                mainBigText.text = ""
            }
            "Del" ->{
                var bigTxt = mainBigText.text.toString()
                if(bigTxt.contains("=")){
                    bigTxt = bigTxt.substring(bigTxt.indexOf("=")+1)
                    if(bigTxt.isNotEmpty()) bigTxt = bigTxt.substring(0,bigTxt.length-1)
                    mainBigText.text = bigTxt
                }
                else{
                    if(bigTxt.isNotEmpty()) bigTxt = bigTxt.substring(0,bigTxt.length-1)
                    mainBigText.text = bigTxt
                }
            }
            "=" ->{
                mainSmallText.text = mainBigText.text
                val answer = evalExpression(mainBigText.text.toString())
                mainBigText.text = "= $answer"

            }
            else ->{
                var bigTxt = mainBigText.text.toString()
                if(bigTxt.contains("=")){
                    bigTxt = bigTxt.substring(bigTxt.indexOf("=")+1)
                    if(bigTxt.isNotEmpty()) bigTxt = bigTxt.substring(0,bigTxt.length)
                    mainBigText.text = bigTxt
                }
                val newTxt = mainBigText.text.toString() + tv.text.toString()
                mainBigText.text = newTxt
            }
        }

    }

    private fun evalExpression(exp: String): String{
        var expression = exp.replace("÷","/")
        expression = expression.replace("x","*")



        val evaluator = DoubleEvaluator()
        val answer = evaluator.evaluate(expression)


        if(answer.toInt().toDouble() == answer){
            return answer.toInt().toString()
        }
        else{
            val df = DecimalFormat("0.00")
            return df.format(answer)
        }

    }

    private fun initExpandAnim(){
        bottomContainer.setTransitionListener(object: MotionLayout.TransitionListener{
            override fun onTransitionStarted(layout: MotionLayout?, start: Int, end: Int) {
                if(hidden1.visibility == View.GONE){
                    hidden1.alpha = 0.0f
                    hidden2.alpha = 0.0f
                    hidden3.alpha = 0.0f
                    hidden4.alpha = 0.0f
                    hidden5.alpha = 0.0f

                    hidden1.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,0.0f)
                    hidden2.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,0.0f)
                    hidden3.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,0.0f)
                    hidden4.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,0.0f)
                    hidden5.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,0.0f)


                    hidden1.visibility = View.VISIBLE
                    hidden2.visibility = View.VISIBLE
                    hidden3.visibility = View.VISIBLE
                    hidden4.visibility = View.VISIBLE
                    hidden5.visibility = View.VISIBLE

                    val animator = ValueAnimator.ofFloat(0.0f,1.0f)
                    animator.addUpdateListener {

                        hidden1.alpha = it.animatedValue as Float
                        hidden2.alpha = it.animatedValue as Float
                        hidden3.alpha = it.animatedValue as Float
                        hidden4.alpha = it.animatedValue as Float
                        hidden5.alpha = it.animatedValue as Float
                        hidden1.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,it.animatedValue as Float)
                        hidden2.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,it.animatedValue as Float)
                        hidden3.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,it.animatedValue as Float)
                        hidden4.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,it.animatedValue as Float)
                        hidden5.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,it.animatedValue as Float)


                    }

                    animator.duration = 500
                    animator.start()


                    val animator2 = ValueAnimator.ofInt(37.toPx(),30.toPx())
                    animator2.addUpdateListener {
                        switchButton.setPadding(it.animatedValue as Int)
                    }
                    animator2.duration = 500
                    animator2.start()


                }
                else{
                    val animator = ValueAnimator.ofFloat(1.0f,0.0f)
                    animator.addUpdateListener {


                        hidden1.alpha = it.animatedValue as Float
                        hidden2.alpha = it.animatedValue as Float
                        hidden3.alpha = it.animatedValue as Float
                        hidden4.alpha = it.animatedValue as Float
                        hidden5.alpha = it.animatedValue as Float
                        hidden1.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,it.animatedValue as Float)
                        hidden2.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,it.animatedValue as Float)
                        hidden3.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,it.animatedValue as Float)
                        hidden4.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,it.animatedValue as Float)
                        hidden5.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,it.animatedValue as Float)

                    }

                    animator.duration = 500
                    animator.start()
                    animator.addListener(onEnd = {
                        Handler().postDelayed({
                            hidden1.visibility = View.GONE
                            hidden2.visibility = View.GONE
                            hidden3.visibility = View.GONE
                            hidden4.visibility = View.GONE
                            hidden5.visibility = View.GONE
                        },0)

                    })

                    val animator2 = ValueAnimator.ofInt(30.toPx(),37.toPx())
                    animator2.addUpdateListener {
                        switchButton.setPadding(it.animatedValue as Int)
                    }
                    animator2.duration = 500
                    animator2.start()



                }
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }
        })

    }

    private fun initDragAction(){
        mainMotion.setTransition(R.id.start,R.id.expanded_top)
        var isReversed = false
        var isTouchEnabled = true;
        topContainer.setOnTouchListener { v, event ->
            if(isTouchEnabled) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (velocityTracker == null) {
                            velocityTracker = VelocityTracker.obtain()
                        }
                        velocityTracker!!.addMovement(event)
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        velocityTracker!!.addMovement(event)
                        velocityTracker!!.computeCurrentVelocity(100)
                        ///Velocity for high for easier tracking of speed
                        val changeHigh = velocityTracker!!.getYVelocity(event.getPointerId(event.actionIndex)) * 0.005
                        val speed = if (changeHigh < 0) changeHigh * -1 else changeHigh
                        //If high speed, do animation automatically
                        if (speed > 2.5) {
                            if (changeHigh < 0) {
                                mainMotion.transitionToStart()
                                isTouchEnabled = false;
                                isReversed = false

                            } else {
                                mainMotion.transitionToEnd()
                                isTouchEnabled = false
                                isReversed = true

                            }
                            Handler().postDelayed({
                                isTouchEnabled = true
                            },200)
                            false
                        } else {
                            ///Change progress based on touch velocity (multiplied by 0.0003 to work within range 0 - 1)
                            val change = velocityTracker!!.getYVelocity(event.getPointerId(event.actionIndex)) * 0.0003f
                            if ((mainMotion.progress + change) in 0f..1f) {
                                mainMotion.progress += change
                            }
                            true
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        ///Adds margin in order to carry out anim automatically
                        if (!isReversed) {
                            if (mainMotion.progress < 0.2f) {
                                mainMotion.transitionToStart()
                                isReversed = false

                            } else {
                                mainMotion.transitionToEnd()
                                isReversed = true
                            }
                        } else {
                            if (mainMotion.progress > 0.8f) {
                                mainMotion.transitionToEnd()
                                isReversed = true

                            } else {
                                mainMotion.transitionToStart()
                                isReversed = false
                            }
                        }
                        false
                    }
                    else -> {
                        false
                    }
                }
            }
            else{
                false
            }
        }

        darkenForeground.setOnClickListener{
            mainMotion.transitionToStart()
            isReversed = false
        }

        mainMotion.setTransitionListener(object: MotionLayout.TransitionListener{
            override fun onTransitionCompleted(p0: MotionLayout?, current: Int) {
                if(current == R.id.start){
                    darkenForeground.visibility = View.GONE
                }
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
            }

            override fun onTransitionStarted(p0: MotionLayout?, start: Int, end: Int) {
                darkenForeground.visibility = View.VISIBLE

            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }
        })


    }


    fun Int.toPx(): Int{
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        return Math.round(this * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }
    fun Float.toPx(): Int{
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        return Math.round(this * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

}
