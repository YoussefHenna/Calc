package com.gooofystudios.calc

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.animation.addListener
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.setPadding
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

    override fun onResume() {
        super.onResume()
        if(appInfoTv.alpha == 0.0f) {
            val anim = ValueAnimator.ofFloat(0.0f, 1.0f)
            anim.addUpdateListener {
                appInfoTv.alpha = it.animatedValue as Float
            }
            anim.duration = 100
            anim.start()
        }
    }



    //Initialized default values and click listeners (instead of cramming "onCreate()")
    private fun initViews(){
        mainBigText.setSelection(mainBigText.text.toString().length)
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

        //Disables keyboard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mainBigText.showSoftInputOnFocus = false
        }
        mainBigText.requestFocus()

        //Disables selection menu
        mainBigText.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }
            override fun onDestroyActionMode(mode: ActionMode?) {}
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return false
            }
        }


    }


    ////Handles Deletion of (sin,cos,log etc..) with a single click instead of letter by letter
    ///Boolean returned is to notify that the deletion has been handled and should'nt be handled agaian (Check function of "Del" button)
    ///A bit messy but works so don't question it
    private fun handleByWhole():Boolean{
        try {
            var bigTxt = mainBigText.text.toString()
            var prevText = ""
            var prevTextSmall = ""
            var startIndex = 0
            if (bigTxt.length > 4) {
                prevText =
                    bigTxt.substring(mainBigText.selectionStart - 4, mainBigText.selectionStart)
                prevTextSmall =
                    bigTxt.substring(mainBigText.selectionStart - 3, mainBigText.selectionStart)
                startIndex = mainBigText.selectionStart - 4

            } else {
                prevText = bigTxt.substring(0, mainBigText.selectionStart)
                prevTextSmall = bigTxt.substring(0, mainBigText.selectionStart)
            }

            if (prevText.contains("c")) {
                var i = 0
                while (prevText != "cos(") {
                    if (i + mainBigText.selectionStart < bigTxt.length) prevText =
                        bigTxt.substring(startIndex, mainBigText.selectionStart + i)
                    var y = 0
                    while (startIndex + y < mainBigText.selectionStart + i) {
                        prevText = bigTxt.substring(startIndex + y, mainBigText.selectionStart + i)
                        if (prevText == "cos(") {
                            startIndex += y
                            break
                        }
                        y++
                    }
                    i++
                }
                bigTxt = bigTxt.substring(0, startIndex) + bigTxt.substring(
                    startIndex + 4,
                    bigTxt.length
                )
                mainBigText.setText(bigTxt)
                mainBigText.setSelection(startIndex)
                return true
            } else if (prevText.contains("s")) {
                var i = 0
                while (prevText != "sin(") {
                    if (i + mainBigText.selectionStart < bigTxt.length) prevText =
                        bigTxt.substring(startIndex, mainBigText.selectionStart + i)
                    var y = 0
                    while (startIndex + y < mainBigText.selectionStart + i) {
                        prevText = bigTxt.substring(startIndex + y, mainBigText.selectionStart + i)
                        if (prevText == "sin(") {
                            startIndex += y
                            break
                        }
                        y++
                    }
                    i++
                }
                bigTxt = bigTxt.substring(0, startIndex) + bigTxt.substring(
                    startIndex + 4,
                    bigTxt.length
                )
                mainBigText.setText(bigTxt)
                mainBigText.setSelection(startIndex)
                return true
            } else if (prevText.contains("t")) {
                var i = 0
                while (prevText != "tan(") {
                    if (i + mainBigText.selectionStart < bigTxt.length) prevText =
                        bigTxt.substring(startIndex, mainBigText.selectionStart + i)
                    var y = 0
                    while (startIndex + y < mainBigText.selectionStart + i) {
                        prevText = bigTxt.substring(startIndex + y, mainBigText.selectionStart + i)
                        if (prevText == "tan(") {
                            startIndex += y
                            break
                        }
                        y++
                    }
                    i++
                }
                bigTxt = bigTxt.substring(0, startIndex) + bigTxt.substring(
                    startIndex + 4,
                    bigTxt.length
                )
                mainBigText.setText(bigTxt)
                mainBigText.setSelection(startIndex)
                return true
            } else if (prevTextSmall.contains("l") && ((bigTxt.substring(bigTxt.indexOf(prevTextSmall), bigTxt.indexOf(prevTextSmall) + 2).contains("n")))
            ) {
                var i = 0
                if (startIndex == mainBigText.selectionStart - 4) {
                    startIndex = mainBigText.selectionStart - 3
                }
                while (prevText != "ln(") {
                    if (i + mainBigText.selectionStart < bigTxt.length) prevText =
                        bigTxt.substring(startIndex, mainBigText.selectionStart + i)
                    var y = 0
                    while (startIndex + y < mainBigText.selectionStart + i) {
                        prevText = bigTxt.substring(startIndex + y, mainBigText.selectionStart + i)
                        if (prevText == "ln(") {
                            startIndex += y
                            break
                        }
                        y++
                    }
                    i++
                }
                bigTxt = bigTxt.substring(0, startIndex) + bigTxt.substring(
                    startIndex + 3,
                    bigTxt.length
                )
                mainBigText.setText(bigTxt)
                mainBigText.setSelection(startIndex)
                return true
            } else if (prevText.contains("l")) {
                var i = 0
                while (prevText != "log(") {
                    if (i + mainBigText.selectionStart < bigTxt.length) prevText =
                        bigTxt.substring(startIndex, mainBigText.selectionStart + i)
                    var y = 0
                    while (startIndex + y < mainBigText.selectionStart + i) {
                        prevText = bigTxt.substring(startIndex + y, mainBigText.selectionStart + i)
                        if (prevText == "log(") {
                            startIndex += y
                            break
                        }
                        y++
                    }
                    i++
                }
                bigTxt = bigTxt.substring(0, startIndex) + bigTxt.substring(
                    startIndex + 4,
                    bigTxt.length
                )
                mainBigText.setText(bigTxt)
                mainBigText.setSelection(startIndex)
                return true
            }
        }catch (e: IndexOutOfBoundsException){
            return false
        }

        return false
    }


    /*
    NOTE:
    mainBigText.setSelection()
    is used repeatedly (in "onButtonClicked()") to maintain the position of the selection pointer whenever text is changed
     */


    //Click listener for all calculator Buttons
    public fun onButtonClicked(v: View){
        val tv = v as TextView
        val text = tv.text.toString()

        //Operations' buttons handled separately in order to enable calculations using the answer of prev calculation
        if(text == "÷" || text == "x" || text == "%" || text == "-" || text == "+"){
            var bigTxt = mainBigText.text.toString()
            if (bigTxt.contains("=")) {
                bigTxt = bigTxt.substring(bigTxt.indexOf("=") + 1)
                if (bigTxt.isNotEmpty()) bigTxt = bigTxt.substring(0, bigTxt.length)
                mainBigText.setText(bigTxt)
                bigTxt = mainBigText.text.toString()
                mainBigText.setSelection(bigTxt.length)

            }
            val pos = mainBigText.selectionStart
            val newTxt = bigTxt.substring(0,mainBigText.selectionStart) + text + bigTxt.substring(mainBigText.selectionEnd,bigTxt.length)
            mainBigText.setText(newTxt)
            mainBigText.setSelection(pos+1)

        }

        ///Trig and log separately to be added alongside "("
        else if(text == "sin" || text == "cos" || text == "tan" || text == "log" || text == "ln"){
            var bigTxt = mainBigText.text.toString()
            if (bigTxt.contains("=")) {
                mainSmallText.setText("")
                mainBigText.setText("")
                bigTxt = mainBigText.text.toString()
            }
            val pos = mainBigText.selectionStart
            val newTxt = bigTxt.substring(0,mainBigText.selectionStart) + text + "(" + bigTxt.substring(mainBigText.selectionEnd,bigTxt.length)
            mainBigText.setText(newTxt)
            if(text != "ln") {
                mainBigText.setSelection(pos + 4)
            }
            else{
                mainBigText.setSelection(pos + 3)

            }
        }
        else {
            when (text) {
                "C" -> {
                    ///Clears calc
                    mainSmallText.setText("")
                    mainBigText.setText("")
                }
                "Del" -> {
                    //Deletes character
                    var bigTxt = mainBigText.text.toString()
                    if (bigTxt.contains("=")) {
                        //Clears if previous was answer
                        mainSmallText.setText("")
                        mainBigText.setText("")
                        bigTxt = mainBigText.text.toString()
                        mainBigText.setSelection(bigTxt.length)

                    } else {
                        //If not handled by "handleByWhole()", a character is removed based on location of cursor
                        //Else nothing is done since "handleByWhole" already deleted the character
                        if(!handleByWhole()) {
                            if (bigTxt.substring(0, mainBigText.selectionStart).isNotEmpty()) bigTxt = bigTxt.substring(0, mainBigText.selectionStart - 1) + bigTxt.substring(mainBigText.selectionEnd, bigTxt.length)
                            val pos = mainBigText.selectionStart
                            mainBigText.setText(bigTxt)
                            bigTxt = mainBigText.text.toString()
                            if (pos - 1 >= 0) {
                                mainBigText.setSelection(pos - 1)
                            }
                        }

                    }

                }
                "√x" ->{
                    var bigTxt = mainBigText.text.toString()
                    if (bigTxt.contains("=")) {
                        mainSmallText.setText("")
                        mainBigText.setText("")
                        bigTxt = mainBigText.text.toString()
                    }
                    val pos = mainBigText.selectionStart
                    val newTxt = bigTxt.substring(0,mainBigText.selectionStart) + "√" + bigTxt.substring(mainBigText.selectionEnd,bigTxt.length)
                    mainBigText.setText(newTxt)
                    mainBigText.setSelection(pos+1)

                }
                "!X" ->{
                    var bigTxt = mainBigText.text.toString()
                    if (bigTxt.contains("=")) {
                        mainSmallText.setText("")
                        mainBigText.setText("")
                        bigTxt = mainBigText.text.toString()
                    }
                    val pos = mainBigText.selectionStart
                    val newTxt = bigTxt.substring(0,mainBigText.selectionStart) + "!" + bigTxt.substring(mainBigText.selectionEnd,bigTxt.length)
                    mainBigText.setText(newTxt)
                    mainBigText.setSelection(pos+1)

                }
                "1/x" ->{
                    var bigTxt = mainBigText.text.toString()
                    if (bigTxt.contains("=")) {
                        mainSmallText.setText("")
                        mainBigText.setText("")
                        bigTxt = mainBigText.text.toString()
                    }
                    val pos = mainBigText.selectionStart
                    val newTxt = bigTxt.substring(0,mainBigText.selectionStart) + "(1/)" + bigTxt.substring(mainBigText.selectionEnd,bigTxt.length)
                    mainBigText.setText(newTxt)
                    mainBigText.setSelection(pos+3)
                }
                "xⁿ" ->{
                    var bigTxt = mainBigText.text.toString()
                    if (bigTxt.contains("=")) {
                        mainSmallText.setText("")
                        mainBigText.setText("")
                        bigTxt = mainBigText.text.toString()
                    }
                    val pos = mainBigText.selectionStart
                    val newTxt = bigTxt.substring(0,mainBigText.selectionStart) + "()^()" + bigTxt.substring(mainBigText.selectionEnd,bigTxt.length)
                    mainBigText.setText(newTxt)
                    mainBigText.setSelection(pos+1)
                }
                "=" -> {
                    ///Calculates Expression and moves eqn to top
                    mainSmallText.text = mainBigText.text
                    val answer = evalExpression(mainBigText.text.toString())
                    mainBigText.setText("= $answer")
                    mainBigText.setSelection(mainBigText.text.toString().length)

                }
                "2nd" -> {

                }
                "deg" ->{

                }
                "rad" ->{

                }
                "App Info" ->{
                    val anim = ValueAnimator.ofFloat(1.0f,0.0f)
                    anim.addUpdateListener {
                        tv.alpha = it.animatedValue as Float
                    }
                    anim.duration = 100
                    anim.start()
                    Handler().postDelayed({
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, appInfo, "appInfoTitle")
                        val intent = Intent(this@MainActivity,AppInfoActivity::class.java)
                        startActivity(intent,options.toBundle())
                    },100)

                }
                else -> {
                    //All other buttons to be added to clicked
                    var bigTxt = mainBigText.text.toString()
                    if (bigTxt.contains("=")) {
                        mainSmallText.setText("")
                        mainBigText.setText("")
                        bigTxt = mainBigText.text.toString()
                    }
                    val pos = mainBigText.selectionStart
                    val newTxt = bigTxt.substring(0,mainBigText.selectionStart) + text + bigTxt.substring(mainBigText.selectionEnd,bigTxt.length)
                    mainBigText.setText(newTxt)
                    mainBigText.setSelection(pos+1)
                }
            }
        }

    }

    ///Evaluated expression as string and return value or syntax error if exists
    private fun evalExpression(exp: String): String{
        var expression = exp.replace("÷","/")
        expression = expression.replace("x","*")
        expression = expression.replace("π","pi")
        expression = addBrackets("√",expression)
        expression = addBrackets("!",expression)
        Log.e("Evaluating: ",expression)



        val evaluator = CustomDoubleEvaluator()
        try {
            val answer = evaluator.evaluate(expression)


            if (answer.toInt().toDouble() == answer) {
                return answer.toInt().toString()
            } else {
                val df = DecimalFormat("0.00")
                return df.format(answer)
            }
        }catch (e: IllegalArgumentException){
            return "Syntax Error"
        }

    }

    ///Adds brackets to surround numbers after a given symbol in order for math evaluator to understand the input (uses recursion)
    //Ex: !5 is converted to !(5)
    private fun addBrackets(symbol: String, exp: String): String{
        val index = exp.indexOf(symbol)
        if(index == -1 || exp.isEmpty()){
            return exp
        }
        else if(index+1 < exp.length){
            var numbersAfter = 0
            if(exp[index+1].toString() != "(") {
                for (char in exp.substring(index + 1, exp.length)) {
                    //Includes letters of trig and log in order to include them in eqn is possible
                    if (char.toString() == "." || char.toString() == "s" || char.toString() == "i" || char.toString() == "n" || char.toString() == "c" || char.toString() == "o" || char.toString() == "t" || char.toString() == "a" || char.toString() == "n" || char.toString() == "l" || char.toString() == "g" || char.toString() == "(" || char.toString() == ")") {
                        numbersAfter++
                        continue
                    }
                    try {
                        char.toString().toDouble()
                        numbersAfter++
                    } catch (e: NumberFormatException) {
                        break
                    }
                }
                var newString = exp.substring(0, index + 1) + "(" + exp.substring(index + 1, index + 1 + numbersAfter) + ")"
                return newString + addBrackets(symbol, exp.substring(index + 1 + numbersAfter, exp.length))
            }
            else{
                return exp.substring(0,index+1) + addBrackets(symbol, exp.substring(index + 1 + numbersAfter, exp.length))

            }
        }
        else{
            return ""
        }

    }

    //Initializes animation of expanding calculator (works alongside motionlayout)
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


    //Initialized drag action for top container (for expansion and retraction)
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
                        //If high speed, do animation automatically (disabled for the time being - feels unnatural)
                        if (speed > 2.5 && false) {
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



    //Coverts values given in dp to px - to simplify number usage between code and layout xml
    fun Int.toPx(): Int{
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        return Math.round(this * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }


}
