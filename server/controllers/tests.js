const config = require('config');
const network = require('../utils/network')
const logger = require('../utils/logger')

exports.setEnvironment = async function(req, res) { 
    logger.log("[start] /test/environment call");
    let result
    if (config.isProduction) {
        result = network.getErrorMsg(501, "This method is only supported for non-production mode for test purposes.")
    } else {
        // TODO clean up database if it is a debug mode
        result = network.getMsg(200, JSON.stringify({"msg" : "just a stub"}));
    }
    logger.log(`[end] response ${JSON.stringify(result)}`)
    network.send(req, res, result);
}

exports.setEnvironment2 = async function(req, res) {
    logger.log("[start] /test/environment call");
    let result
    if (config.isProduction) {
        result = network.getErrorMsg(501, "This method is only supported for non-production mode for test purposes.")
    } else {
        // TODO clean up database if it is a debug mode
        result = network.getErrorMsg(501, "This method is not fully supported yet.")
    }
    logger.log(`[end] response ${JSON.stringify(result)}`)
    network.send(req, res, result);
}