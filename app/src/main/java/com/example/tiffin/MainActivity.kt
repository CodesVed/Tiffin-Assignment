package com.example.tiffin

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
import androidx.compose.ui.tooling.preview.Preview
import com.example.tiffin.ui.theme.TiffinTheme
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.tiffin.data.KitchenAssetLoader
import com.example.tiffin.data.KitchenDataException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TiffinData", "onCreate reached")
        // TEMPORARY (S1 only): proves the data layer works. Removed in S2.
        lifecycleScope.launch {
            try {
                val kitchens = KitchenAssetLoader(applicationContext).load()
                Log.d("TiffinData", "Loaded ${kitchens.size} kitchens: ${kitchens.map { it.name }}")
            } catch (e: KitchenDataException) {
                Log.e("TiffinData", "Could not load kitchens", e)
            }
        }
        enableEdgeToEdge()
        setContent {
            TiffinTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TiffinTheme {
        Greeting("Android")
    }
}