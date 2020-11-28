// 
// Decompiled by Procyon v0.5.36
// 

package org.json.simple.parser;

public class ParseException extends Exception
{
    private /* synthetic */ int position;
    private /* synthetic */ Object unexpectedObject;
    private /* synthetic */ int errorType;
    
    public void setUnexpectedObject(final Object unexpectedObject) {
        this.unexpectedObject = unexpectedObject;
    }
    
    static {
        ERROR_UNEXPECTED_CHAR = 0;
        ERROR_UNEXPECTED_EXCEPTION = 2;
        ERROR_UNEXPECTED_TOKEN = 1;
    }
    
    public int getErrorType() {
        return this.errorType;
    }
    
    public ParseException(final int position, final int errorType, final Object unexpectedObject) {
        this.position = position;
        this.errorType = errorType;
        this.unexpectedObject = unexpectedObject;
    }
    
    public void setErrorType(final int errorType) {
        this.errorType = errorType;
    }
    
    public ParseException(final int n, final Object o) {
        this(-1, n, o);
    }
    
    public ParseException(final int n) {
        this(-1, n, null);
    }
    
    public int getPosition() {
        return this.position;
    }
    
    public void setPosition(final int position) {
        this.position = position;
    }
    
    public Object getUnexpectedObject() {
        return this.unexpectedObject;
    }
    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        switch (this.errorType) {
            case 0: {
                sb.append("Unexpected character (").append(this.unexpectedObject).append(") at position ").append(this.position).append(".");
                break;
            }
            case 1: {
                sb.append("Unexpected token ").append(this.unexpectedObject).append(" at position ").append(this.position).append(".");
                break;
            }
            case 2: {
                sb.append("Unexpected exception at position ").append(this.position).append(": ").append(this.unexpectedObject);
                break;
            }
            default: {
                sb.append("Unkown error at position ").append(this.position).append(".");
                break;
            }
        }
        return sb.toString();
    }
}
