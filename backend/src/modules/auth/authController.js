const { generateToken } = require('../../common/security/tokens');

exports.login = async (req, res, next) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ success: false, message: 'Email and password required' });
  }
  const user = { id: 'usr_prod_1', email, role: 'BROADCASTER' };
  const token = generateToken(user);
  res.json({ success: true, data: { user, token } });
};

exports.me = async (req, res) => {
  res.json({ success: true, data: req.user });
};
