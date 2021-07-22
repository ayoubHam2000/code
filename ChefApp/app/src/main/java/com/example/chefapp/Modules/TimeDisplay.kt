package com.example.chefapp.Modules

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.example.chefapp.Utilities.ACTUAL_LANGUAGE
import java.text.SimpleDateFormat
import java.util.*

class TimeDisplay {

    private var timeMonth : TextView? = null
    private var timeDay : TextView? = null
    private var timeHour : TextView? = null
    private var timeMinute : TextView? = null

    fun intDisplayTime(month : TextView, day : TextView,hour : TextView, minute : TextView){
        timeMonth = month
        timeDay = day
        timeHour = hour
        timeMinute = minute


        changeTime()
    }

    private fun changeTime(){

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                timeDisplay()
                mainHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun timeDisplay(){
        //Mon Feb 24 20:12:27 GMT+01:00 2020
        /*val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        this.sendBroadcast(it)*/
        val currentDate = Date()
        val sdfF = SimpleDateFormat("k-m-E-MMMM-d", Locale.FRENCH)
        val sdfE = SimpleDateFormat("k-m-E-MMMM-d", Locale.ENGLISH)
        val frenchTime = sdfF.format(currentDate)
        val englishTime = sdfE.format(currentDate)


        if(ACTUAL_LANGUAGE){
            val currentTime = frenchTime.split("-")
            setTime(currentTime)
        }else{
            val currentTime = englishTime.split("-")
            setTime(currentTime)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setTime(time : List<String>){
        //20-16-jeu.-February-28
        timeHour?.text = time[0]
        timeMinute?.text = time[1]
        timeDay?.text = time[2]
        timeMonth?.text = "${time[3]}  ${time[4]}"
    }

}