package org.dcm4chee.archive.conf;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;

/**
 * Created by Umberto Cappellini on 6/3/15.
 */

@LDAP(objectClasses = "dcmPrivateTagType", distinguishingField = "cn")
@ConfigurableClass
public class PrivateTag {

    public PrivateTag() {
    }

    public PrivateTag(String commonName, String tag, String creator) {
        this.commonName = commonName;
        this.tag = tag;
        this.creator = creator;
    }

    public PrivateTag(String commonName, String tag, String creator, String description) {
        this.commonName = commonName;
        this.tag = tag;
        this.creator = creator;
        this.description = description;
    }

    @ConfigurableProperty(name = "cn")
    private String commonName;

    @ConfigurableProperty(name = "dcmPrivateTagValue")
    private String tag;

    @ConfigurableProperty(name = "dcmPrivateTagCreator")
    private String creator;

    @ConfigurableProperty(name = "dcmPrivateTagDescription")
    private String description;

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getTag() {
        return tag;
    }

    public int getIntTag() {
        return Integer.valueOf(tag);
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setIntTag(int tag) {
        this.tag = Integer.toHexString(tag);
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
