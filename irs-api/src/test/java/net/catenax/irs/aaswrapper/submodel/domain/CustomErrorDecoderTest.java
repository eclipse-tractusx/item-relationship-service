package net.catenax.irs.aaswrapper.submodel.domain;

import java.nio.charset.Charset;
import java.util.HashMap;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CustomErrorDecoderTest {
   @Test
   void given500Response_whenDecode_thenReturnRetryableException() {
      // given
      final ErrorDecoder decoder = new CustomErrorDecoder();
      final Response response = responseStub(500);

      // when
      final Exception exception = decoder.decode("GET", response);

      // then
      assertTrue(exception instanceof RetryableException);
   }

   @Test
   void given400Response_whenDecode_thenReturnFeignException() {
      // given
      ErrorDecoder decoder = new CustomErrorDecoder();
      Response response = responseStub(400);

      // when
      Exception exception = decoder.decode("GET", response);

      // then
      assertTrue(exception instanceof FeignException);
      assertFalse(exception instanceof RetryableException);
   }

   private Response responseStub(int status) {
      return Response.builder()
                     .request(Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), new byte[0],
                           Charset.defaultCharset(), new RequestTemplate()))
                     .status(status)
                     .build();
   }
}