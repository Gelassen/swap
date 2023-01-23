
/* member fields definition https://stackoverflow.com/a/72278342/3649629 */

class MatchTable {
    static TABLE_NAME = "PotentialMatches"
    static ID = "id"
    static USER_FIRST_PROFILE_ID = "userFirstProfileId"
    static USER_SECOND_PROFILE_ID = "userSecondProfileId"
    static USER_FIRST_SERVICE_ID = "userFirstServiceId"
    static USER_SECOND_SERVICE_ID = "userSecondServiceId"
    static APPROVED_BY_FIRST_USER = "approvedByFirstUser"
    static APPROVED_BY_SECOND_USER = "approvedBySecondUser"

    constructor() { /* no op */}
}

class ChainServicesTable {
    static TABLE_NAME = "ChainServices"
    static ID = "id"
    static USER_ADDRESS = "userAddress"
    static TOKEN_ID = "tokenId"
    static SERVER_SERVICE_ID = "serverServiceId"

    constructor() { /* no op */}
}

class ServerServicesTable {
    static TABLE_NAME = "Service"

    constructor() { /* no op */} 
}

module.exports = { MatchTable, ChainServicesTable, ServerServicesTable }