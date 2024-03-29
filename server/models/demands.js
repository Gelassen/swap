const config = require('config')
const { resolve } = require('path/posix');
const pool = require('../database');
const util = require('../utils/network')
const logger = require('../utils/logger') 
const converter = require('../utils/converter')
const { ChainServicesTable } = require('../models/tables/schema')

const TIMEOUT = config.dbConfig.timeout;
const OFFER = 1;
const DEMAND = 0;

exports.getDemands = function(fullProfile, page, size) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            if (err) throw err;
            // offers have assigned chain's records, but demands doesn't have it
            const sql = `SELECT * FROM Service 
                LEFT OUTER JOIN ${ChainServicesTable.TABLE_NAME} 
                ON Service.id = ${ChainServicesTable.TABLE_NAME}.${ChainServicesTable.SERVER_SERVICE_ID} 
                WHERE profileId != ${fullProfile.id} 
                AND offer = ${DEMAND}  
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

function prepareLimitClause(page, size) {
    const MIN_VALUE = 1;
    let result = '';
    if (page < MIN_VALUE || page == MIN_VALUE) {
        page = MIN_VALUE;
    }
    result = `LIMIT ${size} OFFSET ${page * size - size}`;
    return result;
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