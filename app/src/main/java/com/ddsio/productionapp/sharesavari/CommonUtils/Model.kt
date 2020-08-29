package com.productionapp.amhimemekar.CommonUtils

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class UserDetailsModel(
    val email: String,
    val first_name: String,
    val last_name: String,
    val pk: Int,
    val username: String
)

 class offerRideModel : Serializable {
     var id: String? = null
     var url: String? = null
     var user: String ? = null
     var username: String? = null
     var image: String? = null
     var leaving: String ? = null
     var llat: String ? = null
     var llog: String ? = null
     var lline: String? = null
     var lcity: String? = null
     var going: String? = null
     var glat: String? = null
     var glog: String ? = null
     var gline: String? = null
     var gcity: String ? = null
     var date: String ? = null
     var time: String ? = null
     var rdate: String ? = null
     var rtime: String ? = null
     var price: String ? = null
     var passenger: String? = null
     var comment: String ? = null
     var is_return: String? = null
 }