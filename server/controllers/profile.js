let profile = require('../models/profile')
let match = require('../models/match')
let profileProvider = require('../providers/profile');

let auth = require('../utils/auth')
let network = require('../utils/network')
let validator = require('../utils/validator')
let logger = require('../utils/logger')
let converter = require('../utils/converter')

const config = require('config');

const MAX_PAGE_SIZE = config.dbConfig.maxPageSize;

// const chain = require('../models/chain/chain');
const { SwapToken, SwapChainV2, DebugUtil, chain } = require('../models/chain/chain');

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
            let credentials = network.getAuthNameSecretPair(
                network.getAuthHeaderAsTokens(req)
            );
            logger.log(`[auth header issue] ${JSON.stringify(network.getAuthHeaderAsTokens(req))}`);
            let profiles = await profile.getProfileByCell(req, res, credentials);
            logger.log(`[account::create] [7] profiles result: ${JSON.stringify(profiles)}`);
            if (profileProvider.isAttemptToSignIn(profiles, credentials[1])) {
                // we have to return back to the client a full profile
                let model = await profile.getFullProfile(credentials)
                    .catch(e => network.getErrorMsg(500, JSON.stringify(e)));
                logger.log("Full profile response: " + JSON.stringify(model));
                logger.log(`Json response keys size ${Object.keys(model).length}`);
                if (network.noSuchData(model)) {
                    // should be never invoked due to the first check await profile.getProfileByCell(req, res)
                    result = network.getMsg(204, model);
                } else {
                    result = network.getMsg(200, model);
                }
            } else if (network.thereIsSuchData(profiles)) {
                logger.log(`[account::create] [8] there is already account with this contact ${req.body.contact}`);
                result = network.getErrorMsg(409, `There is an account with contact ${req.body.contact}`);
            } else {
                logger.log(`[account::create] [7] there is no such account - attempt to create`);
                req.body.secret = credentials[1];
                let requestProfile = converter.requestToDomainProfile(req.body);
                result = await profile.create(requestProfile);
                logger.log(`[profile::create] result: ${result}`);
                result = network.noSuchData(result) ? network.getMsg(200, req.body) : result;
            }
        }
    }
    logger.log("[profile] create-response " + JSON.stringify(result));
    network.send(req, res, result)
    logger.log(`[account::create] [9] end`)
}

exports.get = async function(req, res) {
    let result;
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, global.noAuthHeaderMsg)
    } else if (network.getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, network.getAuthHeaderAsTokens(req).result);
    } else {
        let credentials = network.getAuthNameSecretPair(
            network.getAuthHeaderAsTokens(req)
        );
        let model = await profile.getFullProfile(credentials)
            .catch(e => network.getErrorMsg(500, JSON.stringify(e)));
        logger.log("Full profile response: " + JSON.stringify(model));
        logger.log(`Json response keys size ${Object.keys(model).length}`);
        if (network.noSuchData(model)) {
            result = network.getMsg(204, model);
        } else {
            result = network.getMsg(200, model);
        }
    }
    network.send(req, res, result)
}

