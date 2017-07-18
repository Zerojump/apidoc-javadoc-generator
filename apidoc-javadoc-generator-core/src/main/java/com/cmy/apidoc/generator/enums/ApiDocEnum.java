package com.cmy.apidoc.generator.enums;

/**
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>Date: 2017/7/9
 * <p>
 * To change this template use File | Settings | File and Code Templates | Includes .
 */
public enum ApiDocEnum {
    //@api
    //@apiDefine
    //@apiDeprecated
    //@apiDescription
    //@apiError
    //@apiErrorExample
    //@apiExample
    //@apiGroup
    //@apiHeader
    //@apiHeaderExample
    //@apiIgnore
    //@apiName
    //@apiParam
    //@apiParamExample
    //@apiPermission
    //@apiSampleRequest
    //@apiSuccess
    //@apiSuccessExample
    //@apiUse
    //@apiVersion

    API("api"),

    API_DEFINE("apiDefine"),

    API_DEPRECATED("apiDeprecated"),

    API_DESCRIPTION("apiDescription"),

    API_ERROR("apiError"),

    API_ERROR_EXAMPLE("apiErrorExample"),

    API_GROUP("apiGroup"),

    API_HEADER("apiHeader"),

    API_HEADER_EXAMPLE("apiHeaderExamle"),

    API_IGNORE("apiIgnore"),

    API_NAME("apiName"),

    API_PARAM("apiParam"),

    API_PARAM_EXAMPLE("apiParamExample"),

    API_PERMISSION("apiPermission"),

    API_SAMPLE_REQUEST("apiSampleRequest"),

    API_SUCCESS("apiSuccess"),

    API_SUCCESS_EXAMPLE("apiSuccessExample"),

    API_USE("apiUse"),

    API_VERSION("apiVersion"),;

    private String code;

    ApiDocEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
