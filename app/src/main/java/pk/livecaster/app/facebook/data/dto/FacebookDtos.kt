package pk.livecaster.app.facebook.data.dto

data class FacebookPageDto(
    val id: String,
    val name: String,
    val category: String,
    val access_token: String,
    val followers_count: Long? = 0
)

data class FacebookPagesResponse(
    val data: List<FacebookPageDto> = emptyList()
)

data class FacebookLiveVideoDto(
    val id: String,
    val stream_url: String,
    val secure_stream_url: String,
    val status: String
)
