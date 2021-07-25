package com.example.chefapp.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chefapp.R
import com.example.chefapp.Structers.Properties
import java.util.*
import kotlin.collections.ArrayList

class ProductPropertiesAdapter(val context : Context, val propertiesList : ArrayList<Properties>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.detail_command_properties, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return propertiesList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindView(position)
    }

    inner class ViewHolder(itemView : View?) : RecyclerView.ViewHolder(itemView!!){

        private val propertyName = itemView?.findViewById<TextView>(R.id.propertyName)
        private val propertyDetails = itemView?.findViewById<TextView>(R.id.propertyDetails)

        fun bindView(position : Int){
            propertyName?.text = propertiesList[position].getName().toUpperCase(Locale.FRENCH)
            propertyDetails?.text = makeDetailList(position)
        }

        private fun makeDetailList(position: Int) : String{
            var result = ""
            val details = propertiesList[position].details

            for(x in 0 until details.size){
                val detail = details[x]
                result += detail.getName()
                if(x != details.size - 1)
                    result += ", "
            }

            return result
        }
    }
}