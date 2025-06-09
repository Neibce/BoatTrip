package com.boattrip.boattrip

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun goToMain(view: View) {
        if (this::class.java.simpleName != "MainActivity") {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
    fun goBack(view: View) {
        if (!isTaskRoot) {
            finish()
        } else {
            showExitDialog()
        }
    }
    private fun showExitDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("종료")
            .setMessage("앱을 종료하시겠습니까?")
            .setPositiveButton("종료",
                DialogInterface.OnClickListener { dialog, id ->
                })
            .setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, id ->
                })
        builder.show()
    }
//    private fun showExitDialog() {
//        AlertDialog.Builder(this)
//            .setTitle("앱 종료")
//            .setMessage("앱을 종료하시겠습니까?")
//            .setPositiveButton("예") { _: DialogInterface, _: Int ->
//                finishAffinity()
//            }
//            .setNegativeButton("아니오") { dialog: DialogInterface, _: Int ->
//                dialog.dismiss()
//            }
//            .show()
//    }
}