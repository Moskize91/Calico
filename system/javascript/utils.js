// calico system library

var PinyinHelper = Java.type("com.github.stuxuhai.jpinyin.PinyinHelper"),
    PinyinFormat = Java.type("com.github.stuxuhai.jpinyin.PinyinFormat");

exports.convert_to_pinyin = function(str, link) {
    str = PinyinHelper.convertToPinyinString(str, link || "-", PinyinFormat.WITHOUT_TONE);
    str = str.replace(/\s+/g, "-");
    str = str.replace(/[^a-zA-Z\d\-]/g, "");
    str = str.replace(/\-+/g, "-");
    str = str.replace(/(^\-|\-$)/g, "");
    return str;
}