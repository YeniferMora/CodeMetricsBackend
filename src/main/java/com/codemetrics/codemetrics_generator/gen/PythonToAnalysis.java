package com.codemetrics.codemetrics_generator.gen;
/** Creates an analysis */
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
public class PythonToAnalysis extends PythonParserBaseListener {
    // Calcular el número de clases, funciones, variables, comentarios, declaraciones condicionales, bucles y lineas de código
    private int numberComments = 0;
    private int numberCommentsWithoutCode = 0;
    private Set<Integer> onlyCommentsLines = new HashSet<>();
    private Set<Integer> emptyLines = new HashSet<>();
    private int numberIf = 0;
    private int numberFor = 0;
    private int numberWhile = 0;
    private int numberLines = 0;
    private int numberCodeLines = 0;
    private int numberEmptyLines = 0;
    private int numberClasses = 0;
    private int numberGlobalVariables = 0;
    private int numberFunctions = 0;
    private List<String> lines;
    private String currentClass = null;

    // Diccionario para almacenar datos de clases y funciones
    private Map<String, List<Integer>> functionMetrics = new HashMap<>();
    private Map<String, List<Integer>> classMetrics = new HashMap<>();
    private Map<String, List<String>> classFunctions = new HashMap<>();

    private Map<Integer, Integer> sizeLine = new HashMap<>();
    private final Map<String, ComplexityInfo> functionComplexity = new HashMap<>();
    private boolean isRecursive = false;
    private String currentFunction = null;
    private int loopDepth = 0;
    private int maxLoopDepth = 0;
    private int cyclomaticComplexity = 1;

    public static class ComplexityInfo {
        String bigOComplexity;
        int cyclomaticComplexity;
        ComplexityInfo(String bigOComplexity, int cyclomaticComplexity) {
            this.bigOComplexity = bigOComplexity;
            this.cyclomaticComplexity = cyclomaticComplexity;
        }

        public String getBigOComplexity() {
            return bigOComplexity;
        }

        public int getCyclomaticComplexity() {
            return cyclomaticComplexity;
        }
    }
    public PythonToAnalysis(String file){
        lines = Arrays.asList(file.split("\\r?\\n"));
    }

    public static class Dependencia{
        String name;
        String nameFrom;
        int timesUsed;
        ArrayList<Integer> linesUsed;
        ArrayList<String> funcUsed;
        public Dependencia(String name,String nameFrom){
            this.name = name;
            this.nameFrom = nameFrom;
            this.timesUsed = 0;
            this.linesUsed = new ArrayList<Integer>();
            this.funcUsed = new ArrayList<String>();
        }
        public String getName() { return name; }
        public String getNameFrom() { return nameFrom; }
        public int getTimesUsed() { return timesUsed;}
        public ArrayList<Integer> getLinesUsed(){return linesUsed;}
        public ArrayList<String> getFuncUsed(){return funcUsed;}
        public void increaseTimesUsed() { this.timesUsed +=1;}
        public void addLineUsed(int lineNumber){this.linesUsed.add(lineNumber);}
        public void addFuncUsed(String nameFunc){this.funcUsed.add(nameFunc);}
    }

    ArrayList<Dependencia> dependencias = new ArrayList<Dependencia>();
    ArrayList<String> funcionesLlamadas = new ArrayList<String>();

    @Override
    public void enterFile_input(PythonParser.File_inputContext ctx) {
        numberLines = ctx.getStop().getLine();
    }

    @Override
    public void enterFunction_def_raw(PythonParser.Function_def_rawContext ctx) {
        if (ctx.NAME() != null){
            funcionesLlamadas.add(ctx.NAME().getText());
        }
        numberFunctions++;
        String functionName = ctx.NAME().getText();
        int startLine = ctx.getStart().getLine();
        List<Integer> functionData = new ArrayList<>();
        functionData.add(startLine);
        functionMetrics.put(functionName, functionData);

        if (currentClass != null) {
            List<String> functions = classFunctions.getOrDefault(currentClass, new ArrayList<>());
            functions.add(functionName);
            classFunctions.put(currentClass, functions);
        }
    }

    @Override
    public void exitFunction_def_raw(PythonParser.Function_def_rawContext ctx) {
        String functionName = ctx.NAME().getText();
        List<Integer> functionData = functionMetrics.get(functionName);
        int endLine = ctx.getStop().getLine();
        functionData.add(1,endLine);

        int startLine = functionData.get(0);
        int functionSize = endLine - startLine ;
        functionData.add(2,functionSize);
        functionData.add(3,functionSize - discountOnlyCommentsLines(startLine, endLine));
        functionData.add(4,functionSize - discountOnlyCommentsLines(startLine, endLine)- discountEmptyLines(startLine, endLine));
        funcionesLlamadas.remove(funcionesLlamadas.size()-1);
    }

    @Override
    public void enterIf_stmt(PythonParser.If_stmtContext ctx) {
        numberIf++;
        cyclomaticComplexity++;
    }

