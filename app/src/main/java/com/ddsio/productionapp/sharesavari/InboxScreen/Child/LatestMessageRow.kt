package com.ddsio.productionapp.sharesavari.InboxScreen.Child

import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.letsbuildthatapp.kotlinmessenger.models.ChatMessage
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*


class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>() {
  var chatPartnerUser: User? = null
  var LOGIN_TOKEN = ""
  var USER_UPDATE_ID = ""
  lateinit var USER_ID_KEY : String

  override fun bind(viewHolder: ViewHolder, position: Int) {
    viewHolder.itemView.message_textview_latest_message.text = chatMessage.text

    LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",viewHolder.itemView.message_textview_latest_message.context)!!
    USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",viewHolder.itemView.message_textview_latest_message.context)!!
    USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",viewHolder.itemView.message_textview_latest_message.context)!!

    val chatPartnerId: String
    if (chatMessage.fromId == USER_ID_KEY) {
      chatPartnerId = chatMessage.toId
    } else {
      chatPartnerId = chatMessage.fromId
    }

    val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
    ref.addListenerForSingleValueEvent(object: ValueEventListener {
      override fun onDataChange(p0: DataSnapshot) {
        chatPartnerUser = p0.getValue(User::class.java)
        viewHolder.itemView.username_textview_latest_message.text = chatPartnerUser?.username

        val targetImageView = viewHolder.itemView.imageview_latest_message
        Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
      }

      override fun onCancelled(p0: DatabaseError) {

      }
    })
  }

  override fun getLayout(): Int {
    return R.layout.latest_message_row
  }
}