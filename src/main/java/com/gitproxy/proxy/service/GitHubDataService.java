package com.gitproxy.proxy.service;

import com.gitproxy.proxy.builder.GitHubUrlBuilder;
import com.gitproxy.proxy.exception.GitUserNotFoundException;
import com.gitproxy.proxy.gitHubInfo.BranchInfo;
import com.gitproxy.proxy.gitHubInfo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@Component
@AllArgsConstructor
public class GitHubDataService {
    private final RestTemplate restTemplate;
    private final GitHubUrlBuilder gitHubUrlBuilder;

    public boolean userExistsInGitHub(String userName) throws GitUserNotFoundException {
        try {
            ResponseEntity<String> forEntity = restTemplate.getForEntity(gitHubUrlBuilder.getUserURI(userName), String.class);
            return forEntity.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException exception) {
            throw new GitUserNotFoundException();
        }
    }

    public CompletableFuture<List<BranchInfo>> getBranchInformations(String userName, UserRepo userRepo) {
        BranchInfo[] branchInfos = restTemplate.getForObject(gitHubUrlBuilder.getBranchesInfoURI(userName, userRepo), BranchInfo[].class);
        return branchInfos != null ? CompletableFuture.completedFuture(
                Arrays.asList(branchInfos)) : CompletableFuture.completedFuture(new ArrayList<>());
    }

    public List<UserRepo> getUserRepositoriesList(String userName) {
        UserRepo[] userRepos = restTemplate.getForObject(gitHubUrlBuilder.getUserReposURI(userName), UserRepo[].class);
        return userRepos != null ? Arrays.stream(userRepos).filter(isNotForked()).toList() : new ArrayList<>();
    }

    private Predicate<UserRepo> isNotForked() {
        return userRepository -> !userRepository.isFork();
    }

}
