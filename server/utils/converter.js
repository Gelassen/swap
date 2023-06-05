const validator = require('../utils/validator'); 

exports.requestToDomainProfile = function(request) {
    let profile = {};
    profile.contact = request.contact;
    profile.secret = request.secret;
    profile.name = request.name;
    profile.userWalletAddress = request.userWalletAddress;
    profile.offers = request.offers;
    profile.demands = request.demands;
    return profile;
}

exports.dbToDomainProfile = function(rows) {
    if (rows.length == 0) return {};
    let result = [];
    for (id = 0; id < rows.length; id++) {
        let profile = {};
        profile.id = rows[id].id;
        profile.name = rows[id].name;
        profile.contact = rows[id].contact;
        profile.secret = rows[id].secret;
        profile.userWalletAddress = rows[id].userWalletAddress;
        result.push(profile);
    }
    return result;
}

exports.dbToDomainFullProfile = function(rows) {
    if (rows.length == 0) return {};
    let offers = [];
    let demands = [];
    for (id = 0; id < rows.length; id++) {
        if (rows[id].serviceId === null) continue;
        let service = {};
        service.id = rows[id].serviceId;
        service.title = rows[id].serviceTitle;
        service.date = rows[id].serviceDate;
        service.index = prepareIndex(rows[id]);
        
        let chainService = {};
        // chainService.idChainService = rows[id].idChainService;
        chainService.userWalletAddress = rows[id].userWalletAddress;
        chainService.tokenId = rows[id].tokenId;
        // if single field is empty then a whole chainService is empty too, skip this field        
        if (validator.validateStringIsNotEmpty(chainService.userWalletAddress)) {
            service.chainService = chainService;
        }
        if (rows[id].serviceOffer == 1) {
            offers.push(service);
        } else {
            demands.push(service);
        }
    }
    return {
        id: rows[0].profileId,
        contact: rows[0].profileContact,
        secret: rows[0].profileSecret,
        userWalletAddress: rows[0].profileUserWalletAddress,
        name: rows[0].profileName,
        offers: offers,
        demands: demands
    };
}

exports.contactsDbToDomain = function(rows) {
    let result = [];
    if (rows.length == 0) return result;

    for (id = 0; id < rows.length; id++) {
        let contact = {}
        contact.id = rows[id].id;
        contact.name = rows[id].name;
        contact.contact = rows[id].contact;
        result.push(contact);
    }

    return result;
}

exports.offersDbToDomainService = function(rows) {
    let result = [];
    if (rows.length == 0) return result;

    for (id = 0; id < rows.length; id++) {
        let service = {};
        service.id = rows[id].id;
        service.title = rows[id].title;
        service.date = rows[id].date;
        service.index = [rows[id].index];
        /* omit offer type and profile id */
        result.push(service);
    }
    return result;
}

exports.dbToDomainService = function(rows) {
    let result = [];
    if (rows.length == 0) return result;

    for (id = 0; id < rows.length; id++) {
        let service = {};
        service.id = rows[id].serviceId;
        service.title = rows[id].serviceTitle;
        service.date = rows[id].serviceDate;
        service.index = prepareIndex(rows[id]);
        /* omit offer type and profile id */
        result.push(service);
    }
    return result;
}

exports.requestToDomainService = function(reqBody, isSkipChainService = false) {
     // TODO check will be auto increment work if zero is passed as an id? 
    let result = {};
    result.title = reqBody.title;
    result.date = reqBody.date;
    result.index = reqBody.index;
    // result.chainService = ""
    if (!isSkipChainService) {
        result.chainService = requestToDomainChainService(reqBody) 
    }
    return result 
}

exports.requestToDomainChainMatch = function(reqBody) {
    let result = {};
    result.userFirst = reqBody.userFirst;
    result.valueOfFirstUser = reqBody.valueOfFirstUser;
    result.userSecond = reqBody.userSecond;
    result.valueOfSecondUser = reqBody.valueOfSecondUser;
    result.approvedByFirstUser = reqBody.approvedByFirstUser;
    result.approvedBySecondUser = reqBody.approvedBySecondUser;
    result.userFirstServiceId = reqBody.userFirstServiceId; // TODO confirm it is passed from the client
    result.userSecondServiceId = reqBody.userSecondServiceId; // TODO confirm it is passed from the client
    return result;
}

// TOOD refactor row object fields
exports.syntheticDbToDomainServerMatch = function(rows) {
    let result = [];
    if (rows.length == 0) return result;

    for (id = 0; id < rows.length; id++) {
        let match = {};
        match.id = 0; // We omit id as it does not exist yet;
        match.userFirstProfileId =  rows[id].Q1ProfileId; 
        match.userSecondProfileId = rows[id].profileId;
        match.userFirstServiceId = rows[id].Q1Id;
        match.userSecondServiceId = rows[id].id;
        // match.idChainService = 
        match.approvedByFirstUser = false;
        match.approvedBySecondUser = false;
        result.push(match);
    }
    return result;
}

