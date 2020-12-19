package com.ddsio.productionapp.sharesawaari.InboxScreen.Child

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ddsio.productionapp.sharesawaari.R
import com.ddsio.productionapp.sharesawaari.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {

  lateinit var ivBacknumberSaveNew : ImageView
  lateinit var recyclerview_newmessage : RecyclerView

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_new_message)

    ivBacknumberSaveNew = findViewById<ImageView>(R.id.ivBacknumberSaveNew)
    recyclerview_newmessage = findViewById<RecyclerView>(R.id.recyclerview_newmessage)

    supportActionBar?.title = "Select User"

    fetchUsers()



    ivBacknumberSaveNew.setOnClickListener {
      onBackPressed()
    }

  }

  companion object {
    val USER_KEY = "USER_KEY"
  }

  private fun fetchUsers() {
    val ref = FirebaseDatabase.getInstance().getReference("/users")
    ref.addListenerForSingleValueEvent(object: ValueEventListener {

      override fun onDataChange(p0: DataSnapshot) {
        val adapter = GroupAdapter<ViewHolder>()

        p0.children.forEach {
          Log.d("NewMessage", it.toString())
          val user = it.getValue(User::class.java)
          if (user != null) {
            adapter.add(UserItem(user))
          }
        }

        adapter.setOnItemClickListener { item, view ->

          val userItem = item as UserItem

          val intent = Intent(view.context, ChatLogActivity::class.java)
//          intent.putExtra(USER_KEY,  userItem.user.username)
          intent.putExtra(USER_KEY, userItem.user)
          startActivity(intent)

          finish()

        }

        recyclerview_newmessage.adapter = adapter
      }

      override fun onCancelled(p0: DatabaseError) {

      }
    })
  }
}

class UserItem(val user: User): Item<ViewHolder>() {
  override fun bind(viewHolder: ViewHolder, position: Int) {
    viewHolder.itemView.username_textview_new_message.text = user.username

    Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageview_new_message)
  }

  override fun getLayout(): Int {
    return R.layout.user_row_new_message
  }
}

