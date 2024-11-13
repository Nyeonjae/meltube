HTMLElement.prototype.hide = function() {
    this.classList.remove('-visible');
    return this;
}

HTMLElement.prototype.show = function () {
    this.classList.add('-visible');
    return this;
}