/* intended to be used for CI tests only */
exports.delete = async function(req, res) {
    let result; 
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, global.noAuthHeaderMsg);
    } else if (network.getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, network.getAuthHeaderAsTokens(req).result);
    } else {
        let credentials = network.getAuthNameSecretPair(
            network.getAuthHeaderAsTokens(req)
        );
        let model = await profile.getProfileByCell(req, res, credentials);
        if (network.noSuchData(model)) {
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
    network.send(req, res, result)
}

exports.addOffer = async function(req, res) {
    logger.log("[add offer] start");
    let result = network.getMsg(200, "Not defined");
    let offerFromRequest = converter.requestToDomainService(req.body);
    offerFromRequest.index = profileProvider.prepareIndex(offerFromRequest.title);
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, "Did you forget to add authorization header?");
    } else if (network.getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, "Did you add correct authorization header?");
    } else if (!validator.validateService(offerFromRequest)) {
        logger.log(`[add offer] offer payload ${offerFromRequest} check - failed`);
        result = network.getErrorMsg(400, "Did you forget to add a valid profile as a payload?");
    } else {
        let credentials = network.getAuthNameSecretPair(
            network.getAuthHeaderAsTokens(req)
        );
        let profileResult = await profile.getFullProfile(credentials);
        logger.log(`[add offer] offer ${JSON.stringify(offerFromRequest)}`);
        logger.log(`[add offer] full profile ${JSON.stringify(profileResult)}`);
        if (network.noSuchData(profileResult)) {
            result = network.getErrorMsg(401, "There is no account for this credentials. Are you authorized?")
        } else if (profileProvider.isThereSuchService(profileResult.offers, offerFromRequest)) {
            result = network.getErrorMsg(409, `The same offer for this profile already exist ${JSON.stringify(offerFromRequest)}`);
        } else {
            logger.log(`[add ofer] offer to insert ${JSON.stringify(offerFromRequest)}`);
            // TODO verify logic, extend tests and write mobile integration test
            result = await profile.addOffer(offerFromRequest, profileResult.id); 
        }
    }
    network.send(req, res, result);
    logger.log("[add offer] ends");
}

exports.addDemand = async function(req, res) {
    logger.log("[add demand] start");
    let result = network.getMsg(200, "Not defined");
    let demandFromRequest = converter.requestToDomainService(req.body, true);
    demandFromRequest.index = profileProvider.prepareIndex(demandFromRequest.title);
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, "Did you forget to add authorization header?");
    } else if (network.getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, "Did you add correct authorization header?");
    } else if (!validator.validateService(demandFromRequest, true)) {
        logger.log(`[add demand] demand payload ${JSON.stringify(demandFromRequest)} check - failed`);
        result = network.getErrorMsg(400, "Did you forget to add a valid profile as a payload?");
    } else {
        let credentials = network.getAuthNameSecretPair(
            network.getAuthHeaderAsTokens(req)
        );
        let profileResult = await profile.getFullProfile(credentials);
        logger.log(`[add demand] offer ${JSON.stringify(demandFromRequest)}`);
        logger.log(`[add demand] full profile ${JSON.stringify(profileResult)}`);
        if (network.noSuchData(profileResult)) {
            result = network.getErrorMsg(401, "There is no account for this credentials. Are you authorized?")
        } else if (profileProvider.isThereSuchService(profileResult.demands, demandFromRequest)) {
            result = network.getErrorMsg(409, `The same demand for this profile already exist ${JSON.stringify(demandFromRequest)}`);
        } else {
            logger.log(`[add demand] offer to insert ${JSON.stringify(demandFromRequest)}`);
            let addDemandResponse = await profile.addDemand(demandFromRequest, profileResult.id); 
            logger.log(`[add demand] response '${JSON.stringify(addDemandResponse)}'`);
            if (network.isEmpty(addDemandResponse)) {
                logger.log(`[makePotentialMatch] demandFromRequest: ${JSON.stringify(demandFromRequest)}`);
                logger.log(`[makePotentialMatch] title: ${JSON.stringify(demandFromRequest.title)}`);
                result = await match.makePotentialMatch(profileResult.id, demandFromRequest, req, res);
            } else if (addDemandResponse === undefined) {
                result = network.getErrorMsg(500, {});
            } else {
                result = addDemandResponse;
            }
            logger.log(`Add demand response ${JSON.stringify(result)}`);
        }
    }
    network.send(req, res, result); 
    logger.log("[add demand] ends");
}

