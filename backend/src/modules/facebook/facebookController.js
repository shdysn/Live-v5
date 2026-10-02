exports.getPages = async (req, res) => {
  res.json({
    success: true,
    data: [
      { id: 'fb_page_1', name: 'LiveCaster Official Pakistan', followers: 45000 }
    ]
  });
};

exports.createLiveVideo = async (req, res) => {
  const { title } = req.body;
  res.json({
    success: true,
    data: {
      id: `fb_live_${Date.now()}`,
      stream_url: 'rtmps://live-api-s.facebook.com:443/rtmp/',
      stream_key: `fb_key_${Date.now()}`,
      status: 'LIVE_NOW',
      title
    }
  });
};
