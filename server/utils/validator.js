exports.validateProfilePayload = function(reqBody) {
    console.log(reqBody.contact)
    console.log(reqBody.secret)
    return this.validateString(reqBody.contact) 
        && this.validateString(reqBody.secret) 
}

exports.validateString = function(str) {
    return str !== '' && str !== null && str !== undefined
}