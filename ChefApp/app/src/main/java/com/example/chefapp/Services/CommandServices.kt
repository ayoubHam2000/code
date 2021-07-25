package com.example.chefapp.Services

import android.content.Context
import android.util.Log
import com.android.volley.Request.Method.*
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.chefapp.Structers.Details
import com.example.chefapp.Structers.Properties
import com.example.chefapp.Structers.TheCommand
import com.example.chefapp.Structers.UserCommandInfo
import com.example.chefapp.Utilities.COMMANDS_URL
import com.example.chefapp.Utilities.RESTAURANT_ID
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object CommandServices {

    private val usersCommands = ArrayList<UserCommandInfo>()
    val usersCommandsDisplay = ArrayList<UserCommandInfo>()
    val theCommandProduct = ArrayList<TheCommand>()
    var addedTime = 0

    //is just for the request and adding the usersCommands look the other function
    fun getAllCommands(context : Context, complete : (Boolean) -> Unit){

        val url = "$COMMANDS_URL$RESTAURANT_ID"

        val request = JsonArrayRequest(GET, url, null, Response.Listener {response ->

            try{
                //usersCommands.clear()
                for(x in 0 until response.length()){
                    val jsonCommandInfo = response.getJSONObject(x)
                    val id = jsonCommandInfo.getString("_id")
                    if(!isCommandAlreadyExist(id)){
                        val newCommandInfo = getCommandInfo( jsonCommandInfo )
                        usersCommands.add(newCommandInfo)
                    }
                }
                //addedTime = 0
                getUserDisplayCommand(false)
                complete(true)
            }catch (e : JSONException){
                complete(false)
                Log.d("JSON ERROR", "failed t download message : $e")
            }
        }, Response.ErrorListener { error ->
            complete(false)
            Log.d("Failed", "failed to download messages $error")
        })
        Volley.newRequestQueue(context).add(request)

    }

    private fun isCommandAlreadyExist(id : String) : Boolean{
        for(item in usersCommands){
            if(item.id == id){
                return true
            }
        }
        return false
    }

    fun updateCommandProduct(position : Int){
        theCommandProduct.clear()
        theCommandProduct.addAll( usersCommandsDisplay[position].commands )
    }

    fun getUserDisplayCommand(donne : Boolean){
        usersCommandsDisplay.clear()
        for(item in usersCommands){
            if(item.isComplete == donne){
                usersCommandsDisplay.add(item)
            }
        }
        //usersCommandsDisplay.sortBy { it.getTime() }
        usersCommandsDisplay.sortedWith(compareBy{it.getTime()}).reversed()
    }

    fun donneCountOf(donne : Boolean) : Int{
        var i = 0
        for(item in usersCommands){
            if(item.isComplete == donne){
                i++
            }
        }
        return i
    }

    fun patchCommandInfo(context : Context, id : String,complete: (Boolean) -> Unit){
        val url = "$COMMANDS_URL$id"
        val commandInfoJSON = JSONObject()
        commandInfoJSON.put("isComplete", true)
        val requestBody = commandInfoJSON.toString()

        val request = object : JsonObjectRequest(PATCH, url, null, Response.Listener {
            Log.d("SUCCESS", "success post commandInfo")
            complete(true)
        },Response.ErrorListener {error->
            Log.d("FAILED", "failed to post commandInfo $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        Volley.newRequestQueue(context).add(request)
    }

    fun deleteCommand(context : Context, id : String, complete : (Boolean) -> Unit){
        val index = searchWithId(id)
        val url = "$COMMANDS_URL$RESTAURANT_ID/$id"

        val request = JsonObjectRequest(DELETE, url, null, Response.Listener {
            Log.d("success", "success delete command")
            if(index != -1){
                usersCommands.removeAt(index)
            }else{
                println("error index = -1")
            }
            complete(true)
        },Response.ErrorListener { error ->
            Log.d("failed", "failed delete command $error")
            complete(false)
        })
        Volley.newRequestQueue(context).add(request)
    }

    private fun searchWithId(id : String) : Int{
        for((x, item) in usersCommands.withIndex()){
            if(item.id == id){
                return x
            }
        }
        return -1
    }

    fun searchActive() : Int{
        for(x in 0 until usersCommandsDisplay.size){
            val item = usersCommandsDisplay[x]
            if(item.selected){
                return x
            }
        }
        return -1
    }

    fun deSelected(){
        for(item in usersCommandsDisplay){
            item.selected = false
        }
    }

    fun getNextColor() : Int{
        /*var max = 0
        var nb = 0
        for(item in usersCommands){
            if(max < item.color){
                max = item.color
            }
        }
        for(item in usersCommands){
            if(max == item.color){
                nb++
            }
        }*/
        return usersCommands.size
    }

    private fun getCommandInfo(jsonCommandInfo: JSONObject) : UserCommandInfo{
        //println("start getCommandInfo ... ")

        val id = jsonCommandInfo.getString("_id")
        val time = jsonCommandInfo.getLong("time")
        val table = jsonCommandInfo.getInt("table")
        val isComplete = jsonCommandInfo.getBoolean("isComplete")
        val commands = getCommands( jsonCommandInfo.getJSONArray("theCommands") )

        return UserCommandInfo(id, time, table, commands, selected = false, isComplete = isComplete, color = -1)
    }

    private fun getCommands(jsonCommands : JSONArray) : ArrayList<TheCommand>{
        //println("start getCommands ... ")
        val theCommands = ArrayList<TheCommand>()

        for(x in 0 until jsonCommands.length() ){
            val theJsonCommand = jsonCommands.getJSONObject(x)

            val productId = theJsonCommand.getString("productId")
            val categoryId = theJsonCommand.getString("category")
            val productName = getName( theJsonCommand.getJSONArray("productName") )
            val productPrice = theJsonCommand.getDouble("productPrice")
            val productCount = theJsonCommand.getInt("productCount")
            val properties = getProductProperties( theJsonCommand.getJSONArray("properties") )

            val newCommand = TheCommand(
                productId,
                categoryId,
                productName,
                productPrice,
                productCount,
                properties
            )
            theCommands.add(newCommand)
        }

        return theCommands
    }

    private fun getProductProperties(jsonProperties : JSONArray) : ArrayList<Properties>{
        //println("start getProductProperties ... ")
        val properties = ArrayList<Properties>()

        for(x in 0 until jsonProperties.length()){
            val jsonProperty = jsonProperties.getJSONObject(x)

            val propertiesName = getName(jsonProperty.getJSONArray("propertyName"))
            val details = makeDetails( jsonProperty.getJSONArray("details") )

            val newProperty = Properties(propertiesName, details)
            properties.add(newProperty)
        }

        return properties
    }

    private fun makeDetails(jsonDetail : JSONArray) : ArrayList<Details>{
        //println("start makeDetails ... ")
        val details = ArrayList<Details>()

        for(x in 0 until jsonDetail.length()){
            val jsonDetail = jsonDetail.getJSONArray(x)

            val name = getName(jsonDetail)

            val newDetail = Details(name)
            details.add(newDetail)
        }

        return details
    }

    private fun getName(jsonName : JSONArray) : ArrayList<String>{
        val name = ArrayList<String>()

        for(x in 0 until jsonName.length()){
            name.add( jsonName.getString(x) )
        }

        return name
    }


}