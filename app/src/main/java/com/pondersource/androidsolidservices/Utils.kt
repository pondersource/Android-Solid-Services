package com.pondersource.androidsolidservices

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


fun AppCompatActivity.showMessage(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}