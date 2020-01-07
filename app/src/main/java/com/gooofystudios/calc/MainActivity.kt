package com.gooofystudios.calc

import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var velocityTracker: VelocityTracker? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initDragAction()


    }



    public fun onButtonClicked(v: View){
        val tv = v as TextView
        Toast.makeText(this,"${tv.text} Clicked",Toast.LENGTH_SHORT).show()
        ////////NOT WORKING FOR SOME REASON

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
                if(start == R.id.start){
                    darkenForeground.visibility = View.VISIBLE
                }
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }
        })


    }


    fun Int.toPx(): Int{
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        return Math.round(this * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }
}
