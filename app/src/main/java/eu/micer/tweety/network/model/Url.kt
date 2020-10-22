package eu.micer.tweety.network.model

import com.google.gson.annotations.SerializedName

data class Url(
    @SerializedName("display_url")
    val displayUrl: String,
    @SerializedName("expanded_url")
    val expandedUrl: String,
    @SerializedName("indices")
    val indices: List<Int>,
    @SerializedName("url")
    val url: String
)