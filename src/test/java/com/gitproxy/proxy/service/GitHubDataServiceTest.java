package com.gitproxy.proxy.service;

import com.gitproxy.proxy.builder.GitHubUrlBuilder;
import com.gitproxy.proxy.exception.GitUserNotFoundException;
import com.gitproxy.proxy.gitHubInfo.BranchInfo;
import com.gitproxy.proxy.gitHubInfo.CommitInfo;
import com.gitproxy.proxy.gitHubInfo.RepositoryOwnerData;
import com.gitproxy.proxy.gitHubInfo.UserRepo;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class GitHubDataServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GitHubUrlBuilder gitHubUrlBuilder;

    @InjectMocks
    private GitHubDataService gitHubDataService;

    @Test
    public void testUserExistsInGitHub() throws GitUserNotFoundException, URISyntaxException {
        //given
        String userName = "userName";
        URI userURI = new URI("userURI");
        ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
        when(gitHubUrlBuilder.getUserURI(any())).thenReturn(userURI);
        when(restTemplate.getForEntity(userURI, String.class)).thenReturn(stringResponseEntity);

        //when
        boolean result = gitHubDataService.userExistsInGitHub(userName);

        //then
        assertThat(result, is(true));
        verify(gitHubUrlBuilder, times(1)).getUserURI(userName);
        verify(restTemplate, times(1)).getForEntity(userURI, String.class);
        verifyNoMoreInteractions(restTemplate, gitHubUrlBuilder);
    }

    @Test
    public void testUserExistsInGitHubShouldReturnFalse() throws GitUserNotFoundException, URISyntaxException {
        //given
        String userName = "userName";
        URI userURI = new URI("userURI");
        ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        when(gitHubUrlBuilder.getUserURI(any())).thenReturn(userURI);
        when(restTemplate.getForEntity(userURI, String.class)).thenReturn(stringResponseEntity);

        //when
        boolean result = gitHubDataService.userExistsInGitHub(userName);

        //then
        assertThat(result, is(false));
        verify(gitHubUrlBuilder, times(1)).getUserURI(userName);
        verify(restTemplate, times(1)).getForEntity(userURI, String.class);
        verifyNoMoreInteractions(restTemplate, gitHubUrlBuilder);
    }

    @Test
    public void testUserExistsInGitHubShouldThrowException() throws URISyntaxException {
        //given
        String userName = "userName";
        URI userURI = new URI("userURI");
        when(gitHubUrlBuilder.getUserURI(any())).thenReturn(userURI);
        when(restTemplate.getForEntity(userURI, String.class)).thenThrow(HttpClientErrorException.class);

        // when
        Throwable thrown = assertThrows(GitUserNotFoundException.class, () -> gitHubDataService.userExistsInGitHub(userName));

        // then
        assertThat(thrown, isA(GitUserNotFoundException.class));
        verify(gitHubUrlBuilder, times(1)).getUserURI(userName);
        verify(restTemplate, times(1)).getForEntity(userURI, String.class);
        verifyNoMoreInteractions(restTemplate, gitHubUrlBuilder);
    }

    @Test
    public void testGetBranchInformations() throws Exception {
        //given
        String repoName = "repoName";
        String userName = "userName";
        URI branchesInfoURI = new URI("branchesInfoURI");
        BranchInfo[] branchInfos = buildBranchInfos();
        UserRepo userRepo = UserRepo.builder().name(repoName).build();
        when(gitHubUrlBuilder.getBranchesInfoURI(any(), any())).thenReturn(branchesInfoURI);
        when(restTemplate.getForObject(branchesInfoURI, BranchInfo[].class)).thenReturn(branchInfos);

        //when
        CompletableFuture<List<BranchInfo>> result = gitHubDataService.getBranchInformations(userName, userRepo);

        //then
        assertThat(result, is(notNullValue()));
        assertThat(result.get(), hasSize(2));
        assertThat(result.get(), hasItems(branchInfos));
        verify(gitHubUrlBuilder, times(1)).getBranchesInfoURI(userName, userRepo);
        verify(restTemplate, times(1)).getForObject(branchesInfoURI, BranchInfo[].class);
        verifyNoMoreInteractions(restTemplate, gitHubUrlBuilder);
    }

    @Test
    public void testGetUserRepositoriesList() throws URISyntaxException {
        //given
        String userName = "userName";
        UserRepo[] userRepos = buildUserRepos();
        URI userReposURI = new URI("userReposURI");
        List<UserRepo> filteredUserRepos = Arrays.stream(userRepos)
                .filter(userRepo -> !userRepo.isFork())
                .toList();
        when(gitHubUrlBuilder.getUserReposURI(any())).thenReturn(userReposURI);
        when(restTemplate.getForObject(userReposURI, UserRepo[].class)).thenReturn(userRepos);

        //when
        List<UserRepo> result = gitHubDataService.getUserRepositoriesList(userName);

        //then
        assertThat(result, is(notNullValue()));
        assertThat(result, hasSize(filteredUserRepos.size()));
        assertThat(result, containsInAnyOrder(filteredUserRepos.toArray()));
        verify(gitHubUrlBuilder, times(1)).getUserReposURI(userName);
        verify(restTemplate, times(1)).getForObject(userReposURI, UserRepo[].class);
        verifyNoMoreInteractions(restTemplate, gitHubUrlBuilder);
    }

    private UserRepo[] buildUserRepos() {
        return new UserRepo[]{
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
        return new BranchInfo[]{branch1, branch2};
    }

    private BranchInfo buildBranchInfo(String branchName, String sha) {
        return BranchInfo.builder().branchName(branchName).commitInfo(CommitInfo.builder().sha(sha).build()).build();
    }
}
