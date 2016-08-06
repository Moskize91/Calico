// calico system library

function Parameters(params) {
    params = params.replace(/(^\/|\/$)/g, "").replace(/\.\w+$/g, "");
    var components = params.split("/");

    var key = null;
    this.paramsMap = {};
    components.forEach(function(component) {
        if (!key) {
            key = component;
        } else {
            this.paramsMap[key] = component;
            key = null;
        }
    });
    if (key) { // the last component not match value.
        this.paramsMap[key] = key;
    }
}

Parameters.prototype.value = function(key, default_value) {
    return this.paramsMap[key] || default_value;
};

exports.Parameters = Parameters;