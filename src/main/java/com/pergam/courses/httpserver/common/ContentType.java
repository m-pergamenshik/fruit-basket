package com.pergam.courses.httpserver.common;

import com.pergam.courses.httpserver.request.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.pergam.courses.httpserver.common.MediaType.MULTIPART_FORM_DATA;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ContentType {

    public static final String MULTIPART_BOUNDARY_PARAM_NAME = "boundary";

    private final String mediaType;
    private OptionalParam optionalParam; // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types

    private static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("([^/^ ^;^,]+/[^ ^;^,]+)");
    private static final Pattern OPTIONAL_PARAM_PATTERN = Pattern.compile("([^\"^'^;^,^ ]*)=['|\"]?([^\"^'^;^,]+)['|\"]?");

    public ContentType(String contentTypeHeaderRawValue) {

        Matcher mediaTypeMatcher = MEDIA_TYPE_PATTERN.matcher(contentTypeHeaderRawValue);
        if (mediaTypeMatcher.find()) {
            this.mediaType = mediaTypeMatcher.group();
        } else {
            throw new BadRequestException("Unexpected media type format: " + contentTypeHeaderRawValue);
        }

        Matcher optionalParamMatcher = OPTIONAL_PARAM_PATTERN.matcher(contentTypeHeaderRawValue);
        if (optionalParamMatcher.find()) {
            String optionalParamKey = optionalParamMatcher.group(1);
            String optionalParamValue = optionalParamMatcher.group(2);
            this.optionalParam = new OptionalParam(optionalParamKey, optionalParamValue);
        }

        if (MULTIPART_FORM_DATA.toString().equals(this.mediaType) && !isMultipartBoundaryDefined()) {
            throw new BadRequestException("Multipart boundary not defined in Content-type header");
        }
    }

    private boolean isMultipartBoundaryDefined() {
        return this.optionalParam != null && MULTIPART_BOUNDARY_PARAM_NAME.equals(this.optionalParam.getKey());
    }

    @Value
    public static class OptionalParam {
        String key;
        String value;
    }
}
