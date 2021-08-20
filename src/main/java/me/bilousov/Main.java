package me.bilousov;

import me.bilousov.interpreter.Interpreter;
import me.bilousov.parser.AsmParser;
import me.bilousov.writer.BinaryFileWriter;

import java.io.*;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        AsmParser asmParser = new AsmParser();
        List<String> instructionLines = asmParser.parseAsmFile(args[0]);

        // You need to call getFilledSymbolTable only after parseAsmFile method call
        Interpreter interpreter = new Interpreter(asmParser.getFilledSymbolTable());
        List<String> binInstrLines = interpreter.translateToBinary(instructionLines);

        BinaryFileWriter.writeBinaryFileWithLines(binInstrLines, args[0]);
    }
}
