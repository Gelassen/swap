const config = require('config');
const profile = require('../models/profile');
const offers = require('../models/offers');

const network = require('../utils/network');
const logger = require('../utils/logger');

const MAX_PAGE_SIZE = config.dbConfig.maxPageSize;

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
    } else if (!network.isTherePageParamInQuery(req.query)) {
        result = network.getErrorMsg(400, "Did you forget to pass page in query, e.g. ?page=1 ?");
    } else if (!network.isTherePageSizeParamInQuery(req.query)) {
        result = network.getErrorMsg(400, `Did you pass page size? Maximum values per page is ${MAX_PAGE_SIZE}`)
    } else if (req.query.size > MAX_PAGE_SIZE) {
        result = network.getErrorMsg(400, `Did you pass page size within allowed range? Maximum items per page is ${MAX_PAGE_SIZE}, but you passed ${req.query.size}`)
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
            result = network.getMsg(200, [], "Profile doesn't have any demands. In this case there is no need to select offers.");
        } else {
            logger.log("[4]");
            let offersResult = await offers.getOffers(profileResult, req.query.page, req.query.size);
            logger.log(`[get offers] offers ${JSON.stringify(offersResult)}`);
            if (network.noSuchData(offersResult) || offersResult.length == 0) {
                result = network.getMsg(200, []);
            } else {
                result = network.getMsg(200, offersResult);
            }
        }
    } 

    logger.log(`[offers reply] ${JSON.stringify(result)}`);
    network.send(req, res, result);
    logger.log(`[offers] end`);
}