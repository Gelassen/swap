const request = require('supertest');
const app = require('../app');

// test user: TestJames@gmail.com:jms123 (VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=)
// test user: TestJane@gmail.com:jne123 (VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==)
// test non in db user: non.exist@gmail.com:pwd (bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=)

describe('Test suite to cover GET and POSTS under different conditions', () => {
    it.skip('test', async() => {
        await request(app)
            .get('/api/v1')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, {msg: "Hello to open exchange platform!"})
    })
    it('on GET /api/v1 receive a welcome message', async() => {
        var responseMsg = "Hello to open exchange platform! \n"
            + "POST GET /account \n"
            + "DELETE /account/:id \n"
            + "POST GET /account/offers \n"
            + "POST /account/demands \n"
            + "DELETE /account/offers/:id \n"
            + "DELETE /account/demands/:id \n"
            + "GET /offers \n"
            + "GET /demands \n"
            + "GET /contacts \n"
            + 'GET /account/matches \n' 
            + 'POST /account/matches \n'
        await request(app)
            .get('/api/v1/')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect('access-control-allow-origin', '*')
            .expect('access-control-allow-headers', 'Origin, X-Requested-With, Content-Type, Accept')
            .expect(200, { msg: responseMsg });
    });
    it('on GET /api/v1/account receive a non existing profile', async() => {
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
    });
    it('on GET /api/v1/account receive an existing profile', async() => {
        let postPayload = getJamesAccountPayload(); 
        // check there is no such profile in system
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload });

        const response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
    
        postPayload.id = response.body.payload.id;
        let getPayload = { 'payload' : postPayload };
        expect(response.body).toEqual(getPayload);

        // clean database from test data
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204);
    });
    it('on GET /api/v1/account without auth header receives UNAUTHORIZED code', async() => {
        let postPayload = getJamesAccountPayload(); 
        // check there is no such profile in system
        await request(app)
            .get('/api/v1/account')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(401, { "payload" : "There is no auth header." });
    });
    it('on GET /api/v1/account with auth header in wrong format receives BAD_REQUST code', async() => {
        // check there is no such profile in system
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'bm9uLmV4aXN0aW5nQGdtYWlsLmNvbQ==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you forget to add auth header?" });
    });
    it('on GET /api/v1/account without Content-Type header receives OK code for existing profile', async() => {
        let postPayload = getJamesAccountPayload(); 
        // check there is no such profile in system
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })

        const response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
    
        postPayload.id = response.body.payload.id;
        let getPayload = { 'payload' : postPayload };
        expect(response.body).toEqual(getPayload);

        // clean database from test data
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    });
    it('on GET /api/v1/account with account with two offers receives account with two offers', async() => {
        let postPayload = getJamesAccountPayload(); 
        let testPayload = getOfferPayload(); 
        let anotherTestPayload = getAnotherOfferPayload(); 
        // prepare initial database state
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(200, {})
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(anotherTestPayload)
            .expect(200, {})

        const response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(response.body.payload.offers.length).toEqual(2);
        
        // clean database
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    });
    it('on POST /api/v1/account without 1st mandatory field receives BAD_REQUEST code', async() => {
        // do not move in the general class -- it is malformed request
        let postPayload = { "contact":"", "secret":"jms123", "name":"Test James", "userWalletAddress":"0xB54e15454E0711b1917f88E656C2fc3E9dF4117d", "offers":[], "demands":[]};
        // check there is no such profile in system
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
        
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you forget to add a valid profile as a payload?" });

        // check there is no newly created data in system
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    });
    it('on POST /api/v1/account without 2nd mandatory field receives BAD_REQUST code', async() => {
        // do not move in the general class -- it is malformed request
        let postPayload = { "contact":"TestJames@gmail.com", "secret":"", "name":"Test James", "userWalletAddress":"0xB54e15454E0711b1917f88E656C2fc3E9dF4117d", "offers":[], "demands":[]};
        // check there is no such profile in system
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);

        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you forget to add a valid profile as a payload?" });

        // check there is no newly created data in system
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    });
    it('on POST /api/v1/acount with existing profiles is considered as a login and no double extra profile in system', async() => {
        let postPayload = getJamesAccountPayload(); 
        // check there is no such profile in system
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);

        // check the same request is considered as a login and no extra profile in system  
        let secondPostResponse = await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect(200)
        postPayload.id = secondPostResponse.body.payload.id;
        expect(secondPostResponse.body.payload).toEqual(postPayload);
        await request(app)
            .delete(`/api/v1/account/${secondPostResponse.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    });
    it('on POST /api/v1/account with existing contact, but different secret receives CONFLICT code', async() => {
        let postPayload = getJamesAccountPayload(); 
        let anotherPostPayload = getJamesAccountPayload("123jms"); 
        // check there is no such profile in system
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);

        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpkaWZmZXJlbnRTZWNyZXQ=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(anotherPostPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(409, { "payload" : "There is an account with contact TestJames@gmail.com"})
        await request(app)
            .get('/api/v1/account')
            .set('Content-Type', 'application/json; charset=utf-8')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpkaWZmZXJlbnRTZWNyZXQ=')
            .expect(204);

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
    it('on POST /api/v1/account with valid non-existing payload receives OK code', async() => {
        let postPayload = getJamesAccountPayload(); 
        // check there is no such profile in system
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});

        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })

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
    /* account offers block */
    it('on GET /api/v1/account/offers with any payload receives NOT ALLOWED code', async() => {
        await request(app)
            .get('/api/v1/account/offers')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(405, { "payload" : "Only POST and DELETE requests are allowed for this resource." });
    });
    it('on POST /api/v1/account/offers without authorization header receives UNAUTHORIZED code', async() => {
        let testPayload = getOfferPayload(); 
        await request(app)
            .post('/api/v1/account/offers')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(401, { "payload" : "Did you forget to add authorization header?" });
    });
    it('on POST /api/v1/account/offers with authorization in wrong format receives BAD FORMAT code', async() => {
        let testPayload = getOfferPayload(); 
        await request(app)
            .post('/api/v1/account/offers')
            .set('Authorization', 'VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(400, { "payload" : "Did you add correct authorization header?"});
    });
    it('on POST /api/v1/account/offers with no authorized account receives UNAUTHORIZED code', async() => {
        let testPayload = getOfferPayload(); 
        await request(app)
            .post('/api/v1/account/offers')
            .set('Authorization', 'Basic bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(401, { "payload" : "There is no account for this credentials. Are you authorized?" });
    });
    it('on POST /api/v1/account/offers with authorized account receives OK code and full updated profile', async() => {
        let postPayload = getJamesAccountPayload(); 
        let testPayload = getOfferPayload(); 
        // prepare initial database state
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        let response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)

        response = await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(200);

        response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        postPayload.id = response.body.payload.id;
        testPayload.id = response.body.payload.offers.at(0).id;
        postPayload.offers.push(testPayload);
        const newExpectedObj = postPayload;
        expect(response.body.payload).toEqual(newExpectedObj);
        // clean database
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    });
    it('on POST /api/v1/account/offers with payload profile that already exist in system receives CONFLICT code', async() => {
        let postPayload = getJamesAccountPayload(); 
        let testPayload = getOfferPayload(); 
        // prepare initial database state
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        let response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        postPayload.id = response.body.payload.id;
        expect(response.body.payload).toEqual(postPayload);
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(200, {})
        response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        postPayload.id = response.body.payload.id;
        testPayload.id = response.body.payload.offers.at(0).id;
        postPayload.offers.push(testPayload);
        const newExpectedObj = postPayload;
        expect(response.body.payload).toEqual(newExpectedObj);

        testPayload.id = undefined;
        const secondPostRequest = await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(409, { "payload" : `The same offer for this profile already exist ${JSON.stringify(testPayload)}`});
        
        // clean database
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    })
    it('on POST /api/v1/account/offers with two consecutive requests with different offers receives OK code', async() => {
        let postPayload = getJamesAccountPayload(); 
        let testPayload = getOfferPayload(); 
        let anotherTestPayload = getAnotherOfferPayload(); 
        // prepare initial database state
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        let response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        postPayload.id = response.body.payload.id;
        expect(response.body.payload).toEqual(postPayload);
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(200, {})
        response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        postPayload.id = response.body.payload.id;
        testPayload.id = response.body.payload.offers.at(0).id;
        postPayload.offers.push(testPayload);
        const newExpectedObj = postPayload;
        expect(response.body.payload).toEqual(newExpectedObj);

        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(anotherTestPayload)
            .expect(200, {})

        // clean database
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    });
    it('on DELETE /api/v1/account/offers without authorization header receives UNAUTHORIZED code', async() => {
        await request(app)
            .delete('/api/v1/account/offers/101')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(401, { "payload" : "Did you forget to add authorization header?" });
    });
    it('on DELETE /api/v1/account/offers with authorization in wrong format receives BAD FORMAT code', async() => {
        await request(app)
            .delete('/api/v1/account/offers/101')
            .set('Authorization', 'VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you add correct authorization header?"});
    });
    it('on DELETE /api/v1/account/offers no authorized account receives UNAUTHORIZED code', async() => {
        let postPayload = getJamesAccountPayload();
        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })

        await request(app)
            .delete('/api/v1/account/offers/101')
            .set('Authorization', 'Basic bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(401, { "payload" : "There is no account for this credentials. Are you authorized?" });
    
        // clean database from test data
        response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    })
    it('on DELETE /api/v1/account/offers for service which doesn\'t exist receives NOT FOUND code', async() => {
        let postPayload = getJamesAccountPayload(); 
        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        let response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(response.body.payload.offers.length).toEqual(0);

        await request(app)
            .delete('/api/v1/account/offers/101')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(404, { "payload" : `There is an account for this credentials, but there is no offer in for this id 101` } );
        
        // clean database from test data
        response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    })
    it('on DELETE /api/v1/account/offers for service and profile that are not exist receives UNAUTHORIZED CODE', async() => {
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=')
            .expect(204, {});
        
        await request(app)
            .delete('/api/v1/account/offers/101')
            .set('Authorization', 'Basic bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(401, { "payload" : "There is no account for this credentials. Are you authorized?" } );
    });
    it('on DELETE /api/v1/account/offers with existing profile and correct service id receives NO CONTENT code', async() => {
        let postPayload = getJamesAccountPayload(); 
        let testPayload = getOfferPayload(); 
        // prepare the initial database state
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(200, {})
        const response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(response.body.payload.offers.length).toEqual(1);

        await request(app)
            .delete(`/api/v1/account/offers/${response.body.payload.offers.at(0).id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204)
        const getAccountResponse = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(getAccountResponse.body.payload.offers.length).toEqual(0);
        
        // clean database from test data
        await request(app)
            .delete(`/api/v1/account/${getAccountResponse.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);        
    });
    it('on DELETE /api/v1/account/offers with existing profiles and several existing offers returns NO CONTENT and next request to profile returns profile without offer that has been deleted', async() => {
        let postPayload = getJamesAccountPayload(); 
        let testPayload = getOfferPayload(); 
        let anotherTestPayload = getAnotherOfferPayload(); 
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(200, {})
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(anotherTestPayload)
            .expect(200, {})
        let response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);

        let offer = response.body.payload.offers.at(0);
        await request(app)
            .delete(`/api/v1/account/offers/${offer.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
        const getAccountResponse = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(getAccountResponse.body.payload.offers.length).toEqual(1);
        expect(getAccountResponse.body.payload.offers.at(0).title).not.toEqual(offer.title);
        
        // clean database from test data
        await request(app)
            .delete(`/api/v1/account/${getAccountResponse.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);    
    });
    /* account demands block */
    it('on POST /api/v1/account/demands without authorization header receives UNAUTHORIZED code', async() => {
        let testPayload = getOfferPayload(); 
        await request(app)
            .post('/api/v1/account/demands')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(401, { "payload" : "Did you forget to add authorization header?" });
    })
    it('on POST /api/v1/account/demands with authorization in wrong format receives BAD FORMAT code', async() => {
        let testPayload = getOfferPayload(); 
        await request(app)
            .post('/api/v1/account/demands')
            .set('Authorization', 'VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(400, { "payload" : "Did you add correct authorization header?"});
    });
    it('on POST /api/v1/account/demands with no authorized account receives UNAUTHORIZED code', async() => {
        let testPayload = getOfferPayload(); 
        await request(app)
            .post('/api/v1/account/demands')
            .set('Authorization', 'Basic bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(401, { "payload" : "There is no account for this credentials. Are you authorized?" });
    });
    it('on POST /api/v1/account/demands with authorized account receives OK code', async() => {
        let postPayload = getJamesAccountPayload(); 
        let testPayload = getJamesDemandPayload(); 
        // prepare initial database state
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        let response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)

        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(200, {})
        console.log(`[debug] received response from register demand`)

        response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        postPayload.id = response.body.payload.id;
        testPayload.id = response.body.payload.demands.at(0).id;
        postPayload.demands.push(testPayload);
        const newExpectedObj = postPayload;
        expect(response.body.payload).toEqual(newExpectedObj);
        console.log(`[debug] ${JSON.stringify(response.body)}`)
        // clean database
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    });
    it('on POST /api/v1/account/demands with payload profile that already exist in system receives CONFLICT code', async() => {
        let postPayload = getJamesAccountPayload(); 
        let testPayload = getJamesDemandPayload(); 
        // prepare initial database state
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        let response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        postPayload.id = response.body.payload.id;
        expect(response.body.payload).toEqual(postPayload);
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(200, {})
        response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        postPayload.id = response.body.payload.id;
        testPayload.id = response.body.payload.demands.at(0).id;
        postPayload.demands.push(testPayload);
        const newExpectedObj = postPayload;
        expect(response.body.payload).toEqual(newExpectedObj);

        testPayload.id = undefined;
        const secondPostRequest = await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(409, { "payload" : `The same demand for this profile already exist ${JSON.stringify(testPayload)}`});
        
        // clean database
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    });
    it('on POST /api/v1/account/demands with demand value which requires escaping receives OK code', async() => {
        let postPayload = getJamesAccountPayload(); 
        let testPayload = getJamesDemandPayload(); 
        testPayload.title = "Farmer's products";
        // prepare initial database state
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        let response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        postPayload.id = response.body.payload.id;
        expect(response.body.payload).toEqual(postPayload);
        
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(200, {})
        response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        
        // clean database
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    });
    // ethereum chain is not mocked, therefore there should be a full integration test
    it.skip('on POST /api/v1/account/demands with valid payload and existing match, get matches returns single value', async() => {
        // prepare initial database state
        // prepare james offer and demand 
        let jamesPayload = getJamesAccountPayload(); 
        let jamesOfferPayload = getOfferPayload("Software development"); 
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(jamesPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : jamesPayload })
        let responseJames = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(jamesOfferPayload)
            .expect(200);
        let jamesDemandResponse = getJamesDemandPayload(); 
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(jamesDemandResponse)
            .expect(200);
        // prepare jane demand 
        let janePayload = getJaneAccountPayload() 
        let janeOfferPayload = getJaneOfferPayload() 
        let janeDemandPayload = getJaneDemandPayload();
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(janePayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : janePayload })
        let responseJane = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        await request(app)
            .post('/api/v1/account/offers')    
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(janeOfferPayload)
            .expect(200)

        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(janeDemandPayload)
            .expect(200, {})
        
        let matchResponse = await request(app)
            .get('/api/v1/account/matches')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        expect(matchResponse.body.payload.length).toEqual(1);
        // clean database
        await request(app)
            .delete(`/api/v1/account/${responseJames.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204); 
        await request(app)
            .delete(`/api/v1/account/${responseJane.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .expect(204); 
        // TODO clean associated tables like service and matches (CASCADE trigger) 
    });
    /* 
        it is better to omit integration test as it would require implementation of extra methods,
        test this scenario manually from two emulators
    it('on POST /api/v1/account/demands integration test with valid payload and existing match, get matches returns single value', async() => {
        
    }) */
    it('on DELETE /api/v1/account/demands without authorization header receives UNAUTHORIZED code', async() => {
        await request(app)
            .delete('/api/v1/account/demands/101')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(401, { "payload" : "Did you forget to add authorization header?" });
    });
    it('on DELETE /api/v1/account/demands with authorization in wrong format receives BAD FORMAT code', async() => {
        await request(app)
            .delete('/api/v1/account/demands/101')
            .set('Authorization', 'VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you add correct authorization header?"});
    });
    it('on DELETE /api/v1/account/demands no authorized account receives UNAUTHORIZED code', async() => {
        let postPayload = getJamesAccountPayload(); 
        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })

        await request(app)
            .delete('/api/v1/account/demands/101')
            .set('Authorization', 'Basic bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(401, { "payload" : "There is no account for this credentials. Are you authorized?" });
    
        // clean database from test data
        response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    })
    it('on DELETE /api/v1/account/demands for service which doesn\'t exist receives NOT FOUND code', async() => {
        let postPayload = getJamesAccountPayload(); 
        // add account in system
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        let response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(response.body.payload.demands.length).toEqual(0);

        await request(app)
            .delete('/api/v1/account/demands/101')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(404, { "payload" : `There is an account for this credentials, but there is no demand in for this id 101` } );
        
        // clean database from test data
        response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    })
    it('on DELETE /api/v1/account/demands for service and profile that are not exist receives UNAUTHORIZED CODE', async() => {
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=')
            .expect(204, {});
        
        await request(app)
            .delete('/api/v1/account/demands/101')
            .set('Authorization', 'Basic bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(401, { "payload" : "There is no account for this credentials. Are you authorized?" } );
    });
    it('on DELETE /api/v1/account/demands with existing profile and correct service id receives NO CONTENT code', async() => {
        let postPayload = getJamesAccountPayload(); 
        let testPayload = getOfferPayload();
        // prepare the initial database state
        await request(app)
            .post('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(postPayload)
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200, { "payload" : postPayload })
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(200, {})
        const response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(response.body.payload.demands.length).toEqual(1);

        await request(app)
            .delete(`/api/v1/account/demands/${response.body.payload.demands.at(0).id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204)
        const getAccountResponse = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        expect(getAccountResponse.body.payload.demands.length).toEqual(0);
        
        // clean database from test data
        await request(app)
            .delete(`/api/v1/account/${getAccountResponse.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);        
    });

    function getJamesAccountPayload(secret = "jms123") {
        return {
            "contact":"TestJames@gmail.com",
            "secret":secret,
            "name":"Test James", 
            "userWalletAddress":"0xB54e15454E0711b1917f88E656C2fc3E9dF4117d", 
            "offers":[],
            "demands":[]
        };
    }

    function getOfferPayload(title = "Hacking servers by nights") {
        return {
            "title":title,
            "date": 1746057600,
            "index":[title],
            "chainService" : {
                "userWalletAddress" : "0xB54e15454E0711b1917f88E656C2fc3E9dF4117d",
                "tokenId" : 1,
                /* "serverServiceId" : 0 -- it will be added on the backend */
            }
        };
    }

    function getAnotherOfferPayload(title = "Writing software by day") {
        return {
            "title": title,
            "date": 1746057600,
            "index":[title],
            "chainService" : {
                "userWalletAddress" : "0xB54e15454E0711b1917f88E656C2fc3E9dF4117d",
                "tokenId" : 2,
                /* "serverServiceId" : 0 -- it will be added on the backend */
            }
        };
    }

    function getJamesDemandPayload() {
        return {"title":"Product management","date": 1746057600,"index":["Product management"]};
    }

    function getJaneAccountPayload() {
        return {
            "contact":"TestJane@gmail.com",
            "secret":"jne123",
            "name":"Test Jane",
            "userWalletAddress":"0xB54e15454E0711b1917f88E656C2fc3E9dF40000",
            "offers":[],
            "demands":[]
        };
    }

    function getJaneOfferPayload() {
        return {
            "title":"Product management",
            "date": 1746057600,
            "index":["Product management"],
            "chainService" : {
                "userWalletAddress" : "0xB54e15454E0711b1917f88E656C2fc3E9dF40000",
                "tokenId" : 11,
                /* "serverServiceId" : 0 -- it will be added on the backend */
            }
        };
    }

    function getJaneAnotherOfferPayload() {
        return {
            "title":"Project management",
            "date": 1746057600,
            "index":["Project management"],
            "chainService" : {
                "userWalletAddress" : "0xB54e15454E0711b1917f88E656C2fc3E9dF40000",
                "tokenId" : 12,
                /* "serverServiceId" : 0 -- it will be added on the backend */
            }
        };
    }

    function getJaneDemandPayload() {
        return {
            "title":"Software development",
            "date": 1746057600,
            "index":["Software development"]
        };
    }
});
