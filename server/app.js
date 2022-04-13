var express = require('express');
var bodyParser = require('body-parser');
var config = require('./config')
var pool = require('./database');
var app = express();

app.use(express.json())
app.use(express.urlencoded({extended: true}))

app.use(function(req, res, next) {
    console.log("[REQUEST] " + JSON.stringify(req.path) + " at " + new Date().toISOString());
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader("Content-Type", "application/json; charset=utf-8");
    res.set('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept')
    next()
})

app.get('/api/v1', function(req, res, next) {
    res.send({msg: "Hello to open car share server!" })
})

app.post('/api/v1/account', function(req, res) {

})

app.listen(3000)
