// calico system library

function Page(elements, num_each_page) {
    this._elements = elements;
    this._num_each_page = num_each_page;
    this.num_of_page = Math.ceil(elements.length / num_each_page);
}

Page.prototype.elements_on_page = function(page_index) {
    var elements = [],
        start_index = page_index * this._num_each_page;
    for (var i = 0; i < _num_each_page && i + start_index < this._elements.length; i ++) {
        elements[i] = this._elements[i + start_index];
    }
    return elements;
}

return Page;