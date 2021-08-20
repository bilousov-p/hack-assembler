package me.bilousov.parser;

import me.bilousov.symboltable.SymbolTable;

import java.io.*;
import java.util.*;

public class AsmParser {

    private static final String COMMENT_BEGINNING = "//";
    private static final String LABEL_PSEUDO_INSTRUCTOR_MARK = "L";
    private static final String L_COMMAND_WRAPPER_LEFT = "(";
    private static final String L_COMMAND_WRAPPER_RIGHT = ")";
    private static final String A_COMMAND_IDENTIFIER = "@";
    private static final String L_COMMAND_DIVIDER = "#";
    private final Map<String, String> asmLines = new LinkedHashMap<>();
    private final SymbolTable symbolTable = new SymbolTable();

    public List<String> parseAsmFile(String fileName) {
        File asmFile = new File(fileName);

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(asmFile));
            String line = bufferedReader.readLine();
            int lineNumber = 0;

            while (line != null) {
                if (lineIsInstruction(line)) {
                    lineNumber = insertNextLine(line, lineNumber);
                }

                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        scanForLabels(asmLines);
        scanForVariables(asmLines);
        symbolTable.logValues();


        return new ArrayList<>(asmLines.values());
    }

    public SymbolTable getFilledSymbolTable(){
        return symbolTable;
    }

    private int insertNextLine(String line, Integer currentLineNumber) {
        if (!line.startsWith(L_COMMAND_WRAPPER_LEFT)) {
            asmLines.put(currentLineNumber.toString(), line.trim());
            currentLineNumber++;
        } else {
            StringBuilder lCommandId = new StringBuilder(LABEL_PSEUDO_INSTRUCTOR_MARK);

            while (asmLines.containsKey(lCommandId + L_COMMAND_DIVIDER + currentLineNumber.toString())){
                lCommandId.append(LABEL_PSEUDO_INSTRUCTOR_MARK);
            }

            asmLines.put(lCommandId + L_COMMAND_DIVIDER + currentLineNumber, line.trim());
        }

        return currentLineNumber;
    }

    private void scanForLabels(Map<String, String> asmLines) {
        Map<String, String> itemsToDelete = new HashMap<>();
        for (Map.Entry<String, String> entry : asmLines.entrySet()) {
            String asmInstruction = entry.getValue();
            String asmInstrLineNumber = entry.getKey();

            if (lineIsLInstruction(asmInstruction)) {
                symbolTable.addSymbol(getLabelName(asmInstruction), getLabelPointer(asmInstrLineNumber));
                itemsToDelete.put(asmInstrLineNumber, asmInstruction);
            }
        }

        asmLines.keySet().removeAll(itemsToDelete.keySet());
    }

    private void scanForVariables(Map<String, String> asmLines) {
        int currentFreeRegisterNumber = 16;
        for (Map.Entry<String, String> entry : asmLines.entrySet()) {
            String formattedValue = entry.getValue().trim();
            if (formattedValue.startsWith(A_COMMAND_IDENTIFIER)) {
                boolean isVariable;

                try {
                    Integer.parseInt(formattedValue.substring(1));
                    isVariable = false;
                } catch (NumberFormatException ex) {
                    isVariable = true;
                }

                if(isVariable && !symbolTable.containsSymbol(formattedValue.substring(1))){
                    symbolTable.addSymbol(formattedValue.substring(1), currentFreeRegisterNumber);
                    currentFreeRegisterNumber++;
                }
            }
        }
    }

    private boolean lineIsLInstruction(String line){
        return line.contains(L_COMMAND_WRAPPER_LEFT) && line.contains(L_COMMAND_WRAPPER_RIGHT);
    }

    private String getLabelName(String line){
        return line.substring(line.indexOf(L_COMMAND_WRAPPER_LEFT) + 1, line.indexOf(L_COMMAND_WRAPPER_RIGHT));
    }

    private int getLabelPointer(String lInstructionLineNumber){
        return Integer.parseInt(lInstructionLineNumber.split(L_COMMAND_DIVIDER)[1]);
    }

    private boolean lineIsInstruction(String rawLine) {
        String trimmedLine = rawLine.trim();

        return !trimmedLine.equals("") && !trimmedLine.startsWith(COMMENT_BEGINNING);
    }
}
