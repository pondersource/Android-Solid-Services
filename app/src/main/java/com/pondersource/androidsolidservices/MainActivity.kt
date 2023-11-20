package com.pondersource.androidsolidservices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainPageContent()
        }
    }

    @Composable
    fun  MainPageContent() {
        Text("Hello Solid")
    }

    @Preview
    @Composable
    fun PreviewMainPageContent() {
        MainPageContent()
    }
}