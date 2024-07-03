package com.codemetrics.codemetrics_generator.service;

import com.codemetrics.codemetrics_generator.utils.SanitizeTXT;
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
        // Create a lexer and parser for the Python code
        PythonLexer lexer = new PythonLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(tokens);
        ParseTree tree = parser.file_input();

        // Create a listener to analyze the code
        PythonToAnalysis listener = new PythonToAnalysis(code);
        listener.countCommentsAndLines(tokens);

        // Walk the parse tree
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, tree);

        // Generate the analysis result in JSON
        JSONObject result = new JSONObject();
        result.put("number_of_lines", listener.getNumberLines());
        result.put("number_of_code_lines", listener.getNumberCodeLines());
        result.put("number_of_comments", listener.getNumberComments());
        result.put("number_of_comments_per_line", listener.getNumberCommentsPerCodeline());
        result.put("number_of_global_variables", listener.getNumberGlobalVariables());
        result.put("number_of_if_statements", listener.getNumberIf());
        result.put("number_of_for_loops", listener.getNumberFor());
        result.put("number_of_while_loops", listener.getNumberWhile());
        result.put("number_of_classes", listener.getNumberClasses());
        JSONArray classesArray = new JSONArray();
        for (Map.Entry<String, List<Integer>> entry : listener.getClassMetrics().entrySet()) {
            String className = entry.getKey();
            List<Integer> classData = entry.getValue();
            JSONObject classObject = new JSONObject();
            classObject.put("name", className);
            classObject.put("start_line", classData.get(0));
            classObject.put("end_line", classData.get(1));
            classObject.put("size", classData.get(2));
            classObject.put("size_without_comments", classData.get(3));
            classObject.put("size_without_comments_or_empty_lines", classData.get(4));
            if (listener.getClassFunctions().containsKey(className)) {
                classObject.put("functions", listener.getClassFunctions().get(className));
            }
            classesArray.put(classObject);
        }
        result.put("classes", classesArray);

        // Add function results
        result.put("number_of_functions", listener.getNumberFunctions());
        JSONArray functionsArray = new JSONArray();
        for (Map.Entry<String, List<Integer>> entry : listener.getFunctionMetrics().entrySet()) {
            String functionName = entry.getKey();
            List<Integer> functionData = entry.getValue();
            JSONObject functionObject = new JSONObject();
            functionObject.put("name", functionName);
            functionObject.put("start_line", functionData.get(0));
            functionObject.put("end_line", functionData.get(1));
            functionObject.put("size", functionData.get(2));
            functionObject.put("size_without_comments", functionData.get(3));
            functionObject.put("size_without_comments_or_empty_lines", functionData.get(4));
            functionsArray.put(functionObject);
        }
        result.put("functions", functionsArray);

        // Add function complexity
        JSONArray complexityArray = new JSONArray();
        for (Map.Entry<String, PythonToAnalysis.ComplexityInfo> entry : listener.getFunctionComplexity().entrySet()) {
            JSONObject complexityObject = new JSONObject();
            complexityObject.put("name", entry.getKey());
            complexityObject.put("big_o_complexity", entry.getValue().getBigOComplexity());
            complexityObject.put("cyclomatic_complexity", entry.getValue().getCyclomaticComplexity());
            complexityArray.put(complexityObject);
        }
        result.put("function_complexities", complexityArray);

        // Add dependencies
        JSONArray dependenciesArray = new JSONArray();
        for (PythonToAnalysis.Dependencia dependency : listener.getDependences()) {
            JSONObject dependencyObject = new JSONObject();
            dependencyObject.put("name", dependency.getName());
            dependencyObject.put("times_used", dependency.getTimesUsed());
            dependencyObject.put("lines_used", dependency.getLinesUsed());
            dependencyObject.put("used_in_functions", dependency.getFuncUsed());
            dependenciesArray.put(dependencyObject);
        }
        result.put("dependencies", dependenciesArray);

        return result.toString();
    }

    public String smellCodeAnalysis(String metricsResult, String apiKey) {
        String apiResponse = "";
        JSONObject result = new JSONObject();
        try {
            String prompt = "A JSON Object with statistics of a Python source code is provided below. Analyze this data and generate a report on code smells, Detect if There are Code Smells like: Classes and functions that are too large, High cyclomatic complexity, Excessive use of global variables, Excessive conditional statements and loops, Unused dependencies.\\n For the identified code smells, generate recommendations such as:Refactoring large classes and functions. Reducing cyclomatic complexity. Minimizing global variables. Simplifying conditional statements and loops. Managing unused dependencies.\n " + SanitizeTXT.escapeSpecialCharacters(metricsResult) ;
            System.out.println("analysis: "+prompt);
            apiResponse = GeminiAPI.callGeminiAPI(prompt, apiKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        result.put("smell_code_analysis", apiResponse);
        return result.toString();
    }

    public String codeDescription(String code, String apiKey) {
        String apiResponse = "";
        JSONObject result = new JSONObject();
        try {
            String prompt = "Make a really brief description of the following python code: " + SanitizeTXT.escapeSpecialCharacters(code); // Or extract this from the context
            apiResponse = GeminiAPI.callGeminiAPI(prompt, apiKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        result.put("descriptionByGemini", apiResponse);
        return result.toString();
    }
}
