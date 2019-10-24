package com.runsim.backend;

import com.runsim.backend.web.Session;
import com.runsim.backend.web.SessionManager;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.javalin.Javalin;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class App {

    private static HashMap<String, Class<? extends BaseFlow>> flowTypes;

    public static void main(String[] args) throws Exception {
        //compilerOptionsControl();
        findFlowTypes();
        startServer();
    }

    private static void compilerOptionsControl() {
        for (var method : Session.class.getMethods()) {
            for (var parameter : method.getParameters()) {
                if (!parameter.isNamePresent())
                    throw new IllegalArgumentException("Parameter names are not present!");
            }
        }
    }

    private static void findFlowTypes() throws Exception {
        flowTypes = new HashMap<>();
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().ignoreClassVisibility().whitelistPackages(Constants.FLOWS_PREFIX).scan()) {
            var classInfoList = scanResult.getAllClasses();
            for (var classInfo : classInfoList) {
                Class clazz = Class.forName(classInfo.getName());
                if (!BaseFlow.class.isAssignableFrom(clazz)) continue;
                if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isInterface()) continue;
                flowTypes.put(clazz.getSimpleName(), clazz);
            }
        }
    }

    private static void startServer() {
        var app = Javalin.create().start(Constants.BACKEND_PORT);

        app.ws("/", ws -> {
            ws.onConnect(ctx -> {
                var uuid = UUID.randomUUID();
                ctx.attribute("connection-id", uuid);
                SessionManager.onConnect(uuid, ctx::send);
            });
            ws.onError(ctx -> {
                var uuid = (UUID) ctx.attribute("connection-id");
                SessionManager.onError(uuid, ctx.error());
            });
            ws.onMessage(ctx -> {
                var uuid = (UUID) ctx.attribute("connection-id");
                SessionManager.onMessage(uuid, ctx.message());
            });
            ws.onClose(ctx -> {
                var uuid = (UUID) ctx.attribute("connection-id");
                SessionManager.onClose(uuid);
            });
        });
    }

    public static Set<String> getFlowNames() {
        return flowTypes.keySet();
    }

    public static Class<? extends BaseFlow> getFlowType(String flowName) {
        return flowTypes.get(flowName);
    }
}
