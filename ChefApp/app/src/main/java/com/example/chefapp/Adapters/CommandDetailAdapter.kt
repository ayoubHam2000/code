package com.example.chefapp.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chefapp.R
import com.example.chefapp.Structers.TheCommand
import com.example.chefapp.Utilities.COMMAND_VIEW
import com.example.chefapp.Utilities.DONNE_COMMAND
import com.example.chefapp.Utilities.SELECTED_ICON
import com.example.chefapp.Utilities.UNIT_PRICE
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class CommandDetailAdapter(val context : Context, val theCommand : ArrayList<TheCommand>, val donne : (Boolean) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.detail_command_view, parent, false)
        val donneView = LayoutInflater.from(context).inflate(R.layout.donne_command_view, parent, false)
        return if(viewType == COMMAND_VIEW){
            ViewHolder(view)
        }else{
            DonneViewHolder(donneView)
        }
    }

    override fun getItemCount(): Int {
        return theCommand.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == theCommand.size){
            DONNE_COMMAND
        }else{
            COMMAND_VIEW
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType != DONNE_COMMAND){
            (holder as ViewHolder).bindView(position)
        }else{
            (holder as DonneViewHolder).bindView()
        }

    }

    inner class ViewHolder(itemView : View?) : RecyclerView.ViewHolder(itemView!!){

        private lateinit var propertiesAdapter : ProductPropertiesAdapter
        private val commandColor = itemView?.findViewById<LinearLayout>(R.id.commandColor)
        private val commandName = itemView?.findViewById<TextView>(R.id.commandName)
        private val commandNumber = itemView?.findViewById<TextView>(R.id.commandNumber)
        private val commandPrice = itemView?.findViewById<TextView>(R.id.commandPrice)
        private val commandTime = itemView?.findViewById<TextView>(R.id.commandTime)
        private val commandDetailRecyclerView = itemView?.findViewById<RecyclerView>(R.id.commandDetailRecyclerView)

        @SuppressLint("SetTextI18n")
        fun bindView(position : Int){
            val price = DecimalFormat("##.##").format(theCommand[position].productPrice)
            val layoutManager = LinearLayoutManager(context)

            setBackgroundColor(position)
            println("$commandColor || $commandName || $position")
            commandName?.text = theCommand[position].getName().toUpperCase(Locale.FRENCH)
            commandNumber?.text = (position + 1).toString()
            commandPrice?.text = "$price $UNIT_PRICE"
            commandTime?.text = "x ${theCommand[position].productNumber}"

            propertiesAdapter = ProductPropertiesAdapter(context, theCommand[position].properties)

            commandDetailRecyclerView?.adapter = propertiesAdapter
            commandDetailRecyclerView?.layoutManager = layoutManager
        }

        private fun setBackgroundColor(position: Int){
            val colors = context.resources.getIntArray(R.array.properties_color)
            val size = colors.size

            commandColor?.setBackgroundColor(colors[position % size])
        }
    }

    inner class DonneViewHolder(itemView : View?) : RecyclerView.ViewHolder(itemView!!){

        private val donneButton = itemView?.findViewById<Button>(R.id.donne_button)

        fun bindView(){
            if(SELECTED_ICON == 0){
                donneButton?.setText(R.string.donne)
            }else{
                donneButton?.setText(R.string.delete)
            }

            donneButton?.setOnClickListener {
                if(SELECTED_ICON == 0){
                    donne(true)
                }else{
                    donne(false)
                }

            }
        }
    }
}