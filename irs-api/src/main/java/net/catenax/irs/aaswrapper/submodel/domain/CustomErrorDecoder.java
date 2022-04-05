package net.catenax.irs.aaswrapper.submodel.domain;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

import static feign.FeignException.errorStatus;

public class CustomErrorDecoder implements ErrorDecoder {

   @Override
   public Exception decode(final String methodKey, final Response response) {
      final FeignException exception = errorStatus(methodKey, response);
      final int status = response.status();

      if (status >= 500) {
         return new RetryableException(response.status(), exception.getMessage(), response.request().httpMethod(),
               exception, null, response.request());
      }
      return exception;
   }
}
