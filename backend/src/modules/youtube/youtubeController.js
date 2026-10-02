exports.getChannels = async (req, res) => {
  res.json({
    success: true,
    data: [
      { id: 'UC_sample_channel', title: 'LiveCaster News Stream', subscribers: 120000 }
    ]
  });
};

exports.createBroadcast = async (req, res) => {
  const { title } = req.body;
  res.json({
    success: true,
    data: {
      id: `yt_bc_${Date.now()}`,
      rtmpIngestUrl: 'rtmp://a.rtmp.youtube.com/live2',
      streamKey: `yt_stream_${Date.now()}`,
      title
    }
  });
};
