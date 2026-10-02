package pk.livecaster.app.facebook.data.api

import pk.livecaster.app.facebook.data.dto.FacebookLiveVideoDto
import pk.livecaster.app.facebook.data.dto.FacebookPagesResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FacebookApiService {
    @GET("me/accounts")
    suspend fun getManagedPages(
        @Query("access_token") userToken: String
    ): FacebookPagesResponse

    @FormUrlEncoded
    @POST("{page_id}/live_videos")
    suspend fun createLiveVideo(
        @Path("page_id") pageId: String,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("status") status: String = "LIVE_NOW",
        @Query("access_token") pageToken: String
    ): FacebookLiveVideoDto
}
