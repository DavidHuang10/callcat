package com.callcat.backend.dto;

import java.util.List;

public class CallListResponse {
    
    private List<CallResponse> calls;

    public CallListResponse() {}

    public CallListResponse(List<CallResponse> calls) {
        this.calls = calls;
    }

    public List<CallResponse> getCalls() {
        return calls;
    }

    public void setCalls(List<CallResponse> calls) {
        this.calls = calls;
    }
}