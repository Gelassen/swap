var mysql = require('mysql')
const config = require('config');

const dbConfig = config.get('dbConfig');

var pool = mysql.createPool(dbConfig);

pool.on('connection', function(connection) {
    console.log('connected to database')

    connection.on('error', function(err) {
        console.log(new Date(), "MySQL error", err.code)
    });

    connection.on('close', function(connection) {
        console.log(new Date(), "MySQL close", err)
    })

});

pool.on('release', function (connection) {
    console.log('Connection %d released', connection.threadId);
});

module.exports = {
    getConnection: (callback) => {
        return pool.getConnection(callback)
    },

    escape: (param) => {
        return pool.escape(param)
    },
    
    status: () => {
      console.log("passed in max size of the pool", pool.config.connectionLimit)
      console.log("number of free connections awaiting use", pool._freeConnections.length) 
      console.log("number of connections currently created, including ones in use", pool._allConnections.length)
      console.log("number of connections in the process of being acquired", pool._acquiringConnections.length) 
    } 
}
