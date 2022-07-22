const createError = require('http-errors');
const express = require('express');
const app = express();
const config = require('config');
const indexRouter = require('./routes/index');

app.use(express.json());
app.use(express.urlencoded({extended: true}));
app.use(function(req, res, next) {
    console.log(`[REQUEST] ${JSON.stringify(req.path)} with query ${JSON.stringify(req.query)} at ${new Date().toISOString()}`);
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader("Content-Type", "application/json; charset=utf-8");
    res.set('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept')
    next()
});
app.use('/', indexRouter);

// catch 404 and forward to error handler
app.use(function (req, res, next) {
    next(createError(404));
});

// error handler
app.use(function (err, req, res, next) {
    res.send(err.status || 500, { code: err.status, msg: err.message });
});

app.listen(config.get('config').webPort);

module.exports =  app; 
