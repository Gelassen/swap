const config = require('config')
const { resolve } = require('path/posix');
const pool = require('../database');
const { MatchTable, ChainServicesTable, ServerServicesTable, ProfileTable, ServiceTable } = require('../models/tables/schema')
const util = require('../utils/network')
const logger = require('../utils/logger') 
const converter = require('../utils/converter');

const TIMEOUT = config.dbConfig.timeout;

// TODO check connection.release() is called in each case
exports.confirmMatch = function(requesterProfileId, match, req, res) {
    logger.log(`[confirmMatch] for profile #${requesterProfileId} with object ${JSON.stringify(match)}`);
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            let sqlQuery = "";
            // keep hardcoded params in sql safe from sql injections by validating input data first 
            if (requesterProfileId === match.userFirst) {
                sqlQuery = `
                    UPDATE ${MatchTable.TABLE_NAME} 
                    SET ${MatchTable.APPROVED_BY_FIRST_USER} = true 
                    WHERE ${MatchTable.USER_FIRST_PROFILE_ID} = ${requesterProfileId};`;
            } else if (requesterProfileId === match.userSecond) {
                sqlQuery = `
                    UPDATE ${MatchTable.TABLE_NAME} 
                    SET ${MatchTable.APPROVED_BY_SECOND_USER} = true 
                    WHERE ${MatchTable.USER_SECOND_PROFILE_ID} = ${requesterProfileId};`;
            } else {
                let response = util.getMsg(400, {}, `${requesterProfileId} is not valid profile id for passed match ${match}`);
                resolve(response);
                connection.release()
                return;
            }
            connection.query(
                {sql: sqlQuery, timeout: 60000}, 
                function(error, rows, fields) {
                    if (error != null) {
                        logger.log(JSON.stringify(error));
                        let response = util.getErrorMsg(500, error);
                        resolve(response);
                    } else {
                        let response = util.getMsg(200, {}, (rows.affectedRows == 0) ? "No changes has been aplied" : `Match has been approved by user ${requesterProfileId}`);
                        resolve(JSON.stringify(response));
                    }
                    connection.release();
                }
            )
        })
    })
}

exports.getByProfileIdAndServiceIds = function(profileId, firstServiceId, secondServiceId) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            if (err) throw err;

            const sql = `SELECT * 
                FROM ${MatchTable.TABLE_NAME} 
                WHERE 
                    (
                        ${MatchTable.USER_FIRST_PROFILE_ID} = ${profileId}
                         OR  
                        ${MatchTable.USER_SECOND_PROFILE_ID} = ${profileId}
                    )
                     AND 
                    (
                        (
                            ${MatchTable.USER_FIRST_SERVICE_ID} = ${firstServiceId} 
                             AND
                            ${MatchTable.USER_SECOND_SERVICE_ID} = ${secondServiceId}
                        )
                         OR 
                        (
                            ${MatchTable.USER_FIRST_SERVICE_ID} = ${secondServiceId} 
                             AND
                            ${MatchTable.USER_SECOND_SERVICE_ID} = ${firstServiceId}
                        )

                    ) 

            ;`;
            logger.log("sql query: " + sql)
            connection.query(
                {sql: sql, TIMEOUT},
                [], 
                function(error, rows, fields) {
                    logger.log(`rows - ${JSON.stringify(rows)}`);
                    if (error != null) {
                        logger.log(JSON.stringify(error));
                        let response = util.getErrorMsg(500, error);
                        resolve(response);
                    } else if (rows.length != 1) {
                        let msg = (rows.length == 0) ? "There is no known matches for this input params" 
                            : "There is more than a single row for this input params." 
                        let response = util.getServiceMessage(409, msg);
                        resolve(response);
                    } else {
                        let data = converter.dbToDomainServerMatch(rows);
                        let response = util.getMsg(200, data);
                        resolve(response);
                    }
                    connection.release();
                }
            )
        })
    })
}

