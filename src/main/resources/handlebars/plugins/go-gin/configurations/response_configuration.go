package configurations

import (
	"com.github.dekaulitz.codegen/src/helper"
	"encoding/json"
	"github.com/gin-gonic/gin"
	"log"
	"net/http"
	"time"
)

type Response struct {
	context *gin.Context
	headers map[string]string
	body    *ResponseBody
}

type ResponseBody struct {
	Meta ResponseMeta `json:"meta"`
	Data interface{}  `json:"data"`
}

type ResponseMeta struct {
	Timestamp       time.Time `json:"timestamp"`
	XRequestId      string    `json:"x_request_id"`
	ResponseCode    string    `json:"response_code"`
	ResponseMessage string    `json:"response_message"`
}

type ResponseInterface interface {
	SetHeaders(headers map[string]string) *Response
	SetCookies(headers map[string]string) *Response
	Build()
}

func NewResponse(ctx *gin.Context) *Response {
	return &Response{
		context: ctx,
	}
}

func (r *Response) SetHeaders(headers map[string]string) *Response {
	for key, value := range headers {
		r.context.Writer.Header().Set(key, value)
	}
	return r
}
func (r *Response) SetCookies(headers map[string]string) *Response {
	return r
}
func (r *Response) Build() {
	ress, err := json.Marshal(r.body)
	if err != nil {
		log.Fatal(err)
		return
	}
	r.context.Writer.Header().Set("Content-Type", "application/json")
	_, _ = r.context.Writer.Write(ress)
}

func (r *Response) Ok(body interface{}) *Response {
	r.body = &ResponseBody{
		Meta: ResponseMeta{
			Timestamp:       time.Now(),
			XRequestId:      r.context.GetHeader("X-Request-Id"),
			ResponseCode:    helper.SUCCESS.ResponseCode,
			ResponseMessage: helper.SUCCESS.ResponseMessage,
		},
		Data: body,
	}
	r.context.Writer.WriteHeader(http.StatusOK)
	return r
}

func (r *Response) BadRequest(body interface{}, response helper.ResponseMessage) *Response {
	r.body = &ResponseBody{
		Meta: ResponseMeta{
			Timestamp:       time.Now(),
			XRequestId:      r.context.GetHeader("X-Request-Id"),
			ResponseCode:    response.ResponseCode,
			ResponseMessage: response.ResponseMessage,
		},
		Data: body,
	}
	r.context.Writer.WriteHeader(http.StatusBadRequest)
	return r
}

func (r *Response) InternalError(response helper.ResponseMessage) *Response {
	r.body = &ResponseBody{
		Meta: ResponseMeta{
			Timestamp:       time.Now(),
			XRequestId:      r.context.GetHeader("X-Request-Id"),
			ResponseCode:    response.ResponseCode,
			ResponseMessage: response.ResponseMessage,
		},
		Data: nil,
	}
	r.context.Writer.WriteHeader(http.StatusInternalServerError)
	return r
}

func (r *Response) UnprocessableEntity(body interface{}, response helper.ResponseMessage) *Response {
	r.body = &ResponseBody{
		Meta: ResponseMeta{
			Timestamp:       time.Now(),
			XRequestId:      r.context.GetHeader("X-Request-Id"),
			ResponseCode:    response.ResponseCode,
			ResponseMessage: response.ResponseMessage,
		},
		Data: body,
	}
	r.context.Writer.WriteHeader(http.StatusUnprocessableEntity)
	return r
}

func (r *Response) Forbidden(body interface{}, response helper.ResponseMessage) *Response {
	r.body = &ResponseBody{
		Meta: ResponseMeta{
			Timestamp:       time.Now(),
			XRequestId:      r.context.GetHeader("X-Request-Id"),
			ResponseCode:    response.ResponseCode,
			ResponseMessage: response.ResponseMessage,
		},
		Data: body,
	}
	r.context.Writer.WriteHeader(http.StatusForbidden)
	return r
}

func (r *Response) Unauthorized(body interface{}, response helper.ResponseMessage) *Response {
	r.body = &ResponseBody{
		Meta: ResponseMeta{
			Timestamp:       time.Now(),
			XRequestId:      r.context.GetHeader("X-Request-Id"),
			ResponseCode:    response.ResponseCode,
			ResponseMessage: response.ResponseMessage,
		},
		Data: body,
	}
	r.context.Writer.WriteHeader(http.StatusUnauthorized)
	return r
}
