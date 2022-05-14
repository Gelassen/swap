let profile = require('../models/profile')

let auth = require('../utils/auth')
let network = require('../utils/network')
let validator = require('../utils/validator')
let logger = require('../utils/logger')
let converter = require('../utils/converter')
const e = require('express')

/*
    This method covers both cases - sign in and register a new account. It is left 
    by historical reasons and waits for refactoring. 
*/
exports.create = async function(req, res) {
    let result;
    logger.log(`[account::create] [1] start`);
    if (req.get(global.authHeader) === undefined) {
        logger.log(`[account::create] [2] auth header check - failed`);
        result = network.getErrorMsg(401, global.noAuthHeaderMsg);
    } else if (!validator.validateProfilePayload(req.body)) {
        logger.log(`[account::create] [3] profile payload check - failed`);
        result = network.getErrorMsg(400, "Did you forget to add a valid profile as a payload?");
    } else {
        logger.log(`[account::create] [4] main block`);
        let authResult = auth.parse(req.get("Authorization"));
        if (authResult.error) {
            logger.log(`[account::create] [5] auth check - failed`);
            result = network.getErrorMsg(401, authResult.result);
        } else {
            logger.log(`[account::create] [6] try to get profile by cell`);
            let credentials = getAuthNameSecretPair(
                getAuthHeaderAsTokens(req)
            );
            logger.log(`[auth header issue] ${JSON.stringify(getAuthHeaderAsTokens(req))}`);
            let profiles = await profile.getProfileByCell(req, res, credentials);
            logger.log(`[account::create] [7] profiles result: ${JSON.stringify(profiles)}`);
            if (isAttemptToSignIn(profiles, credentials[1])) {
                // we have to return back to the client a full profile
                let model = await profile.getFullProfile(req, res, credentials)
                    .catch(e => network.getErrorMessage(e));
                logger.log("Full profile response: " + JSON.stringify(model));
                logger.log(`Json response keys size ${Object.keys(model).length}`);
                if (noSuchData(model)) {
                    // should be never invoked due to the first check await profile.getProfileByCell(req, res)
                    result = network.getMsg(204, model);
                } else {
                    result = network.getMsg(200, model);
                }
            } else if (thereIsSuchData(profiles)) {
                logger.log(`[account::create] [8] there is already account with this contact ${req.body.contact}`);
                result = network.getErrorMsg(409, `There is an account with contact ${req.body.contact}`);
            } else {
                logger.log(`[account::create] [7] there is no such account - attempt to create`);
                req.body.secret = credentials[1];
                result = await profile.create(req);
            }
        }
    }
    logger.log("[profile] create-response " + JSON.stringify(result));
    send(req, res, result)
    logger.log(`[account::create] [9] end`)
}

exports.get = async function(req, res) {
    let result;
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, global.noAuthHeaderMsg)
    } else if (getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, getAuthHeaderAsTokens(req).result);
    } else {
        let credentials = getAuthNameSecretPair(
            getAuthHeaderAsTokens(req)
        );
        let model = await profile.getFullProfile(req, res, credentials)
            .catch(e => network.getErrorMessage(e));
        logger.log("Full profile response: " + JSON.stringify(model));
        logger.log(`Json response keys size ${Object.keys(model).length}`);
        if (noSuchData(model)) {
            result = network.getMsg(204, model);
        } else {
            result = network.getMsg(200, model);
        }
    }
    send(req, res, result)
}

/* intended to be used for CI tests only */
exports.delete = async function(req, res) {
    let result; 
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, global.noAuthHeaderMsg);
    } else if (getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, getAuthHeaderAsTokens(req).result);
    } else {
        let credentials = getAuthNameSecretPair(
            getAuthHeaderAsTokens(req)
        );
        let model = await profile.getProfileByCell(req, res, credentials);
        if (noSuchData(model)) {
            result = network.getErrorMsg(404, model);
        } else {
            let isSuccess = await profile.deleteProfile(req, res, credentials);
            if (isSuccess) {
                result = network.getMsg(204, {});
            } else {
                result = network.getErrorMsg(400, {});
            }
        }
    }
    send(req, res, result)
}

exports.addOffer = async function(req, res) {
    logger.log("[add offer] start");
    let result = network.getMsg(200, "Not defined");
    let offerFromRequest = converter.requestToDomainService(req.body);
    offerFromRequest.index = prepareIndex(offerFromRequest.title);
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, "Did you forget to add authorization header?");
    } else if (getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, "Did you add correct authorization header?");
    } else if (!validator.validateService(offerFromRequest)) {
        logger.log(`[add offer] offer payload ${offerFromRequest} check - failed`);
        result = network.getErrorMsg(400, "Did you forget to add a valid profile as a payload?");
    } else {
        let credentials = getAuthNameSecretPair(
            getAuthHeaderAsTokens(req)
        );
        let profileResult = await profile.getFullProfile(req, res, credentials);
        logger.log(`[add offer] offer ${JSON.stringify(offerFromRequest)}`);
        logger.log(`[add offer] full profile ${JSON.stringify(profileResult)}`);
        if (noSuchData(profileResult)) {
            result = network.getErrorMsg(401, "There is no account for this credentials. Are you authorized?")
        } else if (isThereSuchService(profileResult.offers, offerFromRequest)) {
            result = network.getErrorMsg(409, `The same offer for this profile already exist ${JSON.stringify(offerFromRequest)}`);
        } else {
            logger.log(`[add ofer] offer to insert ${JSON.stringify(offerFromRequest)}`);
            result = await profile.addOffer(offerFromRequest, profileResult.id); 
        }
    }
    send(req, res, result);
    logger.log("[add offer] ends");
}

