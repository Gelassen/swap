const config = require('config')
const { resolve } = require('path/posix');
const pool = require('../database');
const util = require('../utils/network')
const logger = require('../utils/logger') 
const converter = require('../utils/converter')

const TIMEOUT = config.dbConfig.timeout;

exports.getContactsByServiceId = function(serviceId) {
    return new Promise((resolve) => {
        pool.getConnection(function(err, connection) {
            const sql = `SELECT p.id, p.name, p.contact
                FROM Profile as p
                INNER JOIN Service 
                ON Service.profileId = p.id
                WHERE Service.id = ?;`
            connection.query(
                {sql: sql, timeout: TIMEOUT},
                [serviceId],
                function(error, rows, fields) {
                    logger.log(`rows - ${JSON.stringify(rows)}`);
                    if (error != null) {
                        logger.log(JSON.stringify(error));
                        let response = util.getErrorMsg(500, error);
                        resolve(response);
                    } else {
                        let services = converter.contactsDbToDomain(rows);
                        logger.log(`[get contacts] rows to domain - ${JSON.stringify(services)}`);
                        let response = services;
                        resolve(response);
                    }
                    connection.release();
                }
            )
        })
    });

}