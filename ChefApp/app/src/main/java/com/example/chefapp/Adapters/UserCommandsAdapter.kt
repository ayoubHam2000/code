package com.example.chefapp.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chefapp.R
import com.example.chefapp.Services.CommandServices
import com.example.chefapp.Structers.UserCommandInfo


class UserCommandsAdapter(val context : Context, val userCommands: ArrayList<UserCommandInfo>, val itemClick : (Int) -> Unit ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_command, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userCommands.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindView(position)
    }

    inner class ViewHolder(itemView : View?) : RecyclerView.ViewHolder(itemView!!){

        private val selectedCommand = itemView?.findViewById<ImageView>(R.id.selectedCommand)
        private val theCommandBackground = itemView?.findViewById<ImageView>(R.id.theCommandBackground)
        private val numberOfCommand = itemView?.findViewById<TextView>(R.id.numberOfCommand)
        private val tableNumber = itemView?.findViewById<TextView>(R.id.tableNumber)
        private val commandNumber = itemView?.findViewById<TextView>(R.id.commandNumber)
        private val commandTime = itemView?.findViewById<TextView>(R.id.commandTime)
        private val subDetailCommand = itemView?.findViewById<TextView>(R.id.subDetailCommand)
        private val isCommandDone = itemView?.findViewById<TextView>(R.id.isCommandDone)

        @SuppressLint("SetTextI18n")
        fun bindView(position : Int){
            setBackgroundColor(position)
            setSelectColor(position)
            numberOfCommand?.text = "# ${position + 1}"
            tableNumber?.text = userCommands[position].table.toString()
            commandNumber?.text = userCommands[position].commands.size.toString()
            commandTime?.text =  userCommands[position].getTime()
            subDetailCommand?.text = productListString(position)
            fIsCommandDone(position)

            theCommandBackground?.setOnClickListener {
                disSelected()
                userCommands[position].selected = true
                notifyDataSetChanged()
                itemClick(position)
            }
        }


        private fun productListString(position: Int) : String{
            var result = ""

            val listProduct = userCommands[position].commands
            for(x in 0 until listProduct.size){
                result += listProduct[x].getName()
                if(x != listProduct.size - 1)
                    result += ", "
            }
            return result
        }

        private fun setBackgroundColor(position: Int){
            val colors = context.resources.getIntArray(R.array.background_colors)
            val size = colors.size
            val index = userCommands[position].color

            val drawableShape = theCommandBackground?.background as GradientDrawable
            if(index <= -1 || index >= size){
                val indexOfNewColor = CommandServices.getNextColor() + position
                userCommands[position].color = indexOfNewColor % size
                drawableShape.setColor(colors[indexOfNewColor % size])
            }else{
                drawableShape.setColor(colors[index])
            }

        }

        private fun setSelectColor(position: Int){
            val selected = userCommands[position].selected
            if(selected){
                selectedCommand?.visibility = View.VISIBLE
            }else{
                selectedCommand?.visibility = View.INVISIBLE
            }

        }

        private fun fIsCommandDone(position: Int){
            if(userCommands[position].isComplete){
                isCommandDone?.visibility = View.VISIBLE
            }else{
                isCommandDone?.visibility = View.INVISIBLE
            }
        }

        private fun disSelected(){
            for(item in userCommands){
                if(item.selected){
                    item.selected = false
                    break
                }
            }
        }
    }
}