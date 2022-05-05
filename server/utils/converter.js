
exports.dbToDomainProfile = function(rows) {
    if (rows.length == 0) return {};
    let result = [];
    for (id = 0; id < rows.length; id++) {
        let profile = {};
        profile.id = rows[id].id;
        profile.name = rows[id].name;
        profile.contact = rows[id].contact;
        profile.secret = rows[id].secret;
        result.push(profile);
    }
    return result;
}

exports.dbToDomainFullProfile = function(rows) {
    if (rows.length == 0) return {};
    let offers = [];
    let demands = [];
    for (id = 0; id < rows.length; id++) {
        if (rows[id].serviceId === null) continue;
        let service = {};
        service.id = rows[id].serviceId;
        service.title = rows[id].serviceTitle;
        service.date = rows[id].serviceDate;
        service.index = prepareIndex(rows[id]);
        if (rows[id].serviceOffer == 1) {
            offers.push(service);
        } else {
            demands.push(service);
        }
    }
    return {
        id: rows[0].profileId,
        contact: rows[0].profileContact,
        secret: rows[0].profileSecret,
        name: rows[0].profileName,
        offers: offers,
        demands: demands
    };
}

function prepareIndex(row) {
    let index = []
    if (row.serviceIndex != null || row.serviceIndex != undefined) {
        let tokens = row.serviceIndex.split(",");
        for (aId = 0; aId < tokens.length; aId++) {
            index.push(tokens[aId]);
        }
    }
    return index;
}