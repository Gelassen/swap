const config = require('config')
const { resolve } = require('path/posix');
const pool = require('../database');
const util = require('../utils/network')
const logger = require('../utils/logger') 
const converter = require('../utils/converter')
const profile = require('../models/profile')

const TIMEOUT = config.dbConfig.timeout;
const OFFER = 1;
const DEMAND = 0;

exports.getOffers = function(fullProfile) {
    logger.log(`[get offers] start`)
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            const sql = `SELECT * FROM Service 
                WHERE profileId != ${fullProfile.id} 
                AND offer = ${OFFER} 
                AND (${prepareWhereClause(fullProfile.demands)})`;
            logger.log("sql query: " + sql);
            connection.query(
                {sql: sql},
                [],
                function(error, rows, fields) {
                    if (error != null) {
                        logger.log(JSON.stringify(error));
                        let response = util.getErrorMsg(500, error);
                        resolve(response);
                    } else {
                        let services = converter.dbToDomainService(rows);
                        logger.log(JSON.stringify(services));
                        let response = util.getMsg(services);
                        resolve(response);
                    }
                    connection.release();
                    logger.log(`[get offers] end`)
                }
            )
        })
    });
}

function prepareWhereClause(...conditions) {
    let result = "";
    logger.log(JSON.stringify(conditions));
    conditions.forEach((items) => {
        items.forEach((item) => {
            logger.log(`[prepareWhereClause] ${item}`);
            result += ` Service.index LIKE '%${item.index}%' OR `;
        })
    });
    if (result.length != 0) {
        let lastThreeChars= 3;
        result = result.slice(0, result.length - lastThreeChars);
    }
    return result;
}