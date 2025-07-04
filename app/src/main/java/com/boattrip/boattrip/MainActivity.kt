package com.boattrip.boattrip

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // SplashScreen 설치 (Android 12 이하 버전 호환)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 네비게이션 바 클릭 리스너 설정
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.navigation_view)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_main -> {
                    // 메인 프래그먼트로 복원 (기존 ImageListFragment)
                    replaceFragment(MainFragment())
                    true
                }
                R.id.item_gallery -> {
                    // PhotoGalleryFragment로 교체
                    replaceFragment(PhotoGalleryFragment())
                    true
                }
                R.id.item_archive -> {
                    // ArchiveFragment로 교체
                    replaceFragment(ArchiveFragment())
                    true
                }

                // 다른 네비게이션 항목들도 여기에 추가할 수 있습니다
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragContainer, fragment)
            .commit()
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
        builder
            .setMessage("앱을 종료하시겠습니까?")
            .setPositiveButton(
                "종료",
                DialogInterface.OnClickListener { dialog, id ->
                    finishAffinity()
                })
            .setNegativeButton(
                "취소",
                DialogInterface.OnClickListener { dialog, id ->
                })
        builder.show()
    }
}