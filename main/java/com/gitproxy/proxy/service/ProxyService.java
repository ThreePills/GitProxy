package com.gitproxy.proxy.service;


import java.util.ArrayList;
import java.util.List;

import com.gitproxy.proxy.gitHubInfo.BranchInfo;
import com.gitproxy.proxy.gitHubInfo.UserRepo;
import com.gitproxy.proxy.response.BranchResponse;
import com.gitproxy.proxy.response.RepositoryResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProxyService {

        private final GitHubDataService gitHubClient;


        @SneakyThrows
        public List<RepositoryResponse> getRepositoriesForUser(String userName) {
                List<RepositoryResponse> repositoryResponses = new ArrayList<>();

                if (gitHubClient.userExistsInGitHub(userName)) {
                        List<UserRepo> userRepositoriesList = gitHubClient.getUserRepositoriesList(userName);
                        userRepositoriesList.forEach(userRepository -> {
                                gitHubClient.getBranchInformations(userName, userRepository)
                                            .thenApply(branchInfo -> repositoryResponses.add(
                                                    buildRepositoryResponse(userRepository, branchInfo)));
                        });
                }
                return repositoryResponses;
        }


        private RepositoryResponse buildRepositoryResponse(UserRepo userRepo,
                                                           List<BranchInfo> branchInfos) {
                RepositoryResponse repositoryResponse = new RepositoryResponse();
                repositoryResponse.setRepositoryName(userRepo.getName());
                repositoryResponse.setOwnerLogin(userRepo.getRepositoryOwnerData().getLogin());
                repositoryResponse.setBranchResponse(buildBranchResponse(branchInfos));
                return repositoryResponse;
        }

        private List<BranchResponse> buildBranchResponse(List<BranchInfo> branchInfos) {
                List<BranchResponse> branchResponses = new ArrayList<>();
                for (BranchInfo branchInfo : branchInfos) {
                        BranchResponse branchResponse = new BranchResponse();
                        branchResponse.setBranchName(branchInfo.getBranchName());
                        branchResponse.setSha(branchInfo.getCommitInfo().getSha());
                        branchResponses.add(branchResponse);
                }
                return branchResponses;
        }
}
