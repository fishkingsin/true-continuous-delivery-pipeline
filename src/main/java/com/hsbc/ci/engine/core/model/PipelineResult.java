package com.hsbc.ci.engine.core.model;

public class PipelineResult {
    private final boolean success;
    private final String error;
    private final PipelineContext context;

    private PipelineResult(boolean success, String error, PipelineContext context) {
        this.success = success;
        this.error = error;
        this.context = context;
    }

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public PipelineContext getContext() { return context; }

    public static PipelineResult success(PipelineContext context) {
        return new PipelineResult(true, null, context);
    }

    public static PipelineResult failed(String error) {
        return new PipelineResult(false, error, null);
    }
}
