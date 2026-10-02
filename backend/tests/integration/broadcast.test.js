const request = require('supertest');
const app = require('../../server');

describe('Broadcast API Integration', () => {
  it('GET /health returns status UP', async () => {
    const res = await request(app).get('/health');
    expect(res.statusCode).toEqual(200);
    expect(res.body.status).toEqual('UP');
  });

  it('GET /v1/broadcasts returns list of broadcasts', async () => {
    const res = await request(app).get('/v1/broadcasts');
    expect(res.statusCode).toEqual(200);
    expect(res.body.success).toBe(true);
    expect(Array.isArray(res.body.data)).toBe(true);
  });
});
