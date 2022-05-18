const config = require('config')
const { resolve } = require('path/posix');
const pool = require('../database');
const util = require('../utils/network')
const logger = require('../utils/logger') 
const converter = require('../utils/converter')

const TIMEOUT = config.dbConfig.timeout;
const OFFER = 1;
const DEMAND = 0;

exports.create = function(req) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            logger.log(`[create] ${JSON.stringify(req.body)}`)
            var body = req.body;
            connection.query(
                {sql : 'INSERT INTO Profile SET name = ?, contact = ?, secret = ?', timeout: TIMEOUT},
                [body.name, body.contact, body.secret], 
                function(error, rows, fields) {
                    logger.log(`[account::create (model)] [1] rows: ${JSON.stringify(rows)}`);
                    if (error != null) {
                        logger.log(`[account::create (model)] [2] there is an error in result of SQL exec`);
                        logger.log(JSON.stringify(error));
                        let response = util.getErrorMsg(500, error);
                    } else if (rows.affectedRows == 0) {
                        logger.log(`[account::create (model)] [3] no affected rows`);
                        resolve(util.getErrorMsg(401, 'No row in db is affected'));
                    } else {
                        logger.log(`[account::create (model)] [4] success`);
                        let response = {};
                        resolve(response);
                    }
                    connection.release();
                }
            );
        });
    })
}

exports.getProfileByCell = function(req, res, credentials) {
    logger.log(`[model] getProfileByCell ${JSON.stringify(credentials)}`);
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            connection.query(
                {sql: 'SELECT * FROM Profile WHERE contact = ?', TIMEOUT},
                [credentials[0]],
                function(error, rows, fields) {
                    if (error != null) {
                        logger.log(`[account::create|model::getByCell)] [1] get error ${JSON.stringify(error)}`);
                        logger.log(JSON.stringify(error));
                        let response = util.getErrorMsg(error);
                        resolve(response);
                    } else {
                        logger.log(`[account::create|model::getByCell)] [2] rows: ${JSON.stringify(rows)}`);
                        let profiles = converter.dbToDomainProfile(rows);
                        if (profiles.length > 1) {
                            logger.log(`[account::create|model::getByCell)] [3] profiles are more then one - throw error`);
                            throw new Error("There is more than one account for this contact! Database issue.")
                        } else if (profiles.length == 1) {
                            logger.log(`[account::create|model::getByCell)] [4] leave only one account as an object ${JSON.stringify(profiles)}`);
                            profiles = profiles[0];
                        }
                        logger.log(`[account::create|model::getByCell)] [5] prepare profile as a response ${JSON.stringify(profiles)}`);
                        resolve(profiles);   
                    }
                    connection.release();
                }
            )
        });
    });
}

exports.getProfileByCellAndSecret = function(req, res, credentials) {
    logger.log(`[model] getProfileByCell ${JSON.stringify(credentials)}`);
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            connection.query(
                {sql: 'SELECT * FROM Profile WHERE contact = ? AND secret = ?', TIMEOUT},
                [credentials[0], credentials[1]],
                function(error, rows, fields) {
                    if (error != null) {
                        logger.log(`[account::create|model::getByCell)] [1] get error ${JSON.stringify(error)}`);
                        logger.log(JSON.stringify(error));
                        let response = util.getErrorMsg(error);
                        resolve(response);
                    } else {
                        logger.log(`[account::create|model::getByCell)] [2] rows: ${JSON.stringify(rows)}`);
                        let profiles = converter.dbToDomainProfile(rows);
                        if (profiles.length > 1) {
                            logger.log(`[account::create|model::getByCell)] [3] profiles are more then one - throw error`);
                            throw new Error("There is more than one account for this contact! Database issue.")
                        } else if (profiles.length == 1) {
                            logger.log(`[account::create|model::getByCell)] [4] leave only one account as an object ${JSON.stringify(profiles)}`);
                            profiles = profiles[0];
                        }
                        logger.log(`[account::create|model::getByCell)] [5] prepare profile as a response ${JSON.stringify(profiles)}`);
                        resolve(profiles);   
                    }
                    connection.release();
                }
            )
        });
    });
}