exports.deleteOffer = async function(req, res) {
    logger.log('[delete offer] start');
    let result = network.getMsg(200, "Not defined");
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, "Did you forget to add authorization header?");
    } else if (network.getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, "Did you add correct authorization header?");
    } else {
        let credentials = network.getAuthNameSecretPair(
            network.getAuthHeaderAsTokens(req)
        );
        let profileResult = await profile.getFullProfile(credentials);
        if (network.noSuchData(profileResult)) {
            result = network.getErrorMsg(401, "There is no account for this credentials. Are you authorized?");
        } else if(profileProvider.thereIsNoThisService(profileResult.offers, req.param.id)) {
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
    network.send(req, res, result);
    logger.log('[delete offer] end');
}

exports.deleteDemand = async function(req, res) {
    logger.log('[delete demand] start');
    let result = network.getMsg(200, "Not defined");
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, "Did you forget to add authorization header?");
    } else if (network.getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, "Did you add correct authorization header?");
    } else {
        let credentials = network.getAuthNameSecretPair(
            network.getAuthHeaderAsTokens(req)
        );
        let profileResult = await profile.getFullProfile(credentials);
        if (network.noSuchData(profileResult)) {
            result = network.getErrorMsg(401, "There is no account for this credentials. Are you authorized?");
        } else if(profileProvider.thereIsNoThisService(profileResult.demands, req.param.id)) {
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
    network.send(req, res, result);
    logger.log('[delete demand] end');
}

exports.confirmMatch = async function(req, res) {
    logger.log("[confirm match] start")
    let result = network.getMsg(200, "Not defined")
    let matchObjectFromRequest = converter.requestToDomainChainMatch(req.body)
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, "Did you forget to add authorization header?");
    } else if (network.getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, "Did you add correct authorization header?");
    } else if (!validator.validateMatch(matchObjectFromRequest)) {
        logger.log(`[confirm match] match payload ${matchObjectFromRequest} check - failed`);
        result = network.getErrorMsg(400, "Did you forget to add a valid match object as a payload?");
    } else {
        let credentials = network.getAuthNameSecretPair(
            network.getAuthHeaderAsTokens(req)
        );
        let profileResult = await profile.getFullProfile(credentials);
        // TODO extend sql query and assigned convertor to support chain service data
        let matchResponse = await match.getByProfileIdAndServiceIds(
            profileResult.id,
            matchObjectFromRequest.userFirstServiceId,
            matchObjectFromRequest.userSecondServiceId
        );
        logger.log(`[confirm match] match object ${JSON.stringify(matchResponse)}`);
        logger.log(`[confirm match] full profile ${JSON.stringify(profileResult)}`);
        if (network.noSuchData(profileResult)) {
            result = network.getErrorMsg(401, "There is no account for this credentials. Are you authorized?")
        } else if (matchResponse.code != 200) {
            result = matchResponse; 
        } else {
            result = await match.confirmMatch(profileResult.id, matchObjectFromRequest, req ,res);
        }
    }
    network.send(req, res, result)
    logger.log("[confirm match] end")
}

