package com.fasterxml.jackson.datatype.eclipselink;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;

/**
 * Created by BORIS on 13.11.2016.
 */
public class PackageVersion implements Versioned {

    public final static Version VERSION = VersionUtil.parseVersion("NONE", "NONE", "NONE");

    @Override
    public Version version() {
        return VERSION;
    }
}
