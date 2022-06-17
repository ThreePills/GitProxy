package com.gitproxy.proxy.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.gitproxy.proxy.exception.GitUserNotFoundException;
import com.gitproxy.proxy.gitHubInfo.BranchInfo;
import com.gitproxy.proxy.gitHubInfo.CommitInfo;
import com.gitproxy.proxy.gitHubInfo.RepositoryOwnerData;
import com.gitproxy.proxy.gitHubInfo.UserRepo;
import com.gitproxy.proxy.propeties.GitHubProperties;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith (MockitoExtension.class)
@RunWith (MockitoJUnitRunner.class)
public class GitHubDataServiceTest {

        @Mock
        RestTemplate restTemplate;

        @Mock
        GitHubProperties gitHubProperties;

        @InjectMocks
        GitHubDataService gitHubDataService;

        @Test
        public void testUserExistsInGitHub() throws GitUserNotFoundException {
                //given
                String userName = "userName";
                ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
                when(gitHubProperties.getUserUrl()).thenReturn("userUrl/");
                when(restTemplate.getForEntity(gitHubProperties.getUserUrl() + userName, String.class)).thenReturn(
                        stringResponseEntity);
                //when
                boolean result = gitHubDataService.userExistsInGitHub(userName);
                //then
                assertThat(result, is(true));
                verify(restTemplate, times(1)).getForEntity(gitHubProperties.getUserUrl() + userName, String.class);
                verifyNoMoreInteractions(restTemplate);
        }

        @Test
        public void testUserExistsInGitHubShouldReturnFalse() throws GitUserNotFoundException {
                //given
                String userName = "userName";
                ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
                when(gitHubProperties.getUserUrl()).thenReturn("userUrl/");
                when(restTemplate.getForEntity(gitHubProperties.getUserUrl() + userName, String.class)).thenReturn(
                        stringResponseEntity);
                //when
                boolean result = gitHubDataService.userExistsInGitHub(userName);
                //then
                assertThat(result, is(false));
                verify(restTemplate, times(1)).getForEntity(gitHubProperties.getUserUrl() + userName, String.class);
                verifyNoMoreInteractions(restTemplate);
        }

        @Test
        public void testUserExistsInGitHubShouldThrowException() throws Exception {
                //given
                String userName = "userName";
                ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
                when(gitHubProperties.getUserUrl()).thenReturn("userUrl/");
                when(restTemplate.getForEntity(gitHubProperties.getUserUrl() + userName, String.class)).thenThrow(
                        HttpClientErrorException.class);
                // when
                Throwable thrown = assertThrows(GitUserNotFoundException.class,
                                                () -> gitHubDataService.userExistsInGitHub(userName));

                // then
                assertThat(thrown, isA(GitUserNotFoundException.class));
                verify(restTemplate, times(1)).getForEntity(gitHubProperties.getUserUrl() + userName, String.class);

                verifyNoMoreInteractions(restTemplate);
        }

        @Test
        public void testGetBranchInformations() throws Exception {
                //given
                String repoName = "repoName";
                String userName = "userName";
                BranchInfo[] branchInfos = buildBranchInfos();
                UserRepo userRepo = UserRepo.builder().name(repoName).build();
                when(gitHubProperties.getBranchesUrl()).thenReturn("branchesUrl/");
                when(restTemplate.getForObject(
                        gitHubProperties.getBranchesUrl() + userName + "/" + userRepo.getName() + "/branches",
                        BranchInfo[].class)).thenReturn(branchInfos);
                //when
                CompletableFuture<List<BranchInfo>> result = gitHubDataService.getBranchInformations(userName,
                                                                                                     userRepo);
                //then
                assertThat(result, is(notNullValue()));
                assertThat(result.get(), hasSize(2));
                assertThat(result.get(), hasItems(branchInfos));
                verify(restTemplate, times(1)).getForObject(
                        gitHubProperties.getBranchesUrl() + userName + "/" + userRepo.getName() + "/branches",
                        BranchInfo[].class);
                verifyNoMoreInteractions(restTemplate);
        }

        @Test
        public void testGetUserRepositoriesList() throws Exception {
                //given
                String userName = "userName";
                UserRepo[] userRepos = buildUserRepos();

                List<UserRepo> filteredUserRepos = Arrays.asList(userRepos)
                                                         .stream()
                                                         .filter(userRepo -> !userRepo.isFork())
                                                         .collect(Collectors.toList());
                when(gitHubProperties.getUserUrl()).thenReturn("userUrl/");
                when(restTemplate.getForObject(gitHubProperties.getUserUrl() + userName + "/repos",
                                               UserRepo[].class)).thenReturn(userRepos);
                //when
                List<UserRepo> result = gitHubDataService.getUserRepositoriesList(userName);
                //then
                assertThat(result, is(notNullValue()));
                assertThat(result, hasSize(filteredUserRepos.size()));
                assertThat(result, containsInAnyOrder(filteredUserRepos.toArray()));
                verify(restTemplate, times(1)).getForObject(gitHubProperties.getUserUrl() + userName + "/repos",
                                                            UserRepo[].class);
                verifyNoMoreInteractions(restTemplate);
        }

        private UserRepo[] buildUserRepos() {
                return new UserRepo[] {
                        buildUserRepo("Repo1", false),
                        buildUserRepo("Repo2", false),
                        buildUserRepo("Repo3", true)};
        }

        private UserRepo buildUserRepo(String repoName, boolean isFork) {
                return UserRepo.builder()
                               .isFork(isFork)
                               .name(repoName)
                               .repositoryOwnerData(RepositoryOwnerData.builder().login("LoginTest").build())
                               .build();
        }

        private BranchInfo[] buildBranchInfos() {
                BranchInfo branch1 = buildBranchInfo("branch1", "52566456461");
                BranchInfo branch2 = buildBranchInfo("branch2", "544512362546");
                return new BranchInfo[] {branch1, branch2};
        }

        private BranchInfo buildBranchInfo(String branchName, String sha) {
                return BranchInfo.builder().branchName(branchName).commitInfo(CommitInfo.builder().sha(sha).build())
                                 .build();
        }
}
