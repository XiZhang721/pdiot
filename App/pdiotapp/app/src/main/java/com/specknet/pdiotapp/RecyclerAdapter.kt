package com.specknet.pdiotapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity

import androidx.recyclerview.widget.RecyclerView
import com.specknet.pdiotapp.R
import com.specknet.pdiotapp.bluetooth.ConnectingActivity
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import com.specknet.pdiotapp.utils.ThingyLiveData
import java.lang.Math.round
import kotlin.math.roundToInt

class RecyclerAdapter(var mContext: Context, var respeckOn: Boolean, var thingyOn: Boolean): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    private var titles = arrayOf("Respeck","Thingy")
    private var successConnect = "Connected"
    private var failConnect = "Not Connected"
    private var buttonTextConn = "Connect"
    private var buttonTextReConn = "Re-Connect"
    private var images = intArrayOf(R.drawable.respeck_image,R.drawable.thingy_image)


    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView
        var itemTitle: TextView
        var itemConnectState: TextView
        var connButton: Button

        init {
            itemImage = itemView.findViewById(R.id.item_image)
            itemTitle = itemView.findViewById(R.id.item_title)
            itemConnectState = itemView.findViewById(R.id.item_connect_state)
            connButton = itemView.findViewById(R.id.item_connect_button)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {

        holder.itemTitle.text = titles[position]
        holder.itemImage.setImageResource(images[position])
        var isOn:Boolean = if(position == 0) {
            respeckOn
        }else {
            thingyOn
        }
        if(isOn){
            holder.itemConnectState.text = successConnect
            holder.itemConnectState.setTextColor(Color.parseColor("#00e800"))
            holder.connButton.setText(buttonTextReConn)
        }else{
            holder.itemConnectState.text = failConnect
            holder.itemConnectState.setTextColor(Color.parseColor("#F44336"))
            holder.connButton.setText(buttonTextConn)
        }

        holder.connButton.setOnClickListener {
            val intent = Intent(mContext, ConnectingActivity::class.java)
            mContext.startActivity(intent)
        }

    }


    override fun getItemCount(): Int {
        return titles.size
    }


}