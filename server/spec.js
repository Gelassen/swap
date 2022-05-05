const request = require('supertest');
const app = require('../server/app');

// beforeAll(() => {
//     jest.setTimeout(10000);
// })

// afterAll(() => {
//     // return app.close();
// })

afterEach(() => {
    // TODO clean database from test data
});

// TODO add production and test environment to run test over separate db instance
// TODO fix an issue with pending async request that prevents test suite to finish

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
        await request(app)
            .get('/api/v1/')
            .expect('Content-Type', 'application/json; charset=utf-8')
            .expect('access-control-allow-origin', '*')
            .expect('access-control-allow-headers', 'Origin, X-Requested-With, Content-Type, Accept')
            .expect(200, { msg: 'Hello to open exchange platform!' });
    });
    it('on GET /api/v1/account receive a non existing profile', async() => {
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(204, {});
    });
    it('on GET /api/v1/account receive an existing profile', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
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
            .expect(200, {})

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
    it ('on GET /api/v1/account without auth header receives UNAUTHORIZED code', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
        // check there is no such profile in system
        await request(app)
            .get('/api/v1/account')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(401, { "payload" : "There is no auth header." });


    });
    it ('on GET /api/v1/account with auth header in wrong format receives BAD_REQUST code', async() => {
        // check there is no such profile in system
        await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'bm9uLmV4aXN0aW5nQGdtYWlsLmNvbQ==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you forget to add auth header?" });
    });
    it ('on GET /api/v1/account without Content-Type header receives OK code for existing profile', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
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
            .expect(200, {})

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
    it('on POST /api/v1/account without 1st mandatory field receives BAD_REQUST code', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","name":"Test James","offers":[],"demands":[]};
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
        let postPayload = {"secret":"jms123","name":"Test James","offers":[],"demands":[]};
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
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
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
            .expect(200, {})
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
    it('on POST /api/v1/profile with existing contact, but different secret receives CONFLICT code', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
        let anotherPostPayload = {"contact":"TestJames@gmail.com","secret":"differentSecret","name":"Test James","offers":[],"demands":[]};
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
            .expect(200, {})
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
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
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
            .expect(200, {})

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
    it('on GET /api/v1/account/offers with any payload receives NOT ALLOWED code', async() => {
        await request(app)
            .get('/api/v1/account/offers')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(405, { "payload" : "Only POST and DELETE requests are allowed for this resource." });
    });
    /* account offers block */
    it('on POST /api/v1/account/offers without authorization header receives UNAUTHORIZED code', async() => {
        let testPayload = {"id":"3","name":"Hacking servers by nights","date":"1746057600","index":["hacking servers"]};
        await request(app)
            .post('/api/v1/account/offers')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(401, { "payload" : "Did you forget to add authorization header?" });
    });
    it('on POST /api/v1/account/offers with authorization in wrong format receives BAD FORMAT code', async() => {
        let testPayload = {"id":"3","name":"Hacking servers by nights","date":"1746057600","index":["hacking servers"]};
        await request(app)
            .post('/api/v1/account/offers')
            .set('Authorization', 'VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(400, { "payload" : "Did you add correct authorization header?"});
    });
    it('on POST /api/v1/account/offers with no authorized account receives NONAUTHORIZED code', async() => {
        let testPayload = {"id":"3","name":"Hacking servers by nights","date":"1746057600","index":["hacking servers"]};
        await request(app)
            .post('/api/v1/account/offers')
            .set('Authorization', 'Basic bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(401, { "payload" : "There is no account for this credentials. Are you authorized?" });
    });
    it('on POST /api/v1/account/offers with authorized account receives OK code', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
        let testPayload = {"title":"Hacking servers by nights","date": 1746057600,"index":["hacking servers"]};
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
            .expect(200, {})
        let response = await request(app)
            .get('/api/v1/account')
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)

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
        // clean database
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    });
    /* account demands block */
    it('on POST /api/v1/account/demands without authorization header receives UNAUTHORIZED code', async() => {
        let testPayload = {"id":"3","name":"Hacking servers by nights","date":"1746057600","index":["hacking servers"]};
        await request(app)
            .post('/api/v1/account/demands')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(401, { "payload" : "Did you forget to add authorization header?" });
    })
    it('on POST /api/v1/account/demands with authorization in wrong format receives BAD FORMAT code', async() => {
        let testPayload = {"id":"3","name":"Hacking servers by nights","date":"1746057600","index":["hacking servers"]};
        await request(app)
            .post('/api/v1/account/demands')
            .set('Authorization', 'VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(400, { "payload" : "Did you add correct authorization header?"});
    });
    it('on POST /api/v1/account/demands with no authorized account receives NONAUTHORIZED code', async() => {
        let testPayload = {"id":"3","name":"Hacking servers by nights","date":"1746057600","index":["hacking servers"]};
        await request(app)
            .post('/api/v1/account/demands')
            .set('Authorization', 'Basic bm9uLmV4aXN0QGdtYWlsLmNvbTpwd2Q=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(testPayload)
            .expect(401, { "payload" : "There is no account for this credentials. Are you authorized?" });
    });
    it('on POST /api/v1/account/demands with authorized account receives OK code', async() => {
        let postPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
        let testPayload = {"title":"Hacking servers by nights","date": 1746057600,"index":["hacking servers"]};
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
            .expect(200, {})
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
        // clean database
        await request(app)
            .delete(`/api/v1/account/${response.body.payload.id}`)
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .expect(204);
    });
});
