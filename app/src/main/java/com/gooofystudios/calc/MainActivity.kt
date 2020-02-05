package com.gooofystudios.calc

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.animation.addListener
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {

    private var velocityTracker: VelocityTracker? = null
    private var isAnimating = false
    private var isCircleAnimating = false
    private lateinit var prefs: SharedPreferences
    private var listCount = 0
    private lateinit var layoutM: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initDragAction()
        initViews()
        initExpandAnim()



    }

    override fun onResume() {
        super.onResume()
        if(prefs.getBoolean("madePurchase",false)){
            equalButton.setTextColor(Color.parseColor("#FFD700"))
        }
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
    @SuppressLint("ClickableViewAccessibility")
    private fun initViews(){


        layoutM = LinearLayoutManager(this)

        window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )




        prefs = getSharedPreferences("history", Context.MODE_PRIVATE)
        if(prefs.getString("hisList",null) != null){
            updateHis()
        }


        if(prefs.getBoolean("madePurchase",false)){
            equalButton.setTextColor(Color.parseColor("#FFD700"))
        }

        if(prefs.getInt("numberOpened",0) > 0 && prefs.getInt("numberOpened",0) % 10 == 0 && !prefs.getBoolean("neverAsk",false)){
            AlertDialog.Builder(this)
                .setTitle("Are you enjoying the app?")
                .setPositiveButton("Yes"){ d,i ->
                    d.dismiss()
                    AlertDialog.Builder(this)
                        .setTitle("Do you want to support the app?")
                        .setPositiveButton("Yes"){ d,i ->
                            val intent = Intent(this@MainActivity,AppInfoActivity::class.java)
                            startActivity(intent)
                            d.dismiss()
                        }
                        .setNeutralButton("Not now"){ d,i ->
                            d.dismiss()
                        }
                        .setNegativeButton("Never ask again"){ d,i ->
                            prefs.edit().putBoolean("neverAsk",true).commit()
                            d.dismiss()
                        }
                        .show()
                }
                .setNegativeButton("No"){ d,i ->
                    d.dismiss()
                    prefs.edit().putBoolean("neverAsk",true).commit()
                    AlertDialog.Builder(this)
                        .setTitle("Sorry to hear that, we won't ask again")
                        .setMessage("We recommend you leave an honest review on the play store so that we can try and improve the app.")
                        .setNeutralButton("Ok"){ d,i ->
                            d.dismiss()
                        }
                        .show()
                }
                .show()
        }
        prefs.edit().putInt("numberOpened",prefs.getInt("numberOpened",0) + 1).commit()
        mathView.config(
            "MathJax.Hub.Config({\n"+
                    "  CommonHTML: { linebreaks: { automatic: true } },\n"+
                    "  \"HTML-CSS\": { linebreaks: { automatic: true } },\n"+
                    "         SVG: { linebreaks: { automatic: true } }\n"+
                    "});");



        clearTv.setOnClickListener {
            if(clearTv.alpha == 1.0f) {
                AlertDialog.Builder(this)
                    .setTitle("Are you sure you want to clear")
                    .setPositiveButton("Yes"){ d,i ->
                        prefs.edit().putString("hisList", ObjectSerializerHelper.objectToString(mutableListOf<String>() as Serializable)).commit()
                        updateHis()
                    }
                    .setNegativeButton("No"){ d,i ->
                        d.dismiss()
                    }
                    .show()
            }
        }
        equalButton.setOnClickListener {
            ///Calculates Expression and moves eqn to top
            if(!mainBigText.text.contains("=")) {
                val answer = evalExpression(mainBigText.text.toString())
                if(!answer.isNullOrEmpty()) {
                    mainSmallText.text = mainBigText.text
                    mainBigText.setText("= $answer")
                    mainBigText.setSelection(mainBigText.text.toString().length)
                }
            }
            if(!prefs.getBoolean("equalPressed",false)){
                Toast.makeText(this,"Tip: Holding the equal button will show proper mathematical format of equation",Toast.LENGTH_LONG).show()
                prefs.edit().putBoolean("equalPressed",true).commit()
            }
        }
        equalButton.setOnLongClickListener {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(50, 100))
            } else {
                v.vibrate(50)
            }
            if(!mainBigText.text.contains("=")) {
                val answer = evalExpression(mainBigText.text.toString())
                if(!answer.isNullOrEmpty()) {
                    mainSmallText.text = mainBigText.text
                    mainBigText.setText("= $answer")
                    mainBigText.setSelection(mainBigText.text.toString().length)
                }
            }
            val anim = ValueAnimator.ofFloat(0.0f,1.0f)
            anim.addUpdateListener {
                mathViewForground.alpha = it.animatedValue as Float
            }
            anim.duration = 200
            anim.start()

            true
        }

        equalButton.setOnTouchListener { v, event ->

            if(event.action == MotionEvent.ACTION_UP){
                val anim = ValueAnimator.ofFloat(mathViewForground.alpha,0.0f)
                anim.addUpdateListener {
                    mathViewForground.alpha = it.animatedValue as Float
                }
                anim.duration = 200
                anim.start()
            }
            false
        }

        radTv.setOnLongClickListener {
            Log.e("CLICKED RAD","LOL")
            val imageDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
            imageDialog.setTitle("Degrees are not yet supported\n")
            val showImage = ImageView(this@MainActivity)
            showImage.adjustViewBounds = true
            showImage.setImageResource(R.drawable.rad_gang_meme)
            imageDialog.setView(showImage)
            imageDialog.setNeutralButton("Ok"){ d, i ->
                d.dismiss()
            }
            imageDialog.setNegativeButton("I want degrees though"){ d, i ->
                Toast.makeText(this,"Degrees' support will be added in a future update",Toast.LENGTH_LONG).show()
            }
            imageDialog.show()
            true
        }
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
                },600)
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

        mainBigText.addTextChangedListener(object: TextWatcher{
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mainBigText.error = null
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
        })


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


    public fun onEqualLongClick(v:View){

    }
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
                    ///Clears calc with animations
                    val circleAnim = ValueAnimator.ofInt(0,1000.toPx())
                    circleAnim.addUpdateListener {
                        clearCircle.layoutParams.width = it.animatedValue as Int
                        clearCircle.layoutParams.height = it.animatedValue as Int
                        clearCircle.requestLayout()
                    }
                    circleAnim.duration = 300
                    if(!isCircleAnimating) {
                        circleAnim.start()
                        isCircleAnimating = true
                    }
                    circleAnim.addListener(onEnd= {
                        mainSmallText.setText("")
                        mainBigText.setText("")
                        val circleAnim2 = ValueAnimator.ofFloat(1.0f,00.0f)
                        circleAnim2.addUpdateListener {
                            clearCircle.alpha = it.animatedValue as Float
                        }
                        circleAnim2.duration = 300
                        circleAnim2.addListener(onEnd = {
                            clearCircle.layoutParams.width = 0
                            clearCircle.layoutParams.height = 0
                            clearCircle.requestLayout()

                            clearCircle.alpha = 1.0f
                            isCircleAnimating = false
                        })

                        circleAnim2.start()

                    })

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
        lateinit var hisList: MutableList<String>
        if(prefs.getString("hisList",null) == null){
            hisList = mutableListOf<String>()
        }
        else{
            hisList = ObjectSerializerHelper.stringToObject(prefs.getString("hisList",null)) as MutableList<String>
        }
        var expression = exp.replace("÷","/")
        expression = expression.replace("x","*")
        expression = expression.replace("π","pi")
        expression = addBrackets("√",expression)
        expression = addBrackets("!",expression)
        expression = addNeccMultiply(expression)



        //Used for showing formatted math expression
        var mathExpression = exp
        mathExpression = mathExpression.replace("x","*")
        mathExpression = addBrackets("√",mathExpression)
        mathExpression = mathExpression.replace("√","\\sqrt")
        mathExpression = mathExpression.replace("÷","/")
        mathExpression = convertToFracFormat(mathExpression)
        mathExpression = mathExpression.replace("*","·")
        mathExpression = mathExpression.replace("/","÷")
        mathExpression = replaceBracketWithCurly("√",mathExpression)
        mathExpression = replaceBracketWithCurly("^",mathExpression)





        val evaluator = CustomDoubleEvaluator()
        try {
            val answer = evaluator.evaluate(expression)


            if (answer.toInt().toDouble() == answer) {
                mathView.text = "$$$mathExpression = ${answer.toInt()}$$"
                val item = HistoryItem(exp,answer.toInt().toString())
                val itemStr = Gson().toJson(item)
                hisList.add(itemStr)
                prefs.edit().putString("hisList",ObjectSerializerHelper.objectToString(hisList as Serializable)).commit()
                updateHis()
                return answer.toInt().toString()
            } else {
                if(answer.toString().contains(".")){
                    if(answer.toString().length > 12){
                        val df = DecimalFormat("0.0000000")
                        mathView.text = "$$$mathExpression = ${df.format(answer)}$$"
                        val item = HistoryItem(exp,df.format(answer))
                        val itemStr = Gson().toJson(item)
                        hisList.add(itemStr)
                        prefs.edit().putString("hisList",ObjectSerializerHelper.objectToString(hisList as Serializable)).commit()
                        updateHis()
                        return df.format(answer)

                    }
                }
                mathView.text = "$$$mathExpression = ${answer}$$"
                val item = HistoryItem(exp,answer.toString())
                val itemStr = Gson().toJson(item)
                hisList.add(itemStr)
                prefs.edit().putString("hisList",ObjectSerializerHelper.objectToString(hisList as Serializable)).commit()
                updateHis()
                return answer.toString()
            }
        }catch (e: IllegalArgumentException){
            mainBigText.error =  "Syntax Error"


            return ""
        }

    }

    private fun updateHis(){
        val list = ObjectSerializerHelper.stringToObject(prefs.getString("hisList",null)) as MutableList<String>
        val hisList = mutableListOf<HistoryItem>()
        listCount = hisList.size-1
        for(str in list){
            hisList.add(Gson().fromJson(str,HistoryItem::class.java))
        }

        val adapter = HistoryAdapter(hisList.reversed().toMutableList(),this)
        layoutM = LinearLayoutManager(this)
        layoutM.reverseLayout = true
        layoutM.stackFromEnd = true
        historyRecycler.adapter = adapter

        historyRecycler.layoutManager = layoutM
        layoutM.scrollToPosition(0)





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

    private fun addCurly(symbol: String, exp: String): String{
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
                var newString = exp.substring(0, index + 1) + "{" + exp.substring(index + 1, index + 1 + numbersAfter) + "}"
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

    private fun replaceBracketWithCurly(symbol: String, exp: String):String{
        val index = exp.indexOf(symbol)
        if(index == -1 || exp.isEmpty()){
            return exp
        }
        else if(index+1 < exp.length){
            if(exp[index+1].toString() == "(") {
                val sub = exp.substring(index+1)
                val nextBracket = sub.indexOf(")")
                return exp.substring(0, index+1) + "{" + sub.substring(1,nextBracket) + "}" + replaceBracketWithCurly(symbol,sub.substring(nextBracket+1) )
            }
            else{
                return exp.substring(0,index+1) + replaceBracketWithCurly(symbol,exp.substring(index+1))

            }
        }
        else {
            return ""
        }
    }


    private fun convertToFracFormat(exp: String): String{
        val indicesToAdd = mutableListOf<Pair<Pair<Int,Int>,Pair<String,String>>>()
        var prev = -1
        for(i in exp.indices){
            val c = exp[i]
            if(c.toString() == "/"){
                var before = ""
                var after = ""
                var onlyExitIfBrack = false
                var onlyExitIfBrackForward = false

                if(i-1 >= 0 && exp[i-1].toString() == ")"){
                    onlyExitIfBrack = true
                }
                if(i+1 < exp.length && exp[i+1].toString() == "("){
                    onlyExitIfBrackForward = true
                }
                var inOtherBrack = 0
                for(y in i-1 downTo 0){
                    if(onlyExitIfBrack){
                        if(exp[y].toString() == "("){
                            if(inOtherBrack == 0) {
                                break
                            }
                            else{
                                inOtherBrack--
                            }
                        }
                        else if(exp[y].toString() == ")"){
                            inOtherBrack++
                        }
                    }
                    else {
                        if (exp[y].toString() == "+" || exp[y].toString() == "-" || exp[y].toString() == "*" || exp[y].toString() == "/" || exp[y].toString() == "%" || exp[y].toString() == "("){
                            break
                        }
                    }
                    before += exp[y].toString()
                }
                before = before.reversed()

                var inOtherBrackForward = 0

                for(y in i+1 until exp.length){
                    if(onlyExitIfBrackForward){
                        if(exp[y].toString() == ")"){
                            if(inOtherBrackForward == 0) {
                                break
                            }
                            else{
                                inOtherBrackForward--
                            }
                        }
                        else if(exp[y].toString() == "("){
                            inOtherBrackForward++
                        }
                    }
                    else {
                        if (exp[y].toString() == "+" || exp[y].toString() == "-" || exp[y].toString() == "*" || exp[y].toString() == "/" || exp[y].toString() == "%" || exp[y].toString() == ")"){
                            break
                        }
                    }
                    after += exp[y].toString()


                }


                if(prev!= -1){

                    if(prev != i-before.length+1){
                        indicesToAdd.add(Pair(Pair(i - before.length, i + after.length + 1), Pair(before, after)))
                        prev = i + after.length + 1

                    }
                }
                else {
                    indicesToAdd.add(Pair(Pair(i - before.length, i + after.length + 1), Pair(before, after)))
                    prev = i + after.length + 1
                }




            }
        }

        val builder = StringBuilder(exp)
        var currentOffset = 0
        for(pair in indicesToAdd){
            val indices = pair.first
            val text = pair.second
            builder.delete(indices.first+currentOffset,indices.second+currentOffset)
            builder.insert(indices.first + currentOffset,"\\frac{${text.first}}{${text.second}}")
            currentOffset += 8
        }

        return builder.toString()
    }




    //Adds neccessary times symbol in certain parts of equation for evaluator to understand
    //Ex 2π is converted to 2 * π
    private fun addNeccMultiply(exp: String): String{
        var indicesToAdd = mutableListOf<Int>()
        for(i in exp.indices){
            val char = exp[i]
            //All conditions in which a time should be added
            if(char.toString() == "s" || char.toString() == "c" || char.toString() == "t" || char.toString() == "e" || char.toString() == "l" || (char.toString() == "(" && (i-1 >= 0) && isNumber(exp[i-1].toString()))  || char.toString() == "√" || char.toString() == "p"){
                //Only add if there is something before it
                if(i - 1 >= 0) {
                    val c = exp[i-1]
                    if (c.toString() != "+" && c.toString() != "%" && c.toString() != "/" && c.toString() != "*" && c.toString() != "-") {
                        if((i-1 >= 0) && (isNumber(exp[i-1].toString()) || exp[i-1].toString() == ")"))
                        indicesToAdd.add(i)
                    }
                }
            }
        }
        val builder = StringBuilder(exp)
        var currentOffset = 0
        for(i in indicesToAdd){
            builder.insert(i + currentOffset,"*")
            currentOffset++
        }

        return builder.toString()
    }


    //Checks if string is a number
    private fun isNumber(s: String): Boolean{
        try{
            s.toDouble()
        }catch (e: java.lang.NumberFormatException){
            return false
        }

        return true
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

                    hidden1.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,0.0f)
                    hidden2.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,0.0f)
                    hidden3.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,0.0f)
                    hidden4.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,0.0f)
                    hidden5.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,0.0f)


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
                        hidden1.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,it.animatedValue as Float)
                        hidden2.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,it.animatedValue as Float)
                        hidden3.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,it.animatedValue as Float)
                        hidden4.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,it.animatedValue as Float)
                        hidden5.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,it.animatedValue as Float)


                    }

                    animator.duration = 500
                    animator.start()


                    val animator2 = ValueAnimator.ofInt(35.toPx(),20.toPx())
                    animator2.addUpdateListener {
                        switchButton.layoutParams.width = it.animatedValue as Int
                        switchButton.requestLayout()
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

                    val animator2 = ValueAnimator.ofInt(20.toPx(),35.toPx())
                    animator2.addUpdateListener {
                        switchButton.layoutParams.width = it.animatedValue as Int
                        switchButton.requestLayout()
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
    @SuppressLint("ClickableViewAccessibility")
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
                    historyRecycler.alpha = 0.0f
                    divderRc.alpha = 0.0f
                    clearTv.alpha = 0.0f

                }
                else{
                    if(historyRecycler.alpha == 0.0f) {
                        layoutM.scrollToPosition(0)
                        historyRecycler.scrollToPosition(0)
                        val anim = ValueAnimator.ofFloat(0.0f, 1.0f)
                        anim.addUpdateListener {
                            historyRecycler.alpha = it.animatedValue as Float
                            clearTv.alpha = it.animatedFraction as Float
                        }
                        anim.duration = 300
                        anim.start()

                        val anim2 = ValueAnimator.ofFloat(0.0f, 0.5f)
                        anim2.addUpdateListener {
                            divderRc.alpha = it.animatedValue as Float
                        }
                        anim2.duration = 300
                        anim2.start()
                    }
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
