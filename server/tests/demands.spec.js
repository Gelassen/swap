const request = require('supertest');
const app = require('../app');
const config = require('config');
const dbConfig = config.dbConfig;

const MAX_PAGE_SIZE = dbConfig.maxPageSize;

/**
 *  Bob@gmail.com:bupa (Basic Qm9iQGdtYWlsLmNvbTpidXBh)
 *  Eve@gmail.com:dontbeevilgoogle (Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl)
 */

beforeAll(async() => {
    let postBobProfile = {"contact":"Bob@gmail.com","secret":"bupa","name":"Bob", "userWalletAddress":"0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd", "offers":[],"demands":[]};
    let postBobService = { "title" : "Play tennis", "date" : 0, "chainService" : { "tokenId" : 0, "serverServiceId" : 10, "userWalletAddress" : "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd"}, "index" : ["Play tennis"] };
    
    let postEveProfile = {"contact":"Eve@gmail.com","secret":"dontbeevilgoogle","name":"Eve", "userWalletAddress":"0x52E7400Ba1B956B11394a5045F8BC3682792E1AC", "offers":[],"demands":[]};
    let postEveService = { "title" : "Develop software", "date" : 0, "chainService" : { "tokenId" : 1, "serverServiceId" : 11, "userWalletAddress" : "0x52E7400Ba1B956B11394a5045F8BC3682792E1AC"}, "index" : ["Develop software"] };
    // add account in system
    await request(app)
        .post('/api/v1/account')
        .set('Authorization', 'Basic Qm9iQGdtYWlsLmNvbTpidXBh')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(postBobProfile)
        .expect('Content-Type', 'application/json; charset=utf-8')
        .expect(200)
    await request(app)
        .post('/api/v1/account/demands')    
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
        .post('/api/v1/account/demands')    
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
});

