package com.gitproxy.proxy.propeties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("git")
public class GitHubProperties {

    private String scheme;
    private String baseUrl;
    private String userPath;
    private String reposPath;
    private String branchesPath;


}
