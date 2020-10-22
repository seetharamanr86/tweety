package eu.micer.tweety.network.model

import com.google.gson.annotations.SerializedName

data class UserMention(
    @SerializedName("id")
    val id: Long,
    @SerializedName("id_str")
    val idStr: String,
    @SerializedName("indices")
    val indices: List<Int>,
    @SerializedName("name")
    val name: String,
    @SerializedName("screen_name")
    val screenName: String
)