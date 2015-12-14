package org.dcm4chee.archive.store.hooks;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.hibernate.HibernateQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.StringPath;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4chee.archive.entity.PersonName;
import org.dcm4chee.archive.entity.QPersonName;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by Umberto Cappellini on 12/11/15.
 */
public class DefaultPersonNameManager implements PersonNameManagerHook {

    @PersistenceContext(name = "dcm4chee-arc", unitName="dcm4chee-arc")
    private EntityManager em;

    /**
     * By default, always create a new person name.
     * @throws DicomServiceException
     */
    @Override
    public PersonName findOrCreate(int nametag, Attributes attrs, FuzzyStr fuzzyStr, String nullValue)
            throws DicomServiceException {

        if (attrs==null)
            return null;

        PersonName parsed = PersonName.valueOf(attrs.getString(nametag), fuzzyStr, nullValue, null);

        if (parsed == null)
            return null;

        try {
            em.persist(parsed);
            return parsed;
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    /**
     * updates the existing person name, if needed.
     *
     * @throws DicomServiceException
     */
    public PersonName update(PersonName previous, int nametag, Attributes attrs, FuzzyStr fuzzyStr, String nullValue)
            throws DicomServiceException {

        if (attrs==null)
            return previous;

        PersonName parsed = PersonName.valueOf(attrs.getString(nametag), fuzzyStr, nullValue, null);
        if (parsed == null)
            return previous;

        if (previous!=null) {
            org.dcm4che3.data.PersonName pn = parsed.toPersonName();
            previous.fromDicom(pn, fuzzyStr, nullValue);
            return previous;
        }
        else {
            em.persist(parsed);
            return parsed;
        }
    }

    /**
     * utility method to matches every field of the person name, including null fields
     * (at the moment, not in use)
     */
    private PersonName find (PersonName parsed) {
        //matches every field of the person name, including null fields
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(match(parsed.getFamilyName(), QPersonName.personName.familyName));
        builder.and(match(parsed.getGivenName(),QPersonName.personName.givenName));
        builder.and(match(parsed.getMiddleName(),QPersonName.personName.middleName));
        builder.and(match(parsed.getNamePrefix(),QPersonName.personName.namePrefix));
        builder.and(match(parsed.getNameSuffix(), QPersonName.personName.nameSuffix));
        Session session = em.unwrap(Session.class);
        HibernateQuery query = new HibernateQuery(session).from(QPersonName.personName).where(builder);
        PersonName queried = query.uniqueResult(QPersonName.personName);
        return queried;
    }

    private static BooleanExpression match (String value, StringPath path)
    {
        if (value == null)
            return path.isNull();
        else
            return path.matches(value);
    }
}
