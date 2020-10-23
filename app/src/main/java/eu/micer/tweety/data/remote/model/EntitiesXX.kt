package eu.micer.tweety.data.remote.model

import com.google.gson.annotations.SerializedName

data class EntitiesXX(
    @SerializedName("hashtags")
    val hashtags: List<Any>,
    @SerializedName("symbols")
    val symbols: List<Any>,
    @SerializedName("urls")
    val urls: List<Url>,
    @SerializedName("user_mentions")
    val userMentions: List<Any>
)
