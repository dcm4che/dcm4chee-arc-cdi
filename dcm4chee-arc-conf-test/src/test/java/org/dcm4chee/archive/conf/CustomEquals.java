package org.dcm4chee.archive.conf;

import org.dcm4che3.conf.api.AttributeCoercion;
import org.dcm4che3.conf.api.AttributeCoercions;
import org.dcm4che3.imageio.codec.CompressionRule;
import org.dcm4che3.imageio.codec.CompressionRules;

import java.util.Iterator;

/**
 * Created by aprvf on 10/11/2014.
 */
public class CustomEquals {
    /**
    * Created by aprvf on 10/11/2014.
    */
    static class CompressionRulesDeepEquals implements DeepEquals.CustomDeepEquals {

         @Override
         public boolean deepEquals(Object first, Object second) {
             return deepEquals((CompressionRules) first, (CompressionRules) second);
         }

         public boolean deepEquals(CompressionRules first,
                 CompressionRules second) {

             Iterator<CompressionRule> i = first.iterator();

             while (i.hasNext()) {
                 CompressionRule left = i.next();
                 CompressionRule right = second.findByCommonName(left.getCommonName());

                 if (!DeepEquals.deepEquals(left, right)) return false ;
             }

             return true;
         }
     }

    /**
    * Created by aprvf on 10/11/2014.
    */
    static class AttributeCoercionsDeepEquals implements DeepEquals.CustomDeepEquals {

       @Override
       public boolean deepEquals(Object first, Object second) {
           return deepEquals((AttributeCoercions) first, (AttributeCoercions) second);
       }

       public boolean deepEquals(AttributeCoercions first,
               AttributeCoercions second) {

           Iterator<AttributeCoercion> i = first.iterator();

           while (i.hasNext()) {
               AttributeCoercion left = i.next();
               AttributeCoercion right = second.findByCommonName(left.getCommonName());

               if (!DeepEquals.deepEquals(left, right)) return false ;
           }

           return true;
       }
    }
}
