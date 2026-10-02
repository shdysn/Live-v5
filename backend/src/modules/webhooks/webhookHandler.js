exports.handleRtmpEvent = (req, res) => {
  const { event, stream, client_id } = req.body;
  console.log(`[RTMP Webhook] Event: ${event} on stream: ${stream} from client: ${client_id}`);
  res.status(200).send('OK');
};
