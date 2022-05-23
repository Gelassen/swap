global.noAuthHeaderMsg = "There is no auth header.";
global.authHeader = "Authorization";

const logger = require('./logger');
const auth = require('../utils/auth')

exports.getMsg = function(payload) {
    return { payload }
}

exports.getMsg = function(code, payload) {
    return { code, payload }
}

exports.getErrorMsg = function(code, payload) {
    return { code: code, payload }
}

exports.getMessage = function(code, message) {
    return {code: code, message: message, payload: {}};
}

exports.getPayloadMessage = function(payload) {
    return {payload: payload}
}

exports.getErrorMessage = function() {
    return {code: 500, message: "unsuccess", payload: {}}
}

exports.getErrorMessage = function(payload) {
    return {code: 500, message: "unsuccess", payload: payload}
}

exports.getErrorMessage = function(code, message) {
    return {code: code, message: message, payload: {}}
}

exports.getErrorMessage = function(code, message, payload) {
    return {code: code, message: message, payload: payload}
}

exports.statusSuccess = 1
exports.statusFailed = 0

exports.getServiceMessage = function(statusCode, message) {
    return {status: statusCode, message: message}
}

exports.isTherePageParamInQuery = function isTherePageParamInQuery(query) {
    return query.page != undefined && Number.isInteger(Number(query.page)); 
}

/**
 * @input the result of getAuthHeaderAsTokens(req); both methods require refactoring
 */
exports.getAuthNameSecretPair = function getAuthNameSecretPair(authResult) {
    return authResult.result.split(":");
}

exports.getAuthHeaderAsTokens = function getAuthHeaderAsTokens(req) {
    return auth.parse(req.get(global.authHeader));
}

exports.noSuchData = function noSuchData(obj) {
    return !Object.keys(obj).length
}

exports.send = async function send (req, res, result) {
    if (result.code === undefined) {
        logger.log(`[reply] ${JSON.stringify(result)}`);
        res.send(JSON.stringify(result));
    } else {
        logger.log(`[reply] status code ${JSON.stringify(result.code)}, payload ${JSON.stringify(result)}`);
        res.status(result.code).send({ payload: result.payload });
    }
    res.end();
}