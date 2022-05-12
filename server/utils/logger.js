
const config = require('config');

exports.log = function(str) {
    if (!config.logs.isAllowed) return;
    console.log(str) 
}