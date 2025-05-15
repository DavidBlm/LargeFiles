/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.templates;

import de.monticore.lang.monticar.generator.middleware.templates.compile.BashCompilationGenerator;
import de.monticore.lang.monticar.generator.middleware.templates.compile.MingwCompilationGenerator;
import de.monticore.lang.monticar.generator.middleware.templates.compile.MsbuildCompilationGenerator;
import de.monticore.lang.monticar.generator.middleware.templates.compile.WindowsCompilationGenerator;
import de.se_rwth.commons.logging.Log;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

public class MiddlewareTemplates {

    private static final Template COMPILE_BATCH;
    private static final Template COMPILE_BASH;
    private static final Template COMPILE_SUBST;

    static {
        Configuration conf = new Configuration(Configuration.VERSION_2_3_23);
        conf.setDefaultEncoding("UTF-8");
        conf.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
        conf.setLogTemplateExceptions(false);
        conf.setClassForTemplateLoading(MiddlewareTemplates.class, "");
        try {
            COMPILE_BATCH = conf.getTemplate("compile.bat.ftl");
            COMPILE_BASH = conf.getTemplate("compile.sh.ftl");
            COMPILE_SUBST = conf.getTemplate("subst.bat.ftl");
        } catch (IOException e) {
            String msg = "could not load templates";
            Log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public static String generateCompileMsbuild(MsbuildCompilationGenerator model) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("model", model);
        return generate(COMPILE_BATCH, data);
    }


    public static String generateCompileMingw(MingwCompilationGenerator model) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("model", model);
        return generate(COMPILE_BATCH, data);
    }

    public static String generateCompileBash(BashCompilationGenerator model) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("model", model);
        return generate(COMPILE_BASH, data);
    }

    public static String generateCompileSubst(WindowsCompilationGenerator model) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("model", model);
        return generate(COMPILE_SUBST, data);
    }


    private static String generate(Template template, Object dataForTemplate) {
        Log.errorIfNull(template);
        Log.errorIfNull(dataForTemplate);
        StringWriter sw = new StringWriter();
        try {
            template.process(dataForTemplate, sw);
        } catch (TemplateException | IOException e) {
            Log.error("template generation failed, template: " + template.getName(), e);
        }
        return sw.toString();
    }


}
