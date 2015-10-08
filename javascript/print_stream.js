var Output; // this variable will be set by Java code.

function print() {
    for (var i=0; i<arguments.length; ++i) {
        Output.print(arguments[i]);
    }
}

function println() {
    for (var i=0; i<arguments.length; ++i) {
        if (i < arguments.length - 1) {
            Output.print(arguments[i]);
        } else {
            Output.println(arguments[i]);
        }
    }
}