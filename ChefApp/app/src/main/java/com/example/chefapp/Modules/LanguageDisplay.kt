package com.example.chefapp.Modules

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import com.example.chefapp.R
import com.example.chefapp.Utilities.*

class LanguageDisplay(val context : Context, private val sharedPreferences : SharedPreferences, val change : (Boolean) -> Unit) {

     fun languageManagement(){
        val builder = AlertDialog.Builder(context)
        val builderView = LayoutInflater.from(context).inflate(R.layout.language_changer, null)
        builder.setView(builderView).setPositiveButton("ok", null)
        val dialog = builder.create()

        //set up builder show
        dialog.setOnShowListener {

            //resources
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val frenchLanguage = builderView.findViewById<RadioButton>(R.id.frenchLanguage)
            val englishLanguage = builderView.findViewById<RadioButton>(R.id.englishLanguage)

            //get info
            frenchLanguage.isChecked = ACTUAL_LANGUAGE
            englishLanguage.isChecked = !ACTUAL_LANGUAGE

            frenchLanguage.setOnClickListener { englishLanguage.isChecked = false }
            englishLanguage.setOnClickListener { frenchLanguage.isChecked = false }

            okButton.setOnClickListener {
                ACTUAL_LANGUAGE =  frenchLanguage.isChecked
                if(ACTUAL_LANGUAGE){
                    changeApplicationLanguage("fr")
                }else{
                    changeApplicationLanguage("en")
                }
                dialog.dismiss()
            }

        }
        dialog.show()
    }

    private fun changeApplicationLanguage(language:String){
        val sharedPreferencesEditor = sharedPreferences.edit()
        when (language) {
            ENGLISH -> sharedPreferencesEditor?.putString(SELECTED_LANGUAGE, ENGLISH)
            FRENCH -> sharedPreferencesEditor?.putString(SELECTED_LANGUAGE, FRENCH)
        }
        sharedPreferencesEditor.putBoolean(LANGUAGE_IS_SELECTED, true)
        sharedPreferencesEditor?.apply()

        change(true)
    }
}