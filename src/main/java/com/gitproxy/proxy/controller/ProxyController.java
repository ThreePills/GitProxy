package com.gitproxy.proxy.controller;

import com.gitproxy.proxy.response.FailResponse;
import com.gitproxy.proxy.response.RepositoryResponse;
import com.gitproxy.proxy.service.ProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/git")
@RequiredArgsConstructor
public class ProxyController {

    private final ProxyService proxyService;

    @GetMapping(value = "/{userName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all GitHub repositories information for given user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RepositoryResponse.class))}),
            @ApiResponse(responseCode = "404", description = "User not found", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FailResponse.class))}),
            @ApiResponse(responseCode = "406", description = "Unsupported Media Type", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FailResponse.class))})

    })
    public List<RepositoryResponse> getRepositoriesForUser(@PathVariable String userName) {
        return proxyService.getRepositoriesForUser(userName);
    }

}
