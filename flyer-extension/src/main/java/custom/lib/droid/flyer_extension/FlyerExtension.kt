package custom.lib.droid.flyer_extension

import android.content.Context
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import custom.lib.droid.decrypt_helper.EncryptionHelper
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
            override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>) {

                val isOrganic = conversionData["af_status"].toString().lowercase() == "organic"
                MEDIA_SOURCE = conversionData["media_source"].toString()
                AF_SITEID = conversionData["af_siteid"].toString()
                ADSET = conversionData["adset"].toString()
                CAMPAIGN = conversionData["campaign"].toString()
                ADGROUP = conversionData["adgroup"].toString()
                AF_AD = conversionData["af_ad"].toString()
                res = FlyerStatus.SUCCESS

                if (!isOrganic) {
                    if (CheckPush.checkPush(context)) {
                        onSuccess(
                            FlyerModel(
                                status = FlyerStatus.SUCCESS,
                                content = "?media_source=$MEDIA_SOURCE" + "&af_siteid=$AF_SITEID" + "&campaign=${CAMPAIGN}" + "&adgroup=$ADGROUP" + "&adset=$ADSET" + "&af_ad=$AF_AD"
                            )
                        )
                    }
                }

                onError()
                return
            }

            override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                res = FlyerStatus.ERROR
                onError()
                return
            }

            override fun onAttributionFailure(errorMessage: String?) {
                res = FlyerStatus.ERROR
                onError()
                return

            }

            override fun onConversionDataFail(errorMessage: String?) {
                res = FlyerStatus.ERROR
                onError()
                return
            }
        }

        AppsFlyerLib.getInstance().init(value, conversionListener, context)
        AppsFlyerLib.getInstance().start(context)
        AppsFlyerLib.getInstance().setDebugLog(true)
    }

    fun setExtensionDecode(
        value: String,
        context: Context,
        onError: () -> Unit,
        onSuccess: (FlyerModel) -> Unit
    ) {
        var res: FlyerStatus? = null
        customTimer {
            if (res == null) {
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
        return currentLocale.language == "ru"
    }

    fun checkPush(context: Context): Boolean {
        return detectDeviceLanguage(context)
    }

}


