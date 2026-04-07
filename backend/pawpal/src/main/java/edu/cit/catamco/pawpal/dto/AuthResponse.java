package edu.cit.catamco.pawpal.dto;

public class AuthResponse {

    private final boolean success;
    private final Object data;
    private final Object error;
    private final String timestamp;

    // Private constructor — forces use of Builder
    private AuthResponse(Builder builder) {
        this.success   = builder.success;
        this.data      = builder.data;
        this.error     = builder.error;
        this.timestamp = builder.timestamp;
    }

    public boolean isSuccess()    { return success; }
    public Object getData()       { return data; }
    public Object getError()      { return error; }
    public String getTimestamp()  { return timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean success;
        private Object data;
        private Object error;
        private String timestamp;

        private Builder() {}

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder error(Object error) {
            this.error = error;
            return this;
        }

        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(this);
        }
    }
}
