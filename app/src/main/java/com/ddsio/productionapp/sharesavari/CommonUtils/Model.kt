package com.productionapp.amhimemekar.CommonUtils

import java.io.Serializable


data class UserDetailsModel(
    val email: String,
    val first_name: String,
    val last_name: String,
    val pk: Int,
    val username: String
)

data class UserUpdateDetailsModel(
    val adhar_image: String,
    val bio: String,
    val birthdate: String,
    val gender: Int,
    val id: Int,
    val mobile: String,
    val mobile_status: Boolean,
    val profile_image: String,
    val url: String,
    val user: Int
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


data class FetchProfileData(
    val bio: String,
    val birthdate: String,
    val email: String,
    val first_name: String,
    val gender: String,
    val id: Int,
    val image: String,
    val last_name: String,
    val mobile: String,
    val url: String,
    val verification: String
)


 class BookRideScreenFetchCity : Serializable {
    var city: String? = null
    var type: String? = null
}


class BookRidesPojo : ArrayList<BookRidesPojoItem>()

data class BookRidesPojoItem(
    val comment: String,
    val date: String,
    val gcity: String,
    val glat: String,
    val gline: String,
    val glog: String,
    val going: String,
    val id: Int,
    val image: String,
    val is_return: Boolean,
    val lcity: String,
    val leaving: String,
    val llat: String,
    val lline: String,
    val llog: String,
    val passenger: Int,
    val price: Int,
    val rdate: String,
    val rtime: String,
    val time: String,
    val url: String,
    val user: Int,
    val username: String
)