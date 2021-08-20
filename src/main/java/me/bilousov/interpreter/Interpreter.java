package me.bilousov.interpreter;

import me.bilousov.Main;
import me.bilousov.symboltable.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Interpreter {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String ZERO_BIT = "0";
    private static final String ONE_BIT = "1";
    private static final String ASSIGNMENT_SIGN = "=";
    private static final String JUMP_SEPARATOR = ";";
    private static final String A_SIGN = "A";
    private static final String D_SIGN = "D";
    private static final String M_SIGN = "M";
    private static final String EMPTY_INSTRUCTION = "";

    private static final String C_COMMAND_BITS = "111";
    private static final String A_COMMAND_IDENTIFIER = "@";

    private static final Integer BINARY_BASE = 2;

    private static final Integer COMMAND_LENGTH_PATTERN = 16;

    private static final String JGT_INSTR = "JGT";
    private static final String JEQ_INSTR = "JEQ";
    private static final String JGE_INSTR = "JGE";
    private static final String JLT_INSTR = "JLT";
    private static final String JNE_INSTR = "JNE";
    private static final String JLE_INSTR = "JLE";
    private static final String JMP_INSTR = "JMP";

    private static final String ZERO_INSTR = "0";
    private static final String ONE_INSTR = "1";
    private static final String MINUS_ONE_INSTR = "-1";
    private static final String D_INSTR = "D";
    private static final String A_INSTR = "A";
    private static final String M_INSTR = "M";
    private static final String NOT_A_INSTR = "!A";
    private static final String NOT_M_INSTR = "!M";
    private static final String MINUS_D_INSTR = "-D";
    private static final String MINUS_M_INSTR = "-M";
    private static final String MINUS_A_INSTR = "-A";
    private static final String D_PLUS_ONE_INSTR = "D+1";
    private static final String A_PLUS_ONE_INSTR = "A+1";
    private static final String M_PLUS_ONE_INSTR = "M+1";
    private static final String D_MINUS_ONE_INSTR = "D-1";
    private static final String A_MINUS_ONE_INSTR = "A-1";
    private static final String M_MINUS_ONE_INSTR = "M-1";
    private static final String D_PLUS_A_INSTR = "D+A";
    private static final String D_PLUS_M_INSTR = "D+M";
    private static final String D_MINUS_A_INSTR = "D-A";
    private static final String D_MINUS_M_INSTR = "D-M";
    private static final String A_MINUS_D_INSTR = "A-D";
    private static final String M_MINUS_D_INSTR = "M-D";
    private static final String D_AND_A_INSTR = "D&A";
    private static final String D_AND_M_INSTR = "D&M";
    private static final String D_OR_A_INSTR = "D|A";
    private static final String D_OR_M_INSTR = "D|M";

    private static final String ZERO_INSTR_BITS = "101010";
    private static final String ONE_INSTR_BITS = "111111";
    private static final String MINUS_ONE_INSTR_BITS = "111010";
    private static final String D_INSTR_BITS = "001100";
    private static final String A_M_INSTR_BITS = "110000";
    private static final String NOT_A_NOT_M_INSTR_BITS = "110001";
    private static final String MINUS_D_INSTR_BITS = "001111";
    private static final String MINUS_M_MINUS_A_INSTR_BITS = "110011";
    private static final String D_PLUS_ONE_INSTR_BITS = "011111";
    private static final String A_M_PLUS_ONE_INSTR_BITS = "110111";
    private static final String D_MINUS_ONE_INSTR_BITS = "001110";
    private static final String A_M_MINUS_ONE_INSTR_BITS = "110010";
    private static final String D_PLUS_A_M_INSTR_BITS = "000010";
    private static final String D_MINUS_A_M_INSTR_BITS = "010011";
    private static final String A_M_MINUS_D_INSTR_BITS = "000111";
    private static final String D_AND_A_M_INSTR_BITS = "000000";
    private static final String D_OR_A_M_INSTR_BITS = "010101";
    private static final String ERROR_INSTR_BITS = "||||||";

    private final SymbolTable filledSymbolTable;

    public Interpreter(SymbolTable filledSymbolTable){
        this.filledSymbolTable = filledSymbolTable;
    }

    public List<String> translateToBinary(List<String> asmInstructionLines){
        List<String> binaryInstrLines = new ArrayList<>();

        for(String asmLine : asmInstructionLines){
            LOGGER.log(Level.INFO, "Current line: {0}", asmLine);
            binaryInstrLines.add(getBinaryCommand(asmLine));
        }

        return binaryInstrLines;
    }

    private String getBinaryCommand(String asmInstruction){
        if(asmInstruction.startsWith(A_COMMAND_IDENTIFIER)){
            return getBinaryACommand(asmInstruction);
        }

        return getBinaryCCommand(asmInstruction);
    }

    private String getBinaryACommand(String asmACommand){
        StringBuilder aCommandBits = new StringBuilder();
        aCommandBits.append(ZERO_BIT);

        String binAddress = getBinaryAddress(asmACommand.substring(1));

        // If binary number is less than COMMAND_LENGTH_PATTERN (16) than fit it with additional 0's to mach pattern
        int baseInstrLength = aCommandBits.length() + binAddress.length();
        if(baseInstrLength < COMMAND_LENGTH_PATTERN) {
            fitTo16Bits(baseInstrLength, aCommandBits);
        }

        return aCommandBits.append(binAddress).toString();
    }

    private String getBinaryCCommand(String asmCInstruction){
        return C_COMMAND_BITS
                + getComputeBits(asmCInstruction)
                + getDestinationBits(asmCInstruction)
                + getJumpBits(asmCInstruction);
    }

    private String getComputeBits(String asmInstruction){
        StringBuilder computeBits = new StringBuilder();
        String computeInstr = getComputeInstruction(asmInstruction);
        destSymbolToBit(computeBits, computeInstr, M_SIGN);

        switch (computeInstr) {
            case ZERO_INSTR -> computeBits.append(ZERO_INSTR_BITS);
            case ONE_INSTR -> computeBits.append(ONE_INSTR_BITS);
            case MINUS_ONE_INSTR -> computeBits.append(MINUS_ONE_INSTR_BITS);
            case D_INSTR -> computeBits.append(D_INSTR_BITS);
            case A_INSTR, M_INSTR -> computeBits.append(A_M_INSTR_BITS);
            case NOT_A_INSTR, NOT_M_INSTR -> computeBits.append(NOT_A_NOT_M_INSTR_BITS);
            case MINUS_D_INSTR -> computeBits.append(MINUS_D_INSTR_BITS);
            case MINUS_M_INSTR, MINUS_A_INSTR -> computeBits.append(MINUS_M_MINUS_A_INSTR_BITS);
            case D_PLUS_ONE_INSTR -> computeBits.append(D_PLUS_ONE_INSTR_BITS);
            case A_PLUS_ONE_INSTR, M_PLUS_ONE_INSTR -> computeBits.append(A_M_PLUS_ONE_INSTR_BITS);
            case D_MINUS_ONE_INSTR -> computeBits.append(D_MINUS_ONE_INSTR_BITS);
            case A_MINUS_ONE_INSTR, M_MINUS_ONE_INSTR -> computeBits.append(A_M_MINUS_ONE_INSTR_BITS);
            case D_PLUS_A_INSTR, D_PLUS_M_INSTR -> computeBits.append(D_PLUS_A_M_INSTR_BITS);
            case D_MINUS_A_INSTR, D_MINUS_M_INSTR -> computeBits.append(D_MINUS_A_M_INSTR_BITS);
            case A_MINUS_D_INSTR, M_MINUS_D_INSTR -> computeBits.append(A_M_MINUS_D_INSTR_BITS);
            case D_AND_A_INSTR, D_AND_M_INSTR -> computeBits.append(D_AND_A_M_INSTR_BITS);
            case D_OR_A_INSTR, D_OR_M_INSTR -> computeBits.append(D_OR_A_M_INSTR_BITS);
            default -> computeBits.append(ERROR_INSTR_BITS);
        }

        return computeBits.toString();
    }

    private String getJumpBits(String asmInstruction){
        String jumpInstruction = getJumpInstruction(asmInstruction);

        return switch (jumpInstruction) {
            case JGT_INSTR -> mergeBits(ZERO_BIT, ZERO_BIT, ONE_BIT);
            case JEQ_INSTR -> mergeBits(ZERO_BIT, ONE_BIT, ZERO_BIT);
            case JGE_INSTR -> mergeBits(ZERO_BIT, ONE_BIT, ONE_BIT);
            case JLT_INSTR -> mergeBits(ONE_BIT, ZERO_BIT, ZERO_BIT);
            case JNE_INSTR -> mergeBits(ONE_BIT, ZERO_BIT, ONE_BIT);
            case JLE_INSTR -> mergeBits(ONE_BIT, ONE_BIT, ZERO_BIT);
            case JMP_INSTR -> mergeBits(ONE_BIT, ONE_BIT, ONE_BIT);
            default -> mergeBits(ZERO_BIT, ZERO_BIT, ZERO_BIT);
        };
    }

    private String getDestinationBits(String asmInstruction){
        StringBuilder destBits = new StringBuilder();
        String destInstruction = getDestinationInstruction(asmInstruction);

        destSymbolToBit(destBits, destInstruction, A_SIGN);
        destSymbolToBit(destBits, destInstruction, D_SIGN);
        destSymbolToBit(destBits, destInstruction, M_SIGN);

        return destBits.toString();
    }

    private String getDestinationInstruction(String asmInstruction){
        if(asmInstruction.contains(ASSIGNMENT_SIGN)){
            return asmInstruction.split(ASSIGNMENT_SIGN)[0];
        }

        return EMPTY_INSTRUCTION;
    }

    private String getJumpInstruction(String asmInstruction){
        if(asmInstruction.contains(JUMP_SEPARATOR)){
            return asmInstruction.split(JUMP_SEPARATOR)[1];
        }

        return EMPTY_INSTRUCTION;
    }

    private String getComputeInstruction(String asmInstruction){
        String computeInstr = asmInstruction;

        if(asmInstruction.contains(ASSIGNMENT_SIGN)){
            computeInstr = asmInstruction.split(ASSIGNMENT_SIGN)[1];
        }

        if(asmInstruction.contains(JUMP_SEPARATOR)){
            computeInstr = computeInstr.split(JUMP_SEPARATOR)[0];
        }

        return computeInstr;
    }

    private void destSymbolToBit(StringBuilder destBits, String destInstruction, String checkSymbol){
        if(destInstruction.contains(checkSymbol)){
            destBits.append(ONE_BIT);
        } else {
            destBits.append(ZERO_BIT);
        }
    }

    private void fitTo16Bits(int baseInstrLength, StringBuilder aCommandBits){
        for(;baseInstrLength < COMMAND_LENGTH_PATTERN; baseInstrLength++) {
            aCommandBits.append(ZERO_BIT);
        }
    }

    private String getBinaryAddress(String addressRepresentation){
        String binAddress;

        try {
            // Translate base 10 number to base 2 number
            binAddress = Integer.toString(Integer.parseInt(addressRepresentation), BINARY_BASE);
        } catch (NumberFormatException ex) {
            binAddress = Integer.toString(filledSymbolTable.getSymbolValue(addressRepresentation), BINARY_BASE);
        }

        return binAddress;
    }

    private String mergeBits(String... bits){
        StringBuilder mergedBits = new StringBuilder();
        for (String bit : bits) {
            mergedBits.append(bit);
        }

        return mergedBits.toString();
    }
}
