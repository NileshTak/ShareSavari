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
     var tddate: String? = null
     var tdtime: String? = null
     var is_direct: Boolean? = null
     var carname : String? = null
     var stitle : String? = null
     var slat : String? = null
     var slog : String? = null
     var pets : Boolean? = null
     var smoking : Boolean? = null
     var carcolor : String? = null
     var max_back_2 : Boolean? = null
     var max_back_3 : Boolean? = null
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
    val verification: String,
    val pets: String,
    val smoking: String
)


 class BookRideScreenFetchCity : Serializable {
    var city: String? = null
    var type: String? = null
     var lat: String? = null
     var long: String? = null
     var gline: String? = null
     var gcity: String? = null
}


class BookRidesPojo : ArrayList<BookRidesPojoItem>()

 class BookRidesPojoItem : Serializable {
     var comment: String? = null
     var date: String? = null
     var gcity: String? = null
     var glat: String? = null
     var gline: String? = null
     var glog: String? = null
     var going: String? = null
     var id: String? = null
     var image: String? = null
     var is_return: Boolean? = null
     var lcity: String? = null
     var leaving: String? = null
     var llat: String? = null
     var lline: String? = null
     var llog: String? = null
     var passenger: String? = null
     var price: Int? = null
     var rdate: String? = null
     var rtime: String? = null
     var time: String? = null
     var url: String? = null
     var user: Int? = null
     var username: String? = null
     var tddate: String? = null
     var tdtime: String? = null
     var is_direct: Boolean? = null
     var carname : String? = null
     var stitle : String? = null
     var slat : String? = null
     var slog : String? = null
     var pets : Boolean? = null
     var smoking : Boolean? = null
     var max_back_2 : Boolean? = null
     var max_back_3 : Boolean? = null
     var carcolor : String? = null
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


class RatingModel : ArrayList<RatingModelItem>()


data class RatingModelItem(
    val driver: Int,
    val id: Int,
    val passenger: Int,
    val points: Int
)
