package com.leaps.communicators;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Used to externalize all HTTP communication. Should be injected by other services and used for
 * their external HTTP communication. Note: The injector services is responsible for setting the
 * restTemplate to their desired type.
 */
public abstract class ExternalCommunicator {

  private final RestTemplate restTemplate;

  /**
   * Protected constructor so no instantiation could be done in other classes different from it's
   * successors. This class should be used only via it's successors.
   *
   * @param restTemplate the Rest template
   * @param exceptionsHandler the exceptionHandler
   */
  protected ExternalCommunicator(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /**
   * Post Method with Class responseType.
   */
  public <T> ResponseEntity<T> post(String uri,
                                    HttpEntity<Object> request,
                                    Class<T> responseType) {

    URI buildUpUri = buildUri(uri, null, null);
    return exchange(buildUpUri, HttpMethod.POST, request, responseType);
  }

  /**
   * Post Method with Class responseType and path parameters.
   */
  public <T> ResponseEntity<T> post(String uri,
                                    Map<String, String> pathParams,
                                    HttpEntity<Object> request,
                                    Class<T> responseType) {

    URI buildUpUri = buildUri(uri, null, pathParams);
    return exchange(buildUpUri, HttpMethod.POST, request, responseType);
  }

  /**
   * Post Method with Class responseType.
   */
  public <T> ResponseEntity<T> post(String uri,
                                    MultiValueMap<String, Object> queryParams,
                                    Map<String, String> pathParams,
                                    HttpEntity<Object> request,
                                    Class<T> responseType) {

    URI buildUpUri = buildUri(uri, queryParams, pathParams);
    return exchange(buildUpUri, HttpMethod.POST, request, responseType);
  }

  /**
   * Post Method with ParameterizedTypeReference responseType.
   */
  public <T> ResponseEntity<T> post(String uri,
                                    MultiValueMap<String, Object> queryParams,
                                    Map<String, String> pathParams,
                                    HttpEntity<Object> request,
                                    ParameterizedTypeReference<T> responseType) {

    URI buildUpUri = buildUri(uri, queryParams, pathParams);
    return exchange(buildUpUri, HttpMethod.POST, request, responseType);
  }

  /**
   * Post Method with ParameterizedTypeReference responseType.
   */
  public <T> ResponseEntity<T> post(String uri,
                                    MultiValueMap<String, Object> queryParams,
                                    HttpEntity<Object> request,
                                    ParameterizedTypeReference<T> responseType) {

    URI buildUpUri = buildUri(uri, queryParams, null);
    return exchange(buildUpUri, HttpMethod.POST, request, responseType);
  }

  /**
   * Post Method with Class responseType.
   */
  public <T> ResponseEntity<T> post(String uri,
                                    MultiValueMap<String, Object> queryParams,
                                    HttpEntity<Object> request,
                                    Class<T> responseType) {

    URI buildUpUri = buildUri(uri, queryParams, null);
    return exchange(buildUpUri, HttpMethod.POST, request, responseType);
  }

  /**
   * Post Method with ParameterizedTypeReference responseType.
   */
  public <T> ResponseEntity<T> post(String uri,
                                    HttpEntity<Object> request,
                                    ParameterizedTypeReference<T> responseType) {

    URI buildUpUri = buildUri(uri, null, null);
    return exchange(buildUpUri, HttpMethod.POST, request, responseType);
  }

  /**
   * Get Method with Class responseType.
   */
  public <T> ResponseEntity<T> get(String uri,
                                    MultiValueMap<String, Object> queryParams,
                                    Map<String, String> pathParams,
                                    HttpEntity<Object> request,
                                    Class<T> responseType) {

    URI buildUpUri = buildUri(uri, queryParams, pathParams);

    return exchange(buildUpUri, HttpMethod.GET, request, responseType);
  }

  /**
   * Get Method with Class responseTyp and no path params.
   * @param uri the uri
   * @param queryParams the query params
   * @param request the http entity request
   * @param responseType the response
   * @return HTTP response entity
   */
  public <T> ResponseEntity<T> get(String uri,
                                    MultiValueMap<String, Object> queryParams,
                                    HttpEntity<Object> request,
                                    Class<T> responseType) {

    URI buildUpUri = buildUri(uri, queryParams, null);

    return exchange(buildUpUri, HttpMethod.GET, request, responseType);
  }

  /**
   * Get Method with Class responseTyp and no query params.
   * @param uri the uri
   * @param pathParams the path params
   * @param request the http entity request
   * @param responseType the response type
   * @return HTTP response entity
   */
  public <T> ResponseEntity<T> get(String uri,
                                  Map<String, String> pathParams,
                                  HttpEntity<Object> request,
                                  Class<T> responseType) {

    URI buildUpUri = buildUri(uri, null, pathParams);

    return exchange(buildUpUri, HttpMethod.GET, request, responseType);
  }

  /**
   * Get Method with Class responseTyp and no params.
   * @param uri the URI
   * @param request the http entity request
   * @param responseType the response type
   * @return HTTP entity response
   */
  public <T> ResponseEntity<T> get(String uri,
                                HttpEntity<Object> request,
                                Class<T> responseType) {

    URI buildUpUri = buildUri(uri, null, null);

    return exchange(buildUpUri, HttpMethod.GET, request, responseType);
  }

  /**
   * Get Method with ParameterizedTypeReference responseType.
   */
  public <T> ResponseEntity<T> get(String uri,
                                  MultiValueMap<String, Object> queryParams,
                                  Map<String, String> pathParams,
                                  HttpEntity<Object> request,
                                  ParameterizedTypeReference<T> responseType) {

    URI buildUpUri = buildUri(uri, queryParams, pathParams);

    return exchange(buildUpUri, HttpMethod.GET, request, responseType);
  }

  /**
   * Get Method with ParameterizedTypeReference responseType and no path params.
   * @param uri the URI
   * @param queryParams the query params
   * @param request the http entity request
   * @param responseType the response type
   * @return HTTP entity response
   */
  public <T> ResponseEntity<T> get(String uri,
                                  MultiValueMap<String, Object> queryParams,
                                  HttpEntity<Object> request,
                                  ParameterizedTypeReference<T> responseType) {

    URI buildUpUri = buildUri(uri, queryParams, null);

    return exchange(buildUpUri, HttpMethod.GET, request, responseType);
  }

  /**
   * Get Method with ParameterizedTypeReference responseType and no query params.
   * @param uri the URI
   * @param pathParams the path params
   * @param request the http entity request
   * @param responseType the response type
   * @return HTTP entity response
   */
  public <T> ResponseEntity<T> get(String uri,
                                  Map<String, String> pathParams,
                                  HttpEntity<Object> request,
                                  ParameterizedTypeReference<T> responseType) {

    URI buildUpUri = buildUri(uri, null, pathParams);

    return exchange(buildUpUri, HttpMethod.GET, request, responseType);
  }

  /**
   * Get Method with ParameterizedTypeReference responseType and no params.
   * @param uri the URI
   * @param request the http entity request
   * @param responseType the response type
   * @return HTTP entity response
   */
  public <T> ResponseEntity<T> get(String uri,
                                  HttpEntity<Object> request,
                                  ParameterizedTypeReference<T> responseType) {

    URI buildUpUri = buildUri(uri, null, null);

    return exchange(buildUpUri, HttpMethod.GET, request, responseType);
  }

  private URI buildUri(String hostUriString,
      MultiValueMap<String, Object> queryParams,
      Map<String, String> pathParams) {

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(hostUriString);

    if (null != queryParams) {
      uriComponentsBuilder.queryParams(prepareRequestParameters(queryParams));
    }

    if (null != pathParams) {
      return uriComponentsBuilder.buildAndExpand(pathParams).encode().toUri();
    }

    return uriComponentsBuilder.build().encode().toUri();
  }

  /**
   * Removes empty query params.
   *
   * @param queryParams the query params
   * @return the non empty params
   */
  private MultiValueMap<String, String>
      prepareRequestParameters(MultiValueMap<String, Object> queryParams) {

    queryParams.values()
               .removeIf(p -> CollectionUtils.isEmpty(p)
                              || p.size() == 1
                              && StringUtils.isEmpty(p.get(0)));

    return prepareMultyValues(queryParams);
  }

  /**
   * Prepares multyValue entries. Removes null or empty values.
   *
   * @param queryParams the multy values
   */
  private MultiValueMap<String, String> prepareMultyValues(
                                          MultiValueMap<String, Object> queryParams) {

    MultiValueMap<String, String> result = new LinkedMultiValueMap<>();

    if (null == queryParams || null == queryParams.values()) {
      return result;
    }

    queryParams.entrySet()
        .forEach(e -> result.put(e.getKey(),
                                 e.getValue()
                                   .stream()
                                   .filter(p -> !StringUtils.isEmpty(p))
                                   .map(Object::toString)
                                   .collect(Collectors.toList())));

    return result;
  }

  /**
   * Sends a HTTP Request to the Gateway with Class responseType.
   *
   * @param url to gateway
   * @param request HttpEntity with body and headers
   * @param method GET/POST/DELETE
   * @return the HTTP Response Entity
   */
  private <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<Object> request,
      Class<T> responseType) {

      return restTemplate.exchange(url, method, request, responseType);
  }

  /**
   * Sends a HTTP Request to the Gateway with ParameterizedTypeReference responseType.
   *
   * @param url to gateway
   * @param request HttpEntity with body and headers
   * @param method GET/POST/DELETE
   * @return the HTTP Response Entity
   */
  private <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<Object> request,
      ParameterizedTypeReference<T> responseType) {

      return restTemplate.exchange(url, method, request, responseType);
  }

  public RestTemplate getRestTemplate() {
    return this.restTemplate;
  }

}
