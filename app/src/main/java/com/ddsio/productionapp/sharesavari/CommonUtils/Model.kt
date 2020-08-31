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

 class BookRidesPojoItem : Serializable {
    val comment: String? = null
    val date: String? = null
    val gcity: String? = null
    val glat: String? = null
    val gline: String? = null
    val glog: String? = null
    val going: String? = null
    val id: Int? = null
    val image: String? = null
    val is_return: Boolean? = null
    val lcity: String? = null
    val leaving: String? = null
    val llat: String? = null
    val lline: String? = null
    val llog: String? = null
    val passenger: Int? = null
    val price: Int? = null
    val rdate: String? = null
    val rtime: String? = null
    val time: String? = null
    val url: String? = null
    val user: Int? = null
    val username: String? = null
 }


class bookride : ArrayList<bookrideItem>()

data class bookrideItem(
    val comment: String,
    val id: Int,
    val passenger: Int,
    val plat: Any,
    val plog: Any,
    val ride: Int,
    val ridename: String
)
