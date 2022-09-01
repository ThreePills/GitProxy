package com.gitproxy.proxy.service;

import com.gitproxy.proxy.exception.GitUserNotFoundException;
import com.gitproxy.proxy.gitHubInfo.BranchInfo;
import com.gitproxy.proxy.gitHubInfo.CommitInfo;
import com.gitproxy.proxy.gitHubInfo.RepositoryOwnerData;
import com.gitproxy.proxy.gitHubInfo.UserRepo;
import com.gitproxy.proxy.response.BranchResponse;
import com.gitproxy.proxy.response.RepositoryResponse;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class ProxyServiceTest {

    @Mock
    private GitHubDataService gitHubClient;
    @InjectMocks
    private ProxyService proxyService;


    @Test
    public void testGetRepositoriesForUser() throws GitUserNotFoundException {
        //given
        String userName = "userName";
        String sha = "sha124";
        List<String> repositoryNames = List.of("Repo1", "Repo2", "Repo3");
        List<String> branchNames = List.of("branch1", "branch2");
        List<UserRepo> userRepos = buildUserRepos(repositoryNames, userName);
        List<BranchInfo> branchInfos = buildBranchInfos(branchNames, sha);
        when(gitHubClient.userExistsInGitHub(anyString())).thenReturn(true);
        when(gitHubClient.getBranchInformations(anyString(), any())).thenReturn(
                CompletableFuture.completedFuture(branchInfos));
        when(gitHubClient.getUserRepositoriesList(anyString())).thenReturn(userRepos);

        //when
        List<RepositoryResponse> result = proxyService.getRepositoriesForUser(userName);

        //then
        assertThat(result, hasSize(userRepos.size()));

        for (RepositoryResponse singleRepositoryResponse : result) {
            assertThat(singleRepositoryResponse.getOwnerLogin(), is(userName));
            assertThat(singleRepositoryResponse.getRepositoryName(), in(repositoryNames));
            assertThat(singleRepositoryResponse.getBranchResponse(), hasSize(2));
            for (BranchResponse singleBranchResponse : singleRepositoryResponse.getBranchResponse()) {
                assertThat(singleBranchResponse.getBranchName(), in(branchNames));
                assertThat(singleBranchResponse.getSha(), is(sha));
            }
        }
        verify(gitHubClient, times(1)).userExistsInGitHub(userName);
        verify(gitHubClient, times(1)).getUserRepositoriesList(userName);
        verify(gitHubClient, times(3)).getBranchInformations(eq(userName), any());
        verifyNoMoreInteractions(gitHubClient);
    }

    private List<UserRepo> buildUserRepos(List<String> repositoryNames, String userName) {
        return repositoryNames.stream().map(repositoryName -> buildUserRepo(repositoryName, userName)).toList();
    }

    private UserRepo buildUserRepo(String repoName, String userName) {
        return UserRepo.builder().isFork(false).name(repoName)
                .repositoryOwnerData(RepositoryOwnerData.builder().login(userName).build()).build();
    }

    private List<BranchInfo> buildBranchInfos(List<String> branchNames, String sha) {
        return branchNames.stream().map(branchName -> buildBranchInfo(branchName, sha)).toList();
    }

    private BranchInfo buildBranchInfo(String branchName, String sha) {
        return BranchInfo.builder().branchName(branchName).commitInfo(CommitInfo.builder().sha(sha).build())
                .build();
    }
}

