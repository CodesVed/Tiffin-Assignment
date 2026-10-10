package com.example.tiffin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tiffin.ui.TiffinNavHost
import com.example.tiffin.ui.theme.TiffinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TiffinTheme {
                TiffinNavHost()
            }
        }
    }
}