    @Override
    public void enterFor_stmt(PythonParser.For_stmtContext ctx) {
        numberFor++;
        cyclomaticComplexity++;
        loopDepth++;
        if (loopDepth > maxLoopDepth) {
            maxLoopDepth = loopDepth;
        }
    }

    @Override
    public void enterWhile_stmt(PythonParser.While_stmtContext ctx) {
        numberWhile++;
        loopDepth++;
        cyclomaticComplexity++;
        if (loopDepth > maxLoopDepth) {
            maxLoopDepth = loopDepth;
        }
    }

    @Override
    public void enterGlobal_stmt(PythonParser.Global_stmtContext ctx) {
        numberGlobalVariables ++;
    }

    @Override
    public void enterClass_def_raw(PythonParser.Class_def_rawContext ctx) {
        numberClasses++;
        String className = ctx.NAME().getText();
        int startLine = ctx.getStart().getLine();
        List<Integer> classData = new ArrayList<>();
        classData.add(0,startLine);
        classMetrics.put(className, classData);
        currentClass = className;
    }

    @Override
    public void exitClass_def_raw(PythonParser.Class_def_rawContext ctx) {
        String className = ctx.NAME().getText();
        List<Integer> classData = classMetrics.get(className);
        int endLine = ctx.getStop().getLine();
        classData.add(1,endLine);

        int startLine = classData.get(0);
        int classSize = endLine - startLine;
        classData.add(2,classSize);
        classData.add(3,classSize - discountOnlyCommentsLines(startLine, endLine));
        classData.add(4,classSize - discountOnlyCommentsLines(startLine, endLine) - discountEmptyLines(startLine, endLine) );

        currentClass = null;
    }

    @Override
    public void enterFunction_def(PythonParser.Function_defContext ctx) {
        currentFunction = ctx.function_def_raw().NAME().getText();
        isRecursive = false;
        loopDepth = 0;
        maxLoopDepth = 0;
        cyclomaticComplexity = 1;
    }

    @Override
    public void exitFunction_def(PythonParser.Function_defContext ctx) {
        String complexity = isRecursive ? "No calculable (Recursiva)" : calculateBigOComplexity();
        functionComplexity.put(currentFunction, new ComplexityInfo(complexity, cyclomaticComplexity));
        currentFunction = null;
    }

    @Override
    public void enterExpression(PythonParser.ExpressionContext ctx) {
        if (currentFunction != null && ctx.getText().contains(currentFunction)) {
            isRecursive = true;
        }
    }



    @Override
    public void exitFor_stmt(PythonParser.For_stmtContext ctx) {
        loopDepth--;
    }



    @Override
    public void exitWhile_stmt(PythonParser.While_stmtContext ctx) {
        loopDepth--;
    }


    @Override
    public void enterElif_stmt(PythonParser.Elif_stmtContext ctx) {
        cyclomaticComplexity++;
    }

    @Override
    public void enterElse_block(PythonParser.Else_blockContext ctx) {
        cyclomaticComplexity++;
    }

    @Override
    public void enterTry_stmt(PythonParser.Try_stmtContext ctx) {
        cyclomaticComplexity++;
    }

    @Override
    public void enterExcept_block(PythonParser.Except_blockContext ctx) {
        cyclomaticComplexity++;
    }

    private String calculateBigOComplexity() {
        if (maxLoopDepth == 0) {
            return "O(1)";
        } else if (maxLoopDepth == 1) {
            return "O(n)";
        } else {
            return "O(n^"+maxLoopDepth+")";
        }
    }

    private int discountEmptyLines(int startLine, int endLine){
        int auxEmptyLines = 0;
        for (int i = startLine; i <= endLine; i++) {
            if (emptyLines.contains(i)){
                auxEmptyLines++;
            }
        }
        return auxEmptyLines;
    }

    private int discountOnlyCommentsLines(int startLine, int endLine){
        int auxOnlyCommentsLines = 0;
        for (int i = startLine; i <= endLine; i++) {
            if (onlyCommentsLines.contains(i)){
                auxOnlyCommentsLines++;
            }
        }
        return auxOnlyCommentsLines;
    }

