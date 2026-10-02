package pk.livecaster.app.facebook.domain.model

data class FacebookPage(
    val id: String,
    val name: String,
    val category: String,
    val accessToken: String,
    val followersCount: Long = 0,
    val pictureUrl: String? = null,
    val isDefaultDestination: Boolean = false
)

data class FacebookLiveVideo(
    val id: String,
    val streamUrl: String,
    val secureStreamUrl: String,
    val streamKey: String,
    val status: String,
    val title: String? = null,
    val description: String? = null
)
