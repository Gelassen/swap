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

exports.validateService = function(offer, isSkipChainService = false) {
    let isMainPartValid = this.validateString(offer.index)
        && this.validateString(offer.date)
        && this.validateString(offer.title)
        && offer.index.length != 0;
    let isOptionalPartValid = false;
    if (isSkipChainService) {
        isOptionalPartValid = true;
    } else {
        isOptionalPartValid = this.validateString(offer.chainService.userWalletAddress)
            && this.isInt(offer.chainService.tokenId);
    }
    return isMainPartValid && isOptionalPartValid;

}

exports.validateMatch = function(matchSubj) {
    return this.validateString(matchSubj.userFirst)
        && this.validateString(matchSubj.userSecond)
        && this.isInt(matchSubj.valueOfFirstUser)
        && this.isInt(matchSubj.valueOfSecondUser)
        && this.isBool(matchSubj.approvedByFirstUser)
        && this.isBool(matchSubj.approvedBySecondUser)
}

exports.isInt = function(value) {
    return !isNaN(value) 
        && parseInt(Number(value)) == value 
        && !isNaN(parseInt(value, 10));
}

exports.isBool = function(value) {
    return value === true 
        || value === false 
        || toString.call(value) === '[object Boolean]';
}