// TODO this function looks as a redundant in the favour of dbToNewDomainServerMatch, remove after migration would be completed  
exports.dbToDomainServerMatch = function(rows) {
    let result = [];
    if (rows.length == 0) return result;

    for (id = 0; id < rows.length; id++) {
        let serverMatch = {}
        serverMatch.id = rows[id].id;
        serverMatch.userFirstProfileId = rows[id].userFirstProfileId;
        serverMatch.userSecondProfileId = rows[id].userSecondProfileId;
        serverMatch.userFirstServiceId = rows[id].userFirstServiceId;
        serverMatch.userSecondServiceId = rows[id].userSecondServiceId;
        serverMatch.approvedByFirstUser = rows[id].approvedByFirstUser === 1; // 0 is false, 1 is true
        serverMatch.approvedBySecondUser = rows[id].approvedBySecondUser === 1; // 0 is false, 1 is true

        result.push(serverMatch);
    }
    return result;
}

exports.dbToNewDomainServerMatch = function(rows) {
    let result = [];
    if (rows.length == 0) return result;

    for (id = 0; id < rows.length; id++) {
        let serverMatch = {}
        serverMatch.id = rows[id].id;
        serverMatch.userFirstProfileId = rows[id].userFirstProfileId;
        serverMatch.userSecondProfileId = rows[id].userSecondProfileId;
        serverMatch.userFirstServiceId = rows[id].userFirstServiceId;
        serverMatch.userSecondServiceId = rows[id].userSecondServiceId;
        serverMatch.approvedByFirstUser = rows[id].approvedByFirstUser === 1; // 0 is false, 1 is true
        serverMatch.approvedBySecondUser = rows[id].approvedBySecondUser === 1; // 0 is false, 1 is true
        serverMatch.userFirstProfileName = rows[id].userFirstProfileName;
        serverMatch.userSecondProfileName = rows[id].userSecondProfileName;
        serverMatch.userFirstServiceTitle = rows[id].firstServiceTitle;
        serverMatch.userSecondServiceTitle = rows[id].secondServiceTitle;
        
        let firstChainService = {}
        firstChainService.idChainService = rows[id].idChainServiceFirst;
        firstChainService.userWalletAddress = rows[id].userAddressFirst;
        firstChainService.tokenId = rows[id].tokenIdFirst;
        firstChainService.serverServiceId = serverMatch.userFirstServiceId; // INNER JOIN is done over this column, so they should be equal
        let secondChainService = {}
        secondChainService.idChainService = rows[id].idChainServiceSecond;
        secondChainService.userWalletAddress = rows[id].userAddressSecond;
        secondChainService.tokenId = rows[id].tokenIdSecond;
        secondChainService.serverServiceId = serverMatch.userSecondServiceId; // INNER JOIN is done over this column, so they should be equal

        serverMatch.userFirstService = firstChainService;
        serverMatch.userSecondService = secondChainService;

        result.push(serverMatch);
    }
    return result;
}

// TODO clarify use case or remove it
exports.matchesDbToDomainMatches = function(rows) {
    let result = [];
    if (rows.length == 0) return result;

    for (id = 0; id < rows.length; id++) {
        let match = {};
        match.id = rows[id].id;
        match.userFirstProfileId = rows[id].userFirstProfileId; 
        match.userSecondProfileId = rows[id].userSecondProfileId;
        match.userFirst = rows[id].userFirst; // TODO do we need user address on chain here?
        match.valueOfFirstUser = rows[id].valueOfFirstUser;
        match.userSecond = rows[id].userSecond; // TODO do we need user address on chain here?
        match.valueOfSecondUser = rows[id].valueOfSecondUser;
        match.approvedByFirstUser = rows[id].approvedByFirstUser;
        match.approvedBySecondUser = rows[id].approvedBySecondUser;
        result.push(service);
    }
    return result;
}

function prepareIndex(row) {
    let index = []
    if (row.serviceIndex != null || row.serviceIndex != undefined) {
        let tokens = row.serviceIndex.split(",");
        for (aId = 0; aId < tokens.length; aId++) {
            index.push(tokens[aId]);
        }
    }
    return index;
}

function requestToDomainChainService(reqBody) {
    console.log(`[converter:chainService] ${JSON.stringify(reqBody)}`)
    result = {};
    result.userWalletAddress = reqBody.chainService.userWalletAddress;
    result.tokenId = reqBody.chainService.tokenId;
    result.serverServiceId = reqBody.chainService.serverServiceId;   
    return result;
}