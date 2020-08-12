package com.productionapp.amhimemekar.CommonUtils

data class HomeExploreModel(
    val author: String,
    val author_image: String,
    val category: Int,
    val created_on: String,
    val `file`: String,
    val id: Int,
    val likes: List<Int>,
    val number_of_likes: Int,
    val number_of_saves: Int,
    val saves: List<Int>,
    val template: String,
    val title: String,
    val updated_on: String,
    val url: String
)



data class UploadedClass(
    val author: String,
    val author_image: Any,
    val category: Any,
    val created_on: String,
    val `file`: String,
    val id: Int,
    val likes: List<Any>,
    val number_of_likes: Int,
    val number_of_saves: Int,
    val saves: List<Any>,
    val template: Any,
    val title: String,
    val updated_on: String,
    val url: String
)


data class CommentsModel(
    val created_on: String,
    val id: Int,
    val parent: Any,
    val post: Int,
    val text: String,
    val user: Int,
    val username: String
)


data class UserDetailsModel(
    val email: String,
    val first_name: String,
    val last_name: String,
    val pk: Int,
    val username: String
)


data class PostSaved(
    val saved: Boolean,
    val updated: Boolean
)


data class PostLiked(
    val liked: Boolean,
    val updated: Boolean
)

data class TemplateModel(
    val author: String,
    val id: String,
    val template: String
)


data class CategoryModel(
    val author: Int,
    val created_on: String,
    val `file`: String,
    val id: Int,
    val last_updated: String,
    val status: Boolean,
    val title: String,
    val url: String
)

 data class SubCategoryModel(
    val author: Int,
    val category: Int,
    val created_on: String,
    val `file`: String,
    val id: Int,
    val last_updated: String,
    val status: Boolean,
    val title: String,
    val url: String
)


data class CompetitionsModel(
    val category: Int,
    val details: String,
    val `file`: String,
    val id: Int,
    val last_date: String,
    val prize: String,
    val start_date: String,
    val title: String
)

data class CatByIDModel(
    val author: String,
    val author_image: Any,
    val category: Int,
    val created_on: String,
    val `file`: String,
    val id: Int,
    val likes: List<Int>,
    val number_of_likes: Int,
    val number_of_saves: Int,
    val saves: List<Any>,
    val template: String,
    val title: String,
    val updated_on: String,
    val url: String
)