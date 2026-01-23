//package com.example.Spot.infra.feign;
//
//import jakarta.servlet.http.HttpServletRequest;
//
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import feign.RequestInterceptor;
//import feign.RequestTemplate;
//
//public class FeignHeaderRelayInterceptor implements RequestInterceptor {
//
//    public static final String HEADER_AUTHORIZATION = "Authorization";
//
//    @Override
//    public void apply(RequestTemplate template) {
//        ServletRequestAttributes attrs =
//                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//
//        if (attrs == null) {
//            return;
//        }
//
//        HttpServletRequest request = attrs.getRequest();
//        String auth = request.getHeader(HEADER_AUTHORIZATION);
//
//        if (auth != null && !auth.isBlank()) {
//            template.header(HEADER_AUTHORIZATION, auth);
//        }
//    }
//}