exports.addDemand = async function(req, res) {
    logger.log("[add demand] start");
    let result = network.getMsg(200, "Not defined");
    let demandFromRequest = converter.requestToDomainService(req.body);
    demandFromRequest.index = prepareIndex(demandFromRequest.title);
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, "Did you forget to add authorization header?");
    } else if (getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, "Did you add correct authorization header?");
    } else if (!validator.validateService(demandFromRequest)) {
        logger.log(`[add demand] demand payload ${demandFromRequest} check - failed`);
        result = network.getErrorMsg(400, "Did you forget to add a valid profile as a payload?");
    } else {
        let credentials = getAuthNameSecretPair(
            getAuthHeaderAsTokens(req)
        );
        let profileResult = await profile.getFullProfile(req, res, credentials);
        logger.log(`[add demand] offer ${JSON.stringify(demandFromRequest)}`);
        logger.log(`[add demand] full profile ${JSON.stringify(profileResult)}`);
        if (noSuchData(profileResult)) {
            result = network.getErrorMsg(401, "There is no account for this credentials. Are you authorized?")
        } else if (isThereSuchService(profileResult.demands, demandFromRequest)) {
            result = network.getErrorMsg(409, `The same demand for this profile already exist ${JSON.stringify(demandFromRequest)}`);
        } else {
            logger.log(`[add demand] offer to insert ${JSON.stringify(demandFromRequest)}`);
            result = await profile.addDemand(demandFromRequest, profileResult.id); 
        }
    }
    send(req, res, result); 
    logger.log("[add demand] ends");
}

exports.deleteOffer = async function(req, res) {
    logger.log('[delete offer] start');
    let result = network.getMsg(200, "Not defined");
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, "Did you forget to add authorization header?");
    } else if (getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, "Did you add correct authorization header?");
    } else {
        let credentials = getAuthNameSecretPair(
            getAuthHeaderAsTokens(req)
        );
        let profileResult = await profile.getFullProfile(req, res, credentials);
        if (noSuchData(profileResult)) {
            result = network.getErrorMsg(401, "There is no account for this credentials. Are you authorized?");
        } else if(thereIsNoThisService(profileResult.offers, req.param.id)) {
            result = network.getErrorMsg(404, `There is an account for this credentials, but there is no offer in for this id ${req.params.id}`);
        } else {
            let isSuccess = await profile.deleteOffer(req);
            if (isSuccess) {
                result = network.getMsg(204, {});
            } else {
                result = network.getErrorMsg(400, "There is an account for this credentials and there is a service for this id, but there is no rows affect by executed DELETE sql statement. Generally it shouldn't ever happened.");
            }
        }
    }
    send(req, res, result);
    logger.log('[delete offer] end');
}

exports.deleteDemand = async function(req, res) {
    logger.log('[delete demand] start');
    let result = network.getMsg(200, "Not defined");
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, "Did you forget to add authorization header?");
    } else if (getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, "Did you add correct authorization header?");
    } else {
        let credentials = getAuthNameSecretPair(
            getAuthHeaderAsTokens(req)
        );
        let profileResult = await profile.getFullProfile(req, res, credentials);
        if (noSuchData(profileResult)) {
            result = network.getErrorMsg(401, "There is no account for this credentials. Are you authorized?");
        } else if(thereIsNoThisService(profileResult.demands, req.param.id)) {
            result = network.getErrorMsg(404, `There is an account for this credentials, but there is no demand in for this id ${req.params.id}`);
        } else {
            let isSuccess = await profile.deleteDemand(req);
            if (isSuccess) {
                result = network.getMsg(204, {});
            } else {
                result = network.getErrorMsg(400, "There is an account for this credentials and there is a service for this id, but there is no rows affect by executed DELETE sql statement. Generally it shouldn't ever happened.");
            }
        }
    }
    send(req, res, result);
    logger.log('[delete demand] end');
}

// TODO refactor it by grouping, exposing and encapsualting methods below in separate classes
/*
    It is a stub for feature postponed for future. In original design it should be done on
    client side and offered for user to pick up right keys-indexes for search
 */
function prepareIndex(str) {
    let index = [];
    index.push(str);
    return index;
}

function isThereSuchService(profileServices, subject) {
    let result = false;
    for (let id = 0; id < profileServices.length; id++) {
        if (profileServices.at(id).title === subject.title
        && profileServices.at(id).date === subject.date
        && profileServices.at(id).index.length === subject.index.length
        && profileServices.at(id).index.sort().every((value, index) => value === subject.index[index])) {
            result = true;
            break;
        }
    }
    return result;
}

function thereIsNoThisService(profileServices, requestId) {
    if (profileServices.length == 0) return true;

    let result = false;
    for (let id = 0; id < profileServices.length; id++) {
        if (profileServices.at(id) === requestId) {
            /* return false */
            break;
        }
    }
    return result;
} 

function thereIsSuchData(obj) {
    return Object.keys(obj).length
}

function noSuchData(obj) {
    return !Object.keys(obj).length
}

function isAttemptToSignIn(profile, reqSecret) {
    return Object.keys(profile).length && profile.secret === reqSecret;
}

/*
    @input the result of getAuthHeaderAsTokens(req); both methods require refactoring
 */
function getAuthNameSecretPair(authResult) {
    return authResult.result.split(":");
}

function getAuthHeaderAsTokens(req) {
    return auth.parse(req.get(global.authHeader));
}

async function send (req, res, result) {
    if (result.code === undefined) {
        res.send(JSON.stringify(result));
    } else {
        logger.log(`[reply] status code ${JSON.stringify(result.code)}, payload ${JSON.stringify(result)}`);
        res.status(result.code).send({ payload: result.payload });
    }
    res.end();
}