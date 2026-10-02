package pk.livecaster.app.facebook.data.api

import pk.livecaster.app.facebook.data.dto.FacebookLiveVideoDto
import pk.livecaster.app.facebook.data.dto.FacebookPagesResponse
import pk.livecaster.app.facebook.data.dto.FacebookUserDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FacebookApiService {
    @GET("me")
    suspend fun getProfile(
        @Query("access_token") userToken: String
    ): FacebookUserDto

    @GET("me/accounts")
    suspend fun getManagedPages(
        @Query("access_token") userToken: String
    ): FacebookPagesResponse

    @FormUrlEncoded
    @POST("me/live_videos")
    suspend fun createProfileLiveVideo(
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("status") status: String = "LIVE_NOW",
        @Query("access_token") userToken: String
    ): FacebookLiveVideoDto

    @FormUrlEncoded
    @POST("{page_id}/live_videos")
    suspend fun createLiveVideo(
        @Path("page_id") pageId: String,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("status") status: String = "LIVE_NOW",
        @Query("access_token") pageToken: String
    ): FacebookLiveVideoDto

    @POST("{live_video_id}")
    suspend fun endLiveVideo(
        @Path("live_video_id") liveVideoId: String,
        @Query("end_live_video") endLiveVideo: Boolean = true,
        @Query("access_token") token: String
    ): Map<String, Boolean>
}
