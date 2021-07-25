package com.example.chefapp.Controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PointF
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.chefapp.Adapters.CommandDetailAdapter
import com.example.chefapp.Adapters.UserCommandsAdapter
import com.example.chefapp.Helper.localeLanguageChanger
import com.example.chefapp.Modules.LanguageDisplay
import com.example.chefapp.Modules.TimeDisplay
import com.example.chefapp.R
import com.example.chefapp.Services.CommandServices
import com.example.chefapp.Structers.TheCommand
import com.example.chefapp.Structers.UserCommandInfo
import com.example.chefapp.Utilities.*
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DecimalFormat
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userCommandAdapter : UserCommandsAdapter
    private lateinit var commandProductAdapter : CommandDetailAdapter

    //249
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //full size screen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        //initialize sharedPreferences in onCreate method after setContent
        sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)

        val id : String? = intent.getStringExtra("RestaurantId")
        val unitPrice : String? = intent.getStringExtra("unitPrice")
        val restaurantName : String? = intent.getStringExtra("restaurantName")

        if(id == null || unitPrice == null || restaurantName == null){
            makeMessage(resources.getString(R.string.error))
            startApp()
        }else{
            RESTAURANT_ID = id
            UNIT_PRICE = unitPrice
            RESTAURANT_NAME = restaurantName
            startApp()
        }

        //time
        val timed = TimeDisplay()
        timed.intDisplayTime(timeMonth, timeDay, timeHour, timeMinute)
    }

    private fun startApp(){
        //setUpButtons
        setUpButtons()

        //setUpAdapters
        setUpUserAdapter(CommandServices.usersCommandsDisplay)
        setCommandProductAdapter(CommandServices.theCommandProduct)

        //setUpSwipe
        setUpSwipe()

        //get commands
        getCommands()

        //content
        setUpContent()

        //setUpSocket
        setUpSocket()
    }

    //three function used -->updateContent(position), -->commandDetailNotify(position)
    //-->smoothScroll(commandRecyclerView, position)
    private fun setUpUserAdapter(userCommand : ArrayList<UserCommandInfo>){

        val layoutManager = LinearLayoutManager(this)

        userCommandAdapter = UserCommandsAdapter(this, userCommand){ position ->
            updateContentWhenClickCommand(position)
            commandDetailNotify(position)
            smoothScroll(commandRecyclerView, position)
        }

        commandRecyclerView.adapter = userCommandAdapter
        commandRecyclerView.layoutManager = layoutManager
        //start counting time
        changeAdapterTime()
    }

    //one function used updateWhenClickDonne
    private fun setCommandProductAdapter(commandProducts : ArrayList<TheCommand>){

        val layoutManager = LinearLayoutManager(this)

        commandProductAdapter = CommandDetailAdapter(this, commandProducts){donne ->
            if(donne){
                updateWhenClickDonne()
            }else{
                updateWithClickDelete()
            }

        }

        detailCommandRecyclerView.adapter = commandProductAdapter
        detailCommandRecyclerView.layoutManager = layoutManager
    }

    //used from -->setCommandProductAdapter and it use one function commandDeleted
    private fun updateWithClickDelete(){
        val index = CommandServices.searchActive()
        if(index > -1){
            val id = CommandServices.usersCommandsDisplay[index].id
            CommandServices.deleteCommand(this, id){success ->
                if(success){
                    commandDeleted(index)
                }else{
                    makeMessage(resources.getString(R.string.error))
                }
            }
        }else{
            println("something went wrong index = -1")
        }
    }



    private fun setUpButtons(){

        val lan = LanguageDisplay(this, sharedPreferences){
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }

        languageChanger.setOnClickListener {
            lan.languageManagement()
        }

        commandListButton.setOnClickListener {
            if(SELECTED_ICON != 0){
                changeParameterIconView(0)
                CommandServices.getUserDisplayCommand(false)
                if(CommandServices.usersCommandsDisplay.size > 0){
                    CommandServices.deSelected()
                    CommandServices.usersCommandsDisplay[0].selected = true
                    commandDetailNotify(0)
                    commandRecyclerView.scrollToPosition(0)
                    hideCommandDetail.visibility = View.INVISIBLE
                }else{
                    hideCommandDetail.visibility = View.VISIBLE
                }
                userCommandAdapter.notifyDataSetChanged()
                SELECTED_ICON = 0
            }
        }

        commandedListButton.setOnClickListener {
            if(SELECTED_ICON != 1){
                changeParameterIconView(1)
                CommandServices.getUserDisplayCommand(true)
                if(CommandServices.usersCommandsDisplay.size > 0){
                    CommandServices.deSelected()
                    CommandServices.usersCommandsDisplay[0].selected = true
                    commandDetailNotify(0)
                    commandRecyclerView.scrollToPosition(0)
                    hideCommandDetail.visibility = View.INVISIBLE
                }else{
                    hideCommandDetail.visibility = View.VISIBLE
                }
                userCommandAdapter.notifyDataSetChanged()
                SELECTED_ICON = 1
            }

        }

        exitApp.setOnClickListener {
            onBackPressed()
        }

    }

    private fun setUpSocket(){
        val socket = IO.socket(URL)
        socket.connect()
        socket.on("getCommands", getNewCommands )
    }

    //one function is used updateWhenGetFromSocket()
    private val getNewCommands = Emitter.Listener { args ->
        //args[0] is restaurant ID

        if(args[0] == RESTAURANT_ID){

            println("start update ....")
            CommandServices.getAllCommands(this){success->
                if(success){
                    Log.d("Success", "Success to get command")
                    updateWhenGetFromSocket()
                }else{
                    Log.d("Failed", "failed to get somme command")
                    //sound
                }
            }

        }
    }

    //one function used -->whenGetCommandsSuccess
    //also if the get failed it show the barrier of the commandDetail
    private fun getCommands(){
        CommandServices.getAllCommands(this){success->
            if(success){
                Log.d("Success", "Success to get command")
                whenGetCommandsSuccess()
            }else{
                hideCommandDetail.visibility = View.VISIBLE
                Log.d("Failed", "failed to get somme command")
            }
        }
    }

    //---------------------------- from here

    //used from swipe and getCommands
    private fun whenGetCommandsSuccess(){
        //turn on the visibility of donneNumber and update the number of donne commands
        showDonneView()

        if(SELECTED_ICON == 0){
            CommandServices.getUserDisplayCommand(false)
            changeParameterIconView(0)
        }else{
            CommandServices.getUserDisplayCommand(true)
            changeParameterIconView(1)
        }

        if(CommandServices.usersCommandsDisplay.size > 0){
            val index = CommandServices.searchActive()
            if(index == -1){
                //-update main view and commandDetailAdapter
                updateContentWhenClickCommand(0)
            }else{
                updateContentWhenClickCommand(index)
            }
            userCommandAdapter.notifyDataSetChanged()
            //update the number of all commands showing in the top of the app
            updateAllCommandsView()
            hideCommandDetail.visibility = View.INVISIBLE
        }else{
            hideCommandDetail.visibility = View.VISIBLE
        }
    }

    //user from getNewCommands (we need something here circle show up)
    private fun updateWhenGetFromSocket(){
        playSound()
        showNotDonneView()
        updateAllCommandsView()
        if(SELECTED_ICON == 0){
            CommandServices.getUserDisplayCommand(false)
            if(CommandServices.usersCommandsDisplay.size == 1){
                CommandServices.usersCommandsDisplay[0].selected = true
                hideCommandDetail.visibility = View.INVISIBLE
            }
            userCommandAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateContentWhenClickCommand(position : Int){
        val colors = this.resources.getIntArray(R.array.background_colors)
        val size = colors.size
        val theCommand = CommandServices.usersCommandsDisplay[position]
        val index = theCommand.color
        if(index <= -1 || index >= size){
            val indexOfNewColor = CommandServices.getNextColor() + position
            commandDetailBar.setBackgroundColor(colors[indexOfNewColor % size])
        }else{
            commandDetailBar.setBackgroundColor(colors[index])
        }

        //main content
        commadRank.text = "#${position + 1}"
        tableNumber.text = theCommand.table.toString()
        numberOfCommanded.text = theCommand.commands.size.toString()
        timeOfCommad.text = theCommand.getTime()
        totalPriceOfCommand.text = DecimalFormat("##.##").format(theCommand.getTotalPrice())
        commandUnitPrice.text = UNIT_PRICE


        //display detail
        CommandServices.usersCommandsDisplay[position].selected = true
        commandDetailNotify(position)
    }


    //--------------------------------------
    //this are used from commandDetail adapter with donne clicked
    private fun updateWhenClickDonne(){
        val index = CommandServices.searchActive()
        if(index != -1){
            val id = CommandServices.usersCommandsDisplay[index].id

            CommandServices.patchCommandInfo(this, id){success->
                if(success){
                    commandDonne(index)
                }else{
                    makeMessage(resources.getString(R.string.error))
                }
            }
        }else{
            println("index is -1 something went wrong")
        }
    }

    private fun commandDonne(index : Int){
        //index > -1
        val animSwipeOut = AnimationUtils.loadAnimation(this, R.anim.swipe_out)
        val targetView = commandRecyclerView.layoutManager!!.findViewByPosition(index)
        targetView?.startAnimation(animSwipeOut)
        Handler().postDelayed({
            CommandServices.usersCommandsDisplay[index].isComplete = true
            CommandServices.usersCommandsDisplay[index].selected = false
            if(index  > 0){
                CommandServices.usersCommandsDisplay[index - 1].selected = true
                commandDetailNotify(index - 1)
            }else{
                if(CommandServices.usersCommandsDisplay.size == 1){
                    hideCommandDetail.visibility = View.VISIBLE
                }else{
                    CommandServices.usersCommandsDisplay[index + 1].selected = true
                    commandDetailNotify(index + 1)
                }
            }
            showDonneView()
            CommandServices.usersCommandsDisplay.removeAt(index)
            updateAllCommandsView()
            userCommandAdapter.notifyDataSetChanged()
        }, 450)
    }

    private fun commandDeleted(index : Int){
        //index > -1
        val animSwipeOut = AnimationUtils.loadAnimation(this, R.anim.swipe_out)
        val targetView = commandRecyclerView.layoutManager!!.findViewByPosition(index)
        targetView?.startAnimation(animSwipeOut)
        Handler().postDelayed({
            if(index > 0){
                CommandServices.usersCommandsDisplay[index - 1].selected = true
                commandDetailNotify(index - 1)
            }else{
                if(CommandServices.usersCommandsDisplay.size == 1){
                    hideCommandDetail.visibility = View.VISIBLE
                }else{
                    CommandServices.usersCommandsDisplay[index + 1].selected = true
                    commandDetailNotify(index + 1)
                }
            }
            showDonneView()
            CommandServices.usersCommandsDisplay.removeAt(index)
            userCommandAdapter.notifyDataSetChanged()
        }, 450)
    }

    //this is used from setUpUserAdapter when user click in command the detail show up
    private fun commandDetailNotify(position : Int){
        CommandServices.updateCommandProduct(position)
        commandProductAdapter.notifyDataSetChanged()
    }



    //-----------------------------------------

    private fun setUpContent(){
        restaurantName.text = RESTAURANT_NAME
    }

    private fun changeAdapterTime(){

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                if(CommandServices.addedTime % 60 == 0){
                    userCommandAdapter.notifyDataSetChanged()
                    val selectedItem = CommandServices.searchActive()
                    if(selectedItem != -1)
                        timeOfCommad.text = CommandServices.usersCommandsDisplay[selectedItem].getTime()
                }
                CommandServices.addedTime++
                mainHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun changeParameterIconView(index : Int){
        //when user click to an icon the color will show on it

        setParameterIconBackGroundColor()
        if(index == 0){
            notDonneCommandBack.setBackgroundResource(R.color.gblue)
            notDonneCommandForceBack.setBackgroundResource(R.color.darcker)
        }else if(index == 1){
            donneCommandBack.setBackgroundResource(R.color.gblue)
            donneCommandForceBack.setBackgroundResource(R.color.darcker)
        }

    }

    private fun setParameterIconBackGroundColor(){
        notDonneCommandBack.setBackgroundResource(R.color.transparent)
        notDonneCommandForceBack.setBackgroundResource(R.color.transparent)

        donneCommandBack.setBackgroundResource(R.color.transparent)
        donneCommandForceBack.setBackgroundResource(R.color.transparent)
    }

    //one function used when get success at the swipe action -->whenGetCommandsSuccess()
    private fun setUpSwipe(){
        swiperOfRecyclerView.setOnRefreshListener {
            CommandServices.getAllCommands(this){success->
                if(success){
                    Log.d("Success", "Success to get command")
                    whenGetCommandsSuccess()
                }else{
                    Log.d("Failed", "failed to get somme command")
                    makeMessage(resources.getString(R.string.error))
                    //sound
                }
                swiperOfRecyclerView.isRefreshing = false
            }
        }
    }

    private fun smoothScroll(recyclerView: RecyclerView, position : Int){
        val layout = recyclerView.layoutManager!!
        val MILLISECONDS_PER_INCH = 200f

        val smoothScroller: RecyclerView.SmoothScroller = object : LinearSmoothScroller(this) {

            fun dp2px(dpValue: Float): Int {
                val scale = resources.displayMetrics.density
                return (dpValue * scale + 0.5f).toInt()
            }

            // change this and the return super type to
            // "calculateDyToMakeVisible" if the layout direction is set to VERTICAL
            override fun calculateDxToMakeVisible(view: View?, snapPreference : Int): Int {
                return super.calculateDxToMakeVisible(view, SNAP_TO_END) - dp2px(50f)
            }

            //This controls the direction in which smoothScroll looks for your view
            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                return this.computeScrollVectorForPosition(targetPosition)
            }

            //This returns the milliseconds it takes to scroll one pixel.
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }

            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }

        }

        smoothScroller.targetPosition = position
        layout.startSmoothScroll(smoothScroller)
    }

    private fun updateAllCommandsView(){
        val allCommandsSize = CommandServices.usersCommandsDisplay.size
        numberOfAllCommands.text = if(allCommandsSize in 0..9) "0$allCommandsSize" else "$allCommandsSize"
    }

    private fun showDonneView(){
        donneCercle.visibility = View.VISIBLE
        donneNumber.visibility = View.VISIBLE
        donneNumber.text = CommandServices.donneCountOf(true).toString()
        showNotDonneView()
    }

    private fun showNotDonneView(){
        notDonneCercle.visibility = View.VISIBLE
        notDoneNumber.visibility = View.VISIBLE
        notDoneNumber.text = CommandServices.donneCountOf(false).toString()
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#############--       change language        --####################*/
    /*###################################################################*/
    /*###################################################################*/

    override fun attachBaseContext(newBase: Context?) {

        sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(newBase)
        val lang = sharedPreferences.getString(SELECTED_LANGUAGE, "fr")
        super.attachBaseContext(localeLanguageChanger.wrap(newBase!!, lang!!))
    }


    /*###################################################################*/
    /*###################################################################*/
    /*###################--       Methods       --#######################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun makeMessage(message : String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun playSound(){
        val mp = MediaPlayer.create(this, R.raw.sound_bill)
        mp.start()
    }

}
