package com.gitproxy.proxy.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.gitproxy.proxy.exception.GitUserNotFoundException;
import com.gitproxy.proxy.gitHubInfo.BranchInfo;
import com.gitproxy.proxy.gitHubInfo.CommitInfo;
import com.gitproxy.proxy.gitHubInfo.RepositoryOwnerData;
import com.gitproxy.proxy.gitHubInfo.UserRepo;
import com.gitproxy.proxy.response.RepositoryResponse;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith (MockitoExtension.class)
@RunWith (MockitoJUnitRunner.class)
public class ProxyServiceTest {

        @Mock
        GitHubDataService gitHubClient;
        @InjectMocks
        ProxyService proxyService;


        @Test
        public void testGetRepositoriesForUser() throws GitUserNotFoundException {
                //given
                String userName = "userName";
                List<UserRepo> userRepos = buildUserRepos();
                when(gitHubClient.userExistsInGitHub(anyString())).thenReturn(true);
                when(gitHubClient.getBranchInformations(anyString(), any())).thenReturn(
                        CompletableFuture.completedFuture(buildBranchInfos()));
                when(gitHubClient.getUserRepositoriesList(anyString())).thenReturn(userRepos);

                //when
                List<RepositoryResponse> result = proxyService.getRepositoriesForUser(userName);

                //then
                assertThat(result, hasSize(userRepos.size()));
                verify(gitHubClient, times(1)).userExistsInGitHub(userName);
                verify(gitHubClient, times(1)).getUserRepositoriesList(userName);
                verify(gitHubClient, times(3)).getBranchInformations(eq(userName), any());
                verifyNoMoreInteractions(gitHubClient);
        }

        private List<UserRepo> buildUserRepos() {
                return List.of(buildUserRepo("Repo1"), buildUserRepo("Repo2"), buildUserRepo("Repo3"));
        }

        private UserRepo buildUserRepo(String repoName) {
                return UserRepo.builder().isFork(false).name(repoName)
                               .repositoryOwnerData(RepositoryOwnerData.builder().login("LoginTest").build()).build();
        }

        private List<BranchInfo> buildBranchInfos() {
                BranchInfo branch1 = buildBranchInfo("branch1", "52566456461");
                BranchInfo branch2 = buildBranchInfo("branch2", "544512362546");
                return List.of(branch1, branch2);
        }

        private BranchInfo buildBranchInfo(String branchName, String sha) {
                return BranchInfo.builder().branchName(branchName).commitInfo(CommitInfo.builder().sha(sha).build())
                                 .build();
        }
}

