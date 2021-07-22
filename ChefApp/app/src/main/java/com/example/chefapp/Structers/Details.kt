package com.example.chefapp.Structers

import com.example.chefapp.Utilities.ACTUAL_LANGUAGE

class Details(
    private val detailName : ArrayList<String>
) {
    fun getName() : String{
        return if(ACTUAL_LANGUAGE){
            detailName[0]
        }else{
            if(detailName[1] == ""){
                detailName[0]
            }else{
                detailName[1]
            }
        }
    }
}