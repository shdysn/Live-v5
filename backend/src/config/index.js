module.exports = {
  port: process.env.PORT || 8080,
  jwtSecret: process.env.JWT_SECRET || 'livecaster_super_secret_studio_key',
  rtmpRelayHost: process.env.RTMP_RELAY_HOST || 'live.livecaster.pk',
  facebookAppId: process.env.FACEBOOK_APP_ID || '',
  youtubeClientId: process.env.YOUTUBE_CLIENT_ID || ''
};
