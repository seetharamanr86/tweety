package eu.micer.tweety.data.remote.api

import eu.micer.tweety.presentation.util.Constants
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface TwitterApi {

    @POST(Constants.Network.URL_TWITTER_STATUSES_FILTER)
    @Streaming
    fun getTweetsStream(@Query("track") track: String): Single<ResponseBody>
}
