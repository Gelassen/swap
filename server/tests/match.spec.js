const request = require('supertest');
const app = require('../app');
const config = require('config');
const dbConfig = config.dbConfig;

const MAX_PAGE_SIZE = dbConfig.maxPageSize;

beforeAll(async() => {
    let jamesPayload = {"contact":"TestJames@gmail.com","secret":"jms123","name":"Test James","offers":[],"demands":[]};
    let jamesTestPayload = {"title":"Software development","date": 1746057600,"index":["Hacking servers by nights"]};

    let janePayload = {"contact":"TestJane@gmail.com","secret":"jne123","name":"Test Jane","offers":[],"demands":[]};
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
    let janeOfferPayload = {"title":"Product management","date": 1746057600,"index":["Product management"]};
    await request(app)
        .post('/api/v1/account/offers')    
        .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
        .set('Content-Type', 'application/json; charset=utf-8')
        .send(janeOfferPayload)
        .expect(200)
});

// afterAll(async() => {
//     // clean database from test data
//     const jamesResponse = await request(app)
//         .get('/api/v1/account')
//         .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
//         .expect('Content-Type', 'application/json; charset=utf-8')
//         .expect(200);
//     await request(app)
//         .delete(`/api/v1/account/${jamesResponse.body.payload.id}`)
//         .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
//         .expect(204);

//     const janeResponse = await request(app)
//         .get('/api/v1/account')
//         .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
//         .expect('Content-Type', 'application/json; charset=utf-8')
//         .expect(200);
//     await request(app)
//         .delete(`/api/v1/account/${janeResponse.body.payload.id}`)
//         .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
//         .expect(204);
// });

describe('Test suite to cover match logic', () => {

    it('on POST /api/v1/account/demands with valid payload and existing match, get matches returns single value', async() => {
        // prepare initial database state
        // prepare demands 
        let janeDemandPayload = {"title":"Software development","date": 1746057600,"index":["Software development"]};
        
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
        console.log(`[matchResponse] ${JSON.stringify(matchResponse)}`)
        expect(matchResponse.status).toEqual(200); 
        expect(JSON.parse(matchResponse.text).payload.length).toEqual(1);
    });

    it('on POST /api/v1/account/matches with approve from the first user server returns positive result and query to matches returns response with approved flag', async() => {
        // prepare initial database state
        let janeDemandPayload = {"title":"Software development","date": 1746057600,"index":["Software development"]};
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
            .post('/api/v1/account/matches')    
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(janeConfirmPayload)
            .expect(200)

        let matchSecondResponse = await request(app)
            .get('/api/v1/account/matches')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        let matchSecondResponsePayload = JSON.parse(matchSecondResponse.text).payload; 
        expect(matchSecondResponsePayload[0].approvedByFirstUser).toEqual(true);
    });

    it.only('on POST /api/v1/account/matches with approve from the first and the second users server returns positive result and query to matches returns response with both approved flag', async() => {
        // prepare initial database state
        let janeDemandPayload = {"title":"Software development","date": 1746057600,"index":["Software development"]};
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
            .post('/api/v1/account/matches')    
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(janeConfirmPayload)
            .expect(200)
        await request(app)
            .post('/api/v1/account/matches')    
            .set('Authorization', 'Basic VGVzdEphbWVzQGdtYWlsLmNvbTpqbXMxMjM=')
            .set('Content-Type', 'application/json; charset=utf-8')
            .send(jamesConfirmPayload)
            .expect(200)
        // TODO finish test case
        let matchSecondResponse = await request(app)
            .get('/api/v1/account/matches')
            .set('Authorization', 'Basic VGVzdEphbmVAZ21haWwuY29tOmpuZTEyMw==')
            .set('Content-Type', 'application/json; charset=utf-8')
            .expect(200)
        let matchSecondResponsePayload = JSON.parse(matchSecondResponse.text).payload; 
        console.log(`[debug] matchSecondResponsePayload ${JSON.stringify(matchSecondResponsePayload)}`)
        expect(matchSecondResponsePayload[0].approvedByFirstUser).toEqual(true);
        expect(matchSecondResponsePayload[0].approvedBySecondUser).toEqual(true);
    });
})