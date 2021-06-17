package com.nextsoftware.outside

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }
    fun home(view : View){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}