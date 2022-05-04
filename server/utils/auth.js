
exports.parse = function(authHeaderValue) {
    console.log(authHeaderValue)
    if (authHeaderValue === undefined) throw new Error("Invalid argument exception. Did you pass auth header?")
    var headerValueAsTokens = authHeaderValue.split(" ")
    if (authHeaderValue == "" || headerValueAsTokens.length != 2) return this.format(true, "Did you forget to add auth header?")
    
    var decoded = Buffer.from(headerValueAsTokens[1], "base64").toString()
    if (decoded.split(":").length != 2) return this.format(true, "Did you forget to add auth header in format name:secret?")

    console.log(`[auth header] parsed as ${decoded.split(":")}`);
    return this.format(false, decoded)
}

exports.format = function(error, result) {
    return {error: error, result: result}
}