exports.getByProfileId = function(profileId, page, size) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            if (err) throw err;
            // make sure you prevent sql injection by validating first ${profileId}
            const sql = `SELECT 
                ${MatchTable.TABLE_NAME}.${MatchTable.ID} as id, userFirstProfileId, userSecondProfileId, userFirstServiceId, userSecondServiceId, approvedByFirstUser, approvedBySecondUser, 
                firstUserProfile.name as userFirstProfileName, secondUserProfile.name as userSecondProfileName, firstService.title as firstServiceTitle, secondService.title as secondServiceTitle,
                chainServicesFirst.idChainService as idChainServiceFirst, chainServicesFirst.userWalletAddress as userAddressFirst, chainServicesFirst.tokenId as tokenIdFirst, 
                chainServicesSecond.idChainService as idChainServiceSecond, chainServicesSecond.userWalletAddress as userAddressSecond, chainServicesSecond.tokenId as tokenIdSecond 
                FROM ${MatchTable.TABLE_NAME} 
                INNER JOIN ${ChainServicesTable.TABLE_NAME} as chainServicesFirst
                ON ${MatchTable.USER_FIRST_SERVICE_ID} = chainServicesFirst.${ChainServicesTable.SERVER_SERVICE_ID}
                INNER JOIN ${ChainServicesTable.TABLE_NAME} as chainServicesSecond 
                ON ${MatchTable.USER_SECOND_SERVICE_ID} = chainServicesSecond.${ChainServicesTable.SERVER_SERVICE_ID}
                INNER JOIN ${ProfileTable.TABLE_NAME} as firstUserProfile 
                ON ${MatchTable.TABLE_NAME}.userFirstProfileId = firstUserProfile.${ProfileTable.ID} 
                INNER JOIN ${ProfileTable.TABLE_NAME} as secondUserProfile 
                ON ${MatchTable.TABLE_NAME}.userSecondProfileId = secondUserProfile.${ProfileTable.ID} 
                INNER JOIN ${ServiceTable.TABLE_NAME} as firstService 
                ON firstService.id = ${MatchTable.TABLE_NAME}.${MatchTable.USER_FIRST_SERVICE_ID} 
                INNER JOIN ${ServiceTable.TABLE_NAME} as secondService 
                ON secondService.id = ${MatchTable.TABLE_NAME}.${MatchTable.USER_SECOND_SERVICE_ID}
                WHERE 
                (
                    ${MatchTable.USER_FIRST_PROFILE_ID} = ${profileId}
                     OR  
                    ${MatchTable.USER_SECOND_PROFILE_ID} = ${profileId} 
                ) 
                ${prepareLimitClause(page, size)}
            ;`;
            logger.log("sql query: " + sql)
            connection.query(
                {sql: sql, TIMEOUT},
                [], 
                function(error, rows, fields) {
                    logger.log(`rows - ${JSON.stringify(rows)}`);
                    if (error != null) {
                        logger.log(JSON.stringify(error));
                        let response = util.getErrorMsg(500, error);
                        resolve(response);
                    } else {
                        let data = converter.dbToNewDomainServerMatch(rows);
                        resolve(data);
                    }
                    connection.release();
                }
            )
        })
    })
}


