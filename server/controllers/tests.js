const config = require('config');
const network = require('../utils/network')

exports.setEnvironment = async function(req, res) { 
    let result
    if (config.isProduction) {
        result = network.getErrorMsg(501, "This method is only supported for non-production mode for test purposes.")
    } else {
        // TODO clean up database if it is a debug mode
        result = network.getErrorMsg(501, "This method is not fully supported yet.")
    }
    network.send(req, res, result);
}