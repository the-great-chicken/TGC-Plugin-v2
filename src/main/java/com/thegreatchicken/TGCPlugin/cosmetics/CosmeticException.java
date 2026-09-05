package com.thegreatchicken.TGCPlugin.cosmetics;

public final class CosmeticException extends RuntimeException {
    private final int status;
    private final String code;
    public CosmeticException(int status, String code) {
        super(code);
        this.status = status;
        this.code = code;
    }
    public int status() { return status; }
    public String code() { return code; }
}
