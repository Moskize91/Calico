// calico system library

M.Configuration = function() {
    this.configure = {
        template_directory : "./template",
        target_directory : "./target",
        resource_directory : "./",
        root_page : "/index.html",
    };
};

M.Configuration.prototype.value_of_string = function(name) {
    if (!this.configure[name]) {
        return null;
    }
    return "" + this.configure[name];
};