const profile = require('../models/profile');
const offers = require('../models/offers');

const auth = require('../utils/auth')
const network = require('../utils/network')
const validator = require('../utils/validator')
const logger = require('../utils/logger')
const converter = require('../utils/converter')

exports.get = async function(req, res) {
    let result;
    logger.log(`[offers] get - start`);
    if (req.get(global.authHeader) === undefined) {
        // we do not need authorization for this request, but we use auth header 
        // to evaluate profile/user to match offers, that's why we use BAD REQUEST 
        // code here
        result = network.getErrorMsg(400, `Did you add auth header?`);
    } else if (network.getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, network.getAuthHeaderAsTokens(req).result);
    } else {
        const credentials = network.getAuthNameSecretPair(
            network.getAuthHeaderAsTokens(req)
        );
        let profileResult = await profile.getFullProfile(credentials);
        logger.log(JSON.stringify(profileResult));
        if (network.noSuchData(profileResult)) {
            logger.log("[1]");
            result = network.getErrorMsg(400, "There is no such account.");
        } else if (profileResult.code == 500) {
            logger.log("[2]");
            result = profileResult;
        } else if (profileResult.demands.length == 0) {
            logger.log("[3]");
            result = network.getMsg(200, "Profile doesn't have any demands. In this case there is no need to select offers.");
        } else {
            logger.log("[4]");
            let offersResult = offers.getOffers(profileResult);
            if (network.noSuchData(offersResult)) {
                result = network.getMsg(200, []);
            } else {
                result = offersResult;
            }
        }
        // result = network.getErrorMsg(500, `Not implemented yet`);
    } 

    network.send(req, res, result);
}