// calico system library

L.Configuration = function() {
    this.configure = {
        template_directory : "./template",
        target_directory : "./",
        root_page : "/index.html",
    };
};

L.Configuration.prototype.value_of_string = function(name) {
    if (!this.configure[name]) {
        return null;
    }
    return "" + this.configure[name];
};