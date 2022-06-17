package com.gitproxy.proxy.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BranchResponse {
        @JsonProperty ("branch")
        private String branchName;
        @JsonProperty ("sha")
        private String sha;
}
