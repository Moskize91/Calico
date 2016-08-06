// calico system library

exports.Printer = function(java_printer) {
    this._java_printer = java_printer;
};

exports.Printer.prototype.print = function() {
    for (var i=0; i<arguments.length; ++i) {
        this._java_printer.print(arguments[i]);
    }
};

exports.Printer.prototype.println = function() {
    for (var i=0; i<arguments.length; ++i) {
        if (i < arguments.length - 1) {
            this._java_printer.print(arguments[i]);
        } else {
            this._java_printer.println(arguments[i]);
        }
    }
}