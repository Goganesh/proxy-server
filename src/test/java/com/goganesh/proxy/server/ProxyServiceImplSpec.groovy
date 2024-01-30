package com.goganesh.proxy.server

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class ProxyServiceImplSpec extends Specification {

    def proxyClient = Mock(ProxyClient.class)
    def blockProxyClientService = Mock(BlockProxyClientService.class)
    def proxyClientHolder = Mock(ProxyClientHolder.class)
    def proxyService = new ProxyServiceImpl(blockProxyClientService, proxyClientHolder)

    def "SendGetRequestToProxy - no available proxy clients"() {
        given:
        def url = "https://www.google.com"

        when:
        def actual = proxyService.sendGetRequestToProxy(url)

        then:
        1 * proxyClientHolder.getProxyClient() >> Optional.empty()
        0 * _
        actual.getStatusCode() == HttpStatus.I_AM_A_TEAPOT

    }

    def "SendGetRequestToProxy - correct response"() {
        given:
        def url = "https://www.google.com"

        when:
        def actual = proxyService.sendGetRequestToProxy(url)

        then:
        1 * proxyClientHolder.getProxyClient() >> Optional.of(proxyClient)
        1 * proxyClient.sendRequest(_, HttpMethod.GET, _, _) >> ResponseEntity<String>.ok().build()
        0 * _
        actual.getStatusCode() == HttpStatus.OK

    }

    def "SendGetRequestToProxy - one proxy server broken"() {
        given:
        def url = "https://www.google.com"

        when:
        def actual = proxyService.sendGetRequestToProxy(url)

        then:
        2 * proxyClientHolder.getProxyClient() >> Optional.of(proxyClient)
        1 * proxyClient.sendRequest(_, HttpMethod.GET, _, _) >> ResponseEntity<String>.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        1 * blockProxyClientService.block(proxyClient)
        1 * proxyClient.sendRequest(_, HttpMethod.GET, _, _) >> ResponseEntity<String>.ok().build()
        0 * _
        actual.getStatusCode() == HttpStatus.OK

    }

    def "SendGetRequestToProxy - redirect"() {
        given:
        def url = "https://www.google.com"

        when:
        def actual = proxyService.sendGetRequestToProxy(url)

        then:
        2 * proxyClientHolder.getProxyClient() >> Optional.of(proxyClient)
        1 * proxyClient.sendRequest(_, HttpMethod.GET, _, _) >> ResponseEntity<String>.status(HttpStatus.TEMPORARY_REDIRECT).build()
        1 * proxyClient.sendRequest(_, HttpMethod.GET, _, _) >> ResponseEntity<String>.ok().build()
        0 * _
        actual.getStatusCode() == HttpStatus.OK

    }

    def "SendGetRequestToProxy - exception"() {
        given:
        def url = "https://www.google.com"

        when:
        def actual = proxyService.sendGetRequestToProxy(url)

        then:
        2 * proxyClientHolder.getProxyClient() >> Optional.of(proxyClient)
        1 * proxyClient.sendRequest(_, HttpMethod.GET, _, _) >> new Exception()
        1 * blockProxyClientService.block(proxyClient)
        1 * proxyClient.sendRequest(_, HttpMethod.GET, _, _) >> ResponseEntity<String>.ok().build()
        0 * _
        actual.getStatusCode() == HttpStatus.OK

    }
}
