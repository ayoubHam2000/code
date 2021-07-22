package com.example.chefapp.Structers

import com.example.chefapp.Utilities.ACTUAL_LANGUAGE

class Properties(
    private val propertiesName : ArrayList<String>,
    val details : ArrayList<Details>
) {
    fun getName() : String{
        return if(ACTUAL_LANGUAGE){
            propertiesName[0]
        }else{
            if(propertiesName[1] == ""){
                propertiesName[0]
            }else{
                propertiesName[1]
            }
        }
    }
}