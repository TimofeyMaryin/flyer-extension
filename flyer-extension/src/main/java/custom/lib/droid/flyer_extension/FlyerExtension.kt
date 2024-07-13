package custom.lib.droid.flyer_extension

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import custom.lib.droid.decrypt_helper.EncryptionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

        val job = Job()

        // Запускаем таймер в корутине
        CoroutineScope(Dispatchers.Main + job).launch {
            delay(7_000) // здесь укажите необходимое время задержки

            if (res == null) {
                Log.e("TAG", "setExtension: time out")
                onError()
            }
        }



        val conversionListener = object : AppsFlyerConversionListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>) {
                Log.e("TAG", "onConversionDataSuccess", )
                res = FlyerStatus.SUCCESS

                MEDIA_SOURCE = conversionData["media_source"].toString()
                AF_SITEID = conversionData["af_siteid"].toString()
                ADSET = conversionData["adset"].toString()
                CAMPAIGN = conversionData["campaign"].toString()
                ADGROUP = conversionData["adgroup"].toString()
                AF_AD = conversionData["af_ad"].toString()

                if (CheckPush.checkPush(context) && isNDaysPassed(startDate, 2)) {
                    Log.e("TAG", "onConversionDataSuccess: IS NON ORGANI. CONDITION SUCCESS.", )
                    onSuccess(
                        FlyerModel(
                            status = FlyerStatus.SUCCESS,
                            content = "?media_source=$MEDIA_SOURCE" + "&af_siteid=$AF_SITEID" + "&campaign=${CAMPAIGN}" + "&adgroup=$ADGROUP" + "&adset=$ADSET" + "&af_ad=$AF_AD"
                        )
                    )
                } else{
                    Log.e("TAG", "onConversionDataSuccess: IS NON ORGANIC, CONDITION FAILED.", )
                    onError()
                }

                job.cancel()
            }

            override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                Log.e("TAG", "onAppOpenAttribution: onAppOpenAttribution", )
                res = FlyerStatus.ERROR
                onError()
                job.cancel()
            }

            override fun onAttributionFailure(errorMessage: String?) {
                Log.e("TAG", "onAttributionFailure: onAttributionFailure", )
                res = FlyerStatus.ERROR
                onError()
                job.cancel()
            }

            override fun onConversionDataFail(errorMessage: String?) {
                Log.e("TAG", "onConversionDataFail: onConversionDataFail", )
                res = FlyerStatus.ERROR
                onError()
                job.cancel()
            }
        }

        AppsFlyerLib.getInstance().init(value, conversionListener, context)
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

object CheckPush {

    private fun detectDeviceLanguage(context: Context): Boolean {
        val currentLocale: Locale = context.resources.configuration.locales[0]
        return currentLocale.language == "ru"
    }

    fun checkPush(context: Context): Boolean {
        return detectDeviceLanguage(context)
    }

}


@RequiresApi(Build.VERSION_CODES.O)
fun isNDaysPassed(startDate: LocalDate, n: Int): Boolean {
    val currentDate = LocalDate.now()
    val endDate = startDate.plusDays(n.toLong())
    return currentDate.isAfter(endDate) || currentDate.isEqual(endDate)
}

