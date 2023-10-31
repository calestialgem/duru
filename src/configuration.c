// Implements configuration parsing.

#include "duru.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Type of a lexeme in the configuration file.
typedef enum TokenType {
    // End of the file.
    endOfFile,

    // Punctuation `{`.
    openingBrace,

    // Punctuation `}`.
    closingBrace,

    // Punctuation `;`.
    semicolon,

    // Punctuation `.`.
    dot,

    // Keyword `project`.
    projectKeyword,

    // Keyword `executable`.
    executableKeyword,

    // Word representing a user-defined construct.
    identifier,
} TokenType;

// Smallest meaningful sequence of characters in the configuration file.
typedef struct Token Token;

// Context of the lexing process.
typedef struct Lexer Lexer;

// Create a lexer over the given contents.
static Lexer* createLexer(char const* contents);

// Make the lexer go over to the next token.
static void advanceLexer(Lexer* lexer);

// Make the lexer go over all the whitespace and comments.
static void skipLexer(Lexer* lexer);

// Returns whether the given byte is an ASCII letter.
static bool isLetter(char byte);

// Returns whether the given byte is an ASCII digit.
static bool isDigit(char byte);

// Returns whether the given byte is an ASCII letter or digit, which can be a
// part of a word lexeme.
static bool isWord(char byte);

// Makes the lexer go over the current character.
#define consumeCurrent()                                                       \
    do {                                                                       \
        lexer->index++;                                                        \
        character = lexer->contents[lexer->index];                             \
    } while (0)

struct Token {
    char*     text;
    TokenType type;
};

struct Lexer {
    char const* contents;
    int         index;
    Token       next;
};

struct DuruConfiguration {
    char const* name;
    int         executables;
    char        bytes[];
};

DuruConfiguration* duruParseConfiguration(char const* contents) {
    Lexer* lexer = createLexer(contents);
    while (lexer->next.type != endOfFile) {
        switch (lexer->next.type) {
            case openingBrace: puts("Punctuation `{`"); break;
            case closingBrace: puts("Punctuation `}`"); break;
            case semicolon: puts("Punctuation `;`"); break;
            case dot: puts("Punctuation `.`"); break;
            case projectKeyword: puts("Keyword `project`"); break;
            case executableKeyword: puts("Keyword `executable`"); break;
            case identifier:
                printf("Identifier `%s`\n", lexer->next.text);
                break;
            default: duruCrash("Unknown token type %i!\n", lexer->next.type);
        }
        advanceLexer(lexer);
    }
    free(lexer);
    DuruConfiguration* configuration = malloc(sizeof(DuruConfiguration));
    return configuration;
}

static Lexer* createLexer(char const* contents) {
    Lexer* lexer = malloc(sizeof(*lexer));
    if (lexer == NULL) {
        duruCrash(
          "Could not allocate %zu bytes for the lexer!", sizeof(*lexer));
    }
    *lexer = (Lexer){.contents = contents};
    advanceLexer(lexer);
    return lexer;
}

static void advanceLexer(Lexer* lexer) {
    skipLexer(lexer);
    free(lexer->next.text);

    switch (lexer->contents[lexer->index]) {
        case 0: lexer->next = (Token){.type = endOfFile}; break;
        case '{':
            lexer->index++;
            lexer->next = (Token){.type = openingBrace};
            break;
        case '}':
            lexer->index++;
            lexer->next = (Token){.type = closingBrace};
            break;
        case ';':
            lexer->index++;
            lexer->next = (Token){.type = semicolon};
            break;
        case '.':
            lexer->index++;
            lexer->next = (Token){.type = dot};
            break;
        default:
            if (isLetter(lexer->contents[lexer->index])) {
                int start = lexer->index;
                do {
                    lexer->index++;
                } while (isWord(lexer->contents[lexer->index]));
                int   length = lexer->index - start;
                char* text   = malloc(length + 1);
                if (text == NULL) {
                    duruCrash(
                      "Could not allocate to store the word `%.*s` in the configuration file!",
                      length,
                      lexer->contents + start);
                }
                memcpy(text, lexer->contents + start, length);
                text[length] = 0;
                if (strcmp(text, "project") == 0) {
                    free(text);
                    lexer->next = (Token){.type = projectKeyword};
                    break;
                }
                if (strcmp(text, "executable") == 0) {
                    free(text);
                    lexer->next = (Token){.type = executableKeyword};
                    break;
                }
                lexer->next = (Token){.text = text, .type = identifier};
                break;
            }
            duruCrash(
              "Unknown character `%c` in the project configuration!",
              lexer->contents[lexer->index]);
    }
}

static void skipLexer(Lexer* lexer) {
    char character = lexer->contents[lexer->index];
    while (character != 0) {
        if (character == ' ' || character == '\t' || character == '\n') {
            consumeCurrent();
            continue;
        }
        if (character == '\r') {
            consumeCurrent();
            if (character == '\n') { consumeCurrent(); }
            continue;
        }
        if (character == '#') {
            do {
                consumeCurrent();
                if (character == '\n') {
                    consumeCurrent();
                    break;
                }
            } while (character != 0);
            continue;
        }
        break;
    }
}

static bool isLetter(char byte) {
    return (byte >= 'a' && byte <= 'z') || (byte >= 'A' && byte <= 'Z');
}

static bool isDigit(char byte) { return byte >= '0' && byte <= '9'; }

static bool isWord(char byte) { return isLetter(byte) || isDigit(byte); }
