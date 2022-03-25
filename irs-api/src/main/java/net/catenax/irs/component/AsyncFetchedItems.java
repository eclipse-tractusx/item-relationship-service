package net.catenax.irs.component;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Value
@Builder
public class AsyncFetchedItems {
   /**
    *
    */
   private Integer queue;

   /**
    *
    */
   private Integer running;

   /**
    *
    */
   private Integer complete;

   /**
    *
    */
   private Integer failed;

   /**
    *
    */
   private Integer lost;
}
