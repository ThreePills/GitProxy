package com.gitproxy.proxy.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.gitproxy.proxy.exception.GitUserNotFoundException;
import com.gitproxy.proxy.gitHubInfo.BranchInfo;
import com.gitproxy.proxy.gitHubInfo.UserRepo;
import com.gitproxy.proxy.propeties.GitHubProperties;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@AllArgsConstructor
public class GitHubDataService {

        private final RestTemplate restTemplate;
        private final GitHubProperties gitHubProperties;

        public boolean userExistsInGitHub(String userName) throws GitUserNotFoundException {
                try {
                        ResponseEntity<String> forEntity = restTemplate.getForEntity(
                                gitHubProperties.getUserUrl() + userName, String.class);
                        return forEntity.getStatusCode().is2xxSuccessful();
                } catch (HttpClientErrorException exception) {
                        throw new GitUserNotFoundException();
                }
        }

        public CompletableFuture<List<BranchInfo>> getBranchInformations(String userName, UserRepo userRepo) {
                BranchInfo[] branchInfos = restTemplate.getForObject(
                        gitHubProperties.getBranchesUrl() + userName + "/" + userRepo.getName() + "/branches",
                        BranchInfo[].class);

                return branchInfos != null ? CompletableFuture.completedFuture(
                        Arrays.asList(branchInfos)) : CompletableFuture.completedFuture(new ArrayList<>());
        }

        public List<UserRepo> getUserRepositoriesList(String userName) {
                UserRepo[] userRepos = restTemplate.getForObject(gitHubProperties.getUserUrl() + userName + "/repos",
                                                                 UserRepo[].class);
                return userRepos != null ? (Arrays.stream(userRepos).filter(isNotForked())
                                                  .collect(Collectors.toList())) : new ArrayList<>();
        }

        private Predicate<UserRepo> isNotForked() {
                return userRepository -> !userRepository.isFork();
        }

}
