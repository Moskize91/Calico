(function(){

    function attribute(obj, name) {
        var attr;
        if (typeof(obj.get) == "function") {
            attr = obj.get(name);
            if (attr) {
                return attr;
            }
        }

        attr = obj[name];
        if (attr) {
            return attr;
        }

        var getter = "get"+ name.replace(/^\w/, function(w) {return w.toUpperCase();});

        if (obj[getter]) {
            return obj[getter]();
        }

        return undefined;
    }

    function anyComparatorToCallback(comparator) {
        if (comparator === undefined || comparator === null) {
            return comparator;
        }
        if (typeof(comparator) == "string") {
            comparator = comparator.split(",");
        }
        if (comparator instanceof Array) {
            var nameArray = [],
                descArray = [];

            for (var i=0; i<comparator.length; ++i) {
                var ele = comparator[i].trim();
                descArray[i] = ele.match(/\s+desc$/g) != null;
                nameArray[i] = ele.match(/(\w|\-|_|\.)+/g)[0];
            }
            comparator = function(ele0, ele1) {
                for (var i=0; i<nameArray.length; ++i) {
                    var name = nameArray[i];
                    var rs = compareValue(attribute(ele0, name), attribute(ele1, name), descArray[i]);
                    if (rs != 0) {
                        return rs;
                    }
                }
                return 0;
            };
        }
        return comparator;
    }

    var TypeCompareList = ["function", "object", "string", "number", "boolean", "null", "undefined"];

    function compareValue(value0, value1, desc) {
        if (typeof (value0) != typeof(value1)) {
            value0 = TypeCompareList.indexOf(typeof(value0));
            value1 = TypeCompareList.indexOf(typeof(value1));
        }
        if (value0 > value1) {
            return desc?-1:1;
        } else if (value0 < value1) {
            return desc?1:-1;
        } else {
            return 0;
        }
    }

    function anyConditionToCallback(originalCondition) {
        if (originalCondition === undefined || originalCondition === null) {
            return originalCondition;
        } else if (typeof(originalCondition) == "object") {
            return mapConditionToCallback(originalCondition);
        } else if (typeof(originalCondition) == "string") {
            return stringConditionToCallback(originalCondition);
        }
        return originalCondition;
    }

    function mapConditionToCallback(conditionObj) {
        return function(ele) {
            for (var name in conditionObj) {
                if (attribute(ele, name) != conditionObj[name]) {
                    return false;
                }
            }
            return true;
        };
    }

    function stringConditionToCallback(strCondition) {
        //TODO like this "data >= 2015-10-01 and age >= 18"
        return strCondition;
    }

    function setMethodForArray(methodName, method) {
        Array.prototype[methodName] = method;
        Object.defineProperty(Array.prototype, methodName, {
            enumerable: false,
            configurable: false,
            writable: false
        });
    }

    function replaceArrayMethod(methodName, replaceCallback) {
        var oldMethod = Array.prototype[methodName];
        if (!oldMethod) {
            throw new Error("Not found method "+ methodName);
        }
        var newMethod = replaceCallback(oldMethod);
        setMethodForArray(methodName, newMethod);
    }

    replaceArrayMethod("filter", function(oldMethod) {
        return function(condition, thisArg) {
            return oldMethod.call(this, anyConditionToCallback(condition), thisArg);
        };
    });

    replaceArrayMethod("sort", function(oldMethod) {
        return function (comparator) {
            return oldMethod.call(this, anyComparatorToCallback(comparator));
        };
    });

    setMethodForArray("any", function(condition) {
         condition = anyConditionToCallback(condition);
         for each(var ele in this) {
             if (condition(ele)) {
                 return true;
             }
         }
         return false;
    });

    setMethodForArray("every", function(condition) {
        condition = anyConditionToCallback(condition);
        for each(var ele in this) {
            if (!condition(ele)) {
                return false;
            }
        }
        return true;
    });
})();
