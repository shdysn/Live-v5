function validateBroadcastPayload(body) {
  const errors = [];
  if (!body.title || typeof body.title !== 'string') errors.push('Title is required');
  if (!body.rtmpUrl || typeof body.rtmpUrl !== 'string') errors.push('rtmpUrl is required');
  if (!body.streamKey || typeof body.streamKey !== 'string') errors.push('streamKey is required');
  return errors;
}

module.exports = { validateBroadcastPayload };
