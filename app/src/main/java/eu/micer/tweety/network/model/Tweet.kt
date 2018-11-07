package eu.micer.tweety.network.model

import com.google.gson.annotations.SerializedName

data class Tweet(
    @SerializedName("text")
    val text: String
// TODO implement
)