    private static String sanitizeLine(String input) {
        int index = input.indexOf('#');
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (i < index && (currentChar == ' ' || currentChar == '\t')) {
                continue;
            }
            result.append(currentChar);
        }
        return result.toString();
    }

    public void countCommentsAndLines(CommonTokenStream tokens) {
        for (int i = 0; i < lines.size(); i++) {
            int auxSizeLine = sanitizeLine(lines.get(i)).length();
            sizeLine.put(i+1,auxSizeLine);
            if (auxSizeLine == 0){
                numberEmptyLines++;
                emptyLines.add(i+1);
            }
        }

        for (Token token : tokens.getTokens()) {
            if (token.getChannel() == Token.HIDDEN_CHANNEL && token.getType() == PythonLexer.COMMENT) {
                numberComments++;
                int line = token.getLine();
                if (sizeLine.containsKey(line) && sizeLine.get(line) == token.getText().length()){
                    numberCommentsWithoutCode ++;
                    onlyCommentsLines.add(line);
                }
            }
        }
    }

    private void calculateCodeLines(){
        numberCodeLines = numberLines - numberCommentsWithoutCode - numberEmptyLines;
    }



    @Override
    public void exitFile_input(PythonParser.File_inputContext ctx) {
        calculateCodeLines();

        System.out.println("Number of lines in the file: " + numberLines);
        System.out.println("Number of code lines: " + numberCodeLines);
        System.out.println("Number of comments: " + numberComments);
        System.out.println("Number of global variables: " + numberGlobalVariables);
        System.out.println("Number of if statements: " + numberIf);
        System.out.println("Number of for statements: " + numberFor);
        System.out.println("Number of while statements: " + numberWhile);
        System.out.println("DEPENDENCY:");
        for (int i = 0; i < dependencias.size(); i++) {
            System.out.println(dependencias.get(i).getName()+", Times used:"+dependencias.get(i).getTimesUsed()+", Lines:"+dependencias.get(i).getLinesUsed()+", Used in functions:"+dependencias.get(i).getFuncUsed());
            //for (int j = 0; j < dependencias.get(j).getLinesUsed().size(); j++){
            //System.out.println();
            //}
        }
        // Imprimir resultados de clases
        System.out.println("Number of classes: " + numberClasses);
        for (Map.Entry<String, List<Integer>> entry : classMetrics.entrySet()) {
            String className = entry.getKey();
            List<Integer> classData = entry.getValue();
            System.out.println("Class: " + className + " (" + classData.get(0) + " , " + classData.get(1) +")");
            System.out.println("    Size: " + classData.get(2) + "\n\tSize (without comments): " + classData.get(3) + "\n\tSize (without comments and empty lines):  " + classData.get(4));
            if (classFunctions.containsKey(className)) {
                System.out.println("    Functions: " + classFunctions.get(className));
            }
        }

        System.out.println("Number of functions: " + numberFunctions);
        for (Map.Entry<String, List<Integer>> entry : functionMetrics.entrySet()) {
            String functionName = entry.getKey();
            List<Integer> functionData = entry.getValue();
            System.out.println("Function: " + functionName + " (" + functionData.get(0) + " , " + functionData.get(1) +")");
            System.out.println("    Size: " + functionData.get(2) + "\n\tSize (without comments): " + functionData.get(3) + "\n\tSize (without comments and empty lines):  " + functionData.get(4));
        }
        for (Map.Entry<String, ComplexityInfo> entry : functionComplexity.entrySet()) {
            System.out.println("Función: "+entry.getKey()+" - Complejidad Big(O): "+entry.getValue().bigOComplexity+" - Complejidad Ciclomática: "+entry.getValue().cyclomaticComplexity);
        }
    }

    @Override public void enterImport_name(PythonParser.Import_nameContext ctx) {
        dependencias.add(new Dependencia(ctx.dotted_as_names().getText(),ctx.dotted_as_names().getText()));
        //{System.out.println(ctx.dotted_as_names().getText());
    }
    @Override public void enterImport_from(PythonParser.Import_fromContext ctx) {
        String[] depens = ctx.import_from_targets().getText().split(",");
        for (int i=0; i < depens.length;i++){
            dependencias.add(new Dependencia(depens[i],ctx.dotted_name().getText()));
        }

        //System.out.println(ctx.import_as_names().getText());
    }

    @Override public void enterAtom(PythonParser.AtomContext ctx) {
        for (int i=0; i < dependencias.size();i++){
            if (ctx.getText().equals(dependencias.get(i).getName())){
                dependencias.get(i).increaseTimesUsed();
                dependencias.get(i).addLineUsed(ctx.getStop().getLine());
                if (!funcionesLlamadas.isEmpty()){
                    dependencias.get(i).addFuncUsed(funcionesLlamadas.get(funcionesLlamadas.size()-1));
                }
                break;
            }
        }

    }

    public int getNumberLines() {
        return numberLines;
    }

    public int getNumberCodeLines() {
        return numberCodeLines;
    }

    public int getNumberComments() {
        return numberComments;
    }

    public int getNumberGlobalVariables() {
        return numberGlobalVariables;
    }

    public int getNumberIf() {
        return numberIf;
    }

    public int getNumberFor() {
        return numberFor;
    }

    public int getNumberWhile() {
        return numberWhile;
    }

    public int getNumberClasses() {
        return numberClasses;
    }

    public int getNumberFunctions() {
        return numberFunctions;
    }

    public ArrayList<Dependencia> getDependences() {
        return dependencias;
    }

    public Map<String, List<Integer>> getFunctionMetrics() {
        return functionMetrics;
    }

    public Map<String, List<Integer>> getClassMetrics() {
        return classMetrics;
    }

    public Map<String, List<String>> getClassFunctions() {
        return classFunctions;
    }

    public Map<String, ComplexityInfo> getFunctionComplexity() {
        return functionComplexity;
    }
}
