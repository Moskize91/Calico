// calico system library

exports.Configuration = function() {
    this.configure = {
        port                        : 8080,
        template_directory          : "./",
        target_directory            : "./target",
        resource_directory          : "./resource",
        root_page                   : "/main.html",
        seeds                       : ["/"],
        linked_resource_assets_path : [],
        ignore_copy                 : /$^/,
        ignore_clean                : /.*\.git\/.*/,
        redirect                    : {},
    };
};

exports.Configuration.prototype.value_of_string = function(name) {
    if (!this.configure[name]) {
        return null;
    }
    return "" + this.configure[name];
};

exports.Configuration.prototype.value_of_array = function(name) {
    if (!this.configure[name]) {
        return null;
    }
    return Java.to(this.configure[name], Java.type("java.lang.String[]"));
};

exports.Configuration.prototype.value_of_map = function(name) {
    return this.configure[name];
};

exports.Configuration.prototype.value_of_pattern = function(name) {
    if (!this.configure[name]) {
        return null;
    }
    return ("" + this.configure[name]).replace(/(^\/|\/$)/g, "");
}

exports.Configuration.prototype.value_of_integer = function(name) {
    if (!this.configure[name]) {
        return 0;
    }
    return parseInt(this.configure[name]);
}