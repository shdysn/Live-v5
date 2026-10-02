const jwt = require('jsonwebtoken');
const config = require('../../config');
const AppError = require('../errors/AppError');

module.exports = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return next(new AppError('Unauthorized: Token missing', 401));
  }
  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, config.jwtSecret);
    req.user = decoded;
    next();
  } catch (err) {
    return next(new AppError('Invalid or expired authentication token', 401));
  }
};
