var R;

(function(){

    function wrap(resource) {

        var R = {};

        R.page = function(path) {
            return resource.page(path);
        }

        R.dir = function(path) {
            return wrap(resource.dir(path));
        }

        R.pages = function () {
            var pages = resource.pages(),
                rsPages = [];

            for (var i=0; i<pages.length; ++i) {
                rsPages[i] = pages[i];
            }
            return rsPages;
        }

        R.allPages = function() {
            var pages = resource.allPages(),
                rsPages = [];

            for (var i=0; i<pages.length; ++i) {
                rsPages[i] = pages[i];
            }
            return rsPages;
        }

        R.dirs = function() {
            var dirs = resource.dirs(),
                rsDirs = [];

            for (var i=0; i<dirs.length; ++i) {
                rsDirs[i] = wrap(dirs[i]);
            }
            return rsDirs;
        }
        return R;
    }
    R = wrap(__resource);

})();

__resource = undefined;