class Broadcast {
  constructor({ id, title, rtmpUrl, streamKey, platform, status = 'DRAFT', createdAt = new Date() }) {
    this.id = id;
    this.title = title;
    this.rtmpUrl = rtmpUrl;
    this.streamKey = streamKey;
    this.platform = platform;
    this.status = status;
    this.createdAt = createdAt;
  }
}

module.exports = Broadcast;
