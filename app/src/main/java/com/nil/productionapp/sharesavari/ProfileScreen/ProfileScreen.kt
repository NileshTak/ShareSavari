package com.nil.productionapp.sharesavari.ProfileScreen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.nil.productionapp.sharesavari.R
import de.hdodenhof.circleimageview.CircleImageView

class ProfileScreen : Fragment() {

    var CAMERA_REQUEST = 1888
    var GALLERY_REQUEST = 1666
    lateinit var lottieSelectImage : LottieAnimationView
    lateinit var rlParent : RelativeLayout
    lateinit var ivProf : CircleImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_screen, container, false)

        lottieSelectImage = view.findViewById<LottieAnimationView>(R.id.lottieSelectImage)
        rlParent = view.findViewById<RelativeLayout>(R.id.rlParent)
        ivProf = view.findViewById<CircleImageView>(R.id.ivProf)

        lottieSelectImage.setOnClickListener {
            askGalleryPermissionCamera()
        }

        return view
    }


    private fun askGalleryPermissionCamera() {
        askPermission(
            Manifest.permission.CAMERA ,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {



            val Choice =
                arrayOf<CharSequence>("From Camera", "From Gallery")

            val builder =
                android.app.AlertDialog.Builder(activity)
            builder.setTitle("Select from")
            builder.setItems(Choice) { dialog, which -> // the user clicked on colors[which]
                if (which == 0) {

                    var cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST)

                } else if (which == 1) {

                    val galleryIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(galleryIntent, GALLERY_REQUEST)
                }
            }
            builder.show()


        }.onDeclined { e ->
            if (e.hasDenied()) {
                //the list of denied permissions
                e.denied.forEach {
                }

                AlertDialog.Builder(activity!!)
                    .setMessage("Please accept our permissions.. Otherwise you will not be able to use some of our Important Features.")
                    .setPositiveButton("yes") { _, _ ->
                        e.askAgain()
                    } //ask again
                    .setNegativeButton("no") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            if (e.hasForeverDenied()) {
                //the list of forever denied permissions, user has check 'never ask again'
                e.foreverDenied.forEach {
                }
                // you need to open setting manually if you really need it
                e.goToSettings();
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            var photo = data!!.getExtras()!!.get("data") as Bitmap
            ivProf.setImageBitmap(photo)
            lottieSelectImage.visibility = View.GONE
            rlParent.visibility = View.VISIBLE
        } else if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            val contentURI = data!!.data
            val bitmap =
                MediaStore.Images.Media.getBitmap(
                   activity!!.contentResolver,
                    contentURI
                )
            ivProf.setImageBitmap(bitmap)
            lottieSelectImage.visibility = View.GONE
            rlParent.visibility = View.VISIBLE
        }

        super.onActivityResult(requestCode, resultCode, data)
    }



}