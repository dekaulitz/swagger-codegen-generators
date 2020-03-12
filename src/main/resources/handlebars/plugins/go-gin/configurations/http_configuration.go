package configurations

import (
	"context"
	"io/ioutil"
	"net/http"
	"net/url"
	"time"
)

type Config struct {
	Timeout     time.Duration
	Url         string
	UrlStatic   string
	Context     context.Context
	Retry       int
	WaitingTime time.Duration
}

type HttpInterface interface {
	SetHeaders(headers map[string]string)
	GetResponseBody() []byte
	GetResponse() *http.Response
	Execute() (*HttpClient, error)
}

type HttpClient struct {
	Config       *Config
	Request      http.Request
	responseBody []byte
	response     *http.Response
}

func NewHttpClient(config *Config) *HttpClient {
	return &HttpClient{Config: config}
}

func (h *HttpClient) SetHeaders(headers map[string]string) {
	header := http.Header{}
	for key, value := range headers {
		header.Add(key, value)
	}
	h.Request.Header = header
}
func (h *HttpClient) GetResponseBody() []byte {
	return h.responseBody
}
func (h *HttpClient) GetResponse() *http.Response {
	return h.response
}

func (h *HttpClient) Execute() (*HttpClient, error) {
	res, err := h.doRequest()
	if err != nil {
		return nil, err
	}
	if h.response.StatusCode != 200 {
		for i := 1; i < h.Config.Retry; i++ {
			res, err := h.doRequest()
			if err != nil {
				return nil, err
			}
			if h.response.StatusCode == http.StatusOK {
				return res, nil
			}
		}
	}
	return res, nil
}

func (h *HttpClient) doRequest() (*HttpClient, error) {
	client := http.Client{
		Timeout: h.Config.Timeout,
	}
	h.Request.URL, _ = url.Parse(h.Config.Url)
	response, err := client.Do(&h.Request)
	defer response.Body.Close()
	body, err := ioutil.ReadAll(response.Body)
	if err != nil {
		return nil, err
	}
	h.responseBody = body
	h.response = response
	return h, nil
}
