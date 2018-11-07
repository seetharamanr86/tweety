package eu.micer.tweety.network

import eu.micer.tweety.util.Constants
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface TwitterApi {

    @POST(Constants.Network.URL_TWITTER_STATUSES)
    @Streaming
    fun getTweetsStream(@Query("track") track: String): Observable<ResponseBody>
}