const jwt = require('jsonwebtoken');
const config = require('../../config');

function generateToken(payload, expiresIn = '7d') {
  return jwt.sign(payload, config.jwtSecret, { expiresIn });
}

function verifyToken(token) {
  return jwt.verify(token, config.jwtSecret);
}

module.exports = { generateToken, verifyToken };
