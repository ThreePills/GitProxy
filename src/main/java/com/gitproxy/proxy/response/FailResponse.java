package com.gitproxy.proxy.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FailResponse {
    private int status;
    private String message;
}
