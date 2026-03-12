package main.java.com.authsystem.Jauth.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class GlobalErrorController implements ErrorController {

    /**
     * Handle /error requests and return appropriate response
     * This prevents "page has no mapping for /error" errors
     */
    @RequestMapping("/error")
    @ResponseBody
    public ResponseEntity<?> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");

        if (statusCode == null) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        ErrorResponse error = new ErrorResponse(
                statusCode,
                errorMessage != null ? errorMessage : "An error occurred",
                request.getRequestURI()
        );

        return ResponseEntity.status(statusCode).body(error);
    }

    /**
     * Error response DTO
     */
    public static class ErrorResponse {
        private int status;
        private String message;
        private String path;

        public ErrorResponse(int status, String message, String path) {
            this.status = status;
            this.message = message;
            this.path = path;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }
    }
}
