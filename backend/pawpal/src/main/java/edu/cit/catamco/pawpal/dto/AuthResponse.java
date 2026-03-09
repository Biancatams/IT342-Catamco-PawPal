package edu.cit.catamco.pawpal.dto;

public class AuthResponse {
    private boolean success;
    private Object data;
    private Object error;
    private String timestamp;

    public AuthResponse() {}

    public AuthResponse(boolean success, Object data,
                        Object error, String timestamp) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = timestamp;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public Object getData() { return data; }
    public Object getError() { return error; }
    public String getTimestamp() { return timestamp; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setData(Object data) { this.data = data; }
    public void setError(Object error) { this.error = error; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean success;
        private Object data;
        private Object error;
        private String timestamp;

        public Builder success(boolean success) {
            this.success = success; return this;
        }
        public Builder data(Object data) {
            this.data = data; return this;
        }
        public Builder error(Object error) {
            this.error = error; return this;
        }
        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp; return this;
        }
        public AuthResponse build() {
            return new AuthResponse(success, data, error, timestamp);
        }
    }
}