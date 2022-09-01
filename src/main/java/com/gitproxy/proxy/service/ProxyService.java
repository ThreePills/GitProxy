package com.gitproxy.proxy.service;


import com.gitproxy.proxy.gitHubInfo.BranchInfo;
import com.gitproxy.proxy.gitHubInfo.UserRepo;
import com.gitproxy.proxy.response.BranchResponse;
import com.gitproxy.proxy.response.RepositoryResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ProxyService {

    private final GitHubDataService gitHubClient;


    @SneakyThrows
    public List<RepositoryResponse> getRepositoriesForUser(String userName) {
        List<RepositoryResponse> repositoryResponses = new ArrayList<>();

        if (gitHubClient.userExistsInGitHub(userName)) {
            repositoryResponses = gitHubClient.getUserRepositoriesList(userName).stream().map(userRepository ->
                    buildRepositoryResponse(userRepository, getBranchInformations(userName, userRepository))).toList();
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
        return branchInfos.stream().map(this::buildSingleBranchResponse).toList();
    }

    private BranchResponse buildSingleBranchResponse(BranchInfo branchInfo) {
        BranchResponse branchResponse = new BranchResponse();
        branchResponse.setBranchName(branchInfo.getBranchName());
        branchResponse.setSha(branchInfo.getCommitInfo().getSha());
        return branchResponse;
    }

    private List<BranchInfo> getBranchInformations(String userName, UserRepo userRepository) {
        return gitHubClient.getBranchInformations(userName, userRepository).join();
    }
}
