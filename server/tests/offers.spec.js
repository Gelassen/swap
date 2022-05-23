const request = require('supertest');
const app = require('../app');
const config = require('config');
const dbConfig = config.dbConfig;

/**
 *  Bob@gmail.com:bupa (Basic Qm9iQGdtYWlsLmNvbTpidXBh)
 *  Eve@gmail.com:dontbeevilgoogle (Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl)
 */

beforeAll(async() => {
    let postBobProfile = {"contact":"Bob@gmail.com","secret":"bupa","name":"Bob","offers":[],"demands":[]};
    let postBobService = { "title" : "Play tenis", "date" : 0, "index" : ["Play tenis"] };
    
    let postEveProfile = {"contact":"Eve@gmail.com","secret":"dontbeevilgoogle","name":"Eve","offers":[],"demands":[]};
    let postEveService = { "title" : "Develop software", "date" : 0, "index" : ["Develop software"] };
    // add account in system
    await request(app)
        .post('/api/v1/account')
        .set('Authorization', 'Basic Qm9iQGdtYWlsLmNvbTpidXBh')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(postBobProfile)
        .expect('Content-Type', 'application/json; charset=utf-8')
        .expect(200)
    await request(app)
        .post('/api/v1/account/offers')    
        .set('Authorization', 'Basic Qm9iQGdtYWlsLmNvbTpidXBh=')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(postBobService)
        .expect(200, {})
    
    await request(app)
        .post('/api/v1/account')
        .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(postEveProfile)
        .expect('Content-Type', 'application/json; charset=utf-8')
        .expect(200)
    await request(app)
        .post('/api/v1/account/offers')    
        .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(postEveService)
        .expect(200, {})
});

afterAll(async() => {
    // clean database from test data
    const response = await request(app)
        .get('/api/v1/account')
        .set('Authorization', 'Basic Qm9iQGdtYWlsLmNvbTpidXBh=')
        .expect('Content-Type', 'application/json; charset=utf-8')
        .expect(200);
    await request(app)
        .delete(`/api/v1/account/${response.body.payload.id}`)
        .set('Authorization', 'Basic Qm9iQGdtYWlsLmNvbTpidXBh=')
        .expect(204);

    const eveResponse = await request(app)
        .get('/api/v1/account')
        .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
        .expect('Content-Type', 'application/json; charset=utf-8')
        .expect(200);
    await request(app)
        .delete(`/api/v1/account/${eveResponse.body.payload.id}`)
        .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
        .expect(204);
})

