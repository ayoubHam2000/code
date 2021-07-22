package com.example.chefapp.Structers

import com.example.chefapp.Utilities.ACTUAL_LANGUAGE

class TheCommand(
    private val productId : String,
    private val categoryId : String,
    private val productName : ArrayList<String>,
    val productPrice : Double,
    val productNumber : Int,
    val properties : ArrayList<Properties>
){
    fun getName() : String{
        return if(ACTUAL_LANGUAGE){
            productName[0]
        }else{
            if(productName[1] == ""){
                productName[0]
            }else{
                productName[1]
            }
        }
    }
}