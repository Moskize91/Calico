// calico system library

M.Printer = function(java_printer) {
    this._java_printer = java_printer;
};

M.Printer.prototype.print = function() {
    for (var i=0; i<arguments.length; ++i) {
        this._java_printer.print(arguments[i]);
    }
};

M.Printer.prototype.println = function() {
    for (var i=0; i<arguments.length; ++i) {
        if (i < arguments.length - 1) {
            this._java_printer.print(arguments[i]);
        } else {
            this._java_printer.println(arguments[i]);
        }
    }
}