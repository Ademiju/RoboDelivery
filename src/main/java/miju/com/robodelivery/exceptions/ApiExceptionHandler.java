package miju.com.robodelivery.exceptions;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import miju.com.robodelivery.dto.responses.APIError;
import org.springframework.http.*;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {
 private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


 @ExceptionHandler(MethodArgumentNotValidException.class)
 ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException e) {
  log.error(":: MethodArgumentNotValidException :: {}",e.getMessage());
  Map<String,String> errors = new LinkedHashMap<>();
  e.getBindingResult().getFieldErrors()
          .forEach(x -> errors.put(x.getField(), x.getDefaultMessage()));
  return ResponseEntity.badRequest().body(Map.of("message","Validation failed","errors",errors));
 }


 @ExceptionHandler(APIErrorException.class)
 ResponseEntity<APIError> apiError(APIErrorException e, HttpServletRequest request) {
  log.error(":: APIErrorException :: {}",e.getMessage());
  return new ResponseEntity<>(
          APIError.builder()
                  .statusMessage(e.getMessage())
                  .errorCode(e.getStatusCode())
                  .timestamp(e.getTimestamp())
                  .method(request.getMethod())
                  .path(request.getServletPath())
                  .build(),
          e.getHttpStatus());
 }

 ResponseEntity<APIError> handleMissingRequestHeaderException(MissingRequestHeaderException exception, HttpServletRequest request) {
  log.error("Bad Request received. MissingRequestHeader or ServletRequestBindingException: {}.", exception.getMessage());
  return new ResponseEntity<>(
          APIError.builder()
                  .errorCode("401")
                  .statusMessage(exception.getMessage())
                  .method(request.getMethod())
                  .path(request.getServletPath())
                  .timestamp(getTimestamp())
                  .build(),
          HttpStatus.UNAUTHORIZED);
 }

 @ExceptionHandler(MissingServletRequestParameterException.class)
 ResponseEntity<APIError> handleMissingServletRequestParameterException(
         MissingServletRequestParameterException exception, HttpServletRequest request) {
  log.error(":: MissingServletRequestParameterException :: {}", exception.getMessage());
  return new ResponseEntity<>(
          APIError.builder()
                  .errorCode("400")
                  .statusMessage(exception.getMessage())
                  .method(request.getMethod())
                  .path(request.getServletPath())
                  .timestamp(getTimestamp())
                  .build(),
          BAD_REQUEST);
 }

 private ResponseEntity<APIError> handleUnsupportedMethodException(
         HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
  log.error(":: HttpRequestMethodNotSupportedException :: {}", exception.getMessage());
  return new ResponseEntity<>(
          APIError.builder()
                  .errorCode("405")
                  .statusMessage(exception.getMessage())
                  .method(request.getMethod())
                  .path(request.getServletPath())
                  .timestamp(getTimestamp())
                  .build(),
          HttpStatus.METHOD_NOT_ALLOWED);
 }


 public String getTimestamp() {
  return dateFormat.format(new Date());
 }

}
