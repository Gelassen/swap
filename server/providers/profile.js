
exports.prepareIndex = function(str) {
    let index = [];
    index.push(str);
    return index;
}

exports.isAnyMatchForProfile = function(matches) {
    return (matches.length > 0)
}

exports.isThereSuchService = function(profileServices, subject) {
    let result = false;
    for (let id = 0; id < profileServices.length; id++) {
        if (profileServices.at(id).title === subject.title
        && profileServices.at(id).date === subject.date
        && profileServices.at(id).index.length === subject.index.length
        && profileServices.at(id).index.sort().every((value, index) => value === subject.index[index])) {
            result = true;
            break;
        }
    }
    return result;
}

exports.thereIsNoThisService = function(profileServices, requestId) {
    if (profileServices.length == 0) return true;

    let result = false;
    for (let id = 0; id < profileServices.length; id++) {
        if (profileServices.at(id) === requestId) {
            /* return false */
            break;
        }
    }
    return result;
} 

exports.thereIsSuchData = function(obj) {
    return Object.keys(obj).length
}

/**
 * @deprecated
 */
exports.noSuchData = function(obj) {
    return !Object.keys(obj).length
}

exports.isAttemptToSignIn = function(profile, reqSecret) {
    return Object.keys(profile).length && profile.secret === reqSecret;
}

exports.profileMatch = function(matches, matchObjectFromRequest) {
    // for ()
}