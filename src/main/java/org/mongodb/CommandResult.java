package org.mongodb;


public class CommandResult {

    private com.mongodb.CommandResult surrogate;

    public CommandResult(com.mongodb.CommandResult command) {
        this.surrogate = command;
    }

    public Document getResponse() {
        return new Document(surrogate);
    }

    public boolean isOk() {
        return surrogate.ok();
    }

    public String getErrorMessage() {
        return surrogate.getErrorMessage();
    }

}
