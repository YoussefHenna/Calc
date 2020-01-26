package com.gooofystudios.calc

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_app_info.*
import kotlinx.coroutines.*


class AppInfoActivity: AppCompatActivity() {
    companion object{
        val job = Job()
        val scope = CoroutineScope(job)
    }
    lateinit private var billingClient: BillingClient
    private var isConnected = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_info)

        val anim = ValueAnimator.ofFloat(0.0f,1.0f)
        anim.addUpdateListener {
            infoTitle.alpha = it.animatedValue as Float
        }
        anim.duration = 100
        infoTitle.postDelayed({
            anim.start()

        },500)


        backButton.setOnClickListener {
            onBackPressed()
        }



        javalLib.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://javaluator.sourceforge.net/en/home/"))
            startActivity(browserIntent)
        }

        shimmerLib.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://facebook.github.io/shimmer-android/"))
            startActivity(browserIntent)
        }

        mathViewLib.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jianzhongli/MathView"))
            startActivity(browserIntent)
        }

        shadowLayoutLib.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/lijiankun24/ShadowLayout"))
            startActivity(browserIntent)
        }

        originalIdea.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://uimovement.com/design/calculator-daily-ui-004/"))
            startActivity(browserIntent)
        }

        switchIcon.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.flaticon.com/free-icon/square-measument_76749?term=measure%20square&page=5&position=25"))
            startActivity(browserIntent)
        }

        openSource.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/YoussefHenna/Calc"))
            startActivity(browserIntent)
        }

        privacyPolicy.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://calc.flycricket.io/privacy.html"))
            startActivity(browserIntent)
        }

        billingClient = BillingClient.newBuilder(this).setListener(object: PurchasesUpdatedListener{
            override fun onPurchasesUpdated(result: BillingResult?, purchases: MutableList<Purchase>?) {
                if (result?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        scope.launch(Dispatchers.IO){
                            handlePurchase(purchase)
                        }
                    }
                    AlertDialog.Builder(this@AppInfoActivity)
                        .setTitle("Purchase Successful!")
                        .setMessage("Thank you for your support. You will now receive a golden equal button.")
                        .show()
                } else if (result?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    Toast.makeText(this@AppInfoActivity,"Purchase was cancelled",Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@AppInfoActivity,"An error occurred during purchase",Toast.LENGTH_LONG).show()
                    Log.e("ERROR PURCHASE",result?.debugMessage)

                }

            }
        }).enablePendingPurchases().build()
        connectToBilling()




    }


    private fun connectToBilling(){
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isConnected = true
                    scope.launch {
                        queryPurchases()
                        querySkuDetails()
                    }
                }
            }
            override fun onBillingServiceDisconnected() {
                oneDollar.isEnabled = false
                fiveDollar.isEnabled = false
                tenDollarTv.isEnabled = false
                connectToBilling()
            }
        })

    }


    suspend fun queryPurchases(){
        val response = billingClient.queryPurchases("inapp")
        if(response.billingResult.responseCode == BillingClient.BillingResponseCode.OK){
            for(purchase in response.purchasesList){
                handlePurchase(purchase)
            }
        }
        else{
            scope.launch(Dispatchers.Main){
                Toast.makeText(this@AppInfoActivity,"Something went wrong while connecting to user purchases",Toast.LENGTH_LONG).show()
                Log.e("Purchases Failed", response.billingResult.debugMessage)

            }
        }

    }



    suspend fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add("purchase_one_dollar")
        skuList.add("purchase_five_dollar")
        skuList.add("purchase_ten_dollar")

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }

        if(skuDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK){
            for(item in skuDetailsResult.skuDetailsList!!){
                if(item.sku == "purchase_one_dollar"){
                    scope.launch(Dispatchers.Main) {
                        oneDollar.text = "${item.price} Purchase"
                        oneDollar.setOnClickListener{
                            val flowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(item)
                                .build()
                            val responseCode = billingClient.launchBillingFlow(this@AppInfoActivity, flowParams)
                        }
                        oneDollar.isEnabled = true
                    }

                }
                else if(item.sku == "purchase_five_dollar"){
                    scope.launch(Dispatchers.Main) {
                        fiveDollar.text = "${item.price} Purchase"
                        fiveDollar.setOnClickListener{
                            val flowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(item)
                                .build()
                            val responseCode = billingClient.launchBillingFlow(this@AppInfoActivity, flowParams)
                        }
                        fiveDollar.isEnabled = true
                    }


                }
                else if(item.sku == "purchase_ten_dollar"){
                    scope.launch(Dispatchers.Main) {
                        tenDollarTv.text = "${item.price} Purchase"
                        tenDollarTv.setOnClickListener{
                            val flowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(item)
                                .build()
                            val responseCode = billingClient.launchBillingFlow(this@AppInfoActivity, flowParams)
                        }
                        tenDollarTv.isEnabled = true
                    }

                }

            }
        }
        else{
            scope.launch(Dispatchers.Main){
                Toast.makeText(this@AppInfoActivity,"Something went wrong while connecting to billing",Toast.LENGTH_LONG).show()
                oneDollar.isEnabled = false
                fiveDollar.isEnabled = false
                tenDollarTv.isEnabled = false
                Log.e("Billing Failed",skuDetailsResult.billingResult.debugMessage)

            }
        }

    }

    suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            scope.launch(Dispatchers.Main) {
                val prefs = getSharedPreferences("history", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("madePurchase",true).commit()

                val set = prefs.getStringSet("purchaseTokens", mutableSetOf())
                set!!.add(purchase.purchaseToken)
                prefs.edit().putStringSet("purchaseTokens",set).commit()
            }


            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                val ackPurchaseResult = withContext(Dispatchers.IO) {
                    billingClient.consumePurchase(acknowledgePurchaseParams.build())
                }
            }
        }
        else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            AlertDialog.Builder(this)
                .setTitle("Pending Purchase")
                .setMessage("A purchase has been detected that is still in it's pending state, please follow instructions that you were given in order to complete the purchase")
                .setNeutralButton("Ok"){ d,_ ->
                    d.dismiss()

                }
                .show()

        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
    }
}