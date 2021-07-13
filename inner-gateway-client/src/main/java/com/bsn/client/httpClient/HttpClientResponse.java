package com.bsn.client.httpClient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yangpuguang on 2018/1/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpClientResponse {
    private int httpCode;
    private String responseBody;
}
