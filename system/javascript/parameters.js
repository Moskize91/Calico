// calico system library

function Parameters(params) {
    this._params = params;
    params = params.replace(/(^\/|\/$)/g, "").replace(/\.\w+$/g, "");
    this._components = params.split("/");
}

Parameters.prototype.pick = function(format) {
    var result = {};
    format = format.replace(/(^\/|\/$)/g, "");
    var format_components = format.split("/");
    for (var i = 0; i < format_components.length; i++) {
        var format_component = format_components[i];
        var param_component = this._components[i];
        if (format_component.startsWith(":")) {
            var keyName = format_component.substring(1);
            result[keyName] = param_component;
        } else {
            if (format_component != param_component) {
                throw new Error(["parameters `", this._params, 
                                "` not match format `", format, "`"].join(""));
            }
        }
    }
    return result;
}

exports.Parameters = Parameters;