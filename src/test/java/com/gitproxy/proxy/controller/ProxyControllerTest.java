package com.gitproxy.proxy.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProxyControllerTest {
    private static final String BRANCHES_URL = "/repos/%s/%s/branches";
    private static final String USER_URL = "/users/%s";
    private static final String USER_REPOS_URL = "/users/%s/repos";


    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void init() {
        WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().port(7070));
        wireMockServer.start();
        WireMock.configureFor("localhost", 7070);
    }

    @Test
    void testGetRepositoriesForUser() throws Exception {
        //given
        String userName = "userName";
        String branchName1 = "branch1";
        String branchName2 = "branch2";
        String response = Resources.toString(Resources.getResource("controller/branchInfoResponse.json"), StandardCharsets.UTF_8);
        String branchDetails = Resources.toString(Resources.getResource("controller/branchDetail.json"), StandardCharsets.UTF_8);
        String expectedResponse = Resources.toString(Resources.getResource("controller/expectedResponseTestGetRepositoriesForUser.json"), StandardCharsets.UTF_8);
        stubFor(WireMock.get(urlEqualTo(String.format(USER_URL, userName))).willReturn(aResponse().withStatus(HttpStatus.OK.value())));
        stubFor(WireMock.get(urlEqualTo(String.format(USER_REPOS_URL, userName))).willReturn(okJson(response)));
        stubFor(WireMock.get(urlEqualTo(String.format(BRANCHES_URL, userName, branchName1))).willReturn(okJson(branchDetails)));
        stubFor(WireMock.get(urlEqualTo(String.format(BRANCHES_URL, userName, branchName2))).willReturn(okJson(branchDetails)));

        //when + then
        mockMvc.perform(get("/git/{user}", userName).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse));

    }

    @Test
    void testGetRepositoriesForUserWithWrongContentType() throws Exception {
        //given
        String userName = "userName";
        String expectedResponse = Resources.toString(Resources.getResource("controller/expectedResponseTestGetRepositoriesForUserWithWrongContentType.json"), StandardCharsets.UTF_8);

        //when + then
        mockMvc.perform(get("/git/{user}", userName).contentType(MediaType.APPLICATION_XML))
                .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void testGetRepositoriesForUserNotFound() throws Exception {
        //given
        String userName = "userName";
        String expectedResponse = Resources.toString(Resources.getResource("controller/expectedResponseTestGetRepositoriesForUserNotFound.json"), StandardCharsets.UTF_8);
        stubFor(WireMock.get(urlEqualTo(String.format(USER_URL, userName))).willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

        //when + then
        mockMvc.perform(get("/git/{user}", userName).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse));

    }

}
