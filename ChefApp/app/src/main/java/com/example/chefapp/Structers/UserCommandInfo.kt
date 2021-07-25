package com.example.chefapp.Structers

import com.example.chefapp.Services.CommandServices
import com.example.chefapp.Utilities.ACTUAL_LANGUAGE

class UserCommandInfo(
    val id : String,
    private val time : Long,
    val table : Int,
    val commands : ArrayList<TheCommand>,
    var selected : Boolean,
    var isComplete : Boolean,
    var color : Int
){
    fun getTime() : String{
        val newTime = (time  + CommandServices.addedTime) / 60
        val hours = newTime / 60
        val minutes = newTime % 60
        val sHours = if(hours in 0..9) "0$hours" else hours.toString()
        val sMin = if(minutes in 0..9) "0$minutes" else minutes.toString()


        if(hours > 0){
            return if(ACTUAL_LANGUAGE){
                "il y'a \n$sHours h : $sMin min"
            }else{
                "$sHours : $sMin ago"
            }
        }else{
            return if(newTime == 0L || newTime == 1L){
                if(ACTUAL_LANGUAGE){
                    "il y a \n$sMin minute"
                }else{
                    "$sMin minute ago"
                }
            }else{
                if(ACTUAL_LANGUAGE){
                    "il y a \n$sMin minutes"
                }else{
                    "$sMin minutes ago"
                }
            }
        }
    }

    fun getTotalPrice() : Double{
        var totalPrice = 0.0

        for(command in commands){
            totalPrice += command.productPrice * command.productNumber
        }

        return totalPrice
    }
}