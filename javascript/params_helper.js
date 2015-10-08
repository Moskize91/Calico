var ParamsHelper = {};

(function(){

    ParamsHelper.parse = function(params) {

        var rsObj = {};

        var arrParams = params.replace(/(^\/|\/$)/g, "").split("/"),
            paramCount = Math.floor(arrParams.length/2);

        for (var i=0; i<paramCount; ++i) {
            var name = arrParams[2*i].trim(),
                value = arrParams[2*i + 1].trim();
            rsObj[name] = value;
        }
        return rsObj;
    };

    ParamsHelper.generate = function(path, paramsMap) {

        if (paramsMap === undefined) {
            path = "";
            paramsMap = path;
        }
        path = path.replace(/\/$/g, "");

        var arrKeys = [],
            arrParams = [];

        for (var key in paramsMap) {
            key = key.trim();
            if (key != "") {
                arrKeys.push(key);
            }
        }
        arrKeys.sort();

        for each(var key in arrKeys) {

            var value = ("" + paramsMap[key]).trim();
            arrParams[arrParams.length] = key;
            arrParams[arrParams.length] = value;
        }
        return path + "/" +arrParams.join("/");
    }

})();