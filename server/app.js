var express = require('express');
var bodyParser = require('body-parser');
const app = express();

var config = require('./config');
var pool = require('./database');

var profile = require('./controllers/profile');
const { json } = require('express');
const { createPoolCluster } = require('mysql');

app.use(express.json());
app.use(express.urlencoded({extended: true}));

app.use(function(req, res, next) {
    console.log("[REQUEST] " + JSON.stringify(req.path) + " at " + new Date().toISOString());
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader("Content-Type", "application/json; charset=utf-8");
    res.set('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept')
    next()
})

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

app.listen(3000);

module.exports =  app; 
