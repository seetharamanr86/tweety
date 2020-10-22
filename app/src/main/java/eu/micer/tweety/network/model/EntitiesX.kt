package eu.micer.tweety.network.model

import com.google.gson.annotations.SerializedName

data class EntitiesX(
    @SerializedName("hashtags")
    val hashtags: List<Any>,
    @SerializedName("symbols")
    val symbols: List<Any>,
    @SerializedName("urls")
    val urls: List<Any>,
    @SerializedName("user_mentions")
    val userMentions: List<Any>
)