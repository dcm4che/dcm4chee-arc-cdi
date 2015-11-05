///
///////////////////////////////////////////////////////////////
//                C O P Y R I G H T  (c) 2015                //
//        Agfa HealthCare N.V. and/or its affiliates         //
//                    All Rights Reserved                    //
///////////////////////////////////////////////////////////////
//                                                           //
//       THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF      //
//        Agfa HealthCare N.V. and/or its affiliates.        //
//      The copyright notice above does not evidence any     //
//     actual or intended publication of such source code.   //
//                                                           //
///////////////////////////////////////////////////////////////
//
package org.dcm4chee.archive.conf;

public class Utils {

    public static long parseTimeInterval(String text) {
        int len = text.length();
        long ms = Long.parseLong(text.substring(0, len - 1));
        switch (text.charAt(len - 1)) {
        case 'w':
            ms *= 7;
        case 'd':
            ms *= 24;
        case 'h':
            ms *= 60;
        case 'm':
            ms *= 60;
        case 's':
            ms *= 1000;
        case 'i':
            break;
        default:
            throw new IllegalArgumentException("interval: " + text);
        }
        return ms;
    }
}
