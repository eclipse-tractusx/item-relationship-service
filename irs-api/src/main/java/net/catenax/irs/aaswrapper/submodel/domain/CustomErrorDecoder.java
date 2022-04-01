package net.catenax.irs.aaswrapper.submodel.domain;

import feign.Response;
import feign.codec.ErrorDecoder;

import net.catenax.irs.exceptions.BadRequestException;
import net.catenax.irs.exceptions.NotFoundException;

public class CustomErrorDecoder implements ErrorDecoder {

   @Override
   public Exception decode(final String methodKey, final Response response) {

      switch (response.status()) {
      case 400:
         return new BadRequestException();
      case 404:
         return new NotFoundException();
      default:
         return new Exception("Generic error");
      }
   }
}
