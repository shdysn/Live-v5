const broadcasts = [];

class BroadcastRepository {
  async findAll() { return broadcasts; }
  async findById(id) { return broadcasts.find(b => b.id === id); }
  async create(broadcast) {
    broadcasts.push(broadcast);
    return broadcast;
  }
}

module.exports = new BroadcastRepository();
