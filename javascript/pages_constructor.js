(function(){

    function getNumPages(numElements, countForEachPage) {
        return Math.ceil(numElements/countForEachPage);
    }

    Array.prototype.subArrayOnPage = function(params) {

        var currentPage = params.currentPage;
        var countForEachPage = params.countForEachPage || 5;

        var startIndex = currentPage*countForEachPage,
            subArray = [];

        for (var i=0; startIndex + i < this.length && i<countForEachPage; ++i) {
            subArray[i] = this[startIndex + i];
        }
        return subArray;
    };

    Array.prototype.pagesConstructor = function (params) {

        var currentPage = params.currentPage;
        var countForEachPage = params.countForEachPage || 5;
        var countOfShowPageButtons = params.countOfShowPageButtons || 8;
        var hrefGenerator = params.hrefGenerator || function() {return "/";};
        var className = params.className;
        var showGotoFirstAndLastPageButtons = params.showGotoFirstAndLastPageButtons;

        var tagName = params.tagName || "a";
        var previousPage = params.previousPage || "&lt;"
        var nextPage = params.nextPage || "&gt;";
        var firstPage = params.firstPage || "&lt;&lt;";
        var lastPage = params.lastPage || "&gt;&gt";

        function printButton(content, disabled, href) {
            var cot = [];
            cot.push("<", tagName);
            if (className) {
                cot.push(" class='", className, "'");
            }
            cot.push(" href='", (disabled?"":href), "'");
            if (disabled) {
                cot.push(" disabled style='pointer-events: none'");
            }
            cot.push(">", content, "</", tagName, ">");

            print(cot.join(""));
        }

        var numPages = getNumPages(this.length, countForEachPage);
            isFirstPage = (currentPage == 0),
            isLastPage = (currentPage == numPages - 1);

        if (showGotoFirstAndLastPageButtons) {
            printButton(firstPage, isFirstPage, hrefGenerator(0));
        }
        printButton(previousPage, isFirstPage, hrefGenerator(currentPage - 1));

        var startPage = Math.max(0, currentPage - Math.floor(countOfShowPageButtons/2));

        for (var i=0; i<countOfShowPageButtons; i++) {
            var printPage = startPage + i;
            if (printPage >= numPages) {
                break;
            }
            printButton(printPage + 1, printPage == currentPage, hrefGenerator(printPage));
        }

        printButton(nextPage, isLastPage, hrefGenerator(numPages - 1));
        if (showGotoFirstAndLastPageButtons) {
            printButton(lastPage, isLastPage, hrefGenerator(numPages - 1));
        }
        return "[It will generate html code automatically. You don't need to use <%= ... %>.]"
    };

    for (var property in ["subArrayOnPage", "pagesConstructor"]) {
        Object.defineProperty(Array.prototype, property, {
            enumerable: false,
            configurable: false,
            writable: false
        });
    }

})();

