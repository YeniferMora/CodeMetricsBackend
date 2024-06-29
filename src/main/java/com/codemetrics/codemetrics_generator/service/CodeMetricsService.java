package com.codemetrics.codemetrics_generator.service;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Service;
import com.codemetrics.codemetrics_generator.gen.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

@Service
public class CodeMetricsService {

    public String analyzeCode(String code) {
        // Crear un lexer y parser para el código Python
        PythonLexer lexer = new PythonLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(tokens);
        ParseTree tree = parser.file_input();

        // Crear un listener para analizar el código
        PythonToAnalysis listener = new PythonToAnalysis(code);
        listener.countCommentsAndLines(tokens);

        // Caminar el árbol de análisis
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, tree);

        // Generar el resultado del análisis en JSON
        JSONObject result = new JSONObject();
        result.put("numero_lineas", listener.getNumberLines());
        result.put("numero_lineas_codigo", listener.getNumberCodeLines());
        result.put("numero_comentarios", listener.getNumberComments());
        result.put("numero_variables_globales", listener.getNumberGlobalVariables());
        result.put("numero_declaraciones_if", listener.getNumberIf());
        result.put("numero_bucles_for", listener.getNumberFor());
        result.put("numero_bucles_while", listener.getNumberWhile());
        result.put("numero_clases", listener.getNumberClasses());

        // Añadir resultados de clases
        JSONArray clasesArray = new JSONArray();
        for (Map.Entry<String, List<Integer>> entry : listener.getClassMetrics().entrySet()) {
            String className = entry.getKey();
            List<Integer> classData = entry.getValue();
            JSONObject claseObject = new JSONObject();
            claseObject.put("nombre", className);
            claseObject.put("linea_inicio", classData.get(0));
            claseObject.put("linea_fin", classData.get(1));
            claseObject.put("tamano", classData.get(2));
            claseObject.put("tamano_sin_comentarios", classData.get(3));
            claseObject.put("tamano_sin_comentarios_ni_lineas_vacias", classData.get(4));
            if (listener.getClassFunctions().containsKey(className)) {
                claseObject.put("funciones", listener.getClassFunctions().get(className));
            }
            clasesArray.put(claseObject);
        }
        result.put("clases", clasesArray);

        // Añadir resultados de funciones
        result.put("numero_funciones", listener.getNumberFunctions());
        JSONArray funcionesArray = new JSONArray();
        for (Map.Entry<String, List<Integer>> entry : listener.getFunctionMetrics().entrySet()) {
            String functionName = entry.getKey();
            List<Integer> functionData = entry.getValue();
            JSONObject funcionObject = new JSONObject();
            funcionObject.put("nombre", functionName);
            funcionObject.put("linea_inicio", functionData.get(0));
            funcionObject.put("linea_fin", functionData.get(1));
            funcionObject.put("tamano", functionData.get(2));
            funcionObject.put("tamano_sin_comentarios", functionData.get(3));
            funcionObject.put("tamano_sin_comentarios_ni_lineas_vacias", functionData.get(4));
            funcionesArray.put(funcionObject);
        }
        result.put("funciones", funcionesArray);

        // Añadir complejidad de funciones
        JSONArray complejidadArray = new JSONArray();
        for (Map.Entry<String, PythonToAnalysis.ComplexityInfo> entry : listener.getFunctionComplexity().entrySet()) {
            JSONObject complejidadObject = new JSONObject();
            complejidadObject.put("nombre", entry.getKey());
            complejidadObject.put("complejidad_big_o", entry.getValue().getBigOComplexity());
            complejidadObject.put("complejidad_ciclomatica", entry.getValue().getCyclomaticComplexity());
            complejidadArray.put(complejidadObject);
        }
        result.put("complejidades_funciones", complejidadArray);

        // Añadir dependencias
        JSONArray dependenciasArray = new JSONArray();
        for (PythonToAnalysis.Dependencia dependency : listener.getDependences()) {
            JSONObject dependenciaObject = new JSONObject();
            dependenciaObject.put("nombre", dependency.getName());
            dependenciaObject.put("veces_usada", dependency.getTimesUsed());
            dependenciaObject.put("lineas_usadas", dependency.getLinesUsed());
            dependenciaObject.put("usada_en_funciones", dependency.getFuncUsed());
            dependenciasArray.put(dependenciaObject);
        }
        result.put("dependencias", dependenciasArray);

        return result.toString();
    }
}