describe('Cover /api/v1/demands with tests', () => {
    it('On GET /api/v1/demands without Auth header receives BAD_REQUEST code', async() => {
        await request(app)
            .get('/api/v1/demands')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you add auth header?" })
    });
    it('On GET /api/v1/demands with malformed auth header receives BAD_REQUEST code', async() => {
        await request(app)
            .get('/api/v1/demands')
            .set('Authorization', 'VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you forget to add auth header?" })
    });
    it('On GET /api/v1/demands with non existing account receives BAD_REQUEST code', async() => {
        await request(app)
            .get('/api/v1/demands?page=1&size=10')
            .set('Authorization', 'Basic bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "There is no such account." });
    });
    it('On GET /api/v1/demands with existing account but empty offers receives OK code with error message', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James", "userWalletAddress":"0x0008DC8a5c80db6e8FCc042f0cC54a298F8F2FFd", "offers":[],"demands":[]};
        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)

        await request(app)
            .get('/api/v1/demands?page=1&size=10')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : [], "msg" : "Profile doesn't have any offers. In this case there is no filter to match demands." });

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
    it('On GET /api/v1/demands with existing account but zero matches receives OK code with empty payload', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC", "offers":[],"demands":[]};
        let postServicePayload = { "title" : "there is no match for this offer", "date" : 0, "chainService" : { "tokenId" : 1, "serverServiceId" : 11, "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC"}, "index" : ["there is no match for this offer"]};
        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postServicePayload)
            .expect(200, {})

        await request(app)
            .get('/api/v1/demands?page=1&size=10')
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
    it('On GET /api/v1/demands with existing account and available matches receives OK code with services', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James", "userWalletAddress":"0x0007400Ba1B956B11394a5045F8BC3682792E1AC", "offers":[],"demands":[]};
        let postServicePayload = { "id" : 101, "title" : "Develop software", "date" : 0, "chainService" : { "tokenId" : 1, "serverServiceId" : 101, "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC"}, "index" : ["Develop software"]};
        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postServicePayload)
            .expect(200, {})

        const demandsResponse = await request(app)
            .get('/api/v1/demands?page=1&size=10')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        console.log("Demands response: " + JSON.stringify(demandsResponse));
        expect(demandsResponse.body.payload.length).toEqual(1);
        postServicePayload.id = demandsResponse.body.payload.at(0).id;
        delete postServicePayload["chainService"];
        expect(demandsResponse.body.payload.at(0)).toEqual(postServicePayload);

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
    it('On GET /api/v1/demands with existing account and two available matches receives OK code with 2 services', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James", "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC", "offers":[],"demands":[]};
        let postServicePayload = { "title" : "Develop software", "date" : 0, "chainService" : { "tokenId" : 1, "serverServiceId" : 101, "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC"}, "index" : ["Develop software"]};
        let postAnotherService = { "title" : "Play tennis", "date" : 0, "chainService" : { "tokenId" : 2, "serverServiceId" : 102, "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC"}, "index" : ["Play tennis"] };
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postServicePayload)
            .expect(200, {})
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postAnotherService)
            .expect(200, {})

        const demandsResponse = await request(app)
            .get('/api/v1/demands?page=1&size=10')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(demandsResponse.body.payload.length).toEqual(2);
        postAnotherService.id = demandsResponse.body.payload.at(0).id;
        postServicePayload.id = demandsResponse.body.payload.at(1).id;
        delete postAnotherService["chainService"];
        delete postServicePayload["chainService"];
        expect(demandsResponse.body.payload.at(0)).toEqual(postAnotherService);
        expect(demandsResponse.body.payload.at(1)).toEqual(postServicePayload);

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
    it('On GET /api/v1/demands without page query passed receives BAD_REQUEST with error message', async() => {
        await request(app)
            .get('/api/v1/demands')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you forget to pass page in query, e.g. ?page=1 ?"});
    });
    it('On GET /api/v1/demands?page=1 with existing account and 10+ available matches receives OK and only first 10', async() => {
        // prepare test data
        const theNumberOfLastItemInQuery = '9';
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James", "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC", "offers":[],"demands":[]};
        let postServicePayload = { "title" : "Develop software", "date" : 0, "chainService" : { "tokenId" : 1, "serverServiceId" : 101, "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC"}, "index" : ["Develop software"]};
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postServicePayload)
            .expect(200, {})
        const matchServiceCount = 20;
        for (id = 0; id < matchServiceCount; id++) {
            let postAnotherService = { "title" : `Play tennis ${id}`, "date" : 0, "chainService" : { "tokenId" : `${id}`, "serverServiceId" : `${id}`, "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC"}, "index" : [`Play tennis ${id}`] };
            await request(app)
                .post('/api/v1/account/offers')    
                .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
                .set('Content-Type', 'application/json; charset=utf-8')
                .send(postAnotherService)
                .expect(200, {})
            await request(app)
                .post('/api/v1/account/demands')    
                .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
                .set('Content-Type', 'application/json; charset=utf-8')
                .send(postAnotherService)
                .expect(200, {})
        }

        const PAGE_SIZE = 10;
        const offersResponse = await request(app)
            .get(`/api/v1/demands?page=1&size=${PAGE_SIZE}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(offersResponse.body.payload.length).toEqual(PAGE_SIZE);
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
        // instead of removing all 20+ test services, just delete a whole profile and recreate 
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
        let postEveProfile = {"contact":"Eve@gmail.com","secret":"dontbeevilgoogle","name":"Eve", "userWalletAddress" : "0x52E7400Ba1B956B11394a5045F8BC3682792E1AC", "offers":[],"demands":[]};
        let postEveService = { "title" : "Develop software", "date" : 0, "index" : ["Develop software"] };
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postEveProfile)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postEveService)
            .expect(200, {})
    });
    it('On GET /api/v1/demands?page=2 with existing account and 10+ available matches receives OK and only second 10', async() => {
        //verify database is not in dirty state
        const eveAccountResponse = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(eveAccountResponse.body.payload.demands.length).toEqual(1);
        // prepare initial state
        const theNumberOfLastItemInQuery = '19';
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James", "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC", "chainService" : { "tokenId" : 1, "serverServiceId" : 101, "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC"}, "offers":[],"demands":[]};
        let postServicePayload = { "title" : "Develop software", "date" : 0, "chainService" : { "tokenId" : 1, "serverServiceId" : 101, "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC"}, "index" : ["Develop software"]};
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postServicePayload)
            .expect(200, {})
        const matchServiceCount = 20;
        for (id = 1; id <= matchServiceCount; id++) {
            let postAnotherService = { "title" : `Play tennis ${id}`, "date" : 0, "chainService" : { "tokenId" : `${id}`, "serverServiceId" : `${id}`, "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC"}, "index" : [`Play tennis ${id}`] };
            await request(app)
                .post('/api/v1/account/offers')    
                .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
                .set('Content-Type', 'application/json; charset=utf-8')
                .send(postAnotherService)
                .expect(200, {})
            await request(app)
                .post('/api/v1/account/demands')    
                .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
                .set('Content-Type', 'application/json; charset=utf-8')
                .send(postAnotherService)
                .expect(200, {})
        }

        const PAGE_NUMBER = 2;
        const PAGE_SIZE = 10;
        const offersResponse = await request(app)
            .get(`/api/v1/demands?page=${PAGE_NUMBER}&size=${PAGE_SIZE}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(offersResponse.body.payload.length).toEqual(PAGE_SIZE);
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
        let postEveProfile = {"contact":"Eve@gmail.com","secret":"dontbeevilgoogle","name":"Eve", "userWalletAddress" : "0x52E7400Ba1B956B11394a5045F8BC3682792E1AC", "offers":[],"demands":[]};
        let postEveService = { "title" : "Develop software", "date" : 0, "index" : ["Develop software"] };
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postEveProfile)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic RXZlQGdtYWlsLmNvbTpkb250YmVldmlsZ29vZ2xl')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postEveService)
            .expect(200, {})
    });
    it('On GET /api/v1/demands?page=1 without page size receives BAD_REQUEST status code', async() => {
        // prepare initial state
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James", "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC", "offers":[],"demands":[]};
        let postServicePayload = { "title" : "Develop software", "date" : 0, "chainService" : { "tokenId" : 1, "serverServiceId" : 101, "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC"}, "index" : ["Develop software"]};
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postServicePayload)
            .expect(200, {})

        const PAGE_NUMBER = 1;
        await request(app)
            .get(`/api/v1/demands?page=${PAGE_NUMBER}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : `Did you pass page size? Maximum values per page is ${MAX_PAGE_SIZE}` });

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
    it('On GET /api/v1/demands?page=1&size=101 with out of range page size receives BAD_REQUEST status code', async() => {
        // prepare initial state
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James", "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC", "offers":[],"demands":[]};
        let postServicePayload = { "title" : "Develop software", "date" : 0, "chainService" : { "tokenId" : 1, "serverServiceId" : 101, "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC"}, "index" : ["Develop software"]};
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postServicePayload)
            .expect(200, {})

        const PAGE_NUMBER = 1;
        const OUT_OF_RANGE_PAGE_SIZE = 101;
        await request(app)
            .get(`/api/v1/demands?page=${PAGE_NUMBER}&size=${OUT_OF_RANGE_PAGE_SIZE}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : `Did you pass page size within allowed range? Maximum items per page is ${MAX_PAGE_SIZE}` });

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
});
