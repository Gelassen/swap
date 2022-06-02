const request = require('supertest');
const app = require('../app');

beforeAll(async() => {
    // prepare initial state
    let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
    let postServicePayload = { "title" : "Develop software", "date" : 0, "index" : ["Develop software"]};
    let anotherServicePayload = { "title" : "Draw sketches", "date" : 0, "index" : ["Draw sketches"]};
    await request(app)
        .post('/api/v1/account')
        .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(postPayload)
        .expect('Content-Type', 'application/json; charset=utf-8')
        .expect(200);
    await request(app)
        .post('/api/v1/account/offers')    
        .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(postServicePayload)
        .expect(200, {});
    await request(app)
        .post('/api/v1/account/offers')    
        .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(anotherServicePayload)
        .expect(200, {});
});

afterAll(async() => {
    // clean database from test data
    const response = await request(app)
        .get('/api/v1/account')
        .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
        .expect('Content-Type', 'application/json; charset=utf-8')
        .expect(200);
    await request(app)
        .delete(`/api/v1/account/${response.body.payload.id}`)
        .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
        .expect(204);  
});

describe('Cover /api/v1/contacts with tests', () => {
    it('On GET /api/v1/contacts without service id receives BAD_REQUEST status code', async() => {
        await request(app)
            .get('/api/v1/contacts')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "No service id in request. Did you forget to add service id?" })
    });
    it('On GET /api/v1/contacts without authorization receives UNAUTHORIZED status code', async() => {
        await request(app)
            .get('/api/v1/contacts?serviceId=10')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(401, { "payload" : "Did you forget to add authorization header?" });
    });
    it('On GET /api/v1/contacts with auth header in wrong format receives BAD_REQUST status code', async() => {
        await request(app)
            .get('/api/v1/contacts?serviceId=10')
            .set('Authorization', 'bm9uLmV4aXN0aW5nQGdtYWlsLmNvbQ==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you add correct authorization header?" });
    });
    it('On GET /api/v1/contacts with non existing service id receives BAD_REQUEST status code', async() => {
        await request(app)
            .get('/api/v1/contacts?serviceId=10')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "There is no data associated with this service id. Did you pass correct id?" })
    });
    it('On GET /api/v1/contacts with non existing profile receives UNAUTHORIZED status code', async() => {
        await request(app)
            .get('/api/v1/contacts?serviceId=10')
            .set('Authorization', 'Basic Qm9iQGdtYWlsLmNvbTpidXBh')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(401, { "payload" : "There is no account for this credentials. Are you authorized?" });
    });
    it('On GET /api/v1/contacts with existing profile and existing service receives OK status code', async() => {
        let postBobProfile = {"contact":"Bob@gmail.com","secret":"bupa","name":"Bob","offers":[],"demands":[]};
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic Qm9iQGdtYWlsLmNvbTpidXBh')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postBobProfile)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        const response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        const johnProfile = response.body.payload;

        await request(app)
            .get(`/api/v1/contacts?serviceId=${johnProfile.offers[0].id}`)
            .set('Authorization', 'Basic Qm9iQGdtYWlsLmNvbTpidXBh')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" :  { "id" : johnProfile.id, "name" : `${johnProfile.name}`, "contact" : `${johnProfile.contact}`}});

        // clean database 
        const bobProfileResponse = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic Qm9iQGdtYWlsLmNvbTpidXBh=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        await request(app)
            .delete(`/api/v1/account/${bobProfileResponse.body.payload.id}`)
            .set('Authorization', 'Basic Qm9iQGdtYWlsLmNvbTpidXBh=')
            .expect(204);
    });
});