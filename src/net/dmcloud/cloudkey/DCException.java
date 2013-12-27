package net.dmcloud.cloudkey;

public class DCException extends Exception {

    int code = 0;

    public DCException(String message) {
        super(message);
    }

    public DCException(String message, int _code) {
        this(message);
        this.code = _code;
    }

    public int getCode() {
        return this.code;
    }
}