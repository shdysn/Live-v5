let broadcasts = [
  { id: 1, title: 'National Cricket Live Stream', status: 'LIVE', platform: 'MULTI_DESTINATION', viewers: 3500 }
];

exports.getAll = (req, res) => res.json({ success: true, data: broadcasts });

exports.create = (req, res) => {
  const newBc = { id: Date.now(), ...req.body, status: 'CREATED', createdAt: new Date() };
  broadcasts.push(newBc);
  res.status(201).json({ success: true, data: newBc });
};

exports.updateStatus = (req, res) => {
  const { id } = req.params;
  const { status } = req.body;
  const found = broadcasts.find(b => b.id == id);
  if (found) found.status = status;
  res.json({ success: true, data: found });
};