exports.makePotentialMatch = function(profileId, userDemand, req, res) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            connection.beginTransaction(function(error) {
                if (error) throw err;
                const sqlQuery = 
                `
                    SELECT *
                    FROM 
                        (
                            SELECT * 
                            FROM ${ServerServicesTable.TABLE_NAME} 
                            WHERE offer = 1
                                AND profileId != ?
                                AND title LIKE(?) 
                        ) AS offers
                    INNER JOIN LATERAL
                        (
                            SELECT q1.id as Q1Id, q2.id as Q2Id, q1.title as Q1Title, q2.title as Q2Title, q1.profileId as Q1ProfileId, q2.profileId as Q2ProfileId
                            FROM
                            (
                                SELECT * 
                                FROM ${ServerServicesTable.TABLE_NAME} 
                                WHERE 
                                    (
                                        offer = 1
                                        AND profileId = ?
                                    )
                            ) as q1
                            INNER JOIN LATERAL
                            (	
                                SELECT *
                                FROM ${ServerServicesTable.TABLE_NAME}
                                WHERE 
                                    (
                                        offer = 0
                                        AND profileId != ?
                                        AND title LIKE(q1.title)
                                    ) 
                            ) q2
                            ON q1.title = q2.title
                        ) AS giveBack
                    ON offers.profileId = giveBack.Q2ProfileId
                    ORDER BY title;
                `;
                // INNER JOIN ${ChainServicesTable.TABLE_NAME} as chainServices
                // ON id = chainServices.serverServiceId 
                // LEFT OUTER JOIN ${ChainServicesTable.TABLE_NAME} as chainServices2
                // ON Q1Id = chainServices2.serverServiceId 
                logger.log("sql query: " + sqlQuery);
                connection.query(
                    {sql: sqlQuery, TIMEOUT},
                    [
                        profileId, "%" +  userDemand.title + "%", // known escape upper comma issue
                        profileId, profileId
                    ],
                    function(error, rows, fields) {
                        logger.log(`rows - ${JSON.stringify(rows)}`);
                        if (error != null) {
                            return connection.rollback(function() {
                                connection.release();
                                throw error;
                            })
                        }
                        let matches = converter.syntheticDbToDomainServerMatch(rows);
                        logger.log(`potential matches before insert: ${JSON.stringify(matches)}`);
                        // TODO ref issue with appearing profile - had a thought caused by transaction has not been finished 
                        if (matches.length == 0) {
                            connection.commit(function(error) {
                                if (error != null) {
                                    return connection.rollback(function() {
                                        connection.release();
                                        throw error;
                                    });
                                }
                                var response = {}
                                connection.release()
                                resolve(response)
                            })  
                        } else {
                            const sqlBulkInsert = prepareMatchBulkInsertQuery(matches);
                            logger.log(`bulk insert query: ${sqlBulkInsert}`);
                            connection.query(
                                { sql: sqlBulkInsert, timeout: TIMEOUT }, 
                                function(error, rows, fields) {
                                    logger.log(`[sqlBulkInsert] Rows for insert: ${JSON.stringify(rows)}`);
                                    logger.log(`[sqlBulkInsert] errors: ${JSON.stringify(error)}`);
                                    if (error != null) {
                                        logger.log(`[sqlBulkInsert] error case, rollback`);
                                        return connection.rollback(function() {
                                            connection.release();
                                            throw error;
                                        });
                                    } else {
                                        logger.log(`[sqlBulkInsert] success case, try to commit`);
                                        connection.commit(function(error) {
                                            logger.log(`[sqlBulkInsert] has been committed, return response`);
                                            if (error != null) {
                                                return connection.rollback(function() {
                                                    connection.release();
                                                    throw error;
                                                });
                                            }
                                            var response = {};
                                            connection.release();
                                            resolve(response);
                                        })   
                                    }
                                });
                        }
                        
                    }
                )
                
            })
        });
    })
}

/*
    Do the same like makePotentialMatch() call, but synchronously. Make sure it 
    is called from async thread. 
 */
