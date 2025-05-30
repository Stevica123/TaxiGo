package com.example.taxigo

import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.example.taxigo.utils.LocaleHelper

class WelcomeActivity : BaseActivity() {

    private lateinit var languageSpinner: Spinner
    private var savedLang: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val anonymousButton = findViewById<Button>(R.id.anonymousLoginButton)
        languageSpinner = findViewById(R.id.languageSpinner)


        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.language_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        savedLang = LocaleHelper.getLanguage(this)
        languageSpinner.setSelection(if (savedLang == "mk") 0 else 1)

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) {
                val selectedLangCode = if (position == 0) "mk" else "en"
                if (selectedLangCode != savedLang) {
                    LocaleHelper.saveLanguage(this@WelcomeActivity, selectedLangCode)
                    savedLang = selectedLangCode
                    recreate()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        anonymousButton.setOnClickListener {
            val intent = Intent(this, AnonymousActivity::class.java)
            startActivity(intent)
        }

        updateTexts()
    }

    private fun updateTexts() {
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val anonymousButton = findViewById<Button>(R.id.anonymousLoginButton)

        welcomeText.text = getString(R.string.welcome_text)
        loginButton.text = getString(R.string.login_button)
        registerButton.text = getString(R.string.register_button)
        anonymousButton.text = getString(R.string.anonymous_button)
    }
}
