package controllers;

import java.net.HttpURLConnection;
import java.util.List;

import play.Logger;
import play.mvc.Before;

import models.Module;
import models.Module.Type;
import models.ModuleVersion;

public class RepoAPI extends MyController {
    
    public static final int RESULT_LIMIT = 20;

    @Before
    public static void fixFormat(){
        Logger.info(request.format);
        // default to json
        if(request.format == null
                || (!request.format.equals("json")
                        && !request.format.equals("xml")))
            request.format = "json";
    }
    
    public static void completeVersions(String module, String version, String type){
        Module mod = Module.findByName(module);
        if(mod == null)
            notFound("Module not found");
        Type t = getType(type);
        
        List<ModuleVersion> versions = ModuleVersion.completeVersionForModuleAndBackend(mod, version, t);
        
        render(versions);
    }

    private static Type getType(String type) {
        if(type == null || type.isEmpty())
            return Type.JVM;
        if(type.equalsIgnoreCase("jvm"))
            return Type.JVM;
        if(type.equalsIgnoreCase("javascript"))
            return Type.JS;
        if(type.equalsIgnoreCase("source"))
            return Type.SRC;
        error(HttpURLConnection.HTTP_BAD_REQUEST, "Unknown type, must be one of: jvm,javascript,source");
        // never reached
        return null;
    }

    public static void completeModules(String module, String type){
        Type t = getType(type);

        List<Module> modules = Module.completeForBackend(module, t);
        
        render(modules);
    }
}