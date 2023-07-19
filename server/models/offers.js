const config = require('config');
const { resolve } = require('path/posix');
const pool = require('../database');
const util = require('../utils/network');
const logger = require('../utils/logger');
const converter = require('../utils/converter');
const schema = require('../models/tables/schema');

const TIMEOUT = config.dbConfig.timeout;
const OFFER = 1;
const DEMAND = 0;

exports.getOffers = function(fullProfile, page, size) {
    logger.log(`[get service from model] ${page}`);
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            if (err) throw err;
            
            const sql = `SELECT * FROM Service 
                WHERE profileId != ${fullProfile.id} 
                AND offer = ${OFFER}  
                ${prepareLimitClause(page, size)}`;
            logger.log("sql query: " + sql);
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
                        let services = converter.offersDbToDomainService(rows);
                        logger.log(`[get offers] rows to domain - ${JSON.stringify(services)}`);
                        let response = services;
                        resolve(response);
                    }
                    connection.release();
                }
            )
        })
    });
}

exports.addOffer = function(offerAsObj, profileId) {
    return new Promise((resolve) => {
        logger.log(`[add offer] profile id ${profileId}`);
        pool.getConnection(function(err, connection) {
            connection.beginTransaction(function(error) {
                if (err) {
                    return connection.rollback(function() {
                        connection.release();
                        throw err;
                    })
                }
            
                let chainServiceObj = offerAsObj.chainService;
                connection.query(
                    {sql: 'INSERT INTO Service SET title = ?, date = ?, offer = ?, Service.index = ?, profileId = ?;'},
                    [offerAsObj.title, offerAsObj.date, OFFER, offerAsObj.index, profileId],
                    function(error, rows, fields) {
                        logger.log(`[insert offer] rows ${JSON.stringify(rows)}`)
                        if (error != null) {
                            return connection.rollback(function() {
                                connection.release();
                                throw error;
                            })
                        }
                        let response;
                        if (error != null) {
                            response = util.getErrorMsg(500, error); 
                        } else if (rows.affectedRows == 0) {
                            response = util.getErrorMsg(401, 'No row in db is affected');
                        } else {
                            chainServiceObj.serverServiceId = rows.insertId;
                            response = {};
                        }
                        if (response.code == 500 || response.code == 401) {
                            connection.commit(function(error) {
                                if (error != null) {
                                    return connection.rollback(function() {
                                        connection.release();
                                        throw error;
                                    });
                                }
                                connection.release()
                                resolve(response)
                            })
                        } else {
                            const sqlChainServicesInsert = `INSERT INTO ${schema.ChainServicesTable.TABLE_NAME} 
                                SET 
                                ${schema.ChainServicesTable.USER_ADDRESS}=?, 
                                ${schema.ChainServicesTable.TOKEN_ID}=?, 
                                ${schema.ChainServicesTable.SERVER_SERVICE_ID}=?;`
                            connection.query(
                                {sql: sqlChainServicesInsert},
                                [chainServiceObj.userWalletAddress, chainServiceObj.tokenId, chainServiceObj.serverServiceId],
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
                                        var response = {};
                                        connection.release()
                                        resolve(response)
                                    })    
                                }
                            )
                        }
                    }
                )
            });
        });
    });
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

/**
 * @deprecated since v2 and match moves to 'chains' tab
 */
function prepareWhereClause(...conditions) {
    let result = "";
    conditions.forEach((items) => {
        items.forEach((item) => {
            result += ` Service.index LIKE '%${item.index}%' OR `;
        })
    });
    if (result.length != 0) {
        let lastThreeChars= 3;
        result = result.slice(0, result.length - lastThreeChars);
    }
    return result;
}