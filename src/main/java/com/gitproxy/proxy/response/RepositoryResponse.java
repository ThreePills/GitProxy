package com.gitproxy.proxy.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RepositoryResponse {
    private String repositoryName;
    private String ownerLogin;
    @JsonProperty("branchInformation")
    private List<BranchResponse> branchResponse;
}