exports.makePotentialMatchSync = function(profileId, userDemand, callback) {
    logger.log(`[makePotentialMatchSync] start for profile #${profileId}`);
    pool.getConnection(function(err, connection) {
        connection.beginTransaction(function(error) {
            if (error) throw err;
            const sqlQuery = 
            `
                SELECT *
                FROM 
                    (
                        SELECT * 
                        FROM ${ServerServicesTable.TABLE_NAME} 
                        WHERE offer = 1
                            AND profileId != ${profileId}
                            AND title LIKE('%${userDemand.title}%') 
                    ) AS offers
                INNER JOIN LATERAL
                    (
                        SELECT q1.id as Q1Id, q2.id as Q2Id, q1.title as Q1Title, q2.title as Q2Title, q1.profileId as Q1ProfileId, q2.profileId as Q2ProfileId
                        FROM
                        (
                            SELECT * 
                            FROM ${ServerServicesTable.TABLE_NAME}
                            WHERE 
                                (
                                    offer = 1
                                    AND profileId = ${profileId}
                                )
                        ) as q1
                        INNER JOIN LATERAL
                        (	
                            SELECT *
                            FROM ${ServerServicesTable.TABLE_NAME}
                            WHERE 
                                (
                                    offer = 0
                                    AND profileId != ${profileId}
                                    AND title LIKE(q1.title)
                                ) 
                        ) q2
                        ON q1.title = q2.title
                    ) AS giveBack
                ON offers.profileId = giveBack.Q2ProfileId

                ORDER BY title;
            `;
                            // INNER JOIN ${ChainServicesTable.TABLE_NAME} as chainServices
                // ON id = chainServices.serverServiceId 
                // LEFT OUTER JOIN ${ChainServicesTable.TABLE_NAME} as chainServices2
                // ON Q1Id = chainServices2.serverServiceId 
            logger.log("sql query: " + sqlQuery);
            connection.query(
                {sql: sqlQuery, TIMEOUT},
                [],
                function(error, rows, fields) {
                    logger.log(`rows - ${JSON.stringify(rows)}`);
                    if (error != null) {
                        return connection.rollback(function() {
                            connection.release();
                            throw error;
                        })
                    }
                    let matches = converter.syntheticDbToDomainServerMatch(rows);
                    logger.log(`potential matches before insert: ${JSON.stringify(matches)}`);
                    if (matches.length == 0) {
                        var response = {}
                        connection.release()
                        callback(response)
                        return;
                    }
                    const sqlBulkInsert = prepareMatchBulkInsertQuery(matches);
                    logger.log(`bulk insert query: ${sqlBulkInsert}`);
                    connection.query(
                        { sql: sqlBulkInsert, timeout: TIMEOUT }, 
                        function(error, rows, fields) {
                            logger.log(`[sqlBulkInsert] Rows for insert: ${JSON.stringify(rows)}`);
                            logger.log(`[sqlBulkInsert] errors: ${JSON.stringify(error)}`);
                            if (error != null) {
                                logger.log(`[sqlBulkInsert] error branch ${JSON.stringify(error)}`);
                                return connection.rollback(function() {
                                    connection.release();
                                    throw error;
                                });
                            } else {
                                logger.log(`[sqlBulkInsert] commiting positive tx scenario`);
                                connection.commit(function(error) {
                                    logger.log(`[sqlBulkInsert] commit positive result branch ${JSON.stringify(error)}`);
                                    if (error != null) {
                                        logger.log(`[sqlBulkInsert] error within commit positive result branch ${JSON.stringify(error)}`);
                                        return connection.rollback(function() {
                                            connection.release();
                                            throw error;
                                        });
                                    }
                                    logger.log(`[sqlBulkInsert] successfully commit the result`);
                                    var response = {}
                                    connection.release()
                                    callback(response)
                                })   
                            }
                        });
                }
            )
            
        })
    });
    logger.log(`[makePotentialMatchSync] end for profile #${profileId}`);
}

// TODO check baseSqlQuery string is built with commas between inserted values
function prepareMatchBulkInsertQuery(matches) {
    if (matches.length == 0) {
        throw { 'error' : "Empty array as input param is not allowed. Did you forget your input data before call?"};
    }
    let baseSqlQuery = `INSERT INTO ${MatchTable.TABLE_NAME}
        (
            id, userFirstProfileId, userSecondProfileId, 
            userFirstServiceId, userSecondServiceId,
            approvedByFirstUser, approvedBySecondUser
        ) 
        VALUES `;
        logger.log(`matches: ${JSON.stringify(matches)}`);
    for (var idx = 0; idx < matches.length; idx++) {
        let item = matches[idx];
        logger.log(`Item in matches loop: ${JSON.stringify(item)}`);
        baseSqlQuery += `(
            ${item.id}, 
            ${item.userFirstProfileId}, 
            ${item.userSecondProfileId}, 
            ${item.userFirstServiceId}, 
            ${item.userSecondServiceId},
            ${item.approvedByFirstUser}, 
            ${item.approvedBySecondUser}
            ),`;
    }
    return baseSqlQuery.substring(0, baseSqlQuery.length - 1) + ";";
}


function prepareLimitClause(page, size) {
    const MIN_VALUE = 1;
    let result = '';
    if (page < MIN_VALUE || page == MIN_VALUE) {
        page = MIN_VALUE;
    }
    result = `LIMIT ${size} OFFSET ${page * size - size}`;
    return result;
}

