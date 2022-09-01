package com.gitproxy.proxy.builder;

import com.gitproxy.proxy.gitHubInfo.UserRepo;
import com.gitproxy.proxy.propeties.GitHubProperties;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@AllArgsConstructor
public class GitHubUrlBuilder {
    private final GitHubProperties gitHubProperties;

    @SneakyThrows
    public URI getUserURI(String userName) {
        return getUriBuilderWithDefaultGitHost().setPathSegments(gitHubProperties.getUserPath(), userName).build().normalize();
    }

    @SneakyThrows
    public URI getBranchesInfoURI(String userName, UserRepo userRepo) {
        return getUriBuilderWithDefaultGitHost().setPathSegments(gitHubProperties.getReposPath(), userName, userRepo.getName(), gitHubProperties.getBranchesPath()).build().normalize();
    }

    @SneakyThrows
    public URI getUserReposURI(String userName) {
        return getUriBuilderWithDefaultGitHost().setPathSegments(gitHubProperties.getUserPath(), userName, gitHubProperties.getReposPath()).build().normalize();
    }

    private URIBuilder getUriBuilderWithDefaultGitHost() {
        return new URIBuilder().setScheme(gitHubProperties.getScheme()).setHost(gitHubProperties.getBaseUrl());
    }
}
