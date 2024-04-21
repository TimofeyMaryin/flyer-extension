package custom.lib.droid.appsflyerextension

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import custom.lib.droid.appsflyerextension.ui.theme.AppsflyerExtensionTheme
import custom.lib.droid.flyer_extension.FlyerExtension
import custom.lib.droid.flyer_extension.isNDaysPassed
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    var text by mutableStateOf("")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("TAG", "onCreate: ${isNDaysPassed(startDate = LocalDate.of(2024, 4, 21), 3)}", )
        Log.e("TAG", "onCreate: ${isNDaysPassed(startDate = LocalDate.of(2024, 4, 15), 4)}", )
        FlyerExtension.setExtension(
            value = "i76DHWyqpCeUdgbXaj9XmL",
            context = applicationContext,
            onError = {
                Log.e("TAG", "onCreate: onError call", )
                text = "onError"
            },
            onSuccess = {
                Log.e("TAG", "onCreate: onSuccess call. ${it.content}", )
                text = "onSuccess"
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

                    Text(text = "Test APPSFLYER NON-ORGANIC TEST: $text")
                }
            }
        }
    }
}
