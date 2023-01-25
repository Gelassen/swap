const config = require('config')
const { resolve } = require('path/posix');
const pool = require('../database');
const { MatchTable, ChainServicesTable, ServerServicesTable } = require('../models/tables/schema')
const util = require('../utils/network')
const logger = require('../utils/logger') 
const converter = require('../utils/converter');

const TIMEOUT = config.dbConfig.timeout;

// TODO check connection.release() is called in each case
exports.confirmMatch = function(requesterProfileId, match, req, res) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            let sqlQuery = "";
            // keep hardcoded params in sql safe from sql injections by validating input data first 
            if (requesterProfileId == match.userFirstProfileId) {
                sqlQuery = `
                    UPDATE ${MatchTable.TABLE_NAME} 
                    SET ${MatchTable.approvedByFirstUser} = true 
                    WHERE ${MatchTable.userFirstProfileId} = ${requesterProfileId};`;
            } else if (requesterProfileId == match.userSecondProfileId) {
                sqlQuery = `
                    UPDATE ${MatchTable.TABLE_NAME} 
                    SET ${MatchTable.approvedBySecondUser} = true 
                    WHERE ${MatchTable.userSecondProfileId} = ${requesterProfileId};`;
            } else {
                let response = util.getMsg(400, {}, `${requesterProfileId} is not valid profile id for passed match ${match}`);
                resolve(response);
                connection.release()
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
                        ${MatchTable.USER_FIRST_PROFILE_ID} == ${profileId}
                         OR  
                        ${MatchTable.USER_SECOND_PROFILE_ID} == ${profileId}
                    )
                     AND 
                    (
                        (
                            ${MatchTable.USER_FIRST_SERVICE_ID} == ${firstServiceId} 
                             AND
                            ${MatchTable.USER_SECOND_SERVICE_ID} == ${secondServiceId}
                        )
                         OR 
                        (
                            ${MatchTable.USER_FIRST_SERVICE_ID} == ${secondServiceId} 
                             AND
                            ${MatchTable.USER_SECOND_SERVICE_ID} == ${firstServiceId}
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
                    } else if (rows.count() != 1) {
                        let msg = (rows.count() == 0) ? "There is no known matches for this input params" 
                            : "There is more than a single row for this input params." 
                        let response = util.getServiceMessage(409, msg);
                        resolve(response);
                    } else {
                        let data = converter.dbToDomainServerMatch(rows);
                        let response = util.getMsg(200, data);
                        resolve(JSON.stringify(response));
                    }
                    connection.release();
                }
            )
            connection.release();
        })
    })
}

exports.getByProfileId = function(profileId, req, res) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            if (err) throw err;
            // make sure you prevent sql injection by validating first ${profileId}
            const sql = `SELECT * 
                FROM ${MatchTable.TABLE_NAME} 
                WHERE 
                    ${MatchTable.USER_FIRST_PROFILE_ID} = ${profileId}
                     OR  
                    ${MatchTable.USER_SECOND_PROFILE_ID} = ${profileId}
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
                        let data = converter.dbToDomainServerMatch(rows);
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
                    INNER JOIN ${ChainServicesTable.TABLE_NAME} as chainServices
                    ON id = chainServices.serverServiceId 
                    LEFT OUTER JOIN ${ChainServicesTable.TABLE_NAME} as chainServices2
                    ON Q1Id = chainServices2.serverServiceId 
                    ORDER BY title;
                `;
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
                            var response = util.getMsg(200, [])
                            connection.release()
                            resolve(JSON.stringify(response))
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
                                    return connection.rollback(function() {
                                        connection.release();
                                        throw error;
                                    });
                                }
                                connection.commit(function(error) {
                                    if (error != null) {
                                        return connection.rollback(function() {
                                            connection.release();
                                            throw error;
                                        });
                                    }
                                    var payload = util.getServiceMessage(util.statusSuccess, "")
                                    var response = util.getMsg(payload)
                                    connection.release()
                                    resolve(response)
                                })                                
                            });
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
                INNER JOIN ${ChainServicesTable.TABLE_NAME} as chainServices
                ON id = chainServices.serverServiceId 
                LEFT OUTER JOIN ${ChainServicesTable.TABLE_NAME} as chainServices2
                ON Q1Id = chainServices2.serverServiceId 
                ORDER BY title;
            `;
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
                        var response = util.getMsg(200, [])
                        connection.release()
                        callback(JSON.stringify(response))
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
                                return connection.rollback(function() {
                                    connection.release();
                                    throw error;
                                });
                            }
                            connection.commit(function(error) {
                                if (error != null) {
                                    return connection.rollback(function() {
                                        connection.release();
                                        throw error;
                                    });
                                }
                                var response = {}
                                connection.release()
                                callback(response)
                            })                                
                        });
                }
            )
            
        })
    });
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

