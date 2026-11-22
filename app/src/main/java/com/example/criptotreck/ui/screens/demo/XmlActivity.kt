package com.example.criptotreck.ui.screens.demo

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.criptotreck.R

/**
 * Approach 3: Traditional XML Activity
 */
class XmlActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xml)

        findViewById<Button>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }
}
