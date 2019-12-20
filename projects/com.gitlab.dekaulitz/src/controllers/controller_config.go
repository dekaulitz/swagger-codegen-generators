/*
 * Swagger Generator
 *
 * This is an online swagger codegen server.  You can find out more at https://github.com/swagger-api/swagger-codegen or on [irc.freenode.net, #swagger](http://swagger.io/irc/).
 *
 * API version: 3.0.14
 * Generated by: Swagger Codegen (https://github.com/swagger-api/swagger-codegen.git)
 */
package controllers

import (
	"net/http"
    "github.com/gin-gonic/gin"
    "com.gitlab.dekaulitz/src/vmodels"
)
/**
 * Generates and download code. GenerationRequest input provided as request body.
 * 
 */
func Generate(c *gin.Context) {
    var body vmodels.GenerationRequest
    jsonFail := c.BindJSON(&body)
    if jsonFail != nil {
        panic(jsonFail)
    }
    c.Writer.Header().Set("Content-Type","application/octet-stream")
    c.Writer.WriteHeader(http.StatusOK)
    var responseBody string
    res,_:=json.Marshal(responseBody)
    c.Writer.Write(res)
    return
}
/**
 * Generates and download code. GenerationRequest input provided as JSON available at URL specified in parameter codegenOptionsURL.
 * 
 */
func GenerateFromURL(c *gin.Context) {
    var codegenOptionsURL string=c.Query("codegenOptionsURL")
    c.Writer.Header().Set("Content-Type","application/octet-stream")
    c.Writer.WriteHeader(http.StatusOK)
    var responseBody string
    res,_:=json.Marshal(responseBody)
    c.Writer.Write(res)
    return
}
/**
 * List generator languages of the given type and version
 * 
 */
func Languages(c *gin.Context) {
    var type_ string=c.Param("type")
    var version string=c.Param("version")
    c.Writer.Header().Set("Content-Type","application/json")
    c.Writer.WriteHeader(http.StatusOK)
    var responseBody []string
    res,_:=json.Marshal(responseBody)
    c.Writer.Write(res)
    return
}
/**
 * List generator languages of version defined in &#x27;version parameter (defaults to V3) and type included in &#x27;types&#x27; parameter; all languages
 * 
 */
func LanguagesMulti(c *gin.Context) {
    var types []string=c.Query("types")
    var version string=c.Query("version")
    c.Writer.Header().Set("Content-Type","application/json")
    c.Writer.WriteHeader(http.StatusOK)
    var responseBody []string
    res,_:=json.Marshal(responseBody)
    c.Writer.Write(res)
    return
}
/**
 * Returns options for a given language and version (defaults to V3)
 * 
 */
func ListOptions(c *gin.Context) {
    var language string=c.Query("language")
    var version string=c.Query("version")
    c.Writer.Header().Set("Content-Type","application/json")
    c.Writer.WriteHeader(http.StatusOK)
    var responseBody map[string]CliOption
    res,_:=json.Marshal(responseBody)
    c.Writer.Write(res)
    return
}
