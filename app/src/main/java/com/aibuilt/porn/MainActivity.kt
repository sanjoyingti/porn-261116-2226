package com.aibuilt.porn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aibuilt.porn.ui.AppRoot
import com.aibuilt.porn.ui.theme.PornTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as PornApp).container
        setContent {
            PornTheme {
                AppRoot(container)
            }
        }
    }
}
