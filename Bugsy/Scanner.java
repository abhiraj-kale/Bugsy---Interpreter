package Bugsy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Bugsy.TokenType.*;

public class Scanner {
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    Scanner(String source){
        this.source = source;
    }
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd(){
        return current >= source.length();
    }

    private void scanToken() {
        char c = next();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) next();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ': break;
            case '\r': break;
            case '\t': break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            case 'o':
                if (peek() == 'r') {
                    addToken(OR);
                }
                break;
            default:
                if (isDigit(c)){
                    number();
                    break;
                }
                else if (isAlpha(c)){
                    identifier();
                    break;
                }
                else {
                    Bugsy.error(line, "Unexpected character.");
                }
                Bugsy.error(line, "Unexpected error - [Unidentified token]"); break;
        }
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private void number(){
        while(isDigit(peek())) next();
        // Look for fractional part
        if(peek()=='.' && isDigit(peekNext())){
            next();
            while(isDigit(peek())) next();
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }
    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }
    private void identifier(){
        while(isAlphaNumeric(peek())) next();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        type = type==null?IDENTIFIER:type;
        addToken(type);
    }
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    private char peek(){
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    private char next(){
        return source.charAt(++current-1);
    }
    private void string(){
        while(peek()!='"' && !isAtEnd()){
            if (peek()=='\n') line++;
            next();
        }
        if(isAtEnd()) Bugsy.error(line, "Unterminated String.");
        // Close the string
        next();
        // Slice the String
        String text = source.substring(start+1, current-1);
        // Add to the list
        addToken(STRING, text);
    }
    private boolean match(char expected){
        if (isAtEnd()) return false;
        if (source.charAt(current-1)==expected) return false;
        current++;
        return true;
    }
    private void addToken(TokenType type){
        addToken(type, null);
    }
    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}