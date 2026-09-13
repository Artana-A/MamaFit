package com.mamafit.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SkriningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skrining)

        findViewById<View>(R.id.btnStartSkrining).setOnClickListener {
            val intent = Intent(this, SkriningQuestionActivity::class.java)
            intent.putExtra(SkriningQuestionActivity.EXTRA_INDEX, 0)
            intent.putExtra("EXTRA_TYPE", SkriningType.AWAL.name)
            startActivity(intent)
        }
    }
}