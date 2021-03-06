/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import play.db.jpa.Model;
import util.JDKUtil;

@SuppressWarnings("serial")
@Entity
public class Dependency extends Model {

    @ManyToOne
    public ModuleVersion moduleVersion;
    
    public String name;
    public String version;
    public boolean export;
    public boolean optional;
    public boolean resolvedFromMaven;
    public boolean resolvedFromHerd;
    public boolean nativeJvm;
    public boolean nativeJs;

    public Dependency(ModuleVersion moduleVersion, String name, String version, boolean optional, boolean export, boolean resolvedFromMaven, boolean resolvedFromHerd, 
            boolean nativeJvm, boolean nativeJs) {
        this.moduleVersion = moduleVersion;
        this.name = name;
        this.version = version;
        this.export = export;
        this.optional = optional;
        this.resolvedFromMaven = resolvedFromMaven;
        this.resolvedFromHerd = resolvedFromHerd;
        this.nativeJvm = nativeJvm;
        this.nativeJs = nativeJs;
    }
    
    @Transient
    public boolean isJdk(){
        return JDKUtil.isJdkModule(name);
    }

    @Transient
    public boolean isExists(){
        return ModuleVersion.count("module.name = ? AND version = ?", name, version) > 0;
    }

    @Transient
    public boolean isOtherVersions(){
        return Module.count("name = ?", name) > 0;
    }
    
    @Transient
    public String getMavenUrl(){
        // http://search.maven.org/#artifactdetails%7Cio.vertx%7Cvertx-core%7C2.0.0-beta5%7Cjar
        int idSep = name.lastIndexOf(':');
        if(idSep == -1)
            idSep = name.lastIndexOf('.');
        if(idSep != -1){
            String groupId = name.substring(0, idSep);
            String artifactId = name.substring(idSep+1);
            return "http://search.maven.org/#artifactdetails%7C"+groupId+"%7C"+artifactId+"%7C"+version+"%7Cjar";
        }
        // try to help
        return "http://search.maven.org/#artifactdetails%7C"+name+"%7C"+version+"%7Cjar";
    }

    @Transient
    public String getHerdUrl(){
        // http://modules.ceylon-lang.org/modules/ceylon.language/1.1.0
        return "http://modules.ceylon-lang.org/modules/"+name+"/"+version;
    }

    @Transient
    public String getGroupId(){
        if(!resolvedFromHerd && !resolvedFromMaven){
            ModuleVersion dep = ModuleVersion.findByVersion(name, version);
            if(dep != null)
                return dep.getVirtualGroupId();
        }
        int lastDot = name.lastIndexOf(':');
        if(lastDot == -1)
            lastDot = name.lastIndexOf('.');
        if(lastDot == -1)
            return "";
        return name.substring(0, lastDot);
    }

    @Transient
    public String getArtifactId(){
        if(!resolvedFromHerd && !resolvedFromMaven){
            ModuleVersion dep = ModuleVersion.findByVersion(name, version);
            if(dep != null)
                return dep.getVirtualArtifactId();
        }
        int lastDot = name.lastIndexOf(':');
        if(lastDot == -1)
            lastDot = name.lastIndexOf('.');
        if(lastDot == -1)
            return name;
        return name.substring(lastDot+1);
    }
}
