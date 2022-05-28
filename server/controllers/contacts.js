
const network = require('../utils/network');
const logger = require('../utils/logger');
const auth = require('../utils/auth');

const contacts = require('../models/contacts');
const profile = require('../models/profile');

exports.get = async function(req, res) {
    logger.log("[contacts] start");
    let response;
    if (noServiceId(req.query)) {
        logger.log("[1]");
        response = network.getErrorMsg(400, "No service id in request. Did you forget to add service id?");
    } else if (req.get(global.authHeader) === undefined) {
        logger.log("[2]");
        response = network.getErrorMsg(401, "Did you forget to add authorization header?");
    } else if (network.getAuthHeaderAsTokens(req).error) {
        logger.log("[3]");
        response = network.getErrorMsg(400, "Did you add correct authorization header?");
    } else {
        logger.log("[4]");
        let credentials = network.getAuthNameSecretPair(
            network.getAuthHeaderAsTokens(req)
        );
        let profileResult = await profile.getFullProfile(credentials);
        if (network.noSuchData(profileResult)) {
            response = network.getErrorMsg(401, "There is no account for this credentials. Are you authorized?");
        } else {
            let contactsResult = await contacts.getContactsByServiceId(req.query.serviceId);
            logger.log(`contacts result - ${JSON.stringify(contactsResult)}`);
            if (contactsResult.code == 500) {
                response = contactsResult;
            } else if (network.noSuchData(contactsResult) || contactsResult.length == 0) {
                response = network.getMsg(400, "There is no data associated with this service id. Did you pass correct id?");
            } else if (contactsResult > 1) {
                response = network.getErrorMsg(300, "There are several contacts for this service which is not correct. Likely it is server issue. What you can do is contact administrator for it");
            } else {
                response = network.getMsg(200, contactsResult.at(0));
            }
        }
    }

    network.send(req, res, response);
    logger.log("[contacts] end");
}

function noServiceId(query) {
    return query.serviceId == undefined || !Number.isInteger(Number(query.serviceId));
}