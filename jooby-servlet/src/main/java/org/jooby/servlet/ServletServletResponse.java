package org.jooby.servlet;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jooby.spi.NativeResponse;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Closeables;

public class ServletServletResponse implements NativeResponse {

  private HttpServletRequest req;

  private HttpServletResponse rsp;

  public ServletServletResponse(final HttpServletRequest req, final HttpServletResponse rsp) {
    this.req = requireNonNull(req, "A request is required.");
    this.rsp = requireNonNull(rsp, "A response is required.");
  }

  @Override
  public void cookie(final org.jooby.Cookie cookie) {
    Cookie c = new Cookie(cookie.name(), cookie.value().orElse(null));
    cookie.comment().ifPresent(c::setComment);
    cookie.domain().ifPresent(c::setDomain);
    c.setHttpOnly(cookie.httpOnly());
    c.setMaxAge((int) cookie.maxAge());
    c.setPath(cookie.path());
    c.setSecure(cookie.secure());
    c.setVersion(cookie.version());

    rsp.addCookie(c);
  }

  @Override
  public void clearCookie(final String name) {
    Cookie[] cookies = req.getCookies();
    Optional<Cookie> result = Optional.empty();
    if (cookies != null) {
      result = Arrays.stream(cookies)
          .filter(c -> c.getName().equals(name))
          .findFirst();
    }
    result.ifPresent(cookie -> {
      cookie.setMaxAge(0);
      rsp.addCookie(cookie);
    });
  }

  @Override
  public List<String> headers(final String name) {
    Collection<String> headers = rsp.getHeaders(name);
    if (headers == null) {
      return Collections.emptyList();
    }
    return ImmutableList.copyOf(headers);
  }

  @Override
  public Optional<String> header(final String name) {
    String header = rsp.getHeader(name);
    return header == null || header.isEmpty() ? Optional.empty() : Optional.of(header);
  }

  @Override
  public void header(final String name, final String value) {
    rsp.setHeader(name, value);
  }

  @Override
  public OutputStream out() throws IOException {
    return rsp.getOutputStream();
  }

  @Override
  public int statusCode() {
    return rsp.getStatus();
  }

  @Override
  public void statusCode(final int statusCode) {
    rsp.setStatus(statusCode);
  }

  @Override
  public boolean committed() {
    return rsp.isCommitted();
  }

  @Override
  public void end() throws IOException {
    if (!committed()) {
      Closeables.close(out(), true);
    }
  }

  @Override
  public void reset() {
    rsp.reset();
  }

}