describe('Cover /api/v1/offers with tests', () => {
    it('On GET /api/v1/offers without Auth header receives BAD_REQUEST code', async() => {
        await request(app)
            .get('/api/v1/offers')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you add auth header?" })
    });
    it('On GET /api/v1/offers with mailformed auth header receives BAD_REQUEST code', async() => {
        await request(app)
            .get('/api/v1/offers')
            .set('Authorization', 'VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you forget to add auth header?" })
    });
    it('On GET /api/v1/offers with non existing account receives BAD_REQUEST code', async() => {
        await request(app)
            .get(`/api/v1/offers?page=${1}`)
            .set('Authorization', 'Basic bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "There is no such account." });
    });
    it('On GET /api/v1/offers with existing account but empty demands receives OK code with error message', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)

        await request(app)
            .get('/api/v1/offers?page=1')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : "Profile doesn't have any demands. In this case there is no need to select offers." });

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
    it('On GET /api/v1/offers with existing account but zero matches receives OK code with empty payload', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
        let postServicePayload = { "title" : "there is no match for this demand", "date" : 0, "index" : ["there is no match for this demand"]};
        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postServicePayload)
            .expect(200, {})

        await request(app)
            .get('/api/v1/offers?page=1')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : [] });

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
    it('On GET /api/v1/offers with existing account and avaiable matches receives OK code with services', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
        let postServicePayload = { "title" : "Develop software", "date" : 0, "index" : ["Develop software"]};
        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postServicePayload)
            .expect(200, {})

        const offersResponse = await request(app)
            .get('/api/v1/offers?page=1')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(offersResponse.body.payload.length).toEqual(1);
        postServicePayload.id = offersResponse.body.payload.at(0).id;
        expect(offersResponse.body.payload.at(0)).toEqual(postServicePayload);

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
    it('On GET /api/v1/offers with existing account and two available matches receives OK code with 2 services', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
        let postServicePayload = { "title" : "Develop software", "date" : 0, "index" : ["Develop software"]};
        let postAnotherService = { "title" : "Play tenis", "date" : 0, "index" : ["Play tenis"] };
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postServicePayload)
            .expect(200, {})
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postAnotherService)
            .expect(200, {})

        const offersResponse = await request(app)
            .get('/api/v1/offers?page=1')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(offersResponse.body.payload.length).toEqual(2);
        postAnotherService.id = offersResponse.body.payload.at(0).id;
        postServicePayload.id = offersResponse.body.payload.at(1).id;
        expect(offersResponse.body.payload.at(0)).toEqual(postAnotherService);
        expect(offersResponse.body.payload.at(1)).toEqual(postServicePayload);

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
    it('On GET /api/v1/offers without page query passed receives BAD_REQUEST with error message', async() => {
        await request(app)
            .get('/api/v1/offers')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you forget to pass page in query, e.g. ?page=1 ?"});
    });
    it('On GET /api/v1/offers with existing account and 10+ available matches receives OK and only first 10', async() => {
        // prepare test data
        const maxItemsInPage = dbConfig.maxSize;
        const theNumberOfLastItemInQuery = '9';
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
        let postServicePayload = { "title" : "Develop software", "date" : 0, "index" : ["Develop software"]};
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postServicePayload)
            .expect(200, {})
        const matchServiceCount = 20;
        for (id = 0; id < matchServiceCount; id++) {
            let postAnotherService = { "title" : `Play tenis ${id}`, "date" : 0, "index" : [`Play tenis ${id}`] };
            await request(app)
                .post('/api/v1/account/demands')    
                .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
                .set('Content-Type', 'application/json; charset=utf-8')
                .send(postAnotherService)
                .expect(200, {})
            await request(app)
                .post('/api/v1/account/offers')    
                .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
                .set('Content-Type', 'application/json; charset=utf-8')
                .send(postAnotherService)
                .expect(200, {})
        }

        const offersResponse = await request(app)
            .get('/api/v1/offers?page=1')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(offersResponse.body.payload.length).toEqual(maxItemsInPage);
        expect(offersResponse.body.payload.at(9).title).toEqual(expect.stringContaining(theNumberOfLastItemInQuery));

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
        // instead of removing all 20+ test services, just deop a whole profile and recreate 
        // again based on Jest.beforeAll() implementation
        const eveResponse = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        await request(app)
            .delete(`/api/v1/account/${eveResponse.body.payload.id}`)
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .expect(204);
        let postEveProfile = {"contact":"Eve@gmail.com","secret":"dontbeevilgoogle","name":"Eve","offers":[],"demands":[]};
        let postEveService = { "title" : "Develop software", "date" : 0, "index" : ["Develop software"] };
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postEveProfile)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postEveService)
            .expect(200, {})
    });
    it('On GET /api/v1/offers?page=2 with existing account and 10+ available matches receives OK and only second 10', async() => {
        //verify database is not in dirty state
        const eveAccountResponse = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(eveAccountResponse.body.payload.offers.length).toEqual(1);
        // prepare initial state
        const maxItemsInPage = dbConfig.maxSize;
        const theNumberOfLastItemInQuery = '19';
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
        let postServicePayload = { "title" : "Develop software", "date" : 0, "index" : ["Develop software"]};
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postServicePayload)
            .expect(200, {})
        const matchServiceCount = 20;
        for (id = 1; id <= matchServiceCount; id++) {
            let postAnotherService = { "title" : `Play tenis ${id}`, "date" : 0, "index" : [`Play tenis ${id}`] };
            await request(app)
                .post('/api/v1/account/demands')    
                .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
                .set('Content-Type', 'application/json; charset=utf-8')
                .send(postAnotherService)
                .expect(200, {})
            await request(app)
                .post('/api/v1/account/offers')    
                .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
                .set('Content-Type', 'application/json; charset=utf-8')
                .send(postAnotherService)
                .expect(200, {})
        }

        const PAGE_NUMBER = 2;
        const offersResponse = await request(app)
            .get(`/api/v1/offers?page=${PAGE_NUMBER}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(offersResponse.body.payload.length).toEqual(maxItemsInPage);
        expect(offersResponse.body.payload.at(9).title).toEqual(expect.any(String));
        expect(offersResponse.body.payload.at(9).title).toEqual(expect.stringContaining(`${theNumberOfLastItemInQuery}`));

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
        // instead of removing all 20+ test services, just deop a whole profile and recreate 
        // again based on Jest.beforeAll() implementation
        const eveResponse = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        await request(app)
            .delete(`/api/v1/account/${eveResponse.body.payload.id}`)
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .expect(204);
        let postEveProfile = {"contact":"Eve@gmail.com","secret":"dontbeevilgoogle","name":"Eve","offers":[],"demands":[]};
        let postEveService = { "title" : "Develop software", "date" : 0, "index" : ["Develop software"] };
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postEveProfile)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postEveService)
            .expect(200, {})
    });
});