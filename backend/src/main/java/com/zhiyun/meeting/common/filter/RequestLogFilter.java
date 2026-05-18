package com.zhiyun.meeting.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 请求日志过滤器
 *
 * 作用：
 * 1. 打印每次 HTTP 请求的请求方式、地址、参数
 * 2. 打印请求体 JSON
 * 3. 打印响应结果 JSON
 * 4. 打印接口耗时
 * 5. 自动脱敏 password / token / Authorization
 *
 * 注意：
 * 这个类只处理 HTTP 接口日志。
 * WebSocket 消息日志仍然由 MeetingWebSocketHandler 负责打印。
 */
@Component
public class RequestLogFilter extends OncePerRequestFilter {

    /**
     * 日志最大打印长度
     *
     * 防止响应内容太大导致控制台刷屏。
     */
    private static final int MAX_BODY_LENGTH = 2000;

    /**
     * 是否跳过某些不需要打印的请求
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/webjars")
                || uri.startsWith("/ws/meeting")
                || "/favicon.ico".equals(uri)
                || "/error".equals(uri);
    }

    /**
     * 执行过滤逻辑
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long cost = System.currentTimeMillis() - startTime;

            printRequestLog(requestWrapper, responseWrapper, cost);

            // 必须调用，否则前端拿不到响应体
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * 打印请求日志
     */
    private void printRequestLog(ContentCachingRequestWrapper request,
                                 ContentCachingResponseWrapper response,
                                 long cost) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIp(request);
        int status = response.getStatus();

        Object currentUserIdObj = request.getAttribute("currentUserId");
        String currentUserId = currentUserIdObj == null ? "" : String.valueOf(currentUserIdObj);

        String authorization = maskAuthorization(request.getHeader("Authorization"));
        String requestBody = getRequestBody(request);
        String responseBody = getResponseBody(response);

        System.out.println("\n==================== HTTP REQUEST START ====================");
        System.out.println("[HTTP][BASIC] method=" + method
                + ", uri=" + uri
                + ", query=" + (queryString == null ? "" : queryString)
                + ", status=" + status
                + ", cost=" + cost + "ms"
                + ", clientIp=" + clientIp
                + ", currentUserId=" + currentUserId);

        if (authorization != null && !authorization.isEmpty()) {
            System.out.println("[HTTP][AUTH] " + authorization);
        }

        if (requestBody != null && !requestBody.isEmpty()) {
            System.out.println("[HTTP][REQUEST_BODY] " + requestBody);
        }

        if (responseBody != null && !responseBody.isEmpty()) {
            System.out.println("[HTTP][RESPONSE_BODY] " + responseBody);
        }

        System.out.println("===================== HTTP REQUEST END =====================\n");
    }

    /**
     * 获取请求体
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        String contentType = request.getContentType();

        if (isMultipart(contentType) || isBinaryContent(contentType)) {
            return "[文件或二进制内容，不打印]";
        }

        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }

        Charset charset = StandardCharsets.UTF_8;
        String body = new String(content, charset);

        body = maskSensitiveInfo(body);

        return truncate(body);
    }

    /**
     * 获取响应体
     *
     * 注意：
     * Spring Boot JSON 响应实际基本都是 UTF-8。
     * 有时 response.getCharacterEncoding() 可能拿到 ISO-8859-1，
     * 这会导致中文日志显示成 æä½æå 这种乱码。
     * 所以 JSON 响应这里强制使用 UTF-8 解码。
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();

        if (isBinaryContent(contentType)) {
            return "[二进制响应内容，不打印]";
        }

        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }

        Charset charset = StandardCharsets.UTF_8;

        if (contentType != null) {
            String lowerContentType = contentType.toLowerCase();

            if (!lowerContentType.contains("application/json")
                    && !lowerContentType.contains("text/plain")
                    && !lowerContentType.contains("text/html")) {
                charset = getCharset(response.getCharacterEncoding());
            }
        }

        String body = new String(content, charset);

        body = maskSensitiveInfo(body);

        return truncate(body);
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");

        if (xRealIp != null && !xRealIp.trim().isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 获取字符编码
     */
    private Charset getCharset(String encoding) {
        if (encoding == null || encoding.trim().isEmpty()) {
            return StandardCharsets.UTF_8;
        }

        try {
            return Charset.forName(encoding);
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }

    /**
     * 是否是文件上传
     */
    private boolean isMultipart(String contentType) {
        if (contentType == null) {
            return false;
        }

        return contentType.toLowerCase().contains("multipart/form-data");
    }

    /**
     * 是否是二进制内容
     */
    private boolean isBinaryContent(String contentType) {
        if (contentType == null) {
            return false;
        }

        String lower = contentType.toLowerCase();

        return lower.startsWith("image/")
                || lower.startsWith("video/")
                || lower.startsWith("audio/")
                || lower.contains("application/octet-stream");
    }

    /**
     * 脱敏敏感信息
     */
    private String maskSensitiveInfo(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }

        String result = body;

        result = result.replaceAll("(\"password\"\\s*:\\s*\")([^\"]*)(\")", "$1******$3");
        result = result.replaceAll("(\"token\"\\s*:\\s*\")([^\"]*)(\")", "$1******$3");
        result = result.replaceAll("(\"rtcToken\"\\s*:\\s*\")([^\"]*)(\")", "$1******$3");
        result = result.replaceAll("(\"signalToken\"\\s*:\\s*\")([^\"]*)(\")", "$1******$3");

        return result;
    }

    /**
     * Authorization 请求头脱敏
     */
    private String maskAuthorization(String authorization) {
        if (authorization == null || authorization.trim().isEmpty()) {
            return "";
        }

        if (authorization.length() <= 16) {
            return "Authorization=******";
        }

        return "Authorization=" + authorization.substring(0, 12) + "******";
    }

    /**
     * 截断过长内容
     */
    private String truncate(String text) {
        if (text == null) {
            return "";
        }

        if (text.length() <= MAX_BODY_LENGTH) {
            return text;
        }

        return text.substring(0, MAX_BODY_LENGTH) + "...[内容过长，已截断]";
    }
}