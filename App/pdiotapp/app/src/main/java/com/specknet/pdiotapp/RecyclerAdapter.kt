package com.specknet.pdiotapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity

import androidx.recyclerview.widget.RecyclerView
import com.specknet.pdiotapp.R
import com.specknet.pdiotapp.bluetooth.ConnectingActivity
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import com.specknet.pdiotapp.utils.ThingyLiveData

class RecyclerAdapter(var mContext: Context): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    private var titles = arrayOf("Respeck","Thingy")
    private var connectText = "Connection State:"
    private var successConnect = "Connected"
    private var failConnect = "Not Connected"
    private var buttonTextConn = "Connect"
    private var buttonTextReConn = "Re-Connect"
    private var images = intArrayOf(R.drawable.respeck_image,R.drawable.thingy_image)


    var thingyOn = false
    var respeckOn = false
    lateinit var respeckReceiver: BroadcastReceiver
    lateinit var thingyReceiver: BroadcastReceiver

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView
        var itemTitle: TextView
        var itemConnect: TextView
        var itemConnectState: TextView
        var connButton: Button

        init {
            itemImage = itemView.findViewById(R.id.item_image)
            itemTitle = itemView.findViewById(R.id.item_title)
            itemConnect = itemView.findViewById(R.id.item_connect)
            itemConnectState = itemView.findViewById(R.id.item_connect_state)
            connButton = itemView.findViewById(R.id.item_connect_button)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        respeckReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {
                    respeckOn = true
                }
            }
        }
        thingyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == Constants.ACTION_THINGY_BROADCAST) {
                    thingyOn = true
                }
            }
        }
        holder.itemTitle.text = titles[position]
        holder.itemConnect.text = connectText
        holder.itemImage.setImageResource(images[position])
        var isOn:Boolean = false
        if(position == 0) {
            isOn = respeckOn
        }else {
            isOn = thingyOn
        }
        if(isOn){
            holder.itemConnectState.text = successConnect
            holder.itemConnectState.setTextColor(Color.parseColor("#00FF00"))
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