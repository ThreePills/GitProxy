package com.gitproxy.proxy.gitHubInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties (ignoreUnknown = true)
public class BranchInfo {
        @JsonProperty ("name")
        private String branchName;
        @JsonProperty ("commit")
        private CommitInfo commitInfo;
}
