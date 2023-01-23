const express = require('express');
const router = express.Router();

const profile = require('../controllers/profile');
const offers = require('../controllers/offers'); 
const demands = require('../controllers/demands');
const contacts = require('../controllers/contacts');

router.get('/', function(req, res) {
    res.redirect('/api/v1');
});
router.get('/api/v1', function(req, res) {
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
        + "GET /account/matches \n"
        + "POST /account/matches \n"
    res.send({msg: responseMsg })
});
router.post('/api/v1/account', profile.create);
router.get('/api/v1/account', profile.get);
router.delete('/api/v1/account/:id', profile.delete);
router.get('/api/v1/account/offers', function(req, res) {
    console.log('we are in the /api/v1/account/offers handler!')
    res.send(405, { "payload": "Only POST and DELETE requests are allowed for this resource."})
});
router.post('/api/v1/account/offers', profile.addOffer);
router.post('/api/v1/account/demands', profile.addDemand);
router.delete('/api/v1/account/offers/:id', profile.deleteOffer);
router.delete('/api/v1/account/demands/:id', profile.deleteDemand);
router.get('/api/v1/account/matches', profile.getMatchesByProfile);
router.post('/api/v1/account/matches', profile.confirmMatch)

router.get('/api/v1/offers', offers.get);

router.get('/api/v1/demands', demands.get);

router.get('/api/v1/contacts', contacts.get);

module.exports = router;