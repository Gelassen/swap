
export class MatchTable {
    TABLE_NAME = "PotentialMatches"
    ID = "id"
    USER_FIRST_PROFILE_ID = "userFirstProfileId"
    USER_SECOND_PROFILE_ID = "userSecondProfileId"
    USER_FIRST_SERVICE_ID = "userFirstServiceId"
    USER_SECOND_SERVICE_ID = "userSecondServiceId"
    APPROVED_BY_FIRST_USER = "approvedByFirstUser"
    APPROVED_BY_SECOND_USER = "approvedBySecondUser"

    constructor() { /* no op */}
}

export class ChainServicesTable {
    TABLE_NAME = "ChainServices"
    ID = "id"
    USER_ADDRESS = "userAddress"
    TOKEN_ID = "tokenId"
    SERVER_SERVICE_ID = "serverServiceId"

    constructor() { /* no op */}
}

export class ServerServicesTable {
    TABLE_NAME = "Service"

    constructor() { /* no op */} 
}