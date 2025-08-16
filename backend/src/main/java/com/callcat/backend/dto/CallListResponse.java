package com.callcat.backend.dto;

import java.util.List;

public class CallListResponse {
    
    private List<CallResponse> calls;
    private String nextToken;

    public CallListResponse() {}

    public CallListResponse(List<CallResponse> calls, String nextToken) {
        this.calls = calls;
        this.nextToken = nextToken;
    }

    public List<CallResponse> getCalls() {
        return calls;
    }

    public void setCalls(List<CallResponse> calls) {
        this.calls = calls;
    }

    public String getNextToken() {
        return nextToken;
    }

    public void setNextToken(String nextToken) {
        this.nextToken = nextToken;
    }
}