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