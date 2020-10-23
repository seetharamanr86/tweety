package eu.micer.tweety.data.remote.model

import com.google.gson.annotations.SerializedName

data class ExtendedTweet(
    @SerializedName("display_text_range")
    val displayTextRange: List<Int>,
    @SerializedName("entities")
    val entities: EntitiesX,
    @SerializedName("full_text")
    val fullText: String
)
