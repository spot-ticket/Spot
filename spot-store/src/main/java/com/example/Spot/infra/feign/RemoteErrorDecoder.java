//package com.example.Spot.infra.feign;
//
//import com.example.Spot.infra.feign.exception.RemoteCallFailedException;
//import com.example.Spot.infra.feign.exception.RemoteConflictException;
//import com.example.Spot.infra.feign.exception.RemoteNotFoundException;
//import com.example.Spot.infra.feign.exception.RemoteServiceUnavailableException;
//import feign.Response;
//import feign.Util;
//import feign.codec.ErrorDecoder;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
//public class RemoteErrorDecoder implements ErrorDecoder {
//    private static final int MAX_BODY_CHARS = 2000;
//
//    @Override
//    public Exception decode(String methodKey, Response response) {
//        int status = response.status();
//
//        if (status == 404) {
//            return new RemoteNotFoundException(methodKey);
//        }
//        if (status == 409) {
//            return new RemoteConflictException(methodKey);
//        }
//        if (status >= 500) {
//            return new RemoteServiceUnavailableException(methodKey);
//        }
//        return new RemoteCallFailedException(methodKey, status);
//    }
//
//    // 디버깅: 로그용 (로직추가필요)
//    private String readBody(Response response) {
//        if (response == null || response.body() == null) {
//            return null;
//        }
//        try {
//            String raw = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
//            if (raw == null) {
//                return null;
//            }
//            if (raw.length() <= MAX_BODY_CHARS) {
//                return raw;
//            }
//            return raw.substring(0, MAX_BODY_CHARS);
//        } catch (IOException e) {
//            return null;
//        }
//    }
//}
