const express = require('express');
const app = express();
const config = require('config');

const pool = require('./database');
const profile = require('./controllers/profile');
const offers = require('./controllers/offers'); 
const demands = require('./controllers/demands');
const contacts = require('./controllers/contacts');

app.use(express.json());
app.use(express.urlencoded({extended: true}));

app.use(function(req, res, next) {
    console.log(`[REQUEST] ${JSON.stringify(req.path)} with query ${JSON.stringify(req.query)} at ${new Date().toISOString()}`);
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader("Content-Type", "application/json; charset=utf-8");
    res.set('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept')
    next()
})

/* account */
app.get('/api/v1', function(req, res, next) {
    res.send({msg: "Hello to open exchange platform!" })
})

app.post('/api/v1/account', function(req, res) {
    pool.status();
    profile.create(req, res);
})

app.get('/api/v1/account', function(req, res) {
    pool.status();
    profile.get(req, res);
})

app.delete('/api/v1/account/:id', function(req, res) {
    pool.status();
    profile.delete(req, res);
})

app.get('/api/v1/account/offers', function(req, res) {
    pool.status();
    res.send(405, { "payload": "Only POST and DELETE requests are allowed for this resource."})
})

app.post('/api/v1/account/offers', function(req, res) {
    pool.status();
    profile.addOffer(req, res);
})

app.post('/api/v1/account/demands', function(req, res) {
    pool.status();
    profile.addDemand(req, res);
})

app.delete('/api/v1/account/offers/:id', function(req, res) {
    pool.status();
    profile.deleteOffer(req, res);
})

app.delete('/api/v1/account/demands/:id', function(req, res) {
    pool.status();
    profile.deleteDemand(req, res);
})

/* offers part */
app.get('/api/v1/offers', function(req, res) {
    pool.status();
    offers.get(req, res);
})

/* demands part */
app.get('/api/v1/demands', function(req, res) {
    pool.status();
    demands.get(req, res);
})

/* chains part */

/* contacts part */
app.get('/api/v1/contacts', function(req, res) {
    pool.status();
    contacts.get(req, res);
})

app.listen(config.get('config').webPort);

module.exports =  app; 
