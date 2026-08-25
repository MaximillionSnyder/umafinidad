package com.maximillionsnyder.umafinidad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.maximillionsnyder.umafinidad.ui.theme.UmaAfinidadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UmaAfinidadTheme {
                App()
            }
        }
    }
}

@Composable
private fun App() {
    Scaffold { padding ->
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}
