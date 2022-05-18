const request = require('supertest');
const app = require('../app');

describe('Cover /api/v1/offers with tests', () => {
    it('On GET /api/v1/offers without Auth header receives BAD_REQUEST code', async() => {
        await request(app)
            .get('/api/v1/offers')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you auth header?" })
    })
});