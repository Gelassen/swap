const logger = require('./logger');

exports.validateProfilePayload = function(reqBody) {
    logger.log(reqBody.contact)
    logger.log(reqBody.secret)
    return this.validateString(reqBody.contact) 
        && this.validateString(reqBody.secret) 
}

exports.validateString = function(str) {
    return str !== '' && str !== null && str !== undefined
}
exports.validateOffer = function(offer) {
    return this.validateString(offer.index)
        && this.validateString(offer.date)
        && this.validateString(offer.title)
        && offer.index.length != 0;
}