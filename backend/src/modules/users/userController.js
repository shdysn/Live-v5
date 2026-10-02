exports.getProfile = (req, res) => {
  res.json({
    success: true,
    data: {
      id: req.user ? req.user.id : 'usr_default',
      name: 'Broadcaster Producer',
      tier: 'Pro Enterprise',
      quotaRemainingHours: 120
    }
  });
};
