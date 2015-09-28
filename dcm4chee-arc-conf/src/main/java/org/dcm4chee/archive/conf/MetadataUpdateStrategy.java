package org.dcm4chee.archive.conf;

/**
 * Created by umberto on 9/22/15.
 */
public enum MetadataUpdateStrategy {

    /**
     * Attributes extracted from new received objects
     * replace current stored attributes in the BLOB
     * field (=default for attributes on instance level)
     */
    OVERWRITE,

    /**
     * Attributes extracted from new received objects
     * replace current stored attributes in the BLOB field.
     * Current stored attributes not contained by the new
     * received attributes will remain in the BLOB field.
     */
    OVERWRITE_MERGE,

    /**
     * Stored attributes in the BLOB field extracted from
     * the first received object of the entity will not
     * be altered on receive of other objects of that entity
     * with different attributes
     */
    COERCE,

    /**
     * Stored attributes with non-empty value in the BLOB
     * field will not be altered on received of further
     * objects of that entity with different attribute values.
     * Attributes extracted from the new received objects not
     * already contained or with an empty value in the BLOB field,
     * will be added in the BLOB field (=default for attributes
     * on patient/study/series level)
     */
    COERCE_MERGE

}
