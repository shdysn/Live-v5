const { generateToken, verifyToken } = require('../../src/common/security/tokens');

describe('Auth Security Tokens', () => {
  it('should generate and verify JWT token successfully', () => {
    const payload = { id: 'user_123', email: 'test@livecaster.pk' };
    const token = generateToken(payload);
    expect(typeof token).toBe('string');

    const decoded = verifyToken(token);
    expect(decoded.id).toBe('user_123');
    expect(decoded.email).toBe('test@livecaster.pk');
  });
});
