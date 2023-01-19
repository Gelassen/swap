const config = require('config')
const { resolve } = require('path/posix');
const pool = require('../database');
const util = require('../utils/network')
const logger = require('../utils/logger') 
const converter = require('../utils/converter');
const c = require('config');

exports.getByProfileId = function(profileId, req, res) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            if (err) throw err;

            const sql = `SELECT * FROM Matches WHERE profileId != ${profileId}`;
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
                        let response = converter.syntheticDbToDomainServerMatch(rows);
                        resolve(response);
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
                // TODO check LIKE('%${userDemand}%') works as well as LIKE('%Software%'), there wss a precedent this was not the same
                const sqlQuery = 
                `
                    SELECT *
                    FROM 
                        (
                            SELECT * 
                            FROM Service 
                            WHERE offer = 1
                                AND profileId != ${profileId}
                                AND title LIKE('%${userDemand}%') 
                        ) AS offers
                    LEFT OUTER JOIN LATERAL
                        (
                            SELECT q1.id as Q1Id, q2.id as Q2Id, q1.title as Q1Title, q2.title as Q2Title, q1.profileId as Q1ProfileId, q2.profileId as Q2ProfileId
                            FROM
                            (
                                SELECT * 
                                FROM Service
                                WHERE 
                                    (
                                        offer = 1
                                        AND profileId = ${profileId}
                                    )
                            ) as q1
                            INNER JOIN LATERAL
                            (	
                                SELECT *
                                FROM Service
                                WHERE 
                                    (
                                        offer = 0
                                        AND profileId != ${profileId}
                                        AND title LIKE(q1.title)
                                    ) 
                            ) q2
                            ON q1.title = q2.title
                        ) AS giveBack
                    ON offers.profileId = giveBack.Q2ProfileId;
                `;
                logger.log("sql query: " + sqlQuery);
                connection.query(
                    {sql: sqlQuery, TIMEOUT},
                    [],
                    function(error, rows, fields) {
                        logger.log(`rows - ${JSON.stringify(error)}`);
                        if (error != null) {
                            return connection.rollback(function() {
                                connection.release();
                                throw error;
                            })
                        }
                        let matches = converter.syntheticDbToDomainServerMatch(rows);
                        const sqlBulkInsert = prepareMatchBulkInsertQuery(matches);
                        connection.query(
                            { sql: sqlBulkInsert, timeout: TIMEOUT }, 
                            function(error, rows, fields) {
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
                                    var response = util.getPayloadMessage(payload)
                                    console.log(JSON.stringify(response))
                                    connection.release()
                                    resolve(JSON.stringify(response))
                                })                                
                            });
                    }
                )
                
            })
        });
    })
}

// TODO check baseSqlQuery string is built with commas between inserted values
function prepareMatchBulkInsertQuery(matches) {
    if (matches.length == 0) {
        throw { 'error' : "Empty array as input param is not allowed. Did you forget your input data before call?"};
    }
    let baseSqlQuery = `INSERT 
        (
            id, userFirstProfileId, userSecondProfileId, 
            userFirstServiceId, userSecondServiceId,
            approvedByFirstUser, approvedBySecondUser
        ) 
        VALUES `;
    for (item in matches) {
        baseSqlQuery += `(
            ${  item.id, item.userFirstProfileId, item.userSecondProfileId, 
                item.userFirstServiceId, item.userSecondServiceId,
                item.approvedByFirstUser, item.approvedBySecondUser
            }
            )`;
    }
    return baseSqlQuery;
}