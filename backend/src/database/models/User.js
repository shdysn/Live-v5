class User {
  constructor({ id, email, fullName, role = 'PRODUCER', createdAt = new Date() }) {
    this.id = id;
    this.email = email;
    this.fullName = fullName;
    this.role = role;
    this.createdAt = createdAt;
  }
}

module.exports = User;
