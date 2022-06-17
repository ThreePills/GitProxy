package com.gitproxy.proxy.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RepositoryResponse {
        private String repositoryName;
        private String ownerLogin;
        @JsonProperty ("branchInformation")
        private List<BranchResponse> branchResponse;
}
