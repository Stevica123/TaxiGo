package com.example.taxigo

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.taxigo.utils.LocaleHelper
import com.example.taxigo.ui.profile.ProfileFragment


class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val contextWithLocale = LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase))
        super.attachBaseContext(contextWithLocale)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_host)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    openFragment(HomeFragment())
                    true
                }
                R.id.menu_order -> {
                    openFragment(OrderFragment())
                    true
                }
                R.id.menu_history -> {
                    openFragment(HistoryFragment())
                    true
                }
                R.id.menu_profile -> {
                    openFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun openFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
