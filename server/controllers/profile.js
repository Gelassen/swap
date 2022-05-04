let profile = require('../models/profile')

let auth = require('../utils/auth')
let network = require('../utils/network')
let validator = require('../utils/validator')
let logger = require('../utils/logger')

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
            req.body.secret = getAuthNameSecretPair(authResult)[1];
            let profiles = await profile.getProfileByCell(req, res);
            logger.log(`[account::create] [7] profiles result: ${JSON.stringify(profiles)}`);
            if (isAttemptToSignIn(profiles, req.body.secret)) {
                // we have to return back to the client a full profile
                let authResult = getAuthHeaderAsTokens(req);
                let model = await profile.getFullProfile(req, res, getAuthNameSecretPair(authResult))
                    .catch(e => network.getErrorMessage(e));
                logger.log("Full profile response: " + JSON.stringify(model));
                logger.log(`Json response keys size ${Object.keys(model).length}`);
                if (!Object.keys(model).length) {
                    // should be never invoked due to the first check await profile.getProfileByCell(req, res)
                    result = network.getMsg(204, model);
                } else {
                    result = network.getMsg(200, model);
                }
            } else if (Object.keys(profiles).length) {
                logger.log(`[account::create] [8] there is already account with this contact ${req.body.contact}`);
                result = network.getErrorMsg(409, `There is an account with contact ${req.body.contact}`);
            } else {
                logger.log(`[account::create] [7] there is no such account - attempt to create`);
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
        let authResult = getAuthHeaderAsTokens(req);
        let model = await profile.getFullProfile(req, res, getAuthNameSecretPair(authResult))
            .catch(e => network.getErrorMessage(e));
        logger.log("Full profile response: " + JSON.stringify(model));
        logger.log(`Json response keys size ${Object.keys(model).length}`);
        if (!Object.keys(model).length) {
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
        let authResult = getAuthHeaderAsTokens(req);
        req.body.contact = getAuthNameSecretPair(authResult)[0];
        let model = await profile.getProfileByCell(req, res);
        if (!Object.keys(model).length) {
            result = network.getErrorMsg(404, model);
        } else {
            isSuccess = await profile.deleteProfile(req, res, getAuthNameSecretPair(authResult));
            if (isSuccess) {
                result = network.getMsg(204, {});
            } else {
                result = network.getErrorMsg(400, {});
            }
        }
    }
    send(req, res, result)
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