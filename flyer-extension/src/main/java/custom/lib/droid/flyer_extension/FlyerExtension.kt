package custom.lib.droid.flyer_extension

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import custom.lib.droid.decrypt_helper.EncryptionHelper
import java.time.LocalDate
import java.util.Locale
import java.util.Timer
import java.util.TimerTask


object FlyerExtension {

    private var MEDIA_SOURCE: String? = null
    private var AF_SITEID: String? = null
    private var CAMPAIGN: String? = null
    private var ADGROUP: String? = null
    private var ADSET: String? = null
    private var AF_AD: String? = null

    fun setExtension(
        value: String,
        context: Context,
        startDate: LocalDate,
        onError: () -> Unit,
        onSuccess: (FlyerModel) -> Unit
    ) {
        var res: FlyerStatus? = null
        if (value.length != 22) {
            throw Throwable("Вы ввели не правильно ключ разработчика!")
        }

        customTimer {
            if (res == null) {
                onError()
            }
            return@customTimer
        }

        val conversionListener = object : AppsFlyerConversionListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>) {

                MEDIA_SOURCE = conversionData["media_source"].toString()
                AF_SITEID = conversionData["af_siteid"].toString()
                ADSET = conversionData["adset"].toString()
                CAMPAIGN = conversionData["campaign"].toString()
                ADGROUP = conversionData["adgroup"].toString()
                AF_AD = conversionData["af_ad"].toString()
                res = FlyerStatus.SUCCESS
                Log.e("TAG", "onConversionDataSuccess: ${conversionData["af_status"].toString().lowercase()}", )
                Log.e("TAG", "onConversionDataSuccess: ${conversionData["af_status"].toString().lowercase() == "organic"}", )
                if (CheckPush.checkPush(context) && isNDaysPassed(startDate, 2)) {
                    Log.e("TAG", "onConversionDataSuccess: IS NON ORGANI. CONDITION SUCCESS.", )
                    onSuccess(
                        FlyerModel(
                            status = FlyerStatus.SUCCESS,
                            content = "?media_source=$MEDIA_SOURCE" + "&af_siteid=$AF_SITEID" + "&campaign=${CAMPAIGN}" + "&adgroup=$ADGROUP" + "&adset=$ADSET" + "&af_ad=$AF_AD"
                        )
                    )
                    return
                } else{
                    Log.e("TAG", "onConversionDataSuccess: IS NON ORGANIC, CONDITION FAILED.", )
                    onError()
                    return
                }

            }

            override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                Log.e("TAG", "onAppOpenAttribution: onAppOpenAttribution", )
                res = FlyerStatus.ERROR
                onError()
                return
            }

            override fun onAttributionFailure(errorMessage: String?) {
                Log.e("TAG", "onAttributionFailure: onAttributionFailure", )
                res = FlyerStatus.ERROR
                onError()
                return

            }

            override fun onConversionDataFail(errorMessage: String?) {
                Log.e("TAG", "onConversionDataFail: onConversionDataFail", )
                res = FlyerStatus.ERROR
                onError()
                return
            }
        }

        AppsFlyerLib.getInstance().init(value, conversionListener, context)
        AppsFlyerLib.getInstance().start(context)
        AppsFlyerLib.getInstance().setDebugLog(true)
    }


    @Deprecated("this is appsflyer for DECODE DEV KEY")
    fun setExtensionDecode(
        value: String,
        context: Context,
        onError: () -> Unit,
        onSuccess: (FlyerModel) -> Unit
    ) {
        var res: FlyerStatus? = null
        customTimer {
            if (res == null) {
                Log.e("TAG", "setExtensionDecode: res == null. Time OUT", )
                onError()
            }
            return@customTimer
        }


        val conversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>) {

                // Log.e("TAG_APPS", "onConversionDataSuccess: success", )
                MEDIA_SOURCE = conversionData["media_source"].toString()
                AF_SITEID = conversionData["af_siteid"].toString()
                ADSET = conversionData["adset"].toString()
                CAMPAIGN = conversionData["campaign"].toString()
                ADGROUP = conversionData["adgroup"].toString()
                AF_AD = conversionData["af_ad"].toString()
                res = FlyerStatus.SUCCESS
                onSuccess(
                    FlyerModel(
                        status = FlyerStatus.SUCCESS,
                        content = "?media_source=$MEDIA_SOURCE" + "&af_siteid=$AF_SITEID" + "&campaign=${CAMPAIGN}" + "&adgroup=$ADGROUP" + "&adset=$ADSET" + "&af_ad=$AF_AD"
                    )
                )
            }

            override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                res = FlyerStatus.ERROR
                onError()
            }

            override fun onAttributionFailure(errorMessage: String?) {
                res = FlyerStatus.ERROR
                onError()
            }

            override fun onConversionDataFail(errorMessage: String?) {
                res = FlyerStatus.ERROR
                onError()
            }
        }

        AppsFlyerLib.getInstance().init(EncryptionHelper.decrypt(value), conversionListener, context)
        AppsFlyerLib.getInstance().start(context)
        AppsFlyerLib.getInstance().setDebugLog(true)
    }

    data class FlyerModel(
        var status: FlyerStatus? = null,
        var content: String? = null,
    )

    enum class FlyerStatus {
        SUCCESS, ERROR
    }

}


private fun customTimer(
    onTimeOut: () -> Unit
) {
    val timer = Timer()
    val delay: Long = 5000
    val task = object : TimerTask() {
        override fun run() {
            onTimeOut()
        }
    }

    timer.schedule(task, delay)
}


object CheckPush {

    private fun detectDeviceLanguage(context: Context): Boolean {
        val currentLocale: Locale = context.resources.configuration.locales[0]
        Log.e("TAG", "detectDeviceLanguage: ${currentLocale.language}", )
        return currentLocale.language == "ru"
    }

    fun checkPush(context: Context): Boolean {
        Log.e("TAG", "checkPush: ${detectDeviceLanguage(context)}", )
        return detectDeviceLanguage(context)
    }

}


@RequiresApi(Build.VERSION_CODES.O)
fun isNDaysPassed(startDate: LocalDate, n: Int): Boolean {
    val currentDate = LocalDate.now()
    val endDate = startDate.plusDays(n.toLong())
    return currentDate.isAfter(endDate) || currentDate.isEqual(endDate)
}

