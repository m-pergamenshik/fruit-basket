package com.pergam.courses.httpserver.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum HttpCommonRegex {

    MULTIPART_CONTENT_DISPOSITION_FORM_DATA_REGEX("Content-Disposition:[ |\t]*form-data"),
    MULTIPART_NAME_REGEX("name=['|\"]([^\"^']+)['|\"]"),
    MULTIPART_FILE_NAME_REGEX("filename=['|\"]([^\"^']+)['|\"]"),
    MULTIPART_CONTENT_TYPE_REGEX("Content-Type:[ |	]*([^/^ ^;^,]+/[^ ^;^,^\n]+)");

    private final Pattern pattern;

    HttpCommonRegex(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public String[] apply(String checkedString) {
        return apply(checkedString, null);
    }

    public String[] apply(String checkedString, ThrowExceptionFunction throwExceptionFunction) {
        Matcher matcher = this.pattern.matcher(checkedString);
        final int groups = matcher.groupCount() + 1; // group 0 is not included in count, therefore +1
        String[] groupValues = null;
        if (matcher.matches()) {
            groupValues = new String[groups];
            for (int group = 0; group < groups; group++) {
                groupValues[group] = matcher.group(group);
            }
        } else if (throwExceptionFunction != null) {
            throwExceptionFunction.throwException();
        }
        return groupValues;
    }


    public interface ThrowExceptionFunction {
        void throwException();
    }
}