exports.getMatchesByProfile = async function(req, res) {
    logger.log("[getMatchesByProfile] start");
    let result;
    if (req.get(global.authHeader) === undefined) {
        result = network.getErrorMsg(401, global.noAuthHeaderMsg)
    } else if (network.getAuthHeaderAsTokens(req).error) {
        result = network.getErrorMsg(400, network.getAuthHeaderAsTokens(req).result);
    } else if (!network.isTherePageParamInQuery(req.query)) {
        result = network.getErrorMsg(400, "Did you forget to pass page in query, e.g. ?page=1 ?");
    } else if (!network.isTherePageSizeParamInQuery(req.query)) {
        result = network.getErrorMsg(400, `Did you pass page size? Maximum values per page is ${MAX_PAGE_SIZE}`)
    } else if (req.query.size > MAX_PAGE_SIZE) {
        result = network.getErrorMsg(400, `Did you pass page size within allowed range? Maximum items per page is ${MAX_PAGE_SIZE}, but you passed ${req.query.size}`)
    } else {
        let credentials = network.getAuthNameSecretPair(
            network.getAuthHeaderAsTokens(req)
        );
        let profileResult = await profile.getFullProfile(credentials)
            .catch(e => network.getErrorMsg(500, JSON.stringify(e)));
        logger.log("Full profile response: " + JSON.stringify(profileResult));
        logger.log(`Json response keys size ${Object.keys(profileResult).length}`);
        if (network.noSuchData(profileResult)) {
            result = network.getMsg(204, profileResult);
        } else {
            // TODO draft, has not been tested yet. 
            // TODO consider to add pagination
            /**
             * Current response:
             * 
             * [
   {
      "id":138,
      "userFirstProfileId":4317,
      "userSecondProfileId":4313,
      "userFirstServiceId":14094,
      "userSecondServiceId":14059,
      "approvedByFirstUser":0,
      "approvedBySecondUser":0,
      "idChainServiceFirst":8,
      "userAddressFirst":"0x52E7400Ba1B956B11394a5045F8BC3682792E1AC",
      "tokendIdFirst":77,
      "userAddressSecond":"0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd",
      "tokenIdSecond":20
   },
   {
      "id":139,
      "userFirstProfileId":4317,
      "userSecondProfileId":4313,
      "userFirstServiceId":14094,
      "userSecondServiceId":14057,
      "approvedByFirstUser":0,
      "approvedBySecondUser":0,
      "idChainServiceFirst":6,
      "userAddressFirst":"0x52E7400Ba1B956B11394a5045F8BC3682792E1AC",
      "tokendIdFirst":77,
      "userAddressSecond":"0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd",
      "tokenIdSecond":18
   }
]Connection 12 released[
   "model"
]"model"[
   {
      "id":138,
      "userFirstProfileId":4317,
      "userSecondProfileId":4313,
      "userFirstServiceId":14094,
      "userSecondServiceId":14059,
      "approvedByFirstUser":false,
      "approvedBySecondUser":false,
      "userFirstService":{
         "idChainService":8,
         "userAddress":"0x52E7400Ba1B956B11394a5045F8BC3682792E1AC"
      },
      "userSecondService":{
         "userAddress":"0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd",
         "tokenId":20
      }
   },
   {
      "id":139,
      "userFirstProfileId":4317,
      "userSecondProfileId":4313,
      "userFirstServiceId":14094,
      "userSecondServiceId":14057,
      "approvedByFirstUser":false,
      "approvedBySecondUser":false,
      "userFirstService":{
         "idChainService":6,
         "userAddress":"0x52E7400Ba1B956B11394a5045F8BC3682792E1AC"
      },
      "userSecondService":{
         "userAddress":"0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd",
         "tokenId":18
      }
   }
]
             * 
             */
            let model = await match.getByProfileId(profileResult.id, req.query.page, req.query.size);
            logger.log(`[model] model ${JSON.stringify(model)}`);
            /**
             * Aggregated chain & db query is not necessary here, 
             * see https://github.com/Gelassen/swap/issues/18 for more details 
             * 
             */
            result = network.getMsg(200, model);
            if (model.code !== undefined && model.code == 500) {
                result = model;
            } else {
                result = network.getMsg(200, model);
            }
        }
    }
    network.send(req, res, result)
    logger.log("[getMatchesByProfile] end");
}

exports.getChainOnlyMatches = async function(req, res) {
    let debugUtil = chain.getDebugUtilInstance(); 
    let response = ""
    if (!(await debugUtil.isTokenContractValid()) 
        || !(await debugUtil.isChainContractValid())) {
        response = network.getErrorMsg(500, "One of or both contracts are not deployed by addresses in config")
    } else {
        let swapChainContract = chain.getSwapChainContractInstance();
        let firstUser = "0x367103555b34Eb9a46D92833e7293D540bFd7143";
        let secondUser = "0x1a75262751ac4E6290Ec8287d1De823F33036498";
        
        try {
            // this code is not valid until approveSwap() has been called with one of users
            let result = await swapChainContract.getMatches(firstUser, secondUser);
    
            logger.log(`[get matches] result ${JSON.stringify(result)}`);
            
            if (result.length >= 0) {
                response = network.getMsg(200, result);
            } else {
                response = network.getMsg(409, result, "Failed to obtain matches from chain")
            }
        } catch (exception) {
            response = util.getErrorMsg(500, error);
        }
        
        // let usersResult = await swapChainContract.getUsers();
        // let response = network.getMsg(200, usersResult);
    }

    network.send(req, res, response);
}