exports.getFullProfile = function(req, res, credentials) {
    logger.log(JSON.stringify(credentials));
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            connection.query(
                {sql: 'select profile.id as profileId, profile.name as profileName, profile.contact as profileContact, profile.secret as profileSecret, service.id as serviceId, service.title as serviceTitle, service.date as serviceDate, service.offer as serviceOffer, service.index as serviceIndex, service.profileId as serviceProfileId FROM Profile as profile  LEFT OUTER JOIN Service as service  ON profile.id = service.profileId  WHERE profile.contact = ? AND profile.secret = ?', timeout: TIMEOUT},
                [credentials[0], credentials[1]],
                function(error, rows, fields) {
                    if (error != null) {
                        logger.log(JSON.stringify(error));
                        let response = util.getErrorMsg(500, error);
                        resolve(response);
                    } else {
                        logger.log("Get full profile from db: " + JSON.stringify(rows));
                        let profile = converter.dbToDomainFullProfile(rows);
                        resolve(profile);
                    }
                    connection.release();
                }
            )
        });
    });
}

exports.deleteProfile = function(req, res, credentials) {
    logger.log(`[delete] ${req.params.id} + ${credentials[0]} + ${credentials[1]}`);
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            connection.query(
                {sql: 'DELETE FROM Profile WHERE id = ? AND contact = ? AND secret = ?;'},
                [req.params.id, credentials[0], credentials[1]],
                function(error, rows, fields) {
                    logger.log(`[delete] rows ${JSON.stringify(rows)}`);
                    if (error != null) {
                        let response = util.getErrorMsg(500, error);                        
                        resolve(response);
                    } else {
                        let isSuccess = rows.affectedRows != 0;
                        resolve(isSuccess);
                    }
                    connection.release();
                }
            )
        });
    });
}

exports.addOffer = function(offerAsObj, profileId) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            logger.log(`[add offer] profile id ${profileId}`);
            connection.query(
                {sql: 'INSERT INTO Service SET title = ?, date = ?, offer = ?, Service.index = ?, profileId = ?;'},
                [offerAsObj.title, offerAsObj.date, OFFER, offerAsObj.index, profileId],
                function(error, rows, fields) {
                    logger.log(`[insert offer] rows ${JSON.stringify(rows)}`)
                    let response;
                    if (error != null) {
                        response = util.getErrorMsg(500, error); 
                    } else if (rows.affectedRows == 0) {
                        response = util.getErrorMsg(401, 'No row in db is affected');
                    } else {
                        response = {};
                    }
                    resolve(response);
                    connection.release();
                }
            )
        });
    });
}

exports.addDemand = function(demandAsObj, profileId) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            logger.log(`[add demand] profile id ${profileId}`);
            connection.query(
                {sql: 'INSERT INTO Service SET title = ?, date = ?, offer = ?, Service.index = ?, profileId = ?;'}, 
                [demandAsObj.title, demandAsObj.date, DEMAND, demandAsObj.index, profileId],
                function(error, rows, fields) {
                    logger.log(`[insert offer] rows ${JSON.stringify(rows)} and error ${JSON.stringify(error)}`);
                    let response;
                    if (error != null) {
                        response = util.getErrorMsg(500, error); 
                    } else if (rows.affectedRows == 0) {
                        response = util.getErrorMsg(401, 'No row in db is affected');
                    } else {
                        response = {};
                    }
                    resolve(response);
                    connection.release();
                }
            )
        });
    });
}

exports.deleteOffer = function(req) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            connection.query(
                {sql: 'DELETE FROM Service WHERE id = ?;'},
                [req.params.id],
                function(error, rows, fields) {
                    logger.log(`[delete] rows ${JSON.stringify(rows)} and error ${JSON.stringify(error)}`);
                    if (error != null) {
                        let response = util.getErrorMsg(500, error);                        
                        resolve(response);
                    } else {
                        let isSuccess = rows.affectedRows != 0;
                        resolve(isSuccess);
                    }
                    connection.release();
                }
            )
        });
    });       
}

exports.deleteDemand = function(req) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            connection.query(
                {sql: 'DELETE FROM Service WHERE id = ?;'},
                [req.params.id],
                function(error, rows, fields) {
                    logger.log(`[delete] rows ${JSON.stringify(rows)}`);
                    if (error != null) {
                        let response = util.getErrorMsg(500, error);                        
                        resolve(response);
                    } else {
                        let isSuccess = rows.affectedRows != 0;
                        resolve(isSuccess);
                    }
                    connection.release();
                }
            )
        });
    });      
}