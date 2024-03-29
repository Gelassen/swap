const request = require('supertest');
const app = require('../app');
const config = require('config');
const dbConfig = config.dbConfig;

const MAX_PAGE_SIZE = dbConfig.maxPageSize;

beforeEach(async() => {
    let jamesPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James", "userWalletAddress":"0x0007400Ba1B956B11394a5045F8BC3682792E1AC", "offers":[],"demands":[]};
    let jamesTestPayload = {"title":"Software development","date": 1746057600,"chainService" : { "tokenId" : 0, "serverServiceId" : 10, "userWalletAddress" : "0x0007400Ba1B956B11394a5045F8BC3682792E1AC"},"index":["Hacking servers by nights"]};

    let janePayload = {"contact":"TestJane@gmail.com","secret":"jne123","name":"Test Jane","userWalletAddress":"0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd", "offers":[],"demands":[]};
    // add james in system
    await request(app)
        .post('/api/v1/account')
        .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(jamesPayload)
        .expect('Content-Type', 'application/json; charset=utf-8')
        .expect(200)
    await request(app)
        .post('/api/v1/account/offers')    
        .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(jamesTestPayload)
        .expect(200, {})
    let jamesDemandPayload = {"title":"Product management","date": 1746057600,"index":[]};
    await request(app)
        .post('/api/v1/account/demands')    
        .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(jamesDemandPayload)
        .expect(200)
    // add jane in system
    await request(app)
        .post('/api/v1/account')
        .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(janePayload)
        .expect('Content-Type', 'application/json; charset=utf-8')
        .expect(200)
    let janeOfferPayload = {"title":"Product management","date": 1746057600, "chainService" : { "tokenId" : 0, "serverServiceId" : 10, "userWalletAddress" : "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd"}, "index":["Product management"]};
    await request(app)
        .post('/api/v1/account/offers')    
        .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(janeOfferPayload)
        .expect(200)
});

afterEach(async() => {
    // clean database from test data
    const jamesResponse = await request(app)
        .get('/api/v1/account')
        .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
        .expect('Content-Type', 'application/json; charset=utf-8')
        .expect(200);
    await request(app)
        .delete(`/api/v1/account/${jamesResponse.body.payload.id}`)
        .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
        .expect(204);

    const janeResponse = await request(app)
        .get('/api/v1/account')
        .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
        .expect('Content-Type', 'application/json; charset=utf-8')
        .expect(200);
    await request(app)
        .delete(`/api/v1/account/${janeResponse.body.payload.id}`)
        .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
        .expect(204);
});

