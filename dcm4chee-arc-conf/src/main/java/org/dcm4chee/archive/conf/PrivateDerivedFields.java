package org.dcm4chee.archive.conf;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Umberto Cappellini on 6/8/15.
 */
@ConfigurableClass
public class PrivateDerivedFields implements Iterable<PrivateTag>, Serializable {

    private static final long serialVersionUID = 7463738930681558356L;

    @LDAP(noContainerNode = true)
    @ConfigurableProperty
    private List<PrivateTag> list = new ArrayList<PrivateTag>();

    public enum NAMES {
        StudyUpdateTimeDerivedField,
        NumberVisibleImagesDerivedField;
    }

    public void add(PrivateTag tag) {
        if (findByCommonName(NAMES.valueOf(tag.getCommonName())) != null)
            throw new IllegalStateException("PrivateTag with cn: '"
                    + tag.getCommonName() + "' already exists");
        list.add(tag);
    }

    public List<PrivateTag> getList() {
        return list;
    }

    public void setList(List<PrivateTag> list) {
        this.list.clear();
        for (PrivateTag tag : list)
            add(tag);
    }

    public void add(PrivateDerivedFields tags) {
        for (PrivateTag tag : tags)
            add(tag);
    }

    public boolean remove(PrivateTag tag) {
        return list.remove(tag);
    }

    public void clear() {
        list.clear();
    }

    public PrivateTag findByCommonName(NAMES commonName) {
        for (PrivateTag tag : list)
            if (commonName.equals(tag.getCommonName()))
                return tag;
        return null;
    }

    public PrivateTag findStudyUpdateTimeTag() {
        return findByCommonName(NAMES.StudyUpdateTimeDerivedField);
    }

    @Override
    public Iterator<PrivateTag> iterator() {
        return list.iterator();
    }
}
