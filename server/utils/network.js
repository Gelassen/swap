global.noAuthHeaderMsg = "There is no auth header.";
global.authHeader = "Authorization";

exports.getMsg = function(payload) {
    return { payload }
}

exports.getMsg = function(code, payload) {
    return { code, payload }
}

exports.getErrorMsg = function(code, payload) {
    return { code: code, payload }
}

exports.getMessage = function(code, message) {
    return {code: code, message: message, payload: {}};
}

exports.getPayloadMessage = function(payload) {
    return {payload: payload}
}

exports.getErrorMessage = function() {
    return {code: 500, message: "unsuccess", payload: {}}
}

exports.getErrorMessage = function(payload) {
    return {code: 500, message: "unsuccess", payload: payload}
}

exports.getErrorMessage = function(code, message) {
    return {code: code, message: message, payload: {}}
}

exports.getErrorMessage = function(code, message, payload) {
    return {code: code, message: message, payload: payload}
}

exports.statusSuccess = 1
exports.statusFailed = 0

exports.getServiceMessage = function(statusCode, message) {
    return {status: statusCode, message: message}
}