describe('Test suite to cover match logic', () => {

    it('on POST /api/v1/account/demands with valid payload and existing match, get matches returns single value', async() => {
        // prepare initial database state
        // prepare demands 
        let janeDemandPayload = {"title":"Software development","date": 1746057600, "userWalletAddress":"0x0007400Ba1B956B11394a5045F8BC3682792E1AC", "index":["Software development"]};
        
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(janeDemandPayload)
            .expect(200, {})
        
        let matchResponse = await request(app)
            .get('/api/v1/account/matches?page=1&size=20')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        console.log(`[matchResponse] ${JSON.stringify(matchResponse)}`)
        expect(matchResponse.status).toEqual(200); 
        expect(JSON.parse(matchResponse.text).payload.length).toEqual(1);
    });

    it('on POST /api/v1/account/matches with approve from the first user server returns positive result and query to matches returns response with approved flag', async() => {
        // prepare initial database state
        let janeDemandPayload = {"title":"Software development","date": 1746057600, "userWalletAddress":"0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd", "index":["Software development"]};
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(janeDemandPayload)
            .expect(200, {})
        let matchResponse = await request(app)
            .get('/api/v1/account/matches?page=1&size=20')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        console.log(`matchResponse ${JSON.stringify(matchResponse)}`);
        let matchResponsePayload = JSON.parse(matchResponse.text).payload;
        let janeConfirmPayload = { 
            "userFirst" : matchResponsePayload[0].userFirstProfileId, 
            "userSecond" : matchResponsePayload[0].userSecondProfileId,
            "valueOfFirstUser" : 1001,
            "valueOfSecondUser" : 1002,
            "approvedByFirstUser" : true,
            "approvedBySecondUser" : matchResponsePayload[0].approvedBySecondUser,
            "userFirstServiceId" : matchResponsePayload[0].userFirstServiceId,
            "userSecondServiceId" : matchResponsePayload[0].userSecondServiceId
        }
        expect(matchResponsePayload[0].approvedByFirstUser).toEqual(false);
        
        await request(app)
            .post('/api/v1/account/matches?page=1&size=20')    
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(janeConfirmPayload)
            .expect(200)

        let matchSecondResponse = await request(app)
            .get('/api/v1/account/matches?page=1&size=20')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        let matchSecondResponsePayload = JSON.parse(matchSecondResponse.text).payload; 
        expect(matchSecondResponsePayload[0].approvedByFirstUser).toEqual(true);
    });

    it('on POST /api/v1/account/matches with approve from the first and the second users server returns positive result and query to matches returns response with both approved flag', async() => {
        // prepare initial database state
        let janeDemandPayload = {"title":"Software development","date": 1746057600, "userWalletAddress" : "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd", "index":["Software development"]};
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(janeDemandPayload)
            .expect(200, {})
        let matchResponse = await request(app)
            .get('/api/v1/account/matches?page=1&size=20')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        console.log(`matchResponse ${JSON.stringify(matchResponse)}`);
        let matchResponsePayload = JSON.parse(matchResponse.text).payload;
        let janeConfirmPayload = { 
            "userFirst" : matchResponsePayload[0].userFirstProfileId, 
            "userSecond" : matchResponsePayload[0].userSecondProfileId,
            "valueOfFirstUser" : 1001,
            "valueOfSecondUser" : 1002,
            "approvedByFirstUser" : true,
            "approvedBySecondUser" : matchResponsePayload[0].approvedBySecondUser,
            "userFirstServiceId" : matchResponsePayload[0].userFirstServiceId,
            "userSecondServiceId" : matchResponsePayload[0].userSecondServiceId
        }
        expect(matchResponsePayload[0].approvedByFirstUser).toEqual(false);
        let jamesConfirmPayload = { 
            "userFirst" : matchResponsePayload[0].userFirstProfileId, 
            "userSecond" : matchResponsePayload[0].userSecondProfileId,
            "valueOfFirstUser" : 1001,
            "valueOfSecondUser" : 1002,
            "approvedByFirstUser" : false,
            "approvedBySecondUser" : true,
            "userFirstServiceId" : matchResponsePayload[0].userFirstServiceId,
            "userSecondServiceId" : matchResponsePayload[0].userSecondServiceId
        }
        expect(matchResponsePayload[0].approvedBySecondUser).toEqual(false);
        
        await request(app)
            .post('/api/v1/account/matches?page=1&size=20')    
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(janeConfirmPayload)
            .expect(200)
        await request(app)
            .post('/api/v1/account/matches?page=1&size=20')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(jamesConfirmPayload)
            .expect(200)
        // TODO finish test case
        let matchSecondResponse = await request(app)
            .get('/api/v1/account/matches?page=1&size=20')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        let matchSecondResponsePayload = JSON.parse(matchSecondResponse.text).payload; 
        console.log(`[debug] matchSecondResponsePayload ${JSON.stringify(matchSecondResponsePayload)}`)
        expect(matchSecondResponsePayload[0].approvedByFirstUser).toEqual(true);
        expect(matchSecondResponsePayload[0].approvedBySecondUser).toEqual(true);
    });

    it('on GET /api/v1/account/matches with valid scenario receives match object with ids and all required fields', async() => {
        // prepare initial database state
        let janeDemandPayload = {"title":"Software development","date": 1746057600, "userWalletAddress":"0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd", "index":["Software development"]};
        await request(app)
            .post('/api/v1/account/demands')    
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(janeDemandPayload)
            .expect(200, {})

        let matchResponse = await request(app)
            .get('/api/v1/account/matches?page=1&size=20')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200);
        console.log(`matchResponse ${JSON.stringify(matchResponse)}`);

        let text = JSON.parse(matchResponse.text); 
        let matches = text.payload
        expect(matches.length).toEqual(1); 
        let match = matches[0];
        expect(match.id).toBeDefined(); //.not.toBeUndefined();
        expect(match.userFirstProfileId).toBeDefined();//.not.toBeUndefined();
        expect(match.userSecondProfileId).toBeDefined();//.not.toBeUndefined();
        expect(match.userFirstServiceId).toBeDefined();//.not.toBeUndefined();
        expect(match.userSecondServiceId).toBeDefined();
        expect(match.approvedByFirstUser).toBeDefined();
        expect(match.approvedBySecondUser).toBeDefined();

        expect(match.userFirstProfileName).toBeDefined();
        expect(match.userSecondProfileName).toBeDefined();
        expect(match.userFirstService.idChainService).toBeDefined();
        expect(match.userFirstService.userWalletAddress).toBeDefined();
        expect(match.userFirstService.serverServiceId).toBeDefined();
        expect(match.userFirstServiceTitle).toEqual("Product management");
        expect(match.userSecondService.idChainService).toBeDefined();
        expect(match.userSecondService.userWalletAddress).toBeDefined();
        expect(match.userSecondService.serverServiceId).toBeDefined();
        expect(match.userSecondServiceTitle).toEqual("Software development");
    });

    it('on GET /api/v1/account/matches with missed page and size on the url query receives error code', async() => {
        await request(app)
            .get('/api/v1/account/matches')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you forget to pass page in query, e.g. ?page=1 ?"});
    });

    it('on GET /api/v1/account/matches with missed size on the url query receives error code', async() => {
        await request(app)
            .get('/api/v1/account/matches?page=1')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you pass page size? Maximum values per page is 100"});
    });

    it('on GET /api/v1/account/matches with missed page on the url query receives error code', async() => {
        await request(app)
            .get('/api/v1/account/matches?size=20')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(400, { "payload" : "Did you forget to pass page in query, e.g. ?page=1 ?"});
    });
})