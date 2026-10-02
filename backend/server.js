const express = require('express');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 8080;

app.use(cors());
app.use(express.json());

// Health Check
app.get('/health', (req, res) => {
  res.json({ status: 'UP', service: 'LiveCaster Ingest Relay', timestamp: new Date() });
});

// Mock modules
app.post('/v1/auth/login', (req, res) => {
  const { email } = req.body;
  res.json({
    success: true,
    data: {
      user: { id: 'usr_001', email, fullName: 'Producer Operations' },
      token: 'livecaster_jwt_token_sample'
    }
  });
});

app.get('/v1/broadcasts', (req, res) => {
  res.json({
    success: true,
    data: [
      { id: 1, title: 'Main Feed Broadcast', status: 'READY', rtmpUrl: 'rtmp://live.livecaster.pk/live' }
    ]
  });
});

if (process.env.NODE_ENV !== 'test') {
  app.listen(PORT, () => {
    console.log(`LiveCaster Backend API listening on port ${PORT}`);
  });
}

module.exports = app;
