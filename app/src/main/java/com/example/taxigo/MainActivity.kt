package com.example.taxigo

import android.content.Context
import android.os.Bundle
import android.widget.TextView
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
                    updateHeaderTitle(R.string.menu_home)
                    true
                }
                R.id.menu_order -> {
                    openFragment(OrderFragment())
                    updateHeaderTitle(R.string.menu_order)
                    true
                }
                R.id.menu_history -> {
                    openFragment(HistoryFragment())
                    updateHeaderTitle(R.string.menu_history)
                    true
                }
                R.id.menu_profile -> {
                    openFragment(ProfileFragment())
                    updateHeaderTitle(R.string.menu_profile)
                    true
                }
                else -> false
            }
        }


        updateHeaderTitle(R.string.menu_home)
    }

    private fun openFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateHeaderTitle(titleResId: Int) {
        val headerTitle = findViewById<TextView>(R.id.headerTitle)
        headerTitle.text = getString(titleResId)